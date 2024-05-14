package it.neckar.open.kotlin.serializers

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import org.bson.BsonBinarySubType
import org.bson.codecs.kotlinx.BsonDecoder
import org.bson.codecs.kotlinx.BsonEncoder
import java.nio.ByteBuffer
import java.util.UUID

private val UUID.bytes: ByteArray
  get() {
    val byteBuffer = ByteBuffer.wrap(ByteArray(16)) // Create a new ByteBuffer with 16 bytes (length of a UUID)
    byteBuffer.putLong(this.mostSignificantBits) // Inserts the upper 64 bits
    byteBuffer.putLong(this.leastSignificantBits) // Inserts the lower 64 bits
    return byteBuffer.array()
  }

/**
 * Serializer for [java.util.UUID]
 */
object UUIDSerializer : KSerializer<UUID> {
  override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("UUID", PrimitiveKind.STRING)

  override fun serialize(encoder: Encoder, value: UUID) {
    when (encoder) {
      is BsonEncoder -> {
        val bsonBinary = org.bson.BsonBinary(BsonBinarySubType.UUID_STANDARD, value.bytes)
        encoder.encodeBsonValue(bsonBinary)
      }

      else -> encoder.encodeString(value.toString())
    }
  }

  override fun deserialize(decoder: Decoder): UUID {
    when (decoder) {
      is BsonDecoder -> {
        val reader = decoder.reader()
        val subType = reader.peekBinarySubType()
        require(subType == BsonBinarySubType.UUID_STANDARD.value) { "Expected UUID sub type but was $subType" }

        return reader.readBinaryData().asUuid()
      }

      else -> return UUID.fromString(decoder.decodeString())
    }
  }
}
