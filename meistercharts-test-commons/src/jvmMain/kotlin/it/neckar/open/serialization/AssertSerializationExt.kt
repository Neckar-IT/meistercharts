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
package it.neckar.open.serialization

import assertk.*
import assertk.assertions.isInstanceOf
import assertk.assertions.key
import com.fasterxml.jackson.databind.JsonNode
import it.neckar.open.http.Url
import it.neckar.open.kotlin.serializers.JsonInclusionStrategy
import it.neckar.open.resources.getResourceSafe
import it.neckar.open.test.utils.JsonUtils
import it.neckar.open.test.utils.isOpenApiEqualTo as isOpenApiEqualToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.intOrNull

/**
 *
 */
fun Assert<JsonObject>.isJsonEqualTo(
  expectedJsonUrl: Url,
  actualTreeModifier: JsonNode.() -> Unit = {},
) {
  isJsonEqualTo(javaClass.getResourceSafe(expectedJsonUrl).readText(), actualTreeModifier)
}

fun Assert<JsonObject>.isJsonEqualTo(
  expectedJsonString: String,
  actualTreeModifier: JsonNode.() -> Unit = {},
) {
  given { current ->
    JsonUtils.assertJsonEquals(expectedJsonString, current.toStringPretty(), actualTreeModifier)
  }
}

/**
 * Compares the actual [JsonElement] to the expected JSON string.
 * The actual element is pretty-printed before comparison; key order is ignored, array order is not.
 *
 * Saves the boilerplate of `assertThat(element.toStringPretty()).isJsonEqualTo(...)`.
 */
@JvmName("isJsonEqualToElement")
fun Assert<JsonElement>.isJsonEqualTo(
  expectedJsonString: String,
  actualTreeModifier: JsonNode.() -> Unit = {},
) {
  given { current ->
    JsonUtils.assertJsonEquals(expectedJsonString, current.toStringPretty(), actualTreeModifier)
  }
}

@JvmName("isJsonEqualToElementUrl")
fun Assert<JsonElement>.isJsonEqualTo(
  expectedJsonUrl: Url,
  actualTreeModifier: JsonNode.() -> Unit = {},
) {
  isJsonEqualTo(javaClass.getResourceSafe(expectedJsonUrl).readText(), actualTreeModifier)
}

/**
 * Same as [isJsonEqualTo] but additionally strips `x-source-location` nodes from the actual tree
 * before comparison — makes generated OpenAPI specs comparable across builds.
 */
@JvmName("isOpenApiEqualToElement")
fun Assert<JsonElement>.isOpenApiEqualTo(
  expectedJsonString: String,
  actualTreeModifier: JsonNode.() -> Unit = {},
) {
  given { current ->
    assertThat(current.toStringPretty()).isOpenApiEqualToString(expectedJsonString, actualTreeModifier)
  }
}

@JvmName("isOpenApiEqualToElementUrl")
fun Assert<JsonElement>.isOpenApiEqualTo(
  expectedJsonUrl: Url,
  actualTreeModifier: JsonNode.() -> Unit = {},
) {
  isOpenApiEqualTo(javaClass.getResourceSafe(expectedJsonUrl).readText(), actualTreeModifier)
}

/**
 * Navigates a sequence of object keys, asserting at each step that the key exists and its value is
 * a [JsonObject]. Failure messages carry the full traversed path, so a missing or wrongly-typed
 * intermediate key is immediately diagnosable.
 *
 * Example: `assertThat(doc).path("components", "schemas").key("MyType")…`
 */
fun Assert<JsonObject>.path(vararg keys: String): Assert<JsonObject> {
  var current: Assert<JsonObject> = this
  keys.forEach { key ->
    current = current.key(key).isInstanceOf(JsonObject::class)
  }
  return current
}

