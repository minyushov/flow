package flow;

import androidx.annotation.NonNull;

/**
 * An object to which gets a chance to modify the current {@link History} before
 * it is persisted, or after it is restored.
 */
public interface HistoryFilter {
  @NonNull
  History onRestoreHistory(@NonNull History history);

  @NonNull
  History onSaveHistory(@NonNull History history);
}