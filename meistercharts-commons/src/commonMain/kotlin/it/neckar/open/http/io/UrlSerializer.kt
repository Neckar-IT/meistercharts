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
package it.neckar.open.http.io

import it.neckar.open.http.Url
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

/**
 * Serializer for URL
 */
object UrlSerializer : KSerializer<Url> {
  override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Url", PrimitiveKind.STRING)

  override fun serialize(encoder: Encoder, value: Url) {
    encoder.encodeString(value.toString())
  }

  override fun deserialize(decoder: Decoder): Url {
    return Url.parse(decoder.decodeString())
  }

  object DataScheme : KSerializer<Url.DataScheme> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Url.DataScheme", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: Url.DataScheme) {
      encoder.encodeString(value.toString())
    }

    override fun deserialize(decoder: Decoder): Url.DataScheme {
      return Url.DataScheme(decoder.decodeString())
    }
  }

  object Absolute : KSerializer<Url.Absolute> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Url.Absolute", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: Url.Absolute) {
      encoder.encodeString(value.toString())
    }

    override fun deserialize(decoder: Decoder): Url.Absolute {
      return Url.Absolute(decoder.decodeString())
    }
  }

  object RootRelative : KSerializer<Url.RootRelative> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Url.RootRelative", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: Url.RootRelative) {
      encoder.encodeString(value.toString())
    }

    override fun deserialize(decoder: Decoder): Url.RootRelative {
      return Url.RootRelative(decoder.decodeString())
    }
  }

  object Relative : KSerializer<Url.Relative> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Url.Relative", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: Url.Relative) {
      encoder.encodeString(value.toString())
    }

    override fun deserialize(decoder: Decoder): Url.Relative {
      return Url.Relative(decoder.decodeString())
    }
  }

}

