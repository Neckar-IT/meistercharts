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
import com.fasterxml.jackson.databind.node.ObjectNode
import it.neckar.open.collections.fastForEach
import it.neckar.open.http.Url
import it.neckar.open.kotlin.lang.padEndMaxLength
import it.neckar.open.resources.getResourceSafe
import it.neckar.open.test.utils.matchers.asObjectNode
import org.skyscreamer.jsonassert.JSONCompare
import org.skyscreamer.jsonassert.JSONCompareMode
import java.io.StringWriter
import java.net.URL
import java.nio.charset.Charset
import javax.annotation.Nonnull

/**
 * JSON Utils for comparing JSON strings or files.
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
     * Is called for the actual tree. Can be used to modify the tree inline - before comparing
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
        val compareResult = JSONCompare.compareJSON(expected, actualTree.toPrettyString(), JSONCompareMode.STRICT)
        System.err.println("           Pointer               | Expected value                           -- Actual value")
        System.err.println("---------------------------------------------------------------------------------------------------")

        compareResult.fieldFailures.fastForEach {
          System.err.println("Failure: ${it.field.padEnd(23)} | ${it.expected?.toString().padEndMaxLength(40)} -- ${it.actual?.toString().padEndMaxLength(40)}")
        }

        compareResult.fieldMissing.fastForEach {
          System.err.println("Missing: ${it.field.padEnd(23)} | ${it.expected?.toString()?.padEndMaxLength(40)} -- ${it.actual?.toString().padEndMaxLength(40)}")
        }

        compareResult.fieldUnexpected.fastForEach {
          System.err.println("Unexpected: ${it.field.padEnd(20)} | ${it.expected?.toString()?.padEndMaxLength(40)} -- ${it.actual?.toString().padEndMaxLength(40)}")
        }

        assertThat(formatJson(expected).trim()).fail(formatJson(expected).trim(), formatJson(actualTree.toPrettyString()).trim())
      }
    } catch (e: JsonProcessingException) {
      throw AssertionError("JSON parsing error (" + e.message + ")\n Actual: \n${formatJson(actual).trim { it <= ' ' }}", e)
    }
  }

  /**
   * Formats the given JSON string.
   */
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
    } catch (_: Exception) {
      //Do not format if it is not possible...
      json.toString()
    }
  }
}

/**
 * Compares the given string with the expected JSON file.
 */
fun Assert<String>.isJsonEqualTo(
  expectedJsonUrl: Url,
  actualTreeModifier: JsonNode.() -> Unit = {},
) {
  isJsonEqualTo(javaClass.getResourceSafe(expectedJsonUrl).readText(), actualTreeModifier)
}

/**
 * Compares the given string with the expected JSON string.
 * Parses the JSON strings and compares them.
 *
 * Whitespaces are ignored.
 */
fun Assert<String>.isJsonEqualTo(
  //language=json
  expectedJsonString: String,
  actualTreeModifier: JsonNode.() -> Unit = {},
) {
  given { current ->
    JsonUtils.assertJsonEquals(expected = expectedJsonString, actual = current, actualTreeModifier = actualTreeModifier)
  }
}

/**
 * Removes all properties that start with the given prefix.
 * This is useful to remove properties that are not relevant for the comparison, such as "x-source-location".
 *
 * Modifies the current node in place.
 *
 * Recursively removes all properties that start with the given prefix from the current node and all child nodes.
 */
fun JsonNode.removePropertiesStartingWith(prefix: String) {
  if (this is ObjectNode) {
    val keysToRemove = asObjectNode()
      .properties()
      .asSequence()
      .map { it.key }
      .filter { it.startsWith(prefix) }
      .toList()

    this.remove(keysToRemove)
  }

  //Call recursively for all child nodes
  properties()
    .asSequence()
    .map { it.value }
    //.filterIsInstance<JsonNode>()
    .forEach { it.removePropertiesStartingWith(prefix) }
}

/**
 * Removes the "x-source-location" to simplify comparing open-api-files
 */
fun Assert<String>.isOpenApiEqualTo(
  expectedJsonUrl: Url,
  actualTreeModifier: JsonNode.() -> Unit = {},
) {
  isOpenApiEqualTo(javaClass.getResourceSafe(expectedJsonUrl).readText(), actualTreeModifier)
}

/**
 * Removes the "x-source-location" to simplify comparing open-api-files
 */
fun Assert<String>.isOpenApiEqualTo(
  expectedJsonString: String,
  actualTreeModifier: JsonNode.() -> Unit = {},
) {
  given { current ->
    JsonUtils.assertJsonEquals(expectedJsonString, current) {
      actualTreeModifier()

      //Remove all "location" nodes"
      removeSourceLocation()
    }
  }
}

/**
 * Removes the "x-source-location" from the current node and all child nodes.
 * This is useful to simplify comparing open-api files.
 *
 * Modifies the current node in place.
 */
fun JsonNode.removeSourceLocation() {
  if (this.isObject) {
    val iterator = this.fields()
    while (iterator.hasNext()) {
      val next = iterator.next()
      //This is the same key as in [it.neckar.rest.openapi.SourceRef::SourceInfo]
      if (next.key == "x-source-location") {
        //val oldValue = next.value.asText()
        //val parts = oldValue.split(":")
        iterator.remove()
      } else {
        next.value.removeSourceLocation()
      }
    }
  } else if (this.isArray) {
    this.elements().forEach { it.removeSourceLocation() }
  }
}
