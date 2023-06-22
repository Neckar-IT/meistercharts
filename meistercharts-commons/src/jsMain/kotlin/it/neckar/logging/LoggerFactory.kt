package it.neckar.logging

import it.neckar.logging.impl.LoggerImplJs

/**
 * Logger factory implementation for JS.
 *
 * Is configured using [LoggerLocalStorageKeys] - which loads the settings from the local storage ([LocalStorageKey])
 */
actual object LoggerFactory {
  /**
   * Contains the cached logger instances
   */
  private val cachedInstances = mutableMapOf<String, Logger>()

  fun cachedInstances(): Map<String, Logger> {
    return cachedInstances.toMap()
  }

  /**
   * Returns a new logger instance for JS
   */
  actual fun getLogger(loggerName: String): Logger {
    return cachedInstances.getOrPut(loggerName) {
      LoggerImplJs(loggerName)
    }
  }

  fun getLoggerOrNull(loggerName: String): Logger? {
    return cachedInstances[loggerName]
  }
}