/**
 * Value-extracting counterpart to [path]. Walks a sequence of keys and returns the leaf
 * [JsonObject]. Throws [IllegalStateException] with the path traversed so far and the available
 * keys when a step fails — use this in `val x = doc.atPath(...)` extractions where the result
 * feeds further computation.
 *
 * Example: `val schemas = generated.jsonObject.atPath("components", "schemas")`
 */
fun JsonObject.atPath(vararg keys: String): JsonObject {
  var current: JsonObject = this
  val traversed = mutableListOf<String>()
  keys.forEach { key ->
    val element = current[key]
      ?: error("Missing key \"$key\" at path [${traversed.joinToString("][") { "\"$it\"" }}]. Available keys: ${current.keys}")
    current = element as? JsonObject
      ?: error("Expected JsonObject at path [${(traversed + key).joinToString("][") { "\"$it\"" }}] but was ${element::class.simpleName}: $element")
    traversed.add(key)
  }
  return current
}

/**
 * Extracts the textual content of a [JsonPrimitive]. Numeric and boolean primitives are returned
 * in their textual form (e.g. `"42"`, `"true"`).
 */
fun Assert<JsonElement>.asString(): Assert<String> = transform("asString") { element ->
  when (element) {
    is JsonNull -> error("Expected JsonPrimitive but was JsonNull")
    is JsonPrimitive -> element.content
    else -> error("Expected JsonPrimitive but was ${element::class.simpleName}: $element")
  }
}

/**
 * Extracts the integer value of a [JsonPrimitive]. Fails when the primitive is not convertible
 * to [Int] (non-numeric string, floating-point, JsonNull, …).
 */
fun Assert<JsonElement>.asInt(): Assert<Int> = transform("asInt") { element ->
  val primitive = element as? JsonPrimitive
    ?: error("Expected JsonPrimitive but was ${element::class.simpleName}: $element")
  primitive.intOrNull
    ?: error("Expected JsonPrimitive convertible to Int but was: $element")
}

/**
 * Extracts the boolean value of a [JsonPrimitive]. Fails when the primitive is not exactly
 * `true` or `false` (string content, number, JsonNull, …).
 */
fun Assert<JsonElement>.asBoolean(): Assert<Boolean> = transform("asBoolean") { element ->
  val primitive = element as? JsonPrimitive
    ?: error("Expected JsonPrimitive but was ${element::class.simpleName}: $element")
  primitive.booleanOrNull
    ?: error("Expected JsonPrimitive convertible to Boolean but was: $element")
}

/**
 * Reads the `required` array of a JSON-Schema object as a list of property names.
 * Returns an empty list when `required` is absent — JSON-Schema treats it as optional.
 */
fun Assert<JsonObject>.requiredKeys(): Assert<List<String>> = transform("requiredKeys") { obj ->
  val required = obj["required"] ?: return@transform emptyList()
  val array = required as? JsonArray
    ?: error("Expected 'required' to be a JsonArray but was ${required::class.simpleName}: $required")
  array.map { element ->
    val primitive = element as? JsonPrimitive
      ?: error("Expected string in 'required' but was ${element::class.simpleName}: $element")
    primitive.content
  }
}

/**
 * Extracts `${'$'}ref` values from an `allOf`/`oneOf`/`anyOf` array. Inline schemas without a
 * `${'$'}ref` are skipped — the result contains only the actual references in declaration order.
 */
fun Assert<JsonArray>.refs(): Assert<List<String>> = transform("refs") { array ->
  array.mapNotNull { element ->
    val obj = element as? JsonObject ?: return@mapNotNull null
    val refElement = obj[$$"$ref"] ?: return@mapNotNull null
    (refElement as? JsonPrimitive)?.content
  }
}


/**
 * JSON instance with pretty print enabled
 */
private val JsonPretty: Json = Json {
  defaultJsonConfiguration(
    prettyPrintEnabled = true,
    inclusionStrategy = JsonInclusionStrategy.Default,
  )
}

/**
 * Pretty prints the JSON element
 */
private fun JsonElement.toStringPretty(): String {
  return JsonPretty.encodeToString(JsonElement.serializer(), this)
}

