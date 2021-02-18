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
import android.content.Context
import android.content.ContextWrapper

internal const val SYSTEM_SERVICE_FLOW = "flow.flow"
private const val SYSTEM_SERVICE_KEY_MANAGER = "flow.key_manager"

internal class FlowContextWrapper(
  baseContext: Context,
  private val activity: Activity
) : ContextWrapper(baseContext) {

  private val flow: Flow by lazy { activity.flowFragment().flow }
  private val keyManager: KeyManager by lazy { activity.flowFragment().keyManager }

  override fun getSystemService(name: String): Any? =
    when (name) {
      SYSTEM_SERVICE_FLOW -> flow
      SYSTEM_SERVICE_KEY_MANAGER -> keyManager
      else -> super.getSystemService(name)
    }

}

@SuppressLint("WrongConstant")
fun getContextManager(context: Context): KeyManager? =
  context.getSystemService(SYSTEM_SERVICE_KEY_MANAGER) as KeyManager?