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

package flow.sample.tree

import flow.Services
import flow.ServicesFactory
import flow.sample.tree.model.Contact
import flow.sample.tree.model.ContactEditor
import flow.sample.tree.model.ContactsStorage
import flow.sample.tree.ui.contacts.ContactsUiKey
import flow.sample.tree.ui.contacts.edit.EditContactKey
import java.lang.IllegalStateException

class FlowServices : ServicesFactory() {

  // In a real app, the conditional class matching shown here doesn't scale very far. Decompose by
  // keys. Even better, keep your ServicesFactory lean and simple by using the key to build/lookup
  // a Dagger graph or Mortar scope!

  override fun bindServices(services: Services.Binder) {
    when (val key = services.getKey<Any>()) {
      ContactsUiKey() -> // Setting up the ContactsUiKey means providing storage for Contacts.
        services.bind(CONTACTS_STORAGE, ContactsStorage())
      is EditContactKey -> {
        // Setting up the EditContactKey key means providing an editor for the contact.
        // This editor can be shared among any keys that have the EditContactKey as parent/ancestor!
        val contactId = key.contactId
        val storage = services.getService<ContactsStorage>(CONTACTS_STORAGE) ?: throw IllegalStateException("'ContactsStorage' service not found")
        val contact = storage.getContact(contactId)
        services.bind(CONTACT_EDITOR, ContactEditor(contact!!))
      }
    }
  }

  override fun tearDownServices(services: Services) {
    // Nothing to do in this example, but if you need this hook to release resources, it's here!
    super.tearDownServices(services)
  }

  companion object {
    const val CONTACTS_STORAGE = "CONTACTS_STORAGE"
    const val CONTACT_EDITOR = "CONTACT_EDITOR"
  }

}