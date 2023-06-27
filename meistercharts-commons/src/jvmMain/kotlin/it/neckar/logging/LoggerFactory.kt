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
  actual fun getLogger(loggerName: LoggerName): Logger {
    return LoggerFactory.getLogger(loggerName.value)
  }

  actual fun getLogger(loggerName: String): Logger {
    return getLogger(LoggerName(loggerName))
  }

  /**
   * Returns the logger for the provided class. Uses the qualified name of the class as logger name.
   */
  fun getLogger(kClass: KClass<*>): Logger {
    val qualifiedName = kClass.qualifiedName ?: throw IllegalArgumentException("Only supported for classes with qualified name <$kClass>")
    return getLogger(LoggerName(qualifiedName))
  }
}
