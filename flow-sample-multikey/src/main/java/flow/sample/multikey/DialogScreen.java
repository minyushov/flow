package flow.sample.multikey;

import java.util.Collections;
import java.util.List;

import androidx.annotation.NonNull;
import flow.MultiKey;

public final class DialogScreen implements MultiKey {
  final Object mainContent;

  public DialogScreen(Object mainContent) {
    this.mainContent = mainContent;
  }

  @Override public String toString() {
    return "Do you really want to see screen two?";
  }

  @NonNull @Override public List<Object> getKeys() {
    return Collections.singletonList(mainContent);
  }
}
