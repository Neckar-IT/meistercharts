package it.neckar.open.kotlin.serializers

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

/**
 * Serializer for ByteArray
 *
 * Usage:`val foobar: @Serializable(with = ByteArrayBase64Serializer::class) ByteArray?`
 */
expect object ByteArraySerializer : KSerializer<ByteArray> {
  override fun deserialize(decoder: Decoder): ByteArray
  override val descriptor: SerialDescriptor
  override fun serialize(encoder: Encoder, value: ByteArray)
}
