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

package flow.sample.orientation

import android.annotation.SuppressLint
import android.app.Activity
import android.content.pm.ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
import android.content.pm.ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import flow.Dispatcher
import flow.Traversal
import flow.TraversalCallback

internal class OrientationSampleDispatcher(
  private val activity: Activity
) : Dispatcher {

  private val isPortrait: Boolean
    get() = activity.resources.getBoolean(R.bool.is_portrait)

  override fun dispatch(traversal: Traversal, callback: TraversalCallback) {
    Log.d("BasicDispatcher", "dispatching $traversal")

    val destScreen = traversal.destination.top<OrientationSampleScreen>()
    val incomingNeedsLock = destScreen.requiresLandscape
    val waitForOrientationChange = incomingNeedsLock && requestLandscapeLock()

    if (waitForOrientationChange) {
      // There is about to be an orientation change, which means there will
      // soon be a new activity and a new dispatcher. Let them complete
      // this traversal, so that we don't try to show a landscape-only screen
      // in portrait.
      hangingCallback = callback
    } else {
      val frame = activity.findViewById<ViewGroup>(R.id.basic_activity_frame)
      val destView = LayoutInflater.from(traversal.createContext(destScreen, activity))
        .inflate(destScreen.layoutId, frame, false)

      frame.removeAllViews()
      frame.addView(destView)

      if (!incomingNeedsLock) {
        requestUnlock()
      }
      callback.onTraversalCompleted()
    }
  }

  /**
   * Returns true if we've requested a lock and are expecting an orientation change
   * as a result.
   */
  @SuppressLint("SourceLockedOrientationActivity")
  private fun requestLandscapeLock(): Boolean {
    val requestedOrientation = activity.requestedOrientation

    if (requestedOrientation == SCREEN_ORIENTATION_SENSOR_LANDSCAPE) {
      // We're already locked.
      return false
    }

    activity.requestedOrientation = SCREEN_ORIENTATION_SENSOR_LANDSCAPE

    // We've requested a lock, but there will only be an orientation change
    // if we're not already landscape.
    return isPortrait
  }

  private fun requestUnlock() {
    activity.requestedOrientation = SCREEN_ORIENTATION_UNSPECIFIED
  }

  companion object {
    /**
     * Using a static for this is a fragile hack for demo purposes. In
     * real life you'd want something hanging off of a custom application
     * class--perhaps a Mortar activity scope.
     */
    private var hangingCallback: TraversalCallback? = null

    fun finishPendingTraversal() {
      if (hangingCallback != null) {
        // The previous dispatcher was unable to finish its traversal because it
        // required a configuration change. Let flow know that we're awake again
        // in our new orientation and that traversal is done.

        val ref = hangingCallback
        hangingCallback = null
        ref?.onTraversalCompleted()
      }
    }
  }

}