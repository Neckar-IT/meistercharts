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
  when {
    this is ObjectNode -> {
      val keysToRemove = properties()
        .asSequence()
        .map { it.key }
        .filter { it.startsWith(prefix) }
        .toList()

      this.remove(keysToRemove)

      //Recurse into the remaining field values
      properties()
        .asSequence()
        .map { it.value }
        .forEach { it.removePropertiesStartingWith(prefix) }
    }

    this.isArray -> {
      //Recurse into array elements - object arrays (e.g. `parameters`, `tags`) can carry matching keys too
      elements().forEach { it.removePropertiesStartingWith(prefix) }
    }
  }
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
