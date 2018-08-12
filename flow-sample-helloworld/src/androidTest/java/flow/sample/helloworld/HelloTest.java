package flow.sample.helloworld;

import org.junit.Rule;
import org.junit.Test;

import androidx.test.rule.ActivityTestRule;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

public class HelloTest {
  @Rule public ActivityTestRule rule = new ActivityTestRule<>(HelloWorldActivity.class);

  @Test public void hello() {
    onView(withText("Hello, World!")).perform(click());
  }
}
