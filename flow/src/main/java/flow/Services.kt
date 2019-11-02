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

import java.util.LinkedHashMap

open class Services
private constructor(
  private val key: Any,
  private val delegate: Services?,
  services: Map<String, Any>
) {

  private val services = services.toMutableMap()

  class Binder(
    private val base: Services,
    key: Any
  ) : Services(
    key,
    base,
    emptyMap()
  ) {
    private val services = mutableMapOf<String, Any>()

    fun bind(serviceName: String, service: Any): Binder {
      services[serviceName] = service
      return this
    }

    internal fun build(): Services {
      return Services(getKey(), base, services)
    }
  }

  fun <T> getService(name: String): T? {
    return if (services.containsKey(name)) {
      services[name] as T?
    } else delegate?.getService<T>(name)
  }

  fun <T> getKey(): T {
    return this.key as T
  }

  internal fun extend(key: Any): Binder =
    Binder(this, key)

  companion object {
    val ROOT_SERVICES = Services(Flow.ROOT_KEY, null, emptyMap())
  }

}