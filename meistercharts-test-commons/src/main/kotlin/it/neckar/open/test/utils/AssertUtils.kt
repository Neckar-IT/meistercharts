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

import org.junit.jupiter.api.Assertions
import java.io.File
import java.io.IOException
import java.net.URL
import java.nio.charset.Charset

/**
 *
 * AssertUtils class.
 *
 */
object AssertUtils {

  /**
   *
   * assertEquals
   *
   * @param expectedResourceUri a URL object.
   * @param actual              a Object object.
   * @throws IOException if any.
   */
  @JvmStatic
  @JvmOverloads
  fun assertEquals(expectedResourceUri: URL, actual: Any, charset: Charset = Charsets.UTF_8) {
    Assertions.assertEquals(expectedResourceUri.readText(charset), actual)
  }

  @JvmStatic
  @Deprecated("use kotlin method", ReplaceWith("expectedResourceUri.readText(charset)"))
  fun toString(expectedResourceUri: URL, charset: Charset = Charsets.UTF_8): String {
    return expectedResourceUri.readText(charset)
  }

  /**
   * The directory where the files have been stored
   */
  @JvmStatic
  val FailedFilesDir: File = File(TestUtils.tmpDir, "junit-failed-files-" + System.currentTimeMillis())

  @JvmStatic
  fun guessPathFromStackTrace(): String {
    val elements = Thread.currentThread().stackTrace
    if (elements.size < 4) {
      return "unknown"
    }
    val element = elements[3]
    return element.className + File.separator + element.methodName
  }
}
