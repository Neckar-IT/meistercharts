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
  private val cachedInstances = mutableMapOf<LoggerName, Logger>()

  /**
   * Returns all cached logger instances.
   * This method should only be used for testing/debugging.
   */
  fun cachedInstances(): Map<LoggerName, Logger> {
    return cachedInstances.toMap()
  }

  /**
   * Returns a logger instance for JS.
   * Creates a new instance if there is none.
   */
  actual fun getLogger(loggerName: LoggerName): Logger {
    return cachedInstances.getOrPut(loggerName) {
      LoggerImplJs(loggerName)
    }
  }

  actual fun getLogger(loggerName: String): Logger{
    return getLogger(LoggerName(loggerName))
  }

  /**
   * Returns the logger - if there is one, or returns null
   */
  fun getLoggerOrNull(loggerName: LoggerName): Logger? {
    return cachedInstances[loggerName]
  }

  /**
   * Returns all shortened loggers that match the provided [shortenedLoggerName]
   */
  fun findLoggerByShortenedName(shortenedLoggerName: ShortenedLoggerName): List<Logger> {
    return cachedInstances.values.filter {
      shortenedLoggerName.matches(it.loggerName)
    }
  }
}
