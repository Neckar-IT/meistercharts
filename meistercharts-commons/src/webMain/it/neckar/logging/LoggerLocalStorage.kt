/*
 * Copyright (C) 2013-2026 Neckar IT GmbH, Mössingen, Germany
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Linking this library statically or dynamically with other modules is
 * making a combined work based on this library. Thus, the terms and
 * conditions of the GNU General Public License cover the whole combination.
 *
 * As a special exception, the copyright holders of this library give you
 * permission to link this library with independent modules, regardless of
 * the license terms of these independent modules, and to copy and distribute
 * the resulting combined work under terms of your choice, provided that every
 * copy of the combined work is accompanied by a complete copy of the source
 * code of this library.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
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
      it.neckar.logging.impl.consoleWarn("Invalid value for ${key}: $item. Expected one of ${Level.entries.joinToString(", ")} - $e")
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
   * Clears all logger levels from the local storage.
   *
   * Collects matching keys first, then removes them — iterating and removing
   * in the same pass shifts the remaining indices and silently skips entries.
   */
  fun clearLoggers() {
    val keysToRemove = mutableListOf<String>()
    window.localStorage.length.fastFor { index ->
      val key = window.localStorage.key(index) ?: return@fastFor
      if (key.startsWith(LoggerLocalStorageKeys.LoggerPrefix)) {
        keysToRemove.add(key)
      }
    }
    keysToRemove.forEach { window.localStorage.removeItem(it) }
  }
}
