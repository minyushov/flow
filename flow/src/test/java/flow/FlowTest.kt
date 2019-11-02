/*
 * Copyright 2013 Square Inc.
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

import android.content.Context
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.fail
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Mockito.never
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.MockitoAnnotations.initMocks

class FlowTest {

  private val able = TestKey("Able")
  private val baker = TestKey("Baker")
  private val charlie = TestKey("Charlie")
  private val delta = TestKey("Delta")
  private val noPersist: TestKey = NoPersist()

  @Mock
  private lateinit var keyManager: KeyManager
  @Mock
  private lateinit var modelManager: FlowModelManager
  @Mock
  private lateinit var historyCallback: HistoryCallback

  lateinit var lastStack: History
  lateinit var lastDirection: Direction

  private class Uno
  private class Dos
  private class Tres

  @NotPersistent
  private class NoPersist : TestKey("NoPersist")

  internal inner class FlowDispatcher : Dispatcher {
    override fun dispatch(traversal: Traversal, callback: TraversalCallback) {
      lastStack = traversal.destination
      lastDirection = traversal.direction
      callback.onTraversalCompleted()
    }
  }

  internal inner class AsyncDispatcher : Dispatcher {
    private var traversal: Traversal? = null
    private var callback: TraversalCallback? = null

    override fun dispatch(traversal: Traversal, callback: TraversalCallback) {
      this.traversal = traversal
      this.callback = callback
    }

    fun fire() {
      val oldCallback = callback
      callback = null
      traversal = null
      oldCallback?.onTraversalCompleted()
    }

    fun assertIdle() {
      assertThat(callback).isNull()
      assertThat(traversal).isNull()
    }

    fun assertDispatching(newTop: Any) {
      assertThat(callback).isNotNull
      val key = traversal?.destination?.top<Any>()
      assertThat(key).isEqualTo(newTop)
    }
  }

  @Before
  fun setUp() {
    initMocks(this)
  }

  @Test
  fun oneTwoThree() {
    val history = historyOf(Uno())
    val flow = Flow(keyManager, modelManager, history)
    flow.setDispatcher(FlowDispatcher())
    flow.historyCallback = historyCallback

    flow.set(Dos())

    var top = lastStack.top<Any>()
    assertThat(top).isInstanceOf(Dos::class.java)
    assertThat(lastDirection).isSameAs(Direction.FORWARD)

    flow.set(Tres())

    top = lastStack.top()
    assertThat(top).isInstanceOf(Tres::class.java)
    assertThat(lastDirection).isSameAs(Direction.FORWARD)

    flow.goBack()
    verify(historyCallback, never()).onHistoryCleared()

    top = lastStack.top()
    assertThat(top).isInstanceOf(Dos::class.java)
    assertThat(lastDirection).isSameAs(Direction.BACKWARD)

    flow.goBack()
    verify(historyCallback, never()).onHistoryCleared()

    top = lastStack.top()
    assertThat(top).isInstanceOf(Uno::class.java)
    assertThat(lastDirection).isSameAs(Direction.BACKWARD)

    flow.goBack()
    verify(historyCallback, times(1)).onHistoryCleared()
  }

  @Test
  fun historyChangesAfterListenerCall() {
    val firstHistory = historyOf(Uno())

    class Ourrobouros : Dispatcher {
      var flow = Flow(keyManager, modelManager, firstHistory)

      init {
        flow.setDispatcher(this)
      }

      override fun dispatch(traversal: Traversal, callback: TraversalCallback) {
        assertThat(firstHistory.asList()).hasSameSizeAs(flow.history.asList())
        val original = firstHistory.framesFromTop<Any>().iterator()
        for (o in flow.history.framesFromTop<Any>()) {
          assertThat(o).isEqualTo(original.next())
        }
        callback.onTraversalCompleted()
      }
    }

    val listener = Ourrobouros()
    listener.flow.set(Dos())
  }

  @Test
  fun historyPushAllIsPushy() {
    val history = historyOf(able, baker, charlie)
    assertThat(history.size()).isEqualTo(3)

    val flow = Flow(keyManager, modelManager, history)
    flow.setDispatcher(FlowDispatcher())
    flow.historyCallback = historyCallback

    flow.goBack()
    verify(historyCallback, never()).onHistoryCleared()

    var top = lastStack.top<Any>()
    assertThat(top).isEqualTo(baker)

    flow.goBack()
    verify(historyCallback, never()).onHistoryCleared()

    top = lastStack.top()
    assertThat(top).isEqualTo(able)

    flow.goBack()
    verify(historyCallback, times(1)).onHistoryCleared()
  }

  @Test
  fun setHistoryWorks() {
    val history = historyOf(able, baker)
    val flow = Flow(keyManager, modelManager, history)
    val dispatcher = FlowDispatcher()
    flow.setDispatcher(dispatcher)
    flow.historyCallback = historyCallback

    val newHistory = historyOf(charlie, delta)

    flow.setHistory(newHistory, Direction.FORWARD)
    assertThat(lastDirection).isSameAs(Direction.FORWARD)

    var top = lastStack.top<Any>()
    assertThat(top).isSameAs(delta)

    flow.goBack()
    verify(historyCallback, never()).onHistoryCleared()

    top = lastStack.top()
    assertThat(top).isSameAs(charlie)

    flow.goBack()
    verify(historyCallback, times(1)).onHistoryCleared()
  }

  @Test
  fun setObjectGoesBack() {
    val history = historyOf(able, baker, charlie, delta)
    val flow = Flow(keyManager, modelManager, history)
    flow.setDispatcher(FlowDispatcher())
    flow.historyCallback = historyCallback

    assertThat(history.size()).isEqualTo(4)

    flow.set(charlie)

    var top = lastStack.top<Any>()
    assertThat(top).isEqualTo(charlie)
    assertThat(lastStack.size()).isEqualTo(3)
    assertThat(lastDirection).isEqualTo(Direction.BACKWARD)

    flow.goBack()
    verify(historyCallback, never()).onHistoryCleared()

    top = lastStack.top()
    assertThat(top).isEqualTo(baker)
    assertThat(lastDirection).isEqualTo(Direction.BACKWARD)

    flow.goBack()
    verify(historyCallback, never()).onHistoryCleared()

    top = lastStack.top()
    assertThat(top).isEqualTo(able)
    assertThat(lastDirection).isEqualTo(Direction.BACKWARD)

    flow.goBack()
    verify(historyCallback, times(1)).onHistoryCleared()
  }

  @Test
  fun setObjectToMissingObjectPushes() {
    val history = historyOf(able, baker)
    val flow = Flow(keyManager, modelManager, history)
    flow.setDispatcher(FlowDispatcher())
    flow.historyCallback = historyCallback

    assertThat(history.size()).isEqualTo(2)

    flow.set(charlie)

    var top = lastStack.top<Any>()
    assertThat(top).isEqualTo(charlie)
    assertThat(lastStack.size()).isEqualTo(3)
    assertThat(lastDirection).isEqualTo(Direction.FORWARD)

    flow.goBack()
    verify(historyCallback, never()).onHistoryCleared()

    top = lastStack.top()
    assertThat(top).isEqualTo(baker)
    assertThat(lastDirection).isEqualTo(Direction.BACKWARD)

    flow.goBack()
    verify(historyCallback, never()).onHistoryCleared()

    top = lastStack.top()
    assertThat(top).isEqualTo(able)
    assertThat(lastDirection).isEqualTo(Direction.BACKWARD)

    flow.goBack()
    verify(historyCallback, times(1)).onHistoryCleared()
  }

  @Test
  fun setObjectKeepsOriginal() {
    val history = historyOf(able, baker)
    val flow = Flow(keyManager, modelManager, history)
    flow.setDispatcher(FlowDispatcher())
    assertThat(history.size()).isEqualTo(2)

    flow.set(TestKey("Able"))

    val top = lastStack.top<Any>()
    assertThat(top).isEqualTo(TestKey("Able"))
    assertThat(top === able).isTrue()
    assertThat(top).isSameAs(able)
    assertThat(lastStack.size()).isEqualTo(1)
    assertThat(lastDirection).isEqualTo(Direction.BACKWARD)
  }

  @Test
  fun replaceHistoryResultsInLengthOneHistory() {
    val history = historyOf(able, baker, charlie)
    val flow = Flow(keyManager, modelManager, history)
    flow.setDispatcher(FlowDispatcher())
    assertThat(history.size()).isEqualTo(3)

    flow.replaceHistory(delta, Direction.REPLACE)

    val top = lastStack.top<Any>()
    assertThat(top).isEqualTo(TestKey("Delta"))
    assertThat(top === delta).isTrue()
    assertThat(top).isSameAs(delta)
    assertThat(lastStack.size()).isEqualTo(1)
    assertThat(lastDirection).isEqualTo(Direction.REPLACE)
  }

  @Test
  fun replaceTopDoesNotAlterHistoryLength() {
    val history = historyOf(able, baker, charlie)
    val flow = Flow(keyManager, modelManager, history)
    flow.setDispatcher(FlowDispatcher())
    assertThat(history.size()).isEqualTo(3)

    flow.replaceTop(delta, Direction.REPLACE)

    val top = lastStack.top<Any>()
    assertThat(top).isEqualTo(TestKey("Delta"))
    assertThat(top === delta).isTrue()
    assertThat(top).isSameAs(delta)
    assertThat(lastStack.size()).isEqualTo(3)
    assertThat(lastDirection).isEqualTo(Direction.REPLACE)
  }

  @Test
  fun secondDispatcherIsBootstrapped() {
    val firstDispatcher = AsyncDispatcher()

    val history = historyOf(able)
    val flow = Flow(keyManager, modelManager, history)
    flow.setDispatcher(firstDispatcher)

    // Quick check that we bootstrapped (and test the test dispatcher).
    firstDispatcher.assertDispatching(able)
    firstDispatcher.fire()
    firstDispatcher.assertIdle()

    // No activity, dispatchers change. Maybe pause / resume. Maybe config change.
    flow.removeDispatcher(firstDispatcher)
    val secondDispatcher = AsyncDispatcher()
    flow.setDispatcher(secondDispatcher)

    // New dispatcher is bootstrapped
    secondDispatcher.assertDispatching(able)
    secondDispatcher.fire()
    secondDispatcher.assertIdle()
  }

  @Test
  fun hangingTraversalsSurviveDispatcherChange() {
    val firstDispatcher = AsyncDispatcher()

    val history = historyOf(able)
    val flow = Flow(keyManager, modelManager, history)
    flow.setDispatcher(firstDispatcher)
    firstDispatcher.fire()

    // Start traversal to second screen.
    flow.set(baker)
    firstDispatcher.assertDispatching(baker)

    // Dispatcher is removed before finishing baker--maybe it caused a configuration change.
    flow.removeDispatcher(firstDispatcher)

    // New dispatcher shows up, maybe from new activity after config change.
    val secondDispatcher = AsyncDispatcher()
    flow.setDispatcher(secondDispatcher)

    // New dispatcher is ignored until the in-progress baker traversal is done.
    secondDispatcher.assertIdle()

    // New dispatcher is bootstrapped with baker.
    firstDispatcher.fire()
    secondDispatcher.assertDispatching(baker)

    // Confirm no redundant extra bootstrap traversals enqueued.
    secondDispatcher.fire()
    secondDispatcher.assertIdle()
  }

  @Test
  fun enqueuedTraversalsSurviveDispatcherChange() {
    val firstDispatcher = AsyncDispatcher()

    val history = historyOf(able)
    val flow = Flow(keyManager, modelManager, history)
    flow.setDispatcher(firstDispatcher)
    firstDispatcher.fire()

    // Dispatcher is removed. Maybe we paused.
    flow.removeDispatcher(firstDispatcher)

    // A few traversals are enqueued because software.
    flow.set(baker)
    flow.set(charlie)

    // New dispatcher shows up, we resumed.
    val secondDispatcher = AsyncDispatcher()
    flow.setDispatcher(secondDispatcher)

    // New dispatcher receives baker and charlie traversals and nothing else.
    secondDispatcher.assertDispatching(baker)
    secondDispatcher.fire()
    secondDispatcher.assertDispatching(charlie)
    secondDispatcher.fire()
    secondDispatcher.assertIdle()
  }

  @Test
  fun setHistoryKeepsOriginals() {
    val able = TestKey("Able")
    val baker = TestKey("Baker")
    val charlie = TestKey("Charlie")
    val delta = TestKey("Delta")
    val history = historyOf(able, baker, charlie, delta)
    val flow = Flow(keyManager, modelManager, history)
    flow.setDispatcher(FlowDispatcher())
    assertThat(history.size()).isEqualTo(4)

    val echo = TestKey("Echo")
    val foxtrot = TestKey("Foxtrot")
    val newHistory = historyOf(able, baker, echo, foxtrot)
    flow.setHistory(newHistory, Direction.REPLACE)
    assertThat(lastStack.size()).isEqualTo(4)

    var top = lastStack.top<Any>()
    assertThat(top).isEqualTo(foxtrot)

    flow.goBack()

    assertThat(lastStack.size()).isEqualTo(3)
    top = lastStack.top()
    assertThat(top).isEqualTo(echo)

    flow.goBack()

    assertThat(lastStack.size()).isEqualTo(2)
    top = lastStack.top()
    assertThat(top).isSameAs(baker)

    flow.goBack()

    assertThat(lastStack.size()).isEqualTo(1)
    top = lastStack.top()
    assertThat(top).isSameAs(able)
  }

  private data class Picky(
    private val value: String
  )

  @Test
  fun setCallsEquals() {
    val history = historyOf(Picky("Able"), Picky("Baker"), Picky("Charlie"), Picky("Delta"))
    val flow = Flow(keyManager, modelManager, history)
    flow.setDispatcher(FlowDispatcher())
    flow.historyCallback = historyCallback

    assertThat(history.size()).isEqualTo(4)

    flow.set(Picky("Charlie"))

    var top = lastStack.top<Any>()
    assertThat(top).isEqualTo(Picky("Charlie"))
    assertThat(lastStack.size()).isEqualTo(3)
    assertThat(lastDirection).isEqualTo(Direction.BACKWARD)

    flow.goBack()
    verify(historyCallback, never()).onHistoryCleared()

    top = lastStack.top()
    assertThat(top).isEqualTo(Picky("Baker"))
    assertThat(lastDirection).isEqualTo(Direction.BACKWARD)

    flow.goBack()
    verify(historyCallback, never()).onHistoryCleared()

    top = lastStack.top()
    assertThat(top).isEqualTo(Picky("Able"))
    assertThat(lastDirection).isEqualTo(Direction.BACKWARD)

    flow.goBack()
    verify(historyCallback, times(1)).onHistoryCleared()
  }

  @Test
  fun incorrectFlowGetUsage() {
    val mockContext = Mockito.mock(Context::class.java)

    Mockito.`when`(mockContext.getSystemService(Mockito.anyString())).thenReturn(null)

    try {
      mockContext.flow
      fail("Flow was supposed to throw an exception on wrong usage")
    } catch (ignored: IllegalStateException) {
      // That's good!
    }
  }

  @Test
  fun notPersistentHistoryFilter() {
    val history = historyOf(able, noPersist, charlie)
    val filter = NotPersistentHistoryCallback()
    val expected = historyOf(able, charlie).asList()
    assertThat(filter.onSaveHistory(history).asList()).isEqualTo(expected)
  }
}
