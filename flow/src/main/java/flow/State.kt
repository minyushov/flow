/*
 * Copyright 2016 Square Inc.
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

import android.os.Bundle
import android.os.Parcelable
import android.util.SparseArray
import android.view.View
import java.util.LinkedHashMap

open class State(
  private val key: Any
) {
  open var bundle: Bundle? = null
  internal val viewStateById = LinkedHashMap<Int, SparseArray<Parcelable>>()

  fun <T> getKey(): T {
    @Suppress("UNCHECKED_CAST")
    return key as T
  }

  /**
   * Save view hierarchy state so it can be restored later from [.restore].  The view
   * must have a non-zero id.
   */
  open fun save(view: View) {
    val viewId = view.id
    require(viewId != 0) { "Cannot save state for View with no id ${view.javaClass.simpleName}" }
    val state = SparseArray<Parcelable>()
    view.saveHierarchyState(state)
    viewStateById[viewId] = state
  }

  open fun restore(view: View) {
    val viewState = viewStateById[view.id]
    if (viewState != null) {
      view.restoreHierarchyState(viewState)
    }
  }

  internal fun toBundle(parceler: KeyParceler): Bundle {
    val outState = Bundle()
    outState.putParcelable(KEY, parceler.toParcelable(getKey()))
    val viewIds = IntArray(viewStateById.size)
    var c = 0
    for ((viewId, viewState) in viewStateById) {
      viewIds[c++] = viewId
      if (viewState.size() > 0) {
        outState.putSparseParcelableArray(VIEW_STATE_PREFIX + viewId, viewState)
      }
    }
    outState.putIntArray(VIEW_STATE_IDS, viewIds)
    if (bundle != null && !bundle!!.isEmpty) {
      outState.putBundle(BUNDLE, bundle)
    }
    return outState
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other == null || javaClass != other.javaClass) return false
    val state = other as State?
    return getKey<Any>() == state!!.getKey<Any>()
  }

  override fun hashCode(): Int {
    return getKey<Any>().hashCode()
  }

  override fun toString(): String {
    return getKey<Any>().toString()
  }
}

/**
 * Creates a State instance that has no state and is effectively immutable.
 */
internal fun emptyState(key: Any): State = EmptyState(key)

private class EmptyState(state: Any) : State(state) {
  @Suppress("UNUSED_PARAMETER")
  override var bundle: Bundle?
    get() = null
    set(bundle) = Unit

  override fun save(view: View) = Unit
  override fun restore(view: View) = Unit
}


internal fun Bundle.toState(parceler: KeyParceler): State {
  val parcelable = getParcelable<Parcelable>(KEY) ?: throw NoSuchElementException("Parcelable for key not found")
  val state = State(parceler.toKey(parcelable))
  val viewIds = getIntArray(VIEW_STATE_IDS) ?: throw NoSuchElementException("Null view state ids?")
  for (viewId in viewIds) {
    val viewState = getSparseParcelableArray<Parcelable>(VIEW_STATE_PREFIX + viewId)
    if (viewState != null) {
      state.viewStateById[viewId] = viewState
    }
  }
  state.bundle = getBundle(BUNDLE)
  return state
}

private const val VIEW_STATE_IDS = "VIEW_STATE_IDS"
private const val BUNDLE = "BUNDLE"
private const val VIEW_STATE_PREFIX = "VIEW_STATE_"
private const val KEY = "KEY"