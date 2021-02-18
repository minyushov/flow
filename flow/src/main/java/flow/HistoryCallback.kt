package flow

import android.app.Activity

/**
 * An object to which gets a chance to modify the current [History] before
 * it is persisted, or after it is restored.
 */
interface HistoryCallback {
  fun onRestoreHistory(history: History): History =
    history

  fun onSaveHistory(history: History): History =
    history

  /**
   * Called when history is cleared.
   * Default implementation calls [Activity.finish]
   */
  fun onHistoryCleared() = Unit
}