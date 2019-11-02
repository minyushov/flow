package flow.sample.helloworld

import org.junit.Rule
import org.junit.Test

import androidx.test.rule.ActivityTestRule

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.ViewMatchers.withText

class HelloTest {
  @Rule
  @JvmField
  val rule = ActivityTestRule(HelloWorldActivity::class.java)

  @Test
  fun hello() {
    onView(withText("Hello, World!")).perform(click())
  }
}