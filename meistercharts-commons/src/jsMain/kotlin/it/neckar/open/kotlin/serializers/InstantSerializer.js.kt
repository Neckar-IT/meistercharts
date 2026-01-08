package it.neckar.open.kotlin.serializers

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlin.time.Instant

actual class InstantSerializer : KSerializer<Instant> {
  actual override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("InstantAsIsoDateTime", PrimitiveKind.STRING)


  actual override fun serialize(encoder: Encoder, value: Instant) {
    encoder.encodeString(value.toString())
  }

  actual override fun deserialize(decoder: Decoder): Instant {
    return Instant.parse(decoder.decodeString())
  }
}
