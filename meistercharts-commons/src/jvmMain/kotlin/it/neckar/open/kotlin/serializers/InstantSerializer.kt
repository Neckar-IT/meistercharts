package it.neckar.open.kotlin.serializers

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import org.bson.BsonDateTime
import org.bson.codecs.kotlinx.BsonDecoder
import org.bson.codecs.kotlinx.BsonEncoder
import kotlin.time.Instant

/**
 * Serializer for [Instant] that uses the native format for each context:
 * - BSON DateTime when used with MongoDB (enables queries, smaller storage)
 * - ISO-8601 string when used with JSON (human readable, standard format)
 *
 * Usage: Prefer using the type alias [NativeInstant] instead of manually annotating with this serializer.
 *
 * @see NativeInstant
 */
actual object InstantSerializer : KSerializer<Instant> {
  actual override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("InstantAsIsoDateTime", PrimitiveKind.STRING)

  actual override fun serialize(encoder: Encoder, value: Instant) {
    when (encoder) {
      is BsonEncoder -> encoder.encodeBsonValue(BsonDateTime(value.toEpochMilliseconds()))
      else -> encoder.encodeString(value.toString())
    }
  }

  actual override fun deserialize(decoder: Decoder): Instant {
    return when (decoder) {
      is BsonDecoder -> Instant.fromEpochMilliseconds(decoder.decodeBsonValue().asDateTime().value)
      else -> Instant.parse(decoder.decodeString())
    }
  }
}
