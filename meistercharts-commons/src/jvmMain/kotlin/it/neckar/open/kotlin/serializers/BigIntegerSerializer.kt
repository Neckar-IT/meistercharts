package it.neckar.open.kotlin.serializers

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.math.BigInteger

/**
 * Serializer for BigInteger
 */
object BigIntegerSerializer : KSerializer<BigInteger> {
  override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("BigInteger", PrimitiveKind.STRING)

  override fun serialize(encoder: Encoder, obj: BigInteger) {
    encoder.encodeString(obj.toString())
  }

  override fun deserialize(decoder: Decoder): BigInteger {
    return BigInteger(decoder.decodeString())
  }
}
