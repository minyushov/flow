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

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import flow.DefaultKeyDispatcher
import flow.Direction
import flow.KeyChanger
import flow.State
import flow.TraversalCallback
import flow.flow
import flow.sample.tree.ui.contacts.edit.EditEmailScreen
import flow.sample.tree.ui.contacts.edit.EditNameScreen
import flow.sample.tree.ui.contacts.list.ListContactsScreen
import flow.sample.tree.ui.welcome.WelcomeScreen
import flow.withFlow

class TreeSampleActivity : AppCompatActivity() {

  override fun attachBaseContext(baseContext: Context) {
    super.attachBaseContext(
      baseContext.withFlow(
        activity = this,
        servicesFactories = listOf(FlowServices()),
        dispatcher = DefaultKeyDispatcher(
          activity = this,
          keyChanger = Changer()
        ),
        defaultKey = WelcomeScreen()
      )
    )
  }

  override fun onBackPressed() {
    flow.goBack()
  }

  private inner class Changer : KeyChanger {
    override fun changeKey(
      outgoingState: State?,
      incomingState: State,
      direction: Direction,
      incomingContexts: Map<Any, Context>,
      callback: TraversalCallback
    ) {

      val key = incomingState.getKey<Any>()
      val context = incomingContexts[key]

      if (outgoingState != null) {
        val view = findViewById<ViewGroup>(android.R.id.content)
        outgoingState.save(view.getChildAt(0))
      }

      val view = when (key) {
        is WelcomeScreen -> showKeyAsText(context, key, ListContactsScreen())
        is ListContactsScreen -> showLayout(context, R.layout.list_contacts_screen)
        is EditNameScreen -> showLayout(context, R.layout.edit_name_screen)
        is EditEmailScreen -> showLayout(context, R.layout.edit_email_screen)
        else -> showKeyAsText(context, key, null)
      }

      incomingState.restore(view)
      setContentView(view)
      callback.onTraversalCompleted()
    }

    private fun showLayout(context: Context?, @LayoutRes layout: Int) =
      LayoutInflater.from(context).inflate(layout, null)

    private fun showKeyAsText(context: Context?, key: Any, nextScreenOnClick: Any?): View {
      val view = TextView(context)
      view.text = key.toString()

      if (nextScreenOnClick == null) {
        view.setOnClickListener(null)
      } else {
        view.setOnClickListener { flow.set(nextScreenOnClick) }
      }
      return view
    }
  }

}