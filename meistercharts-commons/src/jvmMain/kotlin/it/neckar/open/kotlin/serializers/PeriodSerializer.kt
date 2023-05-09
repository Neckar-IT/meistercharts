package it.neckar.open.kotlin.serializers

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.time.Period

/**
 * Serializer for Period
 */
object PeriodSerializer : KSerializer<Period> {
  override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Period", PrimitiveKind.STRING)

  override fun serialize(encoder: Encoder, obj: Period) {
    encoder.encodeString(obj.toString())
  }

  override fun deserialize(decoder: Decoder): Period {
    return Period.parse(decoder.decodeString())
  }
}
