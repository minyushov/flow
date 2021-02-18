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

package flow.sample.multikey

import android.view.View
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.RootMatchers.isDialog
import androidx.test.espresso.matcher.RootMatchers.withDecorView
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.rules.activityScenarioRule
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import org.hamcrest.Matchers.`is`
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class MultiKeySampleTest {

  @Rule
  @JvmField
  val rule = activityScenarioRule<MultiKeySampleActivity>()

  lateinit var decorView: View

  @Before
  fun setUp() {
    rule.scenario.onActivity { activity ->
      decorView = activity.window.decorView;
    }
  }

  @Test
  fun walkthrough() {
    // Start on ScreenOne.
    onView(withText(ScreenOne().toString())).check(matches(isDisplayed()))

    // Click to show DialogScreen.
    onView(withText(ScreenOne().toString())).perform(click())
    onView(withText(DialogScreen(ScreenTwo()).toString()))
      .inRoot(isDialog())
      .check(matches(isDisplayed()))

    // We should still see ScreenOne behind the dialog.
    onView(withText(ScreenOne().toString()))
      .inRoot(withDecorView(`is`(decorView)))

    // Let's rotate to make sure we keep the dialog and the view.
    UiDevice.getInstance(InstrumentationRegistry.getInstrumentation()).setOrientationRight()

    // Should still see DialogScreen.
    onView(withText(DialogScreen(ScreenTwo()).toString()))
      .inRoot(isDialog())
      .check(matches(isDisplayed()))
    onView(withText(ScreenOne().toString()))
      .inRoot(withDecorView(`is`(decorView)))

    // Click Yes on dialog to advance to ScreenTwo.
    onView(withText("Yes")).inRoot(isDialog()).perform(click())

    // Should be on ScreenTwo.
    onView(withText(ScreenTwo().toString())).check(matches(isDisplayed()))

    // Press back to go back to the dialog.
    pressBack()

    // Should see the Dialog over ScreenOne again.
    onView(withText(DialogScreen(ScreenTwo()).toString()))
      .inRoot(isDialog())
      .check(matches(isDisplayed()))
    onView(withText(ScreenOne().toString()))
      .inRoot(withDecorView(`is`(decorView)))

    // Click forward to ScreenTwo
    // Click Yes on dialog to advance to ScreenTwo.
    onView(withText("Yes")).inRoot(isDialog()).perform(click())

    // Click screen to go back to ScreenOne, skipping the dialog.
    onView(withText(ScreenTwo().toString())).perform(click())
    onView(withText(ScreenOne().toString())).check(matches(isDisplayed()))
  }
}