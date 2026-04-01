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
import it.neckar.commons.logback.toLogback
import org.junit.jupiter.api.extension.AfterAllCallback
import org.junit.jupiter.api.extension.AfterEachCallback
import org.junit.jupiter.api.extension.BeforeAllCallback
import org.junit.jupiter.api.extension.BeforeEachCallback
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.platform.commons.support.AnnotationSupport

/**
 * Disables logging for tests.
 *
 * Reads the log level from the [DisableLogging] annotation.
 */
class DisableLoggingCondition : BeforeEachCallback, AfterEachCallback, BeforeAllCallback, AfterAllCallback {

  var originalLevelsAll: LoggingTestSupport.OriginalLevels? = null

  override fun beforeAll(context: ExtensionContext) {
    val level = findLogLevel(context)
    originalLevelsAll = LoggingTestSupport.forceLoggingLevel(level = level)
  }

  override fun afterAll(context: ExtensionContext) {
    originalLevelsAll?.let {
      LoggingTestSupport.enableLogging(it)
    }
  }

  var originalLevelsEach: LoggingTestSupport.OriginalLevels? = null

  override fun beforeEach(context: ExtensionContext) {
    val level = findLogLevel(context)
    originalLevelsEach = LoggingTestSupport.forceLoggingLevel(level = level)
  }

  override fun afterEach(context: ExtensionContext) {
    originalLevelsEach?.let {
      LoggingTestSupport.enableLogging(it)
    }
  }

  /**
   * Finds the log level from the [DisableLogging] annotation.
   * Checks the test method first, then the test class.
   * If [DisableLogging.mute] is true, returns [Level.OFF].
   */
  private fun findLogLevel(context: ExtensionContext): Level {
    val annotation = context.testMethod
      .flatMap { AnnotationSupport.findAnnotation(it, DisableLogging::class.java) }
      .or { context.testClass.flatMap { AnnotationSupport.findAnnotation(it, DisableLogging::class.java) } }

    if (annotation.isPresent) {
      val disableLogging = annotation.get()
      if (disableLogging.mute) {
        return Level.OFF
      }
      return disableLogging.level.toLogback()
    }

    return Level.ERROR
  }
}
