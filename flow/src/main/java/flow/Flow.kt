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

@file:Suppress("NOTHING_TO_INLINE")

package flow

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.view.View
import androidx.annotation.CheckResult
import java.util.ArrayList

/**
 * Holds the current truth, the history of screens, and exposes operations to change it.
 */
class Flow
internal constructor(
  private val keyManager: KeyManager,
  internal val modelManager: FlowModelManager,
  var history: History
) {

  var historyCallback: HistoryCallback? = null
  private var dispatcher: Dispatcher? = null
  private var pendingTraversal: PendingTraversal? = null
  private val tearDownKeys = ArrayList<Any>()

  /**
   * Set the dispatcher, may receive an immediate call to [Dispatcher.dispatch]. If a [ ] is currently in progress with a previous Dispatcher, that Traversal will
   * not be affected.
   */
  fun setDispatcher(dispatcher: Dispatcher) {
    setDispatcher(dispatcher, false)
  }

  internal fun setDispatcher(dispatcher: Dispatcher, restore: Boolean) {
    this.dispatcher = dispatcher

    val pendingTraversal = this.pendingTraversal
    if (pendingTraversal == null || pendingTraversal.state == TraversalState.DISPATCHED && pendingTraversal.next == null) {
      // Nothing is happening;
      // OR, there is an outstanding callback and nothing will happen after it;
      // So enqueue a bootstrap traversal.
      move(object : PendingTraversal() {
        override fun doExecute() {
          bootstrap(history, restore)
        }
      })
      return
    }

    if (pendingTraversal.state == TraversalState.ENQUEUED) {
      // A traversal was enqueued while we had no dispatcher, run it now.
      pendingTraversal.execute()
      return
    }

    check(pendingTraversal.state == TraversalState.DISPATCHED) {
      "Hanging traversal in unexpected state ${pendingTraversal.state}"
    }
  }

  /**
   * Remove the dispatcher. A noop if the given dispatcher is not the current one.
   *
   * No further [Traversals][Traversal], including Traversals currently enqueued, will execute
   * until a new dispatcher is set.
   */
  fun removeDispatcher(dispatcher: Dispatcher) {
    // This mechanism protects against out of order calls to this method and setDispatcher
    // (e.g. if an outgoing activity is paused after an incoming one resumes).
    if (this.dispatcher === dispatcher) {
      this.dispatcher = null
    }
  }

  /**
   * Replaces the history with the one given and dispatches in the given direction.
   */
  fun setHistory(history: History, direction: Direction) {
    move(object : PendingTraversal() {
      override fun doExecute() {
        dispatch(preserveEquivalentPrefix(history, history), direction)
      }
    })
  }

  /**
   * Replaces the history with the given key and dispatches in the given direction.
   */
  fun replaceHistory(key: Any, direction: Direction) {
    setHistory(
      history = history.buildUpon().clear().push(key).build(),
      direction = direction
    )
  }

  /**
   * Replaces the top key of the history with the given key and dispatches in the given direction.
   */
  fun replaceTop(key: Any, direction: Direction) {
    setHistory(
      history = history.buildUpon().pop(1).push(key).build(),
      direction = direction
    )
  }

  /**
   * Updates the history such that the given key is at the top and dispatches the updated
   * history.
   *
   * If newTopKey is already at the top of the history, the history will be unchanged, but it will
   * be dispatched with direction [Direction.REPLACE].
   *
   * If newTopKey is already on the history but not at the top, the stack will pop until newTopKey
   * is at the top, and the dispatch direction will be [Direction.BACKWARD].
   *
   * If newTopKey is not already on the history, it will be pushed and the dispatch direction will
   * be [Direction.FORWARD].
   *
   * Objects' equality is always checked using [Object.equals].
   */
  fun set(newTopKey: Any) {
    move(object : PendingTraversal() {
      override fun doExecute() {
        if (newTopKey == history.top<Any>()) {
          if (newTopKey is NonPreservableKey) {
            dispatch(history.buildUpon().pop(1).push(newTopKey).build(), Direction.REPLACE)
          } else {
            dispatch(history, Direction.REPLACE)
          }
          return
        }

        val builder = history.buildUpon()
        var count = 0
        // Search backward to see if we already have newTop on the stack
        var preservedInstance: Any? = null
        for (entry in history.framesFromBottom<Any>()) {
          // If we find newTop on the stack, pop back to it.
          if (entry == newTopKey) {
            for (i in 0 until history.size() - count) {
              preservedInstance = builder.pop()
            }
            break
          } else {
            count++
          }
        }

        val newHistory: History
        if (preservedInstance != null) {
          // newTop was on the history. Put the preserved instance back on and dispatch.
          if (newTopKey is NonPreservableKey) {
            builder.push(newTopKey)
          } else {
            builder.push(preservedInstance)
          }
          newHistory = builder.build()
          dispatch(newHistory, Direction.BACKWARD)
        } else {
          // newTop was not on the history. Push it on and dispatch.
          builder.push(newTopKey)
          newHistory = builder.build()
          dispatch(newHistory, Direction.FORWARD)
        }
      }
    })
  }

  /**
   * Go back one key. Typically called from [Activity.onBackPressed].
   * If there is no way to go back, [HistoryCallback.onHistoryCleared] is called.
   * Use [Installer.historyCallback] to set custom history callback.
   * When not set [Activity.finish] is called.
   */
  fun goBack() {
    val pendingTraversal = this.pendingTraversal
    val canGoBack = history.size() > 1 || pendingTraversal != null && pendingTraversal.state != TraversalState.FINISHED

    if (!canGoBack) {
      historyCallback?.onHistoryCleared()
      return
    }

    move(object : PendingTraversal() {
      override fun doExecute() {
        if (history.size() <= 1) {
          // The history shrank while this op was pending. It happens, let's
          // no-op. See lengthy discussions:
          // https://github.com/square/flow/issues/195
          // https://github.com/square/flow/pull/197
          // https://github.com/square/flow/issues/264
          pendingTraversal?.clearHistory()
          return
        }

        val builder = history.buildUpon()
        builder.pop()
        val newHistory = builder.build()
        dispatch(newHistory, Direction.BACKWARD)
      }
    })
  }

  private fun move(pendingTraversal: PendingTraversal) {
    val traversal = this.pendingTraversal
    if (traversal == null) {
      this.pendingTraversal = pendingTraversal
      // If there is no dispatcher wait until one shows up before executing.
      if (dispatcher != null) pendingTraversal.execute()
    } else {
      traversal.enqueue(pendingTraversal)
    }
  }

  private enum class TraversalState {
    /**
     * [PendingTraversal.execute] has not been called.
     */
    ENQUEUED,
    /**
     * [PendingTraversal.execute] was called, waiting for [PendingTraversal.onTraversalCompleted].
     */
    DISPATCHED,
    /**
     * [PendingTraversal.onTraversalCompleted] was called.
     */
    FINISHED
  }

  private abstract inner class PendingTraversal : TraversalCallback {
    var state = TraversalState.ENQUEUED
    var next: PendingTraversal? = null
    var nextHistory: History? = null

    internal fun enqueue(pendingTraversal: PendingTraversal) {
      val next = this.next
      if (next == null) {
        this.next = pendingTraversal
      } else {
        next.enqueue(pendingTraversal)
      }
    }

    override fun onTraversalCompleted() {
      check(state == TraversalState.DISPATCHED) {
        if (state == TraversalState.FINISHED) {
          "onComplete already called for this transition"
        } else {
          "transition not yet dispatched!"
        }
      }

      // Is not set by noop and bootstrap transitions.
      val nextHistory = this.nextHistory
      if (nextHistory != null) {
        tearDownKeys.add(history.top())
        history = nextHistory
      }

      state = TraversalState.FINISHED
      pendingTraversal = next

      val pendingTraversal = this@Flow.pendingTraversal
      if (pendingTraversal == null) {
        val it = tearDownKeys.iterator()
        while (it.hasNext()) {
          keyManager.tearDown(it.next())
          it.remove()
        }
        keyManager.clearStatesExcept(history.asList())
      } else if (dispatcher != null) {
        pendingTraversal.execute()
      }
    }

    private fun updateModels() {
      val nextHistory = this.nextHistory
      if (nextHistory == null || history == nextHistory) {
        return
      }
      val oldKeys = history.asList()
      val newKeys = nextHistory.asList()
      for (key in oldKeys) {
        if (!newKeys.contains(key) && key is FlowModelUser) {
          modelManager.tearDown(key)
        }
      }
      for (key in newKeys) {
        if (!oldKeys.contains(key) && key is FlowModelUser) {
          modelManager.setUp(key)
        }
      }
    }

    internal fun bootstrap(history: History, restore: Boolean) {
      val dispatcher = this@Flow.dispatcher ?: throw IllegalStateException("Bad doExecute method allowed dispatcher to be cleared")
      if (!restore) {
        keyManager.setUp(history.top())
        for (key in history.framesFromTop<Any>()) {
          if (key is FlowModelUser) {
            modelManager.setUp(key)
          }
        }
      }
      dispatcher.dispatch(Traversal(null, history, Direction.REPLACE, keyManager), this)
    }

    internal fun dispatch(nextHistory: History, direction: Direction) {
      this.nextHistory = nextHistory
      val dispatcher = this@Flow.dispatcher ?: throw IllegalStateException("Bad doExecute method allowed dispatcher to be cleared")
      updateModels()
      keyManager.setUp(nextHistory.top())
      dispatcher.dispatch(Traversal(history, nextHistory, direction, keyManager), this)
    }

    internal fun execute() {
      check(state == TraversalState.ENQUEUED) { "unexpected state $state" }
      checkNotNull(dispatcher) { "Caller must ensure that dispatcher is set" }

      state = TraversalState.DISPATCHED
      doExecute()
    }

    internal fun clearHistory() {
      val it = tearDownKeys.iterator()
      while (it.hasNext()) {
        val next = it.next()
        keyManager.tearDown(next)
        if (next is FlowModelUser) {
          modelManager.tearDown(next)
        }
        it.remove()
      }
      keyManager.clearStatesExcept(emptyList())
      next = null
      pendingTraversal = null
      state = TraversalState.FINISHED
      historyCallback?.onHistoryCleared()
    }

    /**
     * Must be synchronous and end with a call to [dispatch] or [onTraversalCompleted].
     */
    internal abstract fun doExecute()
  }

  companion object {
    internal val ROOT_KEY: Any = object : Any() {
      override fun toString(): String {
        return Flow::class.java.name + ".ROOT_KEY"
      }
    }

    /** Adds a history as an extra to an Intent.  */
    fun addHistory(
      intent: Intent,
      history: History,
      parceler: KeyParceler
    ) {
      InternalLifecycleIntegration.addHistoryToIntent(intent, history, parceler)
    }

    private fun preserveEquivalentPrefix(current: History, proposed: History): History {
      val oldIt = current.framesFromBottom<Any>().iterator()
      val newIt = proposed.framesFromBottom<Any>().iterator()

      val preserving = current.buildUpon().clear()

      while (newIt.hasNext()) {
        val newEntry = newIt.next()
        if (!oldIt.hasNext()) {
          preserving.push(newEntry)
          break
        }

        if (newEntry is NonPreservableKey) {
          preserving.push(newEntry)
          break
        }

        val oldEntry = oldIt.next()
        if (oldEntry == newEntry) {
          preserving.push(oldEntry)
        } else {
          preserving.push(newEntry)
          break
        }
      }

      while (newIt.hasNext()) {
        preserving.push(newIt.next())
      }
      return preserving.build()
    }
  }
}

/**
 * @return null if context has no Flow key embedded.
 */
fun <T> Context.getFlowKey(): T? =
  keyContext?.services?.getKey<T>()

/**
 * @return null if view's Context has no Flow key embedded.
 */
inline fun <T> View.getFlowKey(): T? =
  context.getFlowKey()

/**
 * @return null if context does not contain the named service.
 */
fun <T> Context.getFlowService(serviceName: String): T? =
  keyContext?.services?.getService<T>(serviceName)

/**
 * @return null if context does not contain the named service.
 */
inline fun <T> View.getService(serviceName: String): T? =
  context.getFlowService(serviceName)

/**
 * @return null if context does not contain the model
 */
@Suppress("UNCHECKED_CAST")
fun <T> Context.getModel(scopeClass: Class<*>, tag: String): T? =
  flow.modelManager.getModel(scopeClass, tag) as T?

/**
 * Handles an Intent carrying a History extra.
 *
 * @return true if the Intent contains a History and it was handled.
 */
@CheckResult
fun Activity.onNewFlowIntent(intent: Intent): Boolean {
  if (intent.hasExtra(KEY_FLOW_HISTORY)) {
    flowFragment().onNewIntent(intent)
    return true
  }
  return false
}