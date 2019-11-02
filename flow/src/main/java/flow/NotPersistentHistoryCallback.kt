package flow

/**
 * Default implementation of [HistoryCallback], enforces the contract
 * documented on [NotPersistent].
 */
open class NotPersistentHistoryCallback : HistoryCallback {
  override fun onSaveHistory(history: History): History {
    val builder = historyBuilder()

    for (key in history.framesFromBottom<Any>()) {
      if (!key.javaClass.isAnnotationPresent(NotPersistent::class.java)) {
        builder.push(key)
      }
    }

    return builder.build()
  }
}