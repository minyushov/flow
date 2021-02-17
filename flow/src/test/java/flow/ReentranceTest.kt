/*
 * Copyright 2014 Square Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package flow

import flow.Direction.FORWARD
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Fail.fail
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.MockitoAnnotations.openMocks
import java.util.ArrayList
import java.util.LinkedList
import java.util.concurrent.atomic.AtomicInteger

class ReentranceTest {
  private var mocksClosable: AutoCloseable? = null

  @Mock
  private lateinit var keyManager: KeyManager

  @Mock
  private lateinit var modelManager: FlowModelManager

  @Mock
  private lateinit var historyCallback: HistoryCallback

  @Before
  fun setUp() {
    mocksClosable = openMocks(this)
  }

  @After
  fun shutDown() {
    mocksClosable?.close()
  }

  @Test
  fun reentrantGo() {
    lateinit var lastStack: History

    Flow(keyManager, modelManager, historyOf(Catalog())).apply {
      setDispatcher(object : Dispatcher {
        override fun dispatch(traversal: Traversal, callback: TraversalCallback) {
          lastStack = traversal.destination
          val next = traversal.destination.top<Any>()
          if (next is Detail) {
            set(Loading())
          } else if (next is Loading) {
            set(Error())
          }
          callback.onTraversalCompleted()
        }
      })
      set(Detail())
    }

    verifyHistory(lastStack, Error(), Loading(), Detail(), Catalog())
  }

  @Test
  fun reentrantGoThenBack() {
    lateinit var lastStack: History

    Flow(keyManager, modelManager, historyOf(Catalog())).apply {
      setDispatcher(object : Dispatcher {
        var loading = true

        override fun dispatch(traversal: Traversal, callback: TraversalCallback) {
          lastStack = traversal.destination
          val next = traversal.destination.top<Any>()
          if (loading) {
            when (next) {
              is Detail -> set(Loading())
              is Loading -> set(Error())
              is Error -> {
                loading = false
                goBack()
              }
            }
          } else {
            if (next is Loading) {
              goBack()
            }
          }
          callback.onTraversalCompleted()
        }
      })
      set(Detail())
    }

    verifyHistory(lastStack, Detail(), Catalog())
  }

  @Test
  fun reentrantForwardThenGo() {
    lateinit var lastStack: History

    Flow(keyManager, modelManager, historyOf(Catalog())).apply {
      setDispatcher(object : Dispatcher {
        override fun dispatch(traversal: Traversal, callback: TraversalCallback) {
          lastStack = traversal.destination
          val next = traversal.destination.top<Any>()
          if (next is Detail) {
            setHistory(historyOf(Detail(), Loading()), FORWARD)
          } else if (next is Loading) {
            set(Error())
          }
          callback.onTraversalCompleted()
        }
      })
      set(Detail())
    }

    verifyHistory(lastStack, Error(), Loading(), Detail())
  }

  @Test
  fun goBackQueuesUp() {
    lateinit var lastStack: History
    val callbacks = LinkedList<TraversalCallback>()

    val flow = Flow(keyManager, modelManager, historyOf(Catalog()))
    flow.setDispatcher(object : Dispatcher {
      override fun dispatch(traversal: Traversal, callback: TraversalCallback) {
        callbacks.add(callback)
        lastStack = traversal.destination
      }
    })
    flow.historyCallback = historyCallback

    flow.set(Detail())
    flow.set(Error())

    flow.goBack()
    verify(historyCallback, never()).onHistoryCleared()

    while (!callbacks.isEmpty()) {
      callbacks.poll()?.onTraversalCompleted()
    }

    verifyHistory(lastStack, Detail(), Catalog())
  }

  @Test
  fun overflowQueuedBackupsNoOp() {
    lateinit var lastStack: History
    val callbacks = LinkedList<TraversalCallback>()

    val flow = Flow(keyManager, modelManager, historyOf(Catalog()))
    flow.setDispatcher(object : Dispatcher {
      override fun dispatch(traversal: Traversal, callback: TraversalCallback) {
        callbacks.add(callback)
        lastStack = traversal.destination
      }
    })
    flow.historyCallback = historyCallback

    flow.set(Detail())

    for (i in 0..19) {
      flow.goBack()
      verify(historyCallback, never()).onHistoryCleared()
    }

    var callbackCount = 0
    while (!callbacks.isEmpty()) {
      callbackCount++
      callbacks.poll()!!.onTraversalCompleted()
    }

    assertThat(callbackCount).isEqualTo(3)
    verifyHistory(lastStack, Catalog())
  }

  @Test
  fun reentranceWaitsForCallback() {
    lateinit var lastCallback: TraversalCallback

    val flow = Flow(keyManager, modelManager, historyOf(Catalog()))
    flow.setDispatcher(object : Dispatcher {
      override fun dispatch(traversal: Traversal, callback: TraversalCallback) {
        lastCallback = callback
        val next = traversal.destination.top<Any>()
        if (next is Detail) {
          flow.set(Loading())
        } else if (next is Loading) {
          flow.set(Error())
        }
      }
    })

    lastCallback.onTraversalCompleted()

    flow.set(Detail())
    verifyHistory(flow.history, Catalog())
    lastCallback.onTraversalCompleted()
    verifyHistory(flow.history, Detail(), Catalog())
    lastCallback.onTraversalCompleted()
    verifyHistory(flow.history, Loading(), Detail(), Catalog())
    lastCallback.onTraversalCompleted()
    verifyHistory(flow.history, Error(), Loading(), Detail(), Catalog())
  }

  @Test
  fun onCompleteThrowsIfCalledTwice() {
    lateinit var lastCallback: TraversalCallback

    val flow = Flow(keyManager, modelManager, historyOf(Catalog()))
    flow.setDispatcher(object : Dispatcher {
      override fun dispatch(traversal: Traversal, callback: TraversalCallback) {
        lastCallback = callback
      }
    })

    lastCallback.onTraversalCompleted()
    try {
      lastCallback.onTraversalCompleted()
    } catch (e: IllegalStateException) {
      return
    }

    fail<Nothing>("Second call to onComplete() should have thrown.")
  }

  @Test
  fun bootstrapTraversal() {
    lateinit var lastStack: History

    val flow = Flow(keyManager, modelManager, historyOf(Catalog()))
    flow.setDispatcher(object : Dispatcher {
      override fun dispatch(traversal: Traversal, callback: TraversalCallback) {
        lastStack = traversal.destination
        callback.onTraversalCompleted()
      }
    })

    verifyHistory(lastStack, Catalog())
  }

  @Test
  fun pendingTraversalReplacesBootstrap() {
    lateinit var lastStack: History

    val dispatchCount = AtomicInteger(0)
    val flow = Flow(keyManager, modelManager, historyOf(Catalog()))
    flow.set(Detail())

    flow.setDispatcher(object : Dispatcher {
      override fun dispatch(traversal: Traversal, callback: TraversalCallback) {
        dispatchCount.incrementAndGet()
        lastStack = traversal.destination
        callback.onTraversalCompleted()
      }
    })

    verifyHistory(lastStack, Detail(), Catalog())
    assertThat(dispatchCount.toInt()).isEqualTo(1)
  }

  @Test
  fun allPendingTraversalsFire() {
    lateinit var lastCallback: TraversalCallback

    val flow = Flow(keyManager, modelManager, historyOf(Catalog()))
    flow.set(Loading())
    flow.set(Detail())
    flow.set(Error())

    flow.setDispatcher(object : Dispatcher {
      override fun dispatch(traversal: Traversal, callback: TraversalCallback) {
        lastCallback = callback
      }
    })

    lastCallback.onTraversalCompleted()
    verifyHistory(flow.history, Loading(), Catalog())

    lastCallback.onTraversalCompleted()
    verifyHistory(flow.history, Detail(), Loading(), Catalog())
  }

  @Test
  fun clearingDispatcherMidTraversalPauses() {
    val flow = Flow(keyManager, modelManager, historyOf(Catalog()))

    flow.setDispatcher(object : Dispatcher {
      override fun dispatch(traversal: Traversal, callback: TraversalCallback) {
        flow.set(Loading())
        flow.removeDispatcher(this)
        callback.onTraversalCompleted()
      }
    })

    verifyHistory(flow.history, Catalog())

    flow.setDispatcher(object : Dispatcher {
      override fun dispatch(traversal: Traversal, callback: TraversalCallback) {
        callback.onTraversalCompleted()
      }
    })

    verifyHistory(flow.history, Loading(), Catalog())
  }

  @Test
  fun dispatcherSetInMidFlightWaitsForBootstrap() {
    lateinit var lastCallback: TraversalCallback
    var lastStack: History? = null

    val flow = Flow(keyManager, modelManager, historyOf(Catalog()))
    flow.setDispatcher(object : Dispatcher {
      override fun dispatch(traversal: Traversal, callback: TraversalCallback) {
        lastCallback = callback
      }
    })
    flow.setDispatcher(object : Dispatcher {
      override fun dispatch(traversal: Traversal, callback: TraversalCallback) {
        lastStack = traversal.destination
        callback.onTraversalCompleted()
      }
    })

    assertThat(lastStack).isNull()
    lastCallback.onTraversalCompleted()
    verifyHistory(lastStack!!, Catalog())
  }

  @Test
  fun dispatcherSetInMidFlightWithBigQueueNeedsNoBootstrap() {
    lateinit var lastCallback: TraversalCallback
    var lastStack: History? = null

    val secondDispatcherCount = AtomicInteger(0)
    val flow = Flow(keyManager, modelManager, historyOf(Catalog()))
    flow.setDispatcher(object : Dispatcher {
      override fun dispatch(traversal: Traversal, callback: TraversalCallback) {
        flow.set(Detail())
        lastCallback = callback
      }
    })
    flow.setDispatcher(object : Dispatcher {
      override fun dispatch(traversal: Traversal, callback: TraversalCallback) {
        secondDispatcherCount.incrementAndGet()
        lastStack = traversal.destination
        callback.onTraversalCompleted()
      }
    })

    assertThat(lastStack).isNull()
    lastCallback.onTraversalCompleted()
    verifyHistory(lastStack!!, Detail(), Catalog())
    assertThat(secondDispatcherCount.get()).isEqualTo(1)
  }

  @Test
  fun traversalsQueuedAfterDispatcherRemovedBootstrapTheNextOne() {
    lateinit var lastCallback: TraversalCallback

    val secondDispatcherCount = AtomicInteger(0)
    val flow = Flow(keyManager, modelManager, historyOf(Catalog()))

    flow.setDispatcher(object : Dispatcher {
      override fun dispatch(traversal: Traversal, callback: TraversalCallback) {
        lastCallback = callback
        flow.removeDispatcher(this)
        flow.set(Loading())
      }
    })

    verifyHistory(flow.history, Catalog())

    flow.setDispatcher(object : Dispatcher {
      override fun dispatch(traversal: Traversal, callback: TraversalCallback) {
        secondDispatcherCount.incrementAndGet()
        callback.onTraversalCompleted()
      }
    })

    assertThat(secondDispatcherCount.get()).isZero()
    lastCallback.onTraversalCompleted()

    assertThat(secondDispatcherCount.get()).isEqualTo(1)
    verifyHistory(flow.history, Loading(), Catalog())
  }

  internal class Catalog : TestKey("catalog")
  internal class Detail : TestKey("detail")
  internal class Loading : TestKey("loading")
  internal class Error : TestKey("error")

  private fun verifyHistory(history: History, vararg keys: Any) {
    val actualKeys = ArrayList<Any>(history.size())
    for (entry in history.framesFromTop<Any>()) {
      actualKeys.add(entry)
    }
    assertThat(actualKeys).containsExactly(*keys)
  }
}
