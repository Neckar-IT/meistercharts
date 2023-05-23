package it.neckar.logging

import org.slf4j.LoggerFactory
import kotlin.reflect.KClass

/**
 * This logger factory is required to allow common code to fetch a logger.
 */
actual object LoggerFactory {
  /**
   * Returns the logger for the given name
   */
  actual fun getLogger(loggerName: String): Logger {
    return LoggerFactory.getLogger(loggerName)
  }

  fun getLogger(kClass: KClass<*>): Logger {
    return getLogger(kClass.qualifiedName ?: throw IllegalArgumentException("Only supported for classes with qualified name <$kClass>"))
  }
}
