package it.neckar.open.kotlin.serializers

import kotlinx.serialization.json.Json
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

/**
 * Pretty prints the json element
 */
fun JsonElement.toStringPretty(): String {
  return Json {
    prettyPrint = true
  }.encodeToString(JsonElement.serializer(), this)
}
