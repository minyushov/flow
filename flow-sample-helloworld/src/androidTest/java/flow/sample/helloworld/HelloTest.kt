package flow.sample.helloworld

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.rules.activityScenarioRule
import org.junit.Rule
import org.junit.Test

class HelloTest {
  @Rule
  @JvmField
  val rule = activityScenarioRule<HelloWorldActivity>()

  @Test
  fun hello() {
    onView(withText("Hello, World!")).perform(click())
  }
}