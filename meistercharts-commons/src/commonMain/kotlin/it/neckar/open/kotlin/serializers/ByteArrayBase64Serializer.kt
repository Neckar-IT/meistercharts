package it.neckar.open.kotlin.serializers

import it.neckar.open.kotlin.lang.fromBase64
import it.neckar.open.kotlin.lang.toBase64
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

/**
 * Serializer for ByteArray
 *
 * Usage:`val foobar: @Serializable(with = ByteArrayBase64Serializer::class) ByteArray?`
 */
object ByteArrayBase64Serializer : KSerializer<ByteArray> {
  override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("ByteArrayBase64", PrimitiveKind.STRING)

  override fun serialize(encoder: Encoder, value: ByteArray) {
    encoder.encodeString(value.toBase64())
  }

  override fun deserialize(decoder: Decoder): ByteArray {
    return decoder.decodeString().fromBase64()
  }
}
