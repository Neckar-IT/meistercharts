package it.neckar.logging

import org.slf4j.LoggerFactory
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty
import kotlin.reflect.full.companionObject

/**
 * Provides a slf4j logger that resolves the name of the enclosing class, automatically.
 */
class LoggerDelegate<in R : Any> : ReadOnlyProperty<R, Logger> {
  override fun getValue(thisRef: R, property: KProperty<*>): Logger = LoggerFactory.getLogger(getClassForLogging(thisRef.javaClass))
}

/**
 * Extracts the class for logging
 */
fun <T : Any> getClassForLogging(javaClass: Class<T>): Class<*> {
  return javaClass.enclosingClass?.takeIf {
    it.kotlin.companionObject?.java == javaClass
  } ?: javaClass
}

