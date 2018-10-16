package flow;

import android.app.Activity;
import androidx.annotation.NonNull;

/**
 * An object to which gets a chance to modify the current {@link History} before
 * it is persisted, or after it is restored.
 */
public interface HistoryCallback {
  @NonNull
  default History onRestoreHistory(@NonNull History history) {
    return history;
  }

  @NonNull
  default History onSaveHistory(@NonNull History history) {
    return history;
  }

  /**
   * Called when history is cleared.
   * Default implementation calls {@link Activity#finish()}}
   */
  default void onHistoryCleared() {
  }
}