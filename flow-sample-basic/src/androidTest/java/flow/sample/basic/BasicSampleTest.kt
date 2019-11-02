package flow.sample.basic

import android.content.Context
import android.content.pm.ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
import android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
import android.content.res.Configuration.ORIENTATION_PORTRAIT
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.rule.ActivityTestRule
import org.junit.Rule
import org.junit.Test

/**
 * Verifies that simple navigation works, along with Flow and view state persistence across
 * navigation and across configuration changes.
 */
class BasicSampleTest {

  @Rule
  @JvmField
  val rule = ActivityTestRule(BasicSampleActivity::class.java)

  /** Verifies that the app is in its default state on a cold start.  */
  @Test
  fun defaultKeyIsUsed() {
    onView(withId(R.id.contentView))
      .check(matches(hasDescendant(isAssignableFrom(WelcomeView::class.java))))
  }

  /**
   * Verifies that the current Flow state is maintained, as well as view state associated with
   * Flow state.
   */
  @Test
  fun rotationMaintainsState() {

    // Enter some text on the welcome screen
    onView(withId(R.id.welcomeScreenName))
      .perform(ViewActions.typeText("Bart"))

    rotate()

    // We should still have that text, despite the configuration change.
    onView(withId(R.id.welcomeScreenName))
      .check(matches(withText("Bart")))

    // Continue to the next screen and verify that it's showing info from our Flow state object.
    onView(withId(R.id.welcomeScreenName))
      .perform(ViewActions.typeText("\n"))
    onView(withId(R.id.helloName))
      .check(matches(withText("Hello Bart")))

    // Change the text in the counter TextView. Only this view knows its state, we don't store it
    // anywhere else.
    onView(withId(R.id.helloIncrement))
      .perform(click())
      .perform(click())
    onView(withId(R.id.helloCounter))
      .check(matches(withText("2")))

    rotate()

    // Verify that we still have our Flow state object.
    onView(withId(R.id.helloName))
      .check(matches(withText("Hello Bart")))
    // Verify that the counter TextView's view state was restored.
    onView(withId(R.id.helloCounter))
      .check(matches(withText("2")))
  }

  /** Verifies that states in the history keep their associated view state.  */
  @Test
  fun goingBackWorksAndRestoresState() {

    // Enter some text in the name field and go forward.
    // The field's view state, including the text we entered, should be remembered in the history.
    onView(withId(R.id.welcomeScreenName))
      .perform(ViewActions.typeText("Bart\n"))
    onView(withId(R.id.contentView))
      .check(matches(hasDescendant(isAssignableFrom(HelloView::class.java))))

    pressBack()

    onView(withId(R.id.contentView))
      .check(matches(hasDescendant(isAssignableFrom(WelcomeView::class.java))))

    // When we navigated back, the view state of the name field should have been restored.
    onView(withId(R.id.welcomeScreenName))
      .check(matches(withText("Bart")))
  }

  private fun rotate() {
    val config = ApplicationProvider.getApplicationContext<Context>().resources.configuration
    rule.activity.requestedOrientation = if (config.orientation == ORIENTATION_PORTRAIT)
      SCREEN_ORIENTATION_LANDSCAPE
    else
      SCREEN_ORIENTATION_PORTRAIT
  }
}
