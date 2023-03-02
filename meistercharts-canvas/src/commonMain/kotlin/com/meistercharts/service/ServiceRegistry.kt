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
