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

import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout
import flow.getFlowKey
import kotlinx.android.synthetic.main.hello_screen.view.helloCounter
import kotlinx.android.synthetic.main.hello_screen.view.helloIncrement
import kotlinx.android.synthetic.main.hello_screen.view.helloName

class HelloView
@JvmOverloads
constructor(
  context: Context,
  attrs: AttributeSet? = null,
  defStyleAttr: Int = 0
) : LinearLayout(
  context,
  attrs,
  defStyleAttr
) {

  init {
    orientation = VERTICAL
    id = R.id.hello
  }

  override fun onAttachedToWindow() {
    super.onAttachedToWindow()

    val screen = getFlowKey<HelloScreen>()
    helloName.text = "Hello ${screen?.name}"

    helloIncrement.setOnClickListener {
      val current = helloCounter.text.toString().toIntOrNull() ?: 0
      helloCounter.text = (current + 1).toString()
    }
  }
}
