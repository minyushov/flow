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

interface Dispatcher {
  /**
   * Called when the history is about to change.  Note that Flow does not consider the
   * Traversal to be finished, and will not actually update the history, until the callback is
   * triggered. Traversals cannot be canceled.
   *
   *
   * Also called immediately after [Flow.setDispatcher], to update the new dispatcher
   * to Flow's current state. Such bootstrap Traversals have a null [Traversal.origin],
   * and [Direction.REPLACE] as their direction. It should be noted that the dispatcher
   * is set and unset each time the app pauses and resumes, meaning the dispatcher will receive
   * a bootstrap call each time the app is activated.
   *
   *
   * Dispatchers are required to be idempotent. They should check whether the app is already in
   * the correct state for the incoming key before performing any redundant work. (This probably
   * includes comparing the [key of the currently visible][Flow.getKey] to that in [Traversal.destination] before doing any unnecessary inflation
   * and rendering). If no update  is needed, short circuit
   * by firing the callback immediately and returning.
   *
   * @param callback Must be called to indicate completion of the traversal.
   */
  fun dispatch(traversal: Traversal, callback: TraversalCallback)
}