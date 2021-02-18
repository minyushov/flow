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

package flow.sample.tree.ui.contacts.list

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import flow.sample.tree.R
import flow.sample.tree.model.Contact

class ContactsAdapter : BaseAdapter() {

  private val contacts = mutableListOf<Contact>()

  fun setContacts(contacts: List<Contact>) {
    this.contacts.clear()
    this.contacts.addAll(contacts)
    notifyDataSetChanged()
  }

  override fun getCount() =
    contacts.size

  override fun getItem(position: Int) =
    contacts[position]

  override fun getItemId(position: Int) =
    position.toLong()

  override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
    val contact = getItem(position)
    val view = convertView ?: LayoutInflater.from(parent.context).inflate(R.layout.list_contacts_screen_row_view, parent, false)
    view.findViewById<TextView>(R.id.contact_name).text = contact.name
    view.findViewById<TextView>(R.id.contact_email).text = contact.email
    return view
  }

}