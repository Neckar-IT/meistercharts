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

import it.neckar.open.kotlin.lang.requireNotNull
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonPrimitive

/**
 * Converts "any" value to JSON.
 *
 * Inspired by https://github.com/Kotlin/kotlinx.serialization/issues/296
 */
object AnySerializer : KSerializer<Any> {
  private val delegateSerializer = JsonPrimitive.serializer()

  override val descriptor: SerialDescriptor = delegateSerializer.descriptor

  override fun serialize(encoder: Encoder, value: Any) {
    encoder.encodeSerializableValue(delegateSerializer, value.toJsonPrimitive())
  }

  override fun deserialize(decoder: Decoder): Any {
    val jsonPrimitive = decoder.decodeSerializableValue(delegateSerializer)
    return jsonPrimitive.toAnyValue().requireNotNull { "Expected non-null value but got null" }
  }
}

/**
 * Converts "any" value to JSON.
 *
 * Inspired by https://github.com/Kotlin/kotlinx.serialization/issues/296
 */
object AnyNullableSerializer : KSerializer<Any?> {
  private val delegateSerializer = JsonPrimitive.serializer()

  override val descriptor: SerialDescriptor = delegateSerializer.descriptor

  override fun serialize(encoder: Encoder, value: Any?) {
    encoder.encodeSerializableValue(delegateSerializer, value.toJsonPrimitive())
  }

  override fun deserialize(decoder: Decoder): Any? {
    val jsonPrimitive = decoder.decodeSerializableValue(delegateSerializer)
    return jsonPrimitive.toAnyValue()
  }
}

/**
 * Converts well-known values to JsonPrimitives
 */
private fun Any?.toJsonPrimitive(): JsonPrimitive {
  return when (this) {
    null -> JsonNull
    is JsonPrimitive -> this
    is Boolean -> JsonPrimitive(this)
    is Number -> JsonPrimitive(this)
    is String -> JsonPrimitive(this)
    // add custom convert
    else -> throw IllegalArgumentException("Unsupported type: ${this::class}")
  }
}

private fun JsonPrimitive.toAnyValue(): Any? {
  val content = this.content
  if (this.isString) {
    // add custom string convert
    return content
  }
  if (content.equals("null", ignoreCase = true)) {
    return null
  }
  if (content.equals("true", ignoreCase = true)) {
    return true
  }
  if (content.equals("false", ignoreCase = true)) {
    return false
  }
  val intValue = content.toIntOrNull()
  if (intValue != null) {
    return intValue
  }
  val longValue = content.toLongOrNull()
  if (longValue != null) {
    return longValue
  }
  val doubleValue = content.toDoubleOrNull()
  if (doubleValue != null) {
    return doubleValue
  }
  throw IllegalArgumentException("Unsupported content： $content")
}
