package it.neckar.time.io

import it.neckar.time.Millis
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlin.js.Date

actual object MillisAsIsoDateTimeSerializer : KSerializer<Millis> {
  actual override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("MillisAsIsoDateTime", PrimitiveKind.STRING)

  actual override fun serialize(encoder: Encoder, value: Millis) {
    encoder.encodeString(value.millis.formatUtc())
  }

  actual override fun deserialize(decoder: Decoder): Millis {
    return Millis(Date.parse(decoder.decodeString()))
  }
}

private fun Double.formatUtc(): String {
  return Date(this).toISOString()
}

