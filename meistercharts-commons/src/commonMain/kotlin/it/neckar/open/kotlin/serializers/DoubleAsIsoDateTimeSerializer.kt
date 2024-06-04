package it.neckar.open.kotlin.serializers

import it.neckar.open.unit.si.ms
import kotlinx.serialization.KSerializer
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
expect object DoubleAsIsoDateTimeSerializer : KSerializer<@ms Double> {
  override fun deserialize(decoder: Decoder): Double
  override val descriptor: SerialDescriptor
  override fun serialize(encoder: Encoder, value: Double)
}
