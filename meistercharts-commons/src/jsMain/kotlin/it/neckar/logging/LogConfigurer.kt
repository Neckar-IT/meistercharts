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

import it.neckar.logging.impl.LoggerImplJs


/**
 * Configures the log levels for JS
 */
object LogConfigurer {
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

  fun setLogLevel(loggerName: String, logLevel: Level) {
    setLogLevel(LoggerFactory.getLogger(loggerName), logLevel)
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
   * Will return the root level if no level is configured for the logger itself
   */
  fun getEffectiveLogLevel(logger: Logger): Level {
    return (logger as LoggerImplJs).level ?: rootLevel
  }

  /**
   * Initializes the log configuration from local storage.
   * If no root level could be found in local storage, the provided [fallbackRootLevel] is configured
   */
  fun initializeFromLocalStorage(fallbackRootLevel: Level = Level.INFO) {
    rootLevel = LoggerLocalStorage.readRootLevel() ?: fallbackRootLevel

    LoggerLocalStorage.readLoggerLevels { loggerName: LoggerName, level: Level ->
      setLogLevel(LoggerFactory.getLogger(loggerName), level)
    }
  }

  /**
   * Saves the current configuration to local storage
   */
  fun saveConfigurationToLocalStorage() {
    LoggerLocalStorage.storeRootLevel(rootLevel)

    LoggerFactory.cachedInstances().forEach { (loggerName, logger) ->
      val level = getSpecificLogLevel(logger)
      if (level != null) {
        LoggerLocalStorage.storeLoggerLevel(loggerName, level)
      }
    }
  }
}
