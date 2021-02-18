/*
 * Copyright 2016 Square Inc.
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

@file:Suppress("DEPRECATION")

package flow

import android.app.Activity
import android.app.Application
import android.app.Fragment
import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import java.util.ArrayList

/**
 * Pay no attention to this class. It's only public because it has to be.
 */
class InternalLifecycleIntegration : Fragment() {

  internal lateinit var flow: Flow
  internal lateinit var keyManager: KeyManager
  internal lateinit var flowModelManager: FlowModelManager
  internal lateinit var defaultHistory: History
  internal lateinit var historyCallback: HistoryCallback
  internal lateinit var dispatcher: Dispatcher
  internal lateinit var intent: Intent

  internal var keyParceler: KeyParceler? = null

  internal var configured = false
  private var dispatcherSet: Boolean = false

  init {
    retainInstance = true
  }

  internal fun onNewIntent(intent: Intent) {
    val history = intent.getParcelableExtra<Bundle>(KEY_FLOW_HISTORY).restoreHistory()
    if (history != null) flow.setHistory(history, Direction.REPLACE)
  }

  override fun onActivityCreated(savedInstanceState: Bundle?) {
    super.onActivityCreated(savedInstanceState)
    if (!::flow.isInitialized) {
      var history = savedInstanceState?.getParcelable<Bundle>(KEY_FLOW_HISTORY).restoreHistory()
      if (history != null) {
        history = historyCallback.onRestoreHistory(history)
      }
      if (history == null) {
        history = intent.getParcelableExtra<Bundle>(KEY_FLOW_HISTORY).restoreHistory() ?: defaultHistory
      }
      flow = Flow(keyManager, flowModelManager, history)
      flow.setDispatcher(dispatcher, false)
    } else {
      flow.setDispatcher(dispatcher, true)
    }
    flow.historyCallback = historyCallback
    dispatcherSet = true
  }

  override fun onResume() {
    super.onResume()
    if (!dispatcherSet) {
      flow.setDispatcher(dispatcher, true)
      dispatcherSet = true
    }
  }

  override fun onPause() {
    flow.removeDispatcher(dispatcher)
    dispatcherSet = false
    super.onPause()
  }

  override fun onDestroy() {
    val key = flow.history.top<Any>()
    keyManager.tearDown(key)
    if (key is FlowModelUser) {
      flowModelManager.tearDown(key)
    }
    super.onDestroy()
  }

  override fun onSaveInstanceState(outState: Bundle) {
    super.onSaveInstanceState(outState)
    val bundle = Bundle()
    save(bundle, historyCallback.onSaveHistory(flow.history))
    if (!bundle.isEmpty) {
      outState.putParcelable(KEY_FLOW_HISTORY, bundle)
    }
  }

  private fun save(
    bundle: Bundle,
    history: History
  ) {
    val parceler = this.keyParceler ?: return
    val parcelables = ArrayList<Parcelable>(history.size())
    for (key in history.framesFromBottom<Any>()) {
      if (!key.javaClass.isAnnotationPresent(NotPersistent::class.java)) {
        parcelables.add(keyManager.getState(key).toBundle(parceler))
      }
    }
    bundle.putParcelableArrayList(KEY_FLOW_STATE, parcelables)
  }

  private fun Bundle?.restoreHistory(): History? {
    val flowState = this?.getParcelableArrayList<Parcelable>(KEY_FLOW_STATE) ?: return null
    val parceler = this@InternalLifecycleIntegration.keyParceler ?: return null
    return history {
      flowState
        .map { (it as Bundle).toState(parceler) }
        .forEach { state ->
          push(state.getKey())
          if (!keyManager.hasState(state.getKey())) {
            keyManager.addState(state)
          }
        }
    }
  }

  companion object {
    internal fun addHistoryToIntent(intent: Intent, history: History, parceler: KeyParceler) {
      val bundle = Bundle()
      val parcelables = ArrayList<Parcelable>(history.size())
      for (key in history.framesFromBottom<Any>()) {
        parcelables.add(emptyState(key).toBundle(parceler))
      }
      bundle.putParcelableArrayList(KEY_FLOW_STATE, parcelables)
      intent.putExtra(KEY_FLOW_HISTORY, bundle)
    }
  }
}

internal fun Application.installFlow(
  activity: Activity,
  defaultHistory: History,
  dispatcher: Dispatcher,
  keyManager: KeyManager,
  keyParceler: KeyParceler?,
  flowModelManager: FlowModelManager,
  historyCallback: HistoryCallback
) {
  registerActivityLifecycleCallbacks(object : Application.ActivityLifecycleCallbacks {
    override fun onActivityCreated(createdActivity: Activity, savedInstanceState: Bundle?) {
      if (createdActivity === activity) {
        activity.install(defaultHistory, dispatcher, keyManager, keyParceler, flowModelManager, historyCallback)
        unregisterActivityLifecycleCallbacks(this)
      }
    }

    override fun onActivityStarted(activity: Activity) = Unit
    override fun onActivityResumed(activity: Activity) = Unit
    override fun onActivityPaused(activity: Activity) = Unit
    override fun onActivityStopped(activity: Activity) = Unit
    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) = Unit
    override fun onActivityDestroyed(a: Activity) = Unit
  })
}

private fun Activity.install(
  defaultHistory: History,
  dispatcher: Dispatcher,
  keyManager: KeyManager,
  keyParceler: KeyParceler?,
  flowModelManager: FlowModelManager,
  historyCallback: HistoryCallback
) {
  var fragment = flowFragmentOrNull()
  val addFragment = fragment == null
  if (fragment == null) {
    fragment = InternalLifecycleIntegration()
  }
  if (!fragment.configured) {
    fragment.defaultHistory = defaultHistory
    fragment.keyParceler = keyParceler
    fragment.keyManager = keyManager
    fragment.historyCallback = historyCallback
    fragment.flowModelManager = flowModelManager
    fragment.configured = true
  }
  // We always replace the dispatcher because it frequently references the Activity.
  fragment.dispatcher = dispatcher
  fragment.intent = intent
  if (addFragment) {
    fragmentManager
      .beginTransaction()
      .add(fragment, FRAGMENT_TAG)
      .commit()
  }
}

internal fun Activity.flowFragmentOrNull(): InternalLifecycleIntegration? =
  fragmentManager.findFragmentByTag(FRAGMENT_TAG) as InternalLifecycleIntegration?

internal fun Activity.flowFragment(): InternalLifecycleIntegration =
  flowFragmentOrNull() ?: throw IllegalStateException("Flow services are not yet available. Do not make this call before receiving Activity#onResume().")

internal const val KEY_FLOW_HISTORY = "flow_history"
private const val KEY_FLOW_STATE = "flow_state"
private const val FRAGMENT_TAG = "flow-lifecycle-integration"