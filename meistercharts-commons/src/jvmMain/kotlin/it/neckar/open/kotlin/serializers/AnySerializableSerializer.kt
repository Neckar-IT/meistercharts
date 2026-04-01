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
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.element
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.encoding.decodeStructure
import kotlinx.serialization.encoding.encodeStructure
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.serializer

/**
 * Serializer that can be used to serialize <Any> - *if* the object is serializable itself.
 *
 * Must be used with classes that have been annotated with @[Serializable].
 */
object AnySerializableSerializer : KSerializer<Any> {
  override val descriptor: SerialDescriptor = buildClassSerialDescriptor("AnyReflectionSerializer") {
    element<String>("className")
    element<JsonElement>("content")
  }

  override fun serialize(encoder: Encoder, value: Any) {
    encoder.encodeStructure(descriptor) {
      val qualifiedName = value::class.java.name
      encodeStringElement(descriptor, 0, qualifiedName ?: throw IllegalStateException("Cannot serialize anonymous class"))

      val serializer = try {
        serializer(value::class.java)
      } catch (e: SerializationException) {
        throw IllegalStateException("Please annotate [$qualifiedName] with @Serializable", e)
      }

      encodeSerializableElement(descriptor, 1, serializer, value)
    }
  }

  override fun deserialize(decoder: Decoder): Any {
    return decoder.decodeStructure(descriptor) {
      var className: String? = null
      var data: Any? = null

      while (true) {
        when (val index = decodeElementIndex(descriptor)) {
          0 -> className = decodeStringElement(descriptor, 0)
          1 -> {
            requireNotNull(className) { "className must be set before data" }

            val clazz = Class.forName(className)
            val serializer = serializer(clazz)
            data = decodeSerializableElement(descriptor, 1, serializer)

          }

          CompositeDecoder.DECODE_DONE -> break
          else -> error("Unexpected index: $index")
        }
      }

      data.requireNotNull()
    }
  }
}
