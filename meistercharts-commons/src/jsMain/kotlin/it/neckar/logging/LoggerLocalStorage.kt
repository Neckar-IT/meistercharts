package it.neckar.logging

import it.neckar.open.kotlin.lang.fastFor
import kotlinx.browser.window

/**
 * Offers access to the local storage related to loggin
 */
object LoggerLocalStorage {
  /**
   * Returns the root level (as string) from the local storage
   */
  fun readRootLevel(): Level? {
    val item = window.localStorage.getItem(LoggerLocalStorageKeys.RootLevel) ?: return null
    return parseLevelSafe(item, LoggerLocalStorageKeys.RootLevel)
  }

  /**
   * Stores the root level in the local storage
   */
  fun storeRootLevel(rootLevel: Level) {
    window.localStorage.setItem(LoggerLocalStorageKeys.RootLevel, rootLevel.name)
  }

  /**
   * Parses the level from the given string. Does not throw an exception.
   * Instead, null is returned if the level is invalid.
   */
  private fun parseLevelSafe(item: String, key: String): Level? {
    return try {
      Level.valueOf(item.trim())
    } catch (e: Exception) {
      console.warn("Invalid value for ${key}: $item. Expected one of ${Level.entries.joinToString(", ")}", e)
      null //Fallback to null
    }
  }

  /**
   * Reads all logger levels
   */
  fun readLoggerLevels(callback: (loggerName: LoggerName, level: Level) -> Unit) {
    window.localStorage.length.fastFor { index ->
      val key = window.localStorage.key(index) ?: return@fastFor

      if (key.startsWith(LoggerLocalStorageKeys.LoggerPrefix)) {
        val loggerName = LoggerName(key.substringAfter(LoggerLocalStorageKeys.LoggerPrefix))

        val level = parseLevelSafe(window.localStorage.getItem(key) ?: return@fastFor, key)
        if (level != null) {
          callback(loggerName, level)
        }
      }
    }
  }

  fun storeLoggerLevel(logger: Logger, level: Level) {
    storeLoggerLevel(logger.loggerName, level)
  }

  fun storeLoggerLevel(loggerName: LoggerName, level: Level) {
    window.localStorage.setItem("${LoggerLocalStorageKeys.LoggerPrefix}$loggerName", level.name)
  }

  /**
   * Remove the local storage entry for the root level
   */
  fun clearRootLevel() {
    window.localStorage.removeItem(LoggerLocalStorageKeys.RootLevel)
  }

  /**
   * Clears all logger levels from the local storage
   */
  fun clearLoggers() {
    window.localStorage.length.fastFor { index ->
      val key = window.localStorage.key(index) ?: return@fastFor
      if (key.startsWith(LoggerLocalStorageKeys.LoggerPrefix)) {
        window.localStorage.removeItem(key)
      }
    }
  }
}
