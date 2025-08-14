package it.neckar.time.io

import it.neckar.time.Millis
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

/**
 * Formats a double value as iso format.
 *
 * ATTENTION: Does *not* support nanoseconds!
 */
expect object MillisAsIsoDateTimeSerializer : KSerializer<Millis> {
  override fun deserialize(decoder: Decoder): Millis
  override val descriptor: SerialDescriptor
  override fun serialize(encoder: Encoder, value: Millis)
}
