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
import android.widget.EditText
import android.widget.LinearLayout

import flow.flow

class WelcomeView
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
    id = R.id.welcome
  }

  override fun onFinishInflate() {
    super.onFinishInflate()

    val nameView = findViewById<EditText>(R.id.welcomeScreenName)

    nameView.setOnEditorActionListener { view, actionId, event ->
      view.flow.set(HelloScreen(view.text.toString()))
      true
    }
  }

}