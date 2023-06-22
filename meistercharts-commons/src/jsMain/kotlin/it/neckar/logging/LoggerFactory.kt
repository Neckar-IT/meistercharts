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
      LoggerImplJs(loggerName, calculatePrefix(loggerName))
    }
  }

  /**
   * Creates the prefix for the logger
   */
  private fun calculatePrefix(loggerName: String): String {
    val parts = loggerName.split('.')
    if (parts.isEmpty()) return ""

    return buildString {
      for (i in 0 until parts.size - 1) {
        append(parts[i][0]) //add the first char of the part
        append('.')
      }

      append(parts.last())
    }
  }

  fun getLoggerOrNull(loggerName: String): Logger? {
    return cachedInstances[loggerName]
  }
}
