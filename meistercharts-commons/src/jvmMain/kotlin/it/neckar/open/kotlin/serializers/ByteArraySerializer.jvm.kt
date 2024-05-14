package it.neckar.open.kotlin.serializers

import it.neckar.open.kotlin.lang.fromBase64
import it.neckar.open.kotlin.lang.toBase64
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import org.bson.BsonBinary
import org.bson.BsonBinarySubType
import org.bson.codecs.kotlinx.BsonDecoder
import org.bson.codecs.kotlinx.BsonEncoder

/**
 * Serializer for ByteArray
 *
 * Usage:`val foobar: @Serializable(with = ByteArrayBase64Serializer::class) ByteArray?`
 */
actual object ByteArraySerializer : KSerializer<ByteArray> {
  override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("ByteArrayBase64", PrimitiveKind.STRING)

  override fun serialize(encoder: Encoder, value: ByteArray) {
    when (encoder) {
      is BsonEncoder -> {
        encoder.encodeBsonValue(BsonBinary(BsonBinarySubType.BINARY, value))
      }

      else -> encoder.encodeString(value.toBase64())
    }
  }

  override fun deserialize(decoder: Decoder): ByteArray {
    return when (decoder) {
      is BsonDecoder -> {
        decoder.decodeBsonValue().asBinary().data
      }

      else -> decoder.decodeString().fromBase64()
    }
  }
}
