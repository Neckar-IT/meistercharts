package it.neckar.open.kotlin.serializers

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

/**
 * Serializes an IntRange
 */
object IntRangeSerializer : KSerializer<IntRange> {
  override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("IntRange", PrimitiveKind.STRING)

  override fun serialize(encoder: Encoder, value: IntRange) {
    encoder.encodeString("${value.first}$delimiter${value.last}")
  }

  override fun deserialize(decoder: Decoder): IntRange {
    val decodeString = decoder.decodeString()
    val parts = decodeString.split(delimiter)
    require(parts.size == 2) { "Expected two parts separated by $delimiter for [$decodeString]" }
    return IntRange(parts[0].toInt(), parts[1].toInt())
  }

  /**
   * The delimiter between the two values
   */
  const val delimiter: String = ".."
}
