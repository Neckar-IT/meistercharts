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
