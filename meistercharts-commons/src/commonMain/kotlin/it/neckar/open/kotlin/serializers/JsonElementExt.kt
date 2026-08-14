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

fun JsonObjectBuilder.putNotEmpty(key: String, value: JsonArray) {
  if (value.isNotEmpty()) {
    put(key, value)
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
 *
 * A parser for a foreign API configures its own tolerant [Json] instead.
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
  // kotlinx rejects a non-default prettyPrintIndent unless prettyPrint is on, so only set it then.
  if (prettyPrintEnabled) {
    prettyPrintIndent = "  "
  }

  this.encodeDefaults = inclusionStrategy.encodeDefaults
  this.explicitNulls = inclusionStrategy.explicitNulls
  this.ignoreUnknownKeys = false
}

