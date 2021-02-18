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

import java.util.ArrayList
import java.util.LinkedHashMap

@Mockable
class KeyManager(
  servicesFactories: List<ServicesFactory>
) {

  private val managedServices = LinkedHashMap<Any, ManagedServices>()
  private val states = LinkedHashMap<Any, State>()

  private val servicesFactories = ArrayList<ServicesFactory>()

  init {
    this.servicesFactories.addAll(servicesFactories)
    managedServices[ROOT_KEY] = ManagedServices(Services.ROOT_SERVICES)
  }


  fun hasState(key: Any): Boolean {
    return states.containsKey(key)
  }

  fun addState(state: State) {
    states[state.getKey()] = state
  }

  fun getState(key: Any): State {
    var state = states[key]
    if (state == null) {
      state = State(key)
      addState(state)
    }
    return state
  }

  fun clearStatesExcept(keep: List<Any>) {
    val keys = states.keys.iterator()
    while (keys.hasNext()) {
      val key = keys.next()
      if (!keep.contains(key)) keys.remove()
    }
  }

  fun findServices(key: Any): Services {
    val managed = managedServices[key] ?: throw IllegalStateException("No services currently exists for key $key")
    return managed.services
  }

  fun setUp(key: Any) {
    var parent = managedServices[ROOT_KEY]!!.services
    if (key is MultiKey) {
      for (part in key.keys) {
        setUp(part)
      }
      ensureNode(parent, key).uses++
    } else if (key is TreeKey) {
      val parentKey = key.parentKey
      setUp(parentKey)
      parent = managedServices[parentKey]!!.services
      ensureNode(parent, key).uses++
    } else {
      ensureNode(parent, key).uses++
    }
  }

  fun tearDown(key: Any) {
    if (key is MultiKey) {
      decrementAndMaybeRemoveKey(key)
      val parts = key.keys
      val count = parts.size
      for (i in count - 1 downTo 0) {
        tearDown(parts[i])
      }
    } else if (key is TreeKey) {
      decrementAndMaybeRemoveKey(key)
      tearDown(key.parentKey)
    } else {
      decrementAndMaybeRemoveKey(key)
    }
  }

  private fun ensureNode(parent: Services?, key: Any): ManagedServices {
    var node = managedServices[key]
    if (node == null) {
      // Bind the local key as a service.
      val binder = parent!!.extend(key)//
      // Add any services from the factories
      val count = servicesFactories.size
      for (i in 0 until count) {
        servicesFactories[i].bindServices(binder)
      }
      node = ManagedServices(binder.build())
      managedServices[key] = node
    }
    return node
  }

  private fun decrementAndMaybeRemoveKey(key: Any): Boolean {
    val node = managedServices[key] ?: return false
    node.uses--
    if (key !== ROOT_KEY && node.uses == 0) {
      val count = servicesFactories.size
      for (i in count - 1 downTo 0) {
        servicesFactories[i].tearDownServices(node.services)
      }
      managedServices.remove(key)
      return true
    }
    check(node.uses >= 0) { "Over-decremented uses of key $key" }
    return false
  }

  private class ManagedServices(
    val services: Services
  ) {
    /** Includes uses as a leaf and as a direct parent.  */
    internal var uses = 0
  }

  companion object {
    val ROOT_KEY: Any = object : Any() {
      override fun toString(): String {
        return KeyManager::class.java.simpleName + ".ROOT"
      }
    }
  }
}
