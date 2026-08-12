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
package it.neckar.open.kotlin.serializers

import it.neckar.open.unit.other.JsonText
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.jsonObject

/**
 * An object with no entries.
 */
val EmptyJsonObject: JsonObject = JsonObject(emptyMap())

/**
 * Parses a JSON object from its string representation.
 */
fun parseJsonObject(json: @JsonText String, decoder: Json = Json): JsonObject {
  return decoder.parseToJsonElement(json).jsonObject
}

/**
 * The element at the given [path], or null if a key is missing or its parent is not an object.
 */
fun JsonObject.findAt(path: JsonPath): JsonElement? {
  var current: JsonElement = this

  path.keys.forEach { key ->
    val childElement = (current as? JsonObject)?.get(key) ?: return null
    current = childElement
  }

  return current
}

/**
 * The element at the given [path]. Throws if a key is missing or its parent is not an object.
 *
 * The failure says where the path stops and what is there, in keys and types - never a value. An
 * unconstrained object holds whatever its owner puts there, and this message ends up in logs. See
 * [describeAlong].
 */
fun JsonObject.getAt(path: JsonPath): JsonElement {
  return findAt(path) ?: throw IllegalArgumentException("No element at path <$path>: ${describeAlong(path)}")
}

/**
 * A copy with the entries of [other] added. Keys of [other] win; merging is shallow - a nested object
 * is replaced as a whole, not merged key by key.
 */
fun JsonObject.merge(other: JsonObject): JsonObject {
  return JsonObject(this + other)
}

/**
 * Decodes the element at the given [path] into [T] - the way back out of an unconstrained object into
 * a typed class, for the parts whose shape *is* known.
 */
inline fun <reified T> JsonObject.decodeAt(path: JsonPath, decoder: Json = Json): T {
  return decoder.decodeFromJsonElement<T>(getAt(path))
}

/**
 * Decodes the element at the given [path] into [T], or returns null if there is no such element.
 */
inline fun <reified T> JsonObject.findAndDecodeAt(path: JsonPath, decoder: Json = Json): T? {
  return findAt(path)?.let { decoder.decodeFromJsonElement<T>(it) }
}
