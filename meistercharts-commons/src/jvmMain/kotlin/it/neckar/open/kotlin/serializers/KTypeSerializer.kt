package it.neckar.open.kotlin.serializers

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlin.reflect.KType

/**
 *
 */
object KTypeSerializer : KSerializer<KType> {
  override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("KType", PrimitiveKind.STRING)

  override fun serialize(encoder: Encoder, value: KType) {
    encoder.encodeString(value.toString())
  }

  override fun deserialize(decoder: Decoder): KType {
    throw UnsupportedOperationException("Deserialization of KType is not supported")
  }
}
