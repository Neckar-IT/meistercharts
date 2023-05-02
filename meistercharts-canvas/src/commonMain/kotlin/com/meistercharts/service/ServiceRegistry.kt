/**
 * Copyright 2023 Neckar IT GmbH, Mössingen, Germany
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.meistercharts.service

import it.neckar.open.dispose.Disposable
import kotlin.reflect.KClass

/**
 * Offers a possibility to register and retrieve services
 */
class ServiceRegistry : Disposable {
  @PublishedApi
  internal val map: MutableMap<Any, Any> = mutableMapOf()

  override fun dispose() {
    map
      .values
      .filterIsInstance<Disposable>()
      .forEach {
        it.dispose()
      }

    map.clear()
  }

  /**
   * Returns the service of the given type
   */
  fun <T : Any> find(type: KClass<T>): T? {
    @Suppress("UNCHECKED_CAST")
    return map[type] as? T
  }

  /**
   * Returns the service of the given type.
   * Uses the given function to register an instance of the service if no service is found.
   */
  inline fun <T : Any> get(type: KClass<T>, serviceProvider: () -> T): T {
    return find(type) ?: serviceProvider().also {
      map[type] = it
    }
  }
}
