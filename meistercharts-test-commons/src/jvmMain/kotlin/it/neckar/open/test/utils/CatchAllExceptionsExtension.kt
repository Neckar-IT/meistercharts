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

import org.junit.jupiter.api.extension.AfterEachCallback
import org.junit.jupiter.api.extension.BeforeEachCallback
import org.junit.jupiter.api.extension.ExtensionContext
import java.io.PrintWriter
import java.io.StringWriter

/**
 * This rule catches exceptions on all threads and fails the test if such exceptions are caught
 *
 */
class CatchAllExceptionsExtension : BeforeEachCallback, AfterEachCallback {
  private var oldHandler: Thread.UncaughtExceptionHandler? = null

  override fun beforeEach(context: ExtensionContext) {
    before()
  }

  override fun afterEach(context: ExtensionContext) {
    if (context.executionException.isPresent) {
      afterFailing()
      return
    }
    afterSuccess()
  }

  private fun before() {
    oldHandler = Thread.getDefaultUncaughtExceptionHandler()
    Thread.setDefaultUncaughtExceptionHandler { t, e ->
      caught.add(e)
      oldHandler?.uncaughtException(t, e)
    }
  }

  /**
   * List of all caught throwables
   */
  private val caught = ArrayList<Throwable>()

  private fun afterSuccess() {
    Thread.setDefaultUncaughtExceptionHandler(oldHandler)

    if (caught.isEmpty()) {
      return
    }

    throw AssertionError(buildMessage())
  }

  private fun buildMessage(): String {
    val builder = StringBuilder()
    builder.append(caught.size).append(" exceptions thrown but not caught in other threads:\n")

    caught.forEach { throwable ->
      builder.append("---------------------\n")

      val out = StringWriter()
      throwable.printStackTrace(PrintWriter(out))
      builder.append(out)
    }

    builder.append("---------------------\n")

    return builder.toString()
  }

  private fun afterFailing() {
    Thread.setDefaultUncaughtExceptionHandler(oldHandler)
  }
}
