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
