package it.neckar.open.kotlin.serializers

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

//class AnyMapSerializer {
//}

/**
 * Wraps this object into a [JsonElement]
 */
fun Any?.toJsonElement(): JsonElement = when (this) {
  null -> JsonNull
  is JsonElement -> this
  is Number -> JsonPrimitive(this)
  is Boolean -> JsonPrimitive(this)
  is String -> JsonPrimitive(this)
  is Array<*> -> JsonArray(map { it.toJsonElement() })
  is List<*> -> JsonArray(map { it.toJsonElement() })
  is Map<*, *> -> JsonObject(map { it.key.toString() to it.value.toJsonElement() }.toMap())
  else -> JsonPrimitive(this.toString())
}
