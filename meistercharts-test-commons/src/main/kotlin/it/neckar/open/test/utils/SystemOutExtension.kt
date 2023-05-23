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
import org.junit.jupiter.api.extension.ParameterContext
import org.junit.jupiter.api.extension.ParameterResolutionException
import org.junit.jupiter.api.extension.ParameterResolver
import java.io.ByteArrayOutputStream
import java.io.PrintStream

/**
 * Extension that modifies System.out and System.err during a test
 */
class SystemOutExtension : BeforeEachCallback, AfterEachCallback, ParameterResolver {
  private var newOut: ByteArrayOutputStream? = null
  private var oldOut: PrintStream? = null
  private var newErr: ByteArrayOutputStream? = null
  private var oldErr: PrintStream? = null

  override fun beforeEach(extensionContext: ExtensionContext) {
    newOut = ByteArrayOutputStream()
    newErr = ByteArrayOutputStream()
    oldOut = System.out
    System.setOut(PrintStream(newOut!!))
    oldErr = System.err
    System.setErr(PrintStream(newErr!!))
  }

  override fun afterEach(extensionContext: ExtensionContext) {
    System.setOut(oldOut)
    oldOut = null
    System.setErr(oldErr)
    oldErr = null
    newOut = null
    newErr = null
  }

  val outAsString: String
    get() = newOut.toString()

  val errAsString: String
    get() = newErr.toString()

  fun getOldOut(): PrintStream {
    return checkNotNull(oldOut) { "old out is null. Rule not activated" }
  }

  fun getOldErr(): PrintStream {
    return checkNotNull(oldErr) { "oldErr is null. Rule not activated" }
  }

  @Throws(ParameterResolutionException::class)
  override fun supportsParameter(parameterContext: ParameterContext, extensionContext: ExtensionContext): Boolean {
    return parameterContext.parameter.type == javaClass
  }

  @Throws(ParameterResolutionException::class)
  override fun resolveParameter(parameterContext: ParameterContext, extensionContext: ExtensionContext): Any {
    return this
  }
}
