package it.neckar.logging

import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

/**
 * Provides a logger that resolves the name of the enclosing class, automatically.
 *
 *
 * ATTENTION: Does *NOT* work for companion objects in JS unfortunately
 */
@Deprecated("Does not work in JS very well - at least in companion objects")
class LoggerDelegate<in R : Any> : ReadOnlyProperty<R, Logger> {
  override fun getValue(thisRef: R, property: KProperty<*>): Logger = LoggerFactory.getLogger(getLoggerName(thisRef))

  private fun getLoggerName(thisRef: R): String {
    return thisRef::class.js.name
  }
}
