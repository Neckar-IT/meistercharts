/**
 * Copyright (C) cedarsoft GmbH.
 *
 * Licensed under the GNU General Public License version 3 (the "License")
 * with Classpath Exception; you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.cedarsoft.org/gpl3ce
 * (GPL 3 with Classpath Exception)
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 3 only, as
 * published by the Free Software Foundation. cedarsoft GmbH designates this
 * particular file as subject to the "Classpath" exception as provided
 * by cedarsoft GmbH in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 3 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 3 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact cedarsoft GmbH, 72810 Gomaringen, Germany,
 * or visit www.cedarsoft.com if you need additional information or
 * have any questions.
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
