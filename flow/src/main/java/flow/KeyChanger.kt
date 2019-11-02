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

import android.content.Context
import android.view.View

interface KeyChanger {
  /**
   * Transition from outgoing state to incoming state.  Implementations should call
   * [State.restore] on the incoming view, and (if outgoingState is not null)
   * [State.save] on the outgoing view.  And don't forget to declare your screen layouts
   * with ids (only layouts with ids will have their state saved/restored)!
   */
  fun changeKey(
    outgoingState: State?,
    incomingState: State,
    direction: Direction,
    incomingContexts: Map<Any, Context>,
    callback: TraversalCallback
  )
}