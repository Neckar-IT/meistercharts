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
