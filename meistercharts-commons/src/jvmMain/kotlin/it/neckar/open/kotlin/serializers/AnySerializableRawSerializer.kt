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

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonEncoder
import kotlinx.serialization.serializer
import kotlin.reflect.KType

/**
 * Serializer that can be used to serialize <Any> - *if* the object is serializable itself.
 * Must be used with classes that have been annotated with @[Serializable].
 *
 * Serializes the content as a JsonElement.
 * Does not serialize the class name.
 *
 * Does *NOT* support deserialization!!!
 */
object AnySerializableRawSerializer : KSerializer<Any> {
  override val descriptor: SerialDescriptor = buildClassSerialDescriptor("AnySerializableRawSerializer")

  override fun serialize(encoder: Encoder, value: Any) {
    // Ensure we're working with JSON
    require(encoder is JsonEncoder) {
      "This serializer only supports JSON format. Provided encoder: ${encoder::class}"
    }

    val serializer = try {
      encoder.json.serializersModule.serializer(value::class.java)
    } catch (e: SerializationException) {
      throw IllegalStateException("Please annotate [${value::class.java.name}] with @Serializable", e)
    }

    // Delegate to encoder.encodeJsonElement to avoid tag stack issues
    val jsonElement = try {
      encoder.json.encodeToJsonElement<Any>(serializer as KSerializer<Any>, value)
    } catch (e: SerializationException) {
      throw SerializationException("Failed to serialize [${value::class.java.name}]", e)
    }

    encoder.encodeJsonElement(jsonElement)
  }

  override fun deserialize(decoder: Decoder): Any {
    throw UnsupportedOperationException("Deserialization is not supported.")
  }

  /**
   * Encodes the provided object to a JsonElement
   *
   * @param includeOptionals When true, includes properties with default values (including computed properties)
   * and explicitly serializes null values. This ensures computed properties are included in the output,
   * which is necessary for OpenAPI examples where computed properties are marked as required.
   *
   * Note: The TypeScript example generator (extract-examples.ts) filters out null values to handle
   * the type mismatch where Orval generates `property?: string` instead of `property: string | null`
   * for optional nullable properties.
   */
  fun encodeToJsonElement(elementToEncode: Any, includeOptionals: Boolean = false): JsonElement {
    return jsonFor(includeOptionals).encodeToJsonElement<Any>(AnySerializableRawSerializer, elementToEncode)
  }

  /**
   * Encodes through the serializer of [type] instead of the one of the value's class.
   *
   * The class of a value carries no type arguments, so a value of a generic type has no serializer
   * that can be looked up from it — the caller states the full type.
   */
  fun encodeToJsonElement(elementToEncode: Any, type: KType, includeOptionals: Boolean = false): JsonElement {
    @Suppress("UNCHECKED_CAST")
    val typeSerializer = serializer(type) as KSerializer<Any>
    return jsonFor(includeOptionals).encodeToJsonElement(typeSerializer, elementToEncode)
  }

  private fun jsonFor(includeOptionals: Boolean): Json = if (includeOptionals) {
    Json {
      encodeDefaults = true
      explicitNulls = true
    }
  } else {
    Json {
      encodeDefaults = false
    }
  }
}
