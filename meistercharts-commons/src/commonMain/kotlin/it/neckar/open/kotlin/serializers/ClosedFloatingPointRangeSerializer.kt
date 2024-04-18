package it.neckar.open.kotlin.serializers

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

/**
 *
 */
object ClosedFloatingPointRangeSerializer : KSerializer<ClosedFloatingPointRange<Double>> {
  override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("ClosedFloatingPointRange", PrimitiveKind.STRING)

  override fun serialize(encoder: Encoder, value: ClosedFloatingPointRange<Double>) {
    encoder.encodeString("${value.start}$delimiter${value.endInclusive}")

  }

  override fun deserialize(decoder: Decoder): ClosedFloatingPointRange<Double> {
    val decodeString = decoder.decodeString()
    val parts = decodeString.split(delimiter)
    require(parts.size == 2) { "Expected two parts separated by ${IntRangeSerializer.delimiter} for [$decodeString]" }
    return parts[0].toDouble()..parts[1].toDouble()
  }

  const val delimiter: String = ".."
}
