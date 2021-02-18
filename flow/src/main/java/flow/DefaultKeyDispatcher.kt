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
import java.util.Collections
import java.util.LinkedHashMap

/**
 * A simple Dispatcher that only pays attention to the top keys on the incoming and outgoing
 * histories, and only executes a change if those top keys are not equal.
 */
open class DefaultKeyDispatcher(
  private val activity: Activity,
  private val keyChanger: KeyChanger
) : Dispatcher {

  override fun dispatch(traversal: Traversal, callback: TraversalCallback) {
    val inState = traversal.getState(traversal.destination.top())
    val inKey = inState.getKey<Any>()
    val outState = traversal.origin?.let { traversal.getState(traversal.origin.top()) }
    val outKey = outState?.getKey<Any>()

    // TODO(#126): short-circuit may belong in Flow, since every Dispatcher we have implements it.
    if (inKey == outKey) {
      callback.onTraversalCompleted()
      return
    }

    changeKey(
      outgoingState = outState,
      incomingState = inState,
      direction = traversal.direction,
      incomingContexts = if (inKey is MultiKey) {
        inKey.keys.associateWith { traversal.createContext(it, activity) } +
          (inKey to traversal.createContext(inKey, activity))
      } else {
        mapOf(inKey to traversal.createContext(inKey, activity))
      },
      callback = callback
    )
  }

  open fun changeKey(
    outgoingState: State?,
    incomingState: State,
    direction: Direction,
    incomingContexts: Map<Any, Context>,
    callback: TraversalCallback
  ) =
    keyChanger.changeKey(
      outgoingState,
      incomingState,
      direction,
      incomingContexts,
      callback
    )

}
