package flow;

import android.support.annotation.NonNull;

/**
 * Default implementation of {@link HistoryCallback}, enforces the contract
 * documented on {@link NotPersistent}.
 */
class NotPersistentHistoryCallback implements HistoryCallback {
  @NonNull @Override public History onRestoreHistory(@NonNull History history) {
    return history;
  }

  @NonNull @Override public History onSaveHistory(@NonNull History history) {
    History.Builder builder = History.emptyBuilder();

    for (Object key : history.framesFromBottom()) {
      if (!key.getClass().isAnnotationPresent(NotPersistent.class)) {
        builder.push(key);
      }
    }

    return builder.build();
  }
}