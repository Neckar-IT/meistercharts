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

import assertk.*
import assertk.assertions.support.*
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import java.io.StringWriter
import java.net.URL
import java.nio.charset.Charset
import javax.annotation.Nonnull

/**
 */
object JsonUtils {
  @JvmStatic
  @JvmOverloads
  fun assertJsonEquals(expected: URL, actual: String, charset: Charset = Charsets.UTF_8) {
    assertJsonEquals(AssertUtils.toString(expected, charset), actual)
  }

  @JvmStatic
  @JvmOverloads
  fun assertJsonEquals(
    expected: String?, actual: String?,
    /**
     * is called for the actual tree. Can be used to modify the tree inline - before comparing
     */
    actualTreeModifier: JsonNode.() -> Unit = {},
  ) {
    if (actual == null || actual.trim { it <= ' ' }.isEmpty()) {
      assertThat(formatJson(expected).trim()).fail(formatJson(expected).trim(), formatJson(actual).trim())
    }
    if (expected == null || expected.trim { it <= ' ' }.isEmpty()) {
      assertThat(formatJson(expected).trim()).fail(formatJson(expected).trim(), formatJson(actual).trim())
    }
    try {
      val mapper = ObjectMapper()
      val expectedTree = mapper.readTree(expected)
      val actualTree = mapper.readTree(actual).also(actualTreeModifier)
      if (expectedTree != actualTree) {
        assertThat(formatJson(expected).trim()).fail(formatJson(expected).trim(), formatJson(actual).trim())
      }
    } catch (e: JsonProcessingException) {
      throw AssertionError("JSON parsing error (" + e.message + ")\n Actual: \n${formatJson(actual).trim { it <= ' ' }}", e)
    }
  }

  @JvmStatic
  @Nonnull
  fun formatJson(json: String?): String {
    return try {
      val mapper = ObjectMapper()
      val tree = mapper.readTree(json)
      val out = StringWriter()

      mapper.factory.createGenerator(out).apply {
        useDefaultPrettyPrinter()
      }.writeTree(tree)

      out.toString()
    } catch (ignore: Exception) {
      //Do not format if it is not possible...
      json.toString()
    }
  }
}
