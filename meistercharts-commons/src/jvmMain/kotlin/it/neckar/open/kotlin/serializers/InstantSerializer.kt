package it.neckar.open.kotlin.serializers

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.time.Instant

/**
 * Serializer for Duration
 */
object InstantSerializer : KSerializer<Instant> {
  override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Instant", PrimitiveKind.STRING)

  override fun serialize(encoder: Encoder, obj: Instant) {
    encoder.encodeString(obj.toString())
  }

  override fun deserialize(decoder: Decoder): Instant {
    return Instant.parse(decoder.decodeString())
  }
}
