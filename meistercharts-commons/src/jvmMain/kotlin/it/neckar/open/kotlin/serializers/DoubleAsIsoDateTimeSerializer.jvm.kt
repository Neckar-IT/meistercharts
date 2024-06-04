package it.neckar.open.kotlin.serializers

import it.neckar.open.formatting.formatUtc
import it.neckar.open.formatting.parseUtc
import it.neckar.open.unit.si.ms
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import org.bson.BsonValue
import org.bson.codecs.kotlinx.BsonDecoder
import org.bson.codecs.kotlinx.BsonEncoder

/**
 * Formats a double value as iso format.
 *
 * ATTENTION: Does *not* support nanoseconds!
 *
 * Use like this:
 * `@Serializable(with = DoubleAsIsoDateTimeSerializer::class)`
 */
actual object DoubleAsIsoDateTimeSerializer : KSerializer<@ms Double> {
  actual override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("DoubleAsIsoDateTime", PrimitiveKind.STRING)

  actual override fun serialize(encoder: Encoder, value: @ms Double) {
    when (encoder) {
      is BsonEncoder -> {
        encoder.writer().writeDateTime(value.toLong())
      }

      else -> {
        encoder.encodeString(value.formatUtc())
      }
    }
  }

  actual override fun deserialize(decoder: Decoder): @ms Double {
    return when (decoder) {
      is BsonDecoder -> {
        decoder.reader().readDateTime().toDouble()
      }

      else -> {
        parseUtc(decoder.decodeString())
      }
    }
  }
}
