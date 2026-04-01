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
package it.neckar.time.io

import it.neckar.open.time.formatUtcForDebug
import it.neckar.open.time.utcDateTimeFormat
import it.neckar.time.Millis
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import org.bson.BsonDateTime
import org.bson.codecs.kotlinx.BsonDecoder
import org.bson.codecs.kotlinx.BsonEncoder
import java.time.Instant

/**
 * Formats a double value as iso format.
 *
 * ATTENTION: Does *not* support nanoseconds!
 *
 * Use like this:
 * `@Serializable(with = DoubleAsIsoDateTimeSerializer::class)`
 */
actual object MillisAsIsoDateTimeSerializer : KSerializer<Millis> {
  actual override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("MillisAsIsoDateTime", PrimitiveKind.STRING)

  actual override fun serialize(encoder: Encoder, value: Millis) {
    when (encoder) {
      is BsonEncoder -> {
        encoder.encodeBsonValue(BsonDateTime(value.millis.toLong()))
      }

      else -> {
        encoder.encodeString(value.millis.formatUtcForDebug())
      }
    }
  }

  actual override fun deserialize(decoder: Decoder): Millis {
    return when (decoder) {
      is BsonDecoder -> {
        Millis(decoder.decodeBsonValue().asDateTime().value.toDouble())
      }

      else -> {
        parseUtc(decoder.decodeString())
      }
    }
  }

  private fun parseUtc(decodeString: String): Millis {
    val temporal = utcDateTimeFormat.parse(decodeString)

    val instant = Instant.from(temporal)
    return Millis(instant.toEpochMilli().toDouble())
  }
}
