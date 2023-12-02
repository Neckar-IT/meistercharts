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

/**
 * Formats a double value as iso format.
 *
 * ATTENTION: Does *not* support nanoseconds!
 *
 * Use like this:
 * `@Serializable(with = DoubleAsIsoDateTimeSerializer::class)`
 */
object DoubleAsIsoDateTimeSerializer : KSerializer<@ms Double> {
  override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("DoubleAsIsoDateTime", PrimitiveKind.STRING)

  override fun serialize(encoder: Encoder, value: @ms Double) {
    encoder.encodeString(value.formatUtc())
  }

  override fun deserialize(decoder: Decoder): @ms Double {
    return parseUtc(decoder.decodeString())
  }
}
