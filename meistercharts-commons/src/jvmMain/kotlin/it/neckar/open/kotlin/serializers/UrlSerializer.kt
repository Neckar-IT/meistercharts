package it.neckar.open.kotlin.serializers

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.net.URL

/**
 * Serializer for URL
 */
object UrlSerializer : KSerializer<URL> {
  override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("URL", PrimitiveKind.STRING)

  override fun serialize(encoder: Encoder, value: URL) {
    encoder.encodeString(value.toString())
  }

  override fun deserialize(decoder: Decoder): URL {
    return URL(decoder.decodeString())
  }
}
