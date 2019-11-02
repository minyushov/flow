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
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.widget.doOnTextChanged
import flow.flow
import flow.getService
import flow.sample.tree.FlowServices
import flow.sample.tree.R
import flow.sample.tree.model.ContactEditor
import flow.sample.tree.model.ContactsStorage
import flow.sample.tree.ui.contacts.list.ListContactsScreen

class EditEmailView(
  context: Context,
  attrs: AttributeSet
) : LinearLayout(
  context,
  attrs
) {

  private var editor = getService<ContactEditor>(FlowServices.CONTACT_EDITOR) ?: throw IllegalStateException()

  private lateinit var nameView: TextView
  private lateinit var emailView: EditText
  private lateinit var emailWatcher: TextWatcher
  private lateinit var saveButton: View

  init {
    orientation = VERTICAL
  }

  override fun onFinishInflate() {
    super.onFinishInflate()
    nameView = findViewById(R.id.name)
    emailView = findViewById(R.id.edit_email)
    saveButton = findViewById(R.id.save)
  }

  override fun onAttachedToWindow() {
    super.onAttachedToWindow()
    nameView.text = editor.name
    emailView.setText(editor.email)
    emailWatcher = emailView.doOnTextChanged { text, _, _, _ ->
      editor.email = text.toString()
    }

    saveButton.setOnClickListener {
      editor.email = emailView.text.toString()
      val storage = getService<ContactsStorage>(FlowServices.CONTACTS_STORAGE)
      storage?.saveContact(editor.toContact())
      flow.set(ListContactsScreen())
    }
  }

  override fun onDetachedFromWindow() {
    saveButton.setOnClickListener(null)
    emailView.removeTextChangedListener(emailWatcher)
    super.onDetachedFromWindow()
  }

}