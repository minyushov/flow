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

package flow.sample.tree.model

val CONTACT_HOMER = Contact("iuoxcuioxcuiov", "Homer Simpson", "homer@example.net")
val CONTACT_MARGE = Contact("9sduoihoi9h0h9", "Marge Simpson", "marge@example.net")
val CONTACT_BART = Contact("kjaskdjaksdjad", "Bart Simpson", "bart@example.net")
val CONTACT_LISA = Contact("n9chzdfofnoifh", "Lisa Simpson", "lisa@example.net")
val CONTACT_MAGGIE = Contact("a1j3j4k1241jk2", "Maggie Simpson", "maggie@example.net")

class ContactsStorage {

  private val contacts = mutableMapOf<String, Contact>()

  init {
    saveContact(CONTACT_HOMER)
    saveContact(CONTACT_MARGE)
    saveContact(CONTACT_BART)
    saveContact(CONTACT_LISA)
    saveContact(CONTACT_MAGGIE)
  }

  fun contacts(): List<Contact> =
    contacts.values.toList()

  fun getContact(id: String): Contact? =
    contacts[id]

  fun saveContact(contact: Contact) {
    contacts[contact.id] = contact
  }

}