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

class Traversal
internal constructor(
  /** May be null if this is a traversal into the start state.  */
  val origin: History?,
  val destination: History,
  val direction: Direction,
  private val keyManager: KeyManager
) {

  /**
   * Creates a Context for the given key.
   *
   * Contexts can be created only for keys at the top of the origin and destination Histories.
   */
  fun createContext(key: Any, baseContext: Context): Context =
    KeyContextWrapper(baseContext, keyManager.findServices(key))

  fun getState(key: Any): State =
    keyManager.getState(key)

}