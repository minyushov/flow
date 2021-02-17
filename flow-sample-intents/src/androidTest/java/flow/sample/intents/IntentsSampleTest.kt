package flow.sample.intents

import android.content.Intent
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.pressBack
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.platform.app.InstrumentationRegistry
import flow.Flow
import flow.historyOf
import org.junit.Test

/**
 * Demonstrates the use of Intents to drive espresso tests to specific screens.
 */
class IntentsSampleTest {
  @Test
  fun singleInstanceActivity() {
    val history = historyOf("Able", "Baker", "Charlie")

    val context = InstrumentationRegistry.getInstrumentation().targetContext
    val intent = Intent(context, IntentsSingleInstanceSampleActivity::class.java)
    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
    Flow.addHistory(intent, history, StringParceler())

    context.startActivity(intent)

    onView(withText("Charlie")).perform(pressBack())
    onView(withText("Baker")).perform(pressBack())
    onView(withText("Able")).perform(click())
  }

  @Test
  fun standardActivity() {
    val history = historyOf("Higgledy", "Piggledy", "Pop")

    val context = InstrumentationRegistry.getInstrumentation().targetContext
    val intent = Intent(context, IntentsStandardSampleActivity::class.java)
    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
    Flow.addHistory(intent, history, StringParceler())

    context.startActivity(intent)

    onView(withText("Pop")).perform(pressBack())
    onView(withText("Piggledy")).perform(pressBack())
    onView(withText("Higgledy")).perform(click())
  }
}