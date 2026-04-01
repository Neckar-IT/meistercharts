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
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlin.reflect.KClass

/**
 * Serializes a [KClass] by its fully qualified name
 */
object KClassSerializer : KSerializer<KClass<*>> {
  override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("KType", PrimitiveKind.STRING)

  override fun serialize(encoder: Encoder, value: KClass<*>) {
    encoder.encodeString(value.java.name)
  }

  override fun deserialize(decoder: Decoder): KClass<*> {
    decoder.decodeString().let { className ->
      return getKotlinClassByName(className)
    }
  }

  fun getKotlinClassByName(name: String): KClass<*> {
    return when (name) {
      "kotlin.Int" -> Int::class
      "kotlin.Long" -> Long::class
      "kotlin.Double" -> Double::class
      "kotlin.Float" -> Float::class
      "kotlin.Boolean" -> Boolean::class
      "kotlin.Char" -> Char::class
      "kotlin.Byte" -> Byte::class
      "kotlin.Short" -> Short::class
      "kotlin.String" -> String::class
      else -> Class.forName(name).kotlin
    }
  }
}
