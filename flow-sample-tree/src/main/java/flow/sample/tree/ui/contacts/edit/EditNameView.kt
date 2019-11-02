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

package flow.sample.tree.ui.contacts.edit

import android.content.Context
import android.text.TextWatcher
import android.util.AttributeSet
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.widget.doOnTextChanged
import flow.flow
import flow.getService
import flow.sample.tree.FlowServices
import flow.sample.tree.R
import flow.sample.tree.model.ContactEditor

class EditNameView(
  context: Context,
  attrs: AttributeSet
) : LinearLayout(
  context,
  attrs
) {

  private val editor = getService<ContactEditor>(FlowServices.CONTACT_EDITOR) ?: throw IllegalStateException()

  private lateinit var emailView: TextView
  private lateinit var nameView: EditText
  private lateinit var nameWatcher: TextWatcher

  init {
    orientation = VERTICAL
  }

  override fun onFinishInflate() {
    super.onFinishInflate()
    emailView = findViewById(R.id.email)
    nameView = findViewById(R.id.edit_name)
  }

  override fun onAttachedToWindow() {
    super.onAttachedToWindow()
    emailView.text = editor.email
    nameView.setText(editor.name)
    nameWatcher = nameView.doOnTextChanged { text, _, _, _ ->
      editor.name = text.toString()
    }

    nameView.setOnEditorActionListener { _, _, _ ->
      flow.set(EditEmailScreen(editor.id))
      true
    }
  }

  override fun onDetachedFromWindow() {
    nameView.setOnEditorActionListener(null)
    nameView.removeTextChangedListener(nameWatcher)
    super.onDetachedFromWindow()
  }

}