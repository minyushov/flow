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

package flow.sample.basic

import android.app.Activity
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import flow.Dispatcher
import flow.Traversal
import flow.TraversalCallback
import flow.getFlowKey

internal class BasicDispatcher(
  private val activity: Activity
) : Dispatcher {

  override fun dispatch(traversal: Traversal, callback: TraversalCallback) {
    Log.d("BasicDispatcher", "dispatching $traversal")

    val destKey = traversal.destination.top<Any>()

    val frame = activity.findViewById<ViewGroup>(R.id.contentView)

    // We're already showing something, clean it up.
    if (frame.childCount > 0) {
      val currentView = frame.getChildAt(0)

      // Save the outgoing view state.
      val origin = traversal.origin
      if (origin != null) {
        traversal.getState(origin.top()).save(currentView)
      }

      // Short circuit if we would just be showing the same view again.
      val currentKey = currentView.getFlowKey<Any>()
      if (destKey == currentKey) {
        callback.onTraversalCompleted()
        return
      }

      frame.removeAllViews()
    }

    @LayoutRes
    val layout = when (destKey) {
      is HelloScreen -> R.layout.hello_screen
      is WelcomeScreen -> R.layout.welcome_screen
      else -> throw AssertionError("Unrecognized screen $destKey")
    }

    val incomingView = LayoutInflater.from(traversal.createContext(destKey, activity)).inflate(layout, frame, false)

    frame.addView(incomingView)
    traversal.getState(traversal.destination.top()).restore(incomingView)

    callback.onTraversalCompleted()
  }

}