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
package it.neckar.open.test.utils

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.LoggerContext
import it.neckar.commons.logback.toLogback
import org.slf4j.LoggerFactory

/**
 * Utility object to disable/enable logging for tests.
 */
object LoggingTestSupport {

  fun forceLoggingLevel(level: org.slf4j.event.Level): OriginalLevels {
    return forceLoggingLevel(level.toLogback())
  }

  /**
   * Forces the logging level of all loggers to the given level.
   */
  fun forceLoggingLevel(level: Level = Level.ERROR): OriginalLevels {
    val loggerContext = LoggerFactory.getILoggerFactory() as LoggerContext

    val originalLevels = loggerContext.getLoggerList().filter { it.level != null }.associate { it.name to it.level }
    loggerContext.getLoggerList().forEach {
      if (it.level != null) {
        it.level = level
      }
    }

    return OriginalLevels(originalLevels)
  }

  /**
   * Sets the original levels (again) for all loggers.
   */
  fun enableLogging(originalLevels: OriginalLevels) {
    originalLevels.levels.forEach { (name, originalLevel) ->
      val logger = LoggerFactory.getLogger(name) as ch.qos.logback.classic.Logger
      logger.level = originalLevel
    }
  }

  data class OriginalLevels(val levels: Map<String, Level>)
}
