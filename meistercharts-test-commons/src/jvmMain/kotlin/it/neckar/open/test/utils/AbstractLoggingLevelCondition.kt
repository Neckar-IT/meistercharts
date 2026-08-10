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
import org.junit.jupiter.api.extension.AfterAllCallback
import org.junit.jupiter.api.extension.AfterEachCallback
import org.junit.jupiter.api.extension.BeforeAllCallback
import org.junit.jupiter.api.extension.BeforeEachCallback
import org.junit.jupiter.api.extension.ExtensionContext

/**
 * Abstract base class for extensions that force a logging level for the duration of a test
 * and restore the original levels afterwards.
 *
 * Subclasses only decide *which* level is forced - see [findLogLevel].
 */
abstract class AbstractLoggingLevelCondition : BeforeEachCallback, AfterEachCallback, BeforeAllCallback, AfterAllCallback {

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
   * Returns the log level that is forced for the given context.
   */
  protected abstract fun findLogLevel(context: ExtensionContext): Level
}
