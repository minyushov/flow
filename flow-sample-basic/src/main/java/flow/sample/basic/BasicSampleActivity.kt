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
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import flow.HistoryCallback
import flow.flow
import flow.withFlow

class BasicSampleActivity : AppCompatActivity(), HistoryCallback {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.sample_activity)
  }

  override fun attachBaseContext(baseContext: Context) {
    super.attachBaseContext(
      baseContext.withFlow(
        activity = this,
        dispatcher = BasicDispatcher(this),
        defaultKey = WelcomeScreen(),
        keyParceler = BasicKeyParceler(),
        historyCallback = this
      )
    )
  }

  override fun onBackPressed() {
    flow.goBack()
  }

  override fun onHistoryCleared() {
    super.onBackPressed()
  }
}