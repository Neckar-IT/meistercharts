package it.neckar.open.kotlin.serializers

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonBuilder
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObjectBuilder
import kotlinx.serialization.json.put

/**
 * Puts the value into the builder if it is not null.
 * Skips the value otherwise.
 */
fun JsonObjectBuilder.putNotNull(key: String, value: String?) {
  if (value != null) {
    put(key, value)
  }
}

fun JsonObjectBuilder.putNotNull(key: String, element: JsonElement?) {
  if (element != null) {
    put(key, element)
  }
}

fun JsonObjectBuilder.putNotNull(key: String, value: Boolean?) {
  if (value != null) {
    put(key, value)
  }
}

fun JsonObjectBuilder.putNotEmpty(key: String, buildJsonArray: JsonArray) {
  if (buildJsonArray.isNotEmpty()) {
    put(key, buildJsonArray)
  }
}

/**
 * JSON instance with pretty print enabled
 */
val JsonPretty: Json = Json {
  defaultJsonConfiguration(inclusionStrategy = JsonInclusionStrategy.SkipDefaultsIncludeNulls)
}

/**
 * Pretty prints the JSON element
 */
fun JsonElement.toStringPretty(): String {
  return JsonPretty.encodeToString(JsonElement.serializer(), this)
}

/**
 * Default properties for JSON Serialization.
 */
fun JsonBuilder.defaultJsonConfiguration(
  /**
   * Pretty print enabled
   */
  prettyPrintEnabled: Boolean = true,
  /**
   * Defines which values are encoded
   */
  inclusionStrategy: JsonInclusionStrategy = JsonInclusionStrategy.EncodeDefaultsIncludeNulls,
) {
  prettyPrint = prettyPrintEnabled
  prettyPrintIndent = "  "

  this.encodeDefaults = inclusionStrategy.encodeDefaults
  this.explicitNulls = inclusionStrategy.explicitNulls
}

