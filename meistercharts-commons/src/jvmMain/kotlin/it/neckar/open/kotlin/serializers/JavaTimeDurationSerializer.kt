package it.neckar.open.kotlin.serializers

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.time.Duration

/**
 * Serializer for Duration.
 *
 * Use like this:
 * ```
 * @file: UseSerializers(DurationSerializer::class)
 * ```
 */
object JavaTimeDurationSerializer : KSerializer<Duration> {
  override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("java.time.Duration", PrimitiveKind.STRING)

  override fun serialize(encoder: Encoder, value: Duration) {
    encoder.encodeString(value.toString())
  }

  override fun deserialize(decoder: Decoder): Duration {
    return Duration.parse(decoder.decodeString())
  }
}
