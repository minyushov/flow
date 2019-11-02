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

import android.app.Activity
import android.content.Context
import android.view.Gravity
import android.widget.TextView

internal class DefaultKeyChanger(
  private val activity: Activity
) : KeyChanger {

  private var textView: TextView? = null

  override fun changeKey(
    outgoingState: State?,
    incomingState: State,
    direction: Direction,
    incomingContexts: Map<Any, Context>,
    callback: TraversalCallback
  ) {
    if (textView == null) {
      textView = TextView(incomingContexts[incomingState.getKey()])
        .apply {
          gravity = Gravity.CENTER
          activity.setContentView(this)
        }
    }
    textView?.text = incomingState.getKey<Any>().toString()
    callback.onTraversalCompleted()
  }

}