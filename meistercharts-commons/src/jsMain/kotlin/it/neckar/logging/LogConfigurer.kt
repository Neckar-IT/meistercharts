package it.neckar.logging

import it.neckar.logging.impl.LoggerImplJs
import kotlinx.browser.window


/**
 * Configures the log levels for JS
 */
object LogConfigurer {
  /**
   * The key for the debug root level
   */
  const val LocalStorageKey: String = it.neckar.logging.LoggerLocalStorage.LocalStorageKey

  /**
   * The root logger level
   */
  var rootLevel: Level = Level.DEBUG
    private set

  /**
   * Sets the root logger level
   */
  fun setRootLoggerLevel(logLevel: Level) {
    rootLevel = logLevel
  }

  /**
   * Sets the log level for a given logger
   */
  fun setLogLevel(logger: Logger, logLevel: Level) {
    (logger as LoggerImplJs).level = logLevel
  }

  /**
   * Returns the directly configured log level for the logger.
   * Call [getEffectiveLogLevel] in most cases instead.
   */
  fun getSpecificLogLevel(logger: Logger): Level? {
    return (logger as LoggerImplJs).level
  }

  /**
   * Returns the effective log level.
   * Will return the root level if no level if configured for the logger itself
   */
  fun getEffectiveLogLevel(logger: Logger): Level {
    return (logger as LoggerImplJs).level ?: rootLevel
  }

  /**
   * Loads the root level from local storage
   */
  fun getRootLevelFromLocalStorage(): Level? {
    val item = window.localStorage.getItem(LoggerLocalStorage.LocalStorageKey) ?: return null
    return try {
      Level.valueOf(item.trim())
    } catch (e: Exception) {
      console.warn("Invalid value for ${LoggerLocalStorage.LocalStorageKey}: $item. Expected one of ${Level.entries.joinToString(", ")}")
      null //Fallback to null
    }
  }

  /**
   * Initializes the log configuration from local storage.
   * If no root level could be found in local storage, the provided [fallbackRootLevel] is configured
   */
  fun initializeFromLocalStorage(fallbackRootLevel: Level = Level.INFO) {
    rootLevel = getRootLevelFromLocalStorage() ?: fallbackRootLevel
  }
}
