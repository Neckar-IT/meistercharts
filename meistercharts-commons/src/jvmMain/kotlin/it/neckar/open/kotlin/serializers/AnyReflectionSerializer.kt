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
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.element
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonElement
import kotlin.reflect.KProperty

/**
 * A helper serializer that serializes all objects using reflection.
 * Does *not* support **deserialization**.
 */
object AnyReflectionSerializer : KSerializer<Any> {
  override val descriptor: SerialDescriptor = buildClassSerialDescriptor("AnyReflectionSerializer") {
    element<String>("className")
    element<JsonElement>("properties")
  }

  override fun serialize(encoder: Encoder, value: Any) {
    encoder.beginStructure(descriptor).apply {
      val valueKlass = value::class
      encodeStringElement(descriptor, 0, valueKlass.qualifiedName ?: throw IllegalStateException("Cannot serialize anonymous class"))

      val properties = valueKlass.members.filterIsInstance<KProperty<*>>()

      val propName2value = properties.associate { it.name to it.call(value) }
      encodeSerializableElement(descriptor, 1, JsonElement.serializer(), propName2value.toJsonElement())

      endStructure(descriptor)
    }
  }

  override fun deserialize(decoder: Decoder): Any {
    throw UnsupportedOperationException("Not supported")
  }
}
