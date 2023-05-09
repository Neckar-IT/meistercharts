package it.neckar.open.kotlin.serializers

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.time.LocalTime
import java.time.format.DateTimeFormatter

/**
 * Serializer for LocalTime
 */
object LocalTimeSerializer : KSerializer<LocalTime> {
  override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("LocalTime", PrimitiveKind.STRING)

  override fun serialize(encoder: Encoder, obj: LocalTime) {
    encoder.encodeString(obj.format(DateTimeFormatter.ISO_TIME))
  }

  override fun deserialize(decoder: Decoder): LocalTime {
    return LocalTime.from(DateTimeFormatter.ISO_TIME.parse(decoder.decodeString()))
  }
}
