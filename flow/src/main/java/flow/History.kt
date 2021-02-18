/*
 * Copyright 2013 Square Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package flow

/**
 * Describes the history of a [Flow] at a specific point in time.
 *
 * *Note: use of this class as an [Iterable] is deprecated. Use [framesFromTop]
 * and [framesFromBottom] instead.*
 */
class History
private constructor(
  private val history: List<Any>
) {

  init {
    require(history.isNotEmpty()) { "History may not be empty" }
  }

  fun <T> framesFromBottom(): Iterable<T> =
    object : Iterable<T> {
      @Suppress("UNCHECKED_CAST")
      override fun iterator() =
        history.iterator() as Iterator<T>
    }

  fun <T> framesFromTop(): Iterable<T> =
    object : Iterable<T> {
      @Suppress("UNCHECKED_CAST")
      override fun iterator() =
        history.asReversed().iterator() as Iterator<T>
    }

  fun size(): Int =
    history.size

  /**
   * Returns the app state at the provided index in history. 0 is the newest entry.
   */
  @Suppress("UNCHECKED_CAST")
  fun <T> peek(index: Int): T =
    history[history.size - index - 1] as T

  fun <T> top(): T =
    peek(0)

  fun asList(): List<Any> =
    history.toList()

  /**
   * Get a builder to modify a copy of this history.
   *
   * The builder returned will retain all internal information related to the keys in the
   * history, including their states. It is safe to remove keys from the builder and push them back
   * on, nothing will be lost in those operations.
   */
  fun buildUpon(): Builder =
    Builder(history)

  override fun toString() =
    history.toTypedArray().contentDeepToString()

  class Builder(history: Collection<Any>) {
    private val history = history.toMutableList()

    val isEmpty: Boolean
      get() = history.isEmpty()

    /**
     * Removes all keys from this builder. But note that if this builder was created
     * via [buildUpon], any state associated with the cleared
     * keys will be preserved and will be restored if they are [pushed][push]
     * back on.
     */
    fun clear(): Builder {
      // Clear by popping everything (rather than just calling history.clear()) to
      // fill up entryMemory. Otherwise we drop view state on the floor.
      while (!isEmpty) {
        pop()
      }
      return this
    }

    /**
     * Adds a key to the builder. If this builder was created via [buildUpon],
     * and the pushed key was previously [popped][pop] or [cleared][clear]
     * from the builder, the key's associated state will be restored.
     */
    fun push(key: Any): Builder {
      history.add(key)
      return this
    }

    /**
     * [Pushes][push] all of the keys in the collection onto this builder.
     */
    fun pushAll(history: Collection<Any>): Builder {
      for (key in history) {
        push(key)
      }
      return this
    }

    /**
     * @return null if the history is empty.
     */
    fun peek(): Any? =
      history.lastOrNull()

    /**
     * Removes the last state added. Note that if this builder was created
     * via [buildUpon], any view state associated with the popped
     * state will be preserved, and restored if it is [pushed][push]
     * back in.
     *
     * @throws IllegalStateException if empty
     */
    fun pop(): Any {
      check(!isEmpty) { "Cannot pop from an empty builder" }
      return history.removeAt(history.lastIndex)
    }

    /**
     * Pops the history until the given state is at the top.
     *
     * @throws IllegalArgumentException if the given state isn't in the history.
     */
    fun popTo(state: Any): Builder {
      while (!isEmpty && peek() != state) {
        pop()
      }
      require(!isEmpty) { "$state not found in history" }
      return this
    }

    fun pop(count: Int): Builder {
      val size = history.size
      require(count <= size) { "Cannot pop $count elements, history only has $size" }
      var counter = count
      while (counter-- > 0) {
        pop()
      }
      return this
    }

    fun build(): History =
      History(history.toList())

    override fun toString() =
      history.toTypedArray().contentDeepToString()
  }
}

internal fun historyBuilder(): History.Builder =
  History.Builder(emptyList())

fun history(action: History.Builder.() -> Unit): History =
  historyBuilder().apply(action).build()

fun historyOf(key: Any): History =
  history { push(key) }

fun historyOf(vararg keys: Any): History =
  history { pushAll(keys.asList()) }

fun historyOf(keys: List<Any>): History =
  history { pushAll(keys) }