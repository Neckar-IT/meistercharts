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
  defaultJsonConfiguration(true)
}

/**
 * Pretty prints the JSON element
 */
fun JsonElement.toStringPretty(): String {
  return JsonPretty.encodeToString(JsonElement.serializer(), this)
}

/**
 * Default properties for JSON Serialization
 * */
fun JsonBuilder.defaultJsonConfiguration(prettyPrintEnabled: Boolean = true) {
  prettyPrint = prettyPrintEnabled
  prettyPrintIndent = "  "
  /**
   * encode default properties of Serializable Classes
   * */
  encodeDefaults = true
}
