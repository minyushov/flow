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

package flow

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.content.Context
import android.view.View

fun Context.withFlow(
  activity: Activity,
  servicesFactories: List<ServicesFactory> = emptyList(),
  flowModelScopes: List<FlowModelScope> = emptyList(),
  defaultKey: Any? = null,
  dispatcher: Dispatcher? = null,
  keyParceler: KeyParceler? = null,
  historyCallback: HistoryCallback? = null

): Context {
  check(activity.flowFragmentOrNull() === null) { "Flow is already installed in this Activity." }
  (applicationContext as Application).installFlow(
    activity = activity,
    defaultHistory = historyOf(defaultKey ?: "Hello, World!"),
    dispatcher = dispatcher ?: DefaultKeyDispatcher(activity, DefaultKeyChanger(activity)),
    keyManager = KeyManager(servicesFactories),
    keyParceler = keyParceler,
    flowModelManager = FlowModelManager(flowModelScopes),
    historyCallback = historyCallback ?: object : NotPersistentHistoryCallback() {
      override fun onHistoryCleared() {
        activity.finish()
      }
    }
  )
  return FlowContextWrapper(this, activity)
}

/**
 * Returns the Flow instance for the [Activity] that owns the given context.
 * Note that it is not safe to call this method before the first call to that
 * Activity's [Activity.onResume] method in the current Android task. In practice
 * this boils down to two rules:
 *
 *  1. In views, do not access Flow before [View.onAttachedToWindow] is called.
 *  1. In activities, do not access flow before [Activity.onResume] is called.
 *
 */
val Context.flow: Flow
  @SuppressLint("WrongConstant")
  get() = getSystemService(SYSTEM_SERVICE_FLOW) as Flow?
    ?: throw IllegalStateException("Context was not wrapped with flow. Make sure attachBaseContext was overridden in your main activity")

/**
 * Shortcut of [Context.flow].
 */
inline val View.flow: Flow
  get() = context.flow