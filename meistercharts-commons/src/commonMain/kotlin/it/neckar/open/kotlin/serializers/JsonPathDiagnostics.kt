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

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

/**
 * Where [path] ends inside this area and what is there instead of the addressed element - the whole
 * diagnosis of a failed path.
 *
 * Carries keys and types, never a value: a free-form area holds the customer's own data - names,
 * addresses, meter readings - and this text ends up in logs and error responses. The keys may go in
 * because they come from the customer's schema, which is configuration.
 *
 * Keys are listed sorted, not in insertion order, so the same area always produces the same text.
 *
 * ```
 * area {"abrechnung": {"stand": 1, "datum": "..."}}, path abrechnung.standKwh
 *   -> "<abrechnung.standKwh> is not there, <abrechnung> has the keys [datum, stand]"
 *
 * area {"abrechnung": 42}, path abrechnung.standKwh
 *   -> "<abrechnung> holds a number, so <standKwh> cannot be reached"
 *
 * area {"abrechnung": {"stand": 1}}, path abrechnung.stand
 *   -> "<abrechnung.stand> holds a number"
 * ```
 */
fun JsonObject.describeAlong(path: JsonPath): String {
  var current: JsonElement = this
  val walked: MutableList<String> = mutableListOf()

  path.keys.forEach { key ->
    val parent: JsonObject = current as? JsonObject
      ?: return "${walked.describePosition()} holds ${current.jsonTypeName()}, so <$key> cannot be reached"

    val child: JsonElement = parent[key]
      ?: return "${(walked + key).describePosition()} is not there, ${parent.describeKeysAt(walked)}"

    walked += key
    current = child
  }

  //Reached when the path does resolve - no caller asks then, but a diagnosis that lies about the
  //one case it was not written for is worse than one that answers it
  return "${path.keys.describePosition()} holds ${current.jsonTypeName()}"
}

/**
 * The keys of this object, named by where it sits.
 */
private fun JsonObject.describeKeysAt(walked: List<String>): String {
  return "${walked.describePosition()} has the keys ${keys.sorted()}"
}

/**
 * These keys as a place inside the area - the area itself when there are none.
 */
private fun List<String>.describePosition(): String {
  return if (isEmpty()) "the area" else "<${joinToString(".")}>"
}

/**
 * The JSON type of this element, in words - never its value, for the same reason as [describeAlong].
 */
fun JsonElement.jsonTypeName(): String = when (this) {
  is JsonObject -> "an object"
  is JsonArray -> "an array"
  JsonNull -> "null"
  is JsonPrimitive -> when {
    isString -> "a string"
    content == "true" || content == "false" -> "a boolean"
    else -> "a number"
  }
}
