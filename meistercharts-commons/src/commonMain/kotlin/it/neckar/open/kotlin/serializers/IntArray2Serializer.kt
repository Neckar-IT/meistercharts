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

import it.neckar.open.collections.IntArray2
import it.neckar.open.collections.fastForEach
import it.neckar.open.kotlin.lang.fromBase64
import it.neckar.open.kotlin.lang.toBase64
import it.neckar.open.kotlin.bytearray.ByteArrayBuilder
import it.neckar.open.kotlin.bytearray.ByteArrayReader
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

/**
 * Serializer for IntArray2
 */
object IntArray2Serializer : KSerializer<IntArray2> {
  override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("IntArray2", PrimitiveKind.STRING)

  override fun serialize(encoder: Encoder, value: IntArray2) {
    encoder.encodeString(toByteArray(value).toBase64())
  }

  override fun deserialize(decoder: Decoder): IntArray2 {
    return parse(decoder.decodeString().fromBase64())
  }

  fun toByteArray(values: IntArray2): ByteArray {
    val builder = ByteArrayBuilder()

    val width = values.width
    val height = values.height

    builder.s16BE(width)
    builder.s16BE(height)

    if (width == 0 || height == 0) {
      //Return immediately - the array is empty
      return builder.toByteArray()
    }

    values.data.fastForEach {
      builder.s32BE(it)
    }

    return builder.toByteArray()
  }

  /**
   * Parses a byte array into a values array
   */
  fun parse(values: ByteArray): IntArray2 {
    val reader = ByteArrayReader(values, 0)

    val width = reader.s16BE()
    val height = reader.s16BE()

    if (width == 0 || height == 0) {
      //Array is empty
      return IntArray2(width, height, 0)
    }

    return IntArray2(width, height) {
      reader.s32BE()
    }
  }

}
