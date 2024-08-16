package it.neckar.open.http.io

import it.neckar.open.http.Url
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

/**
 * Serializer for URL
 */
object UrlSerializer : KSerializer<Url> {
  override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Url", PrimitiveKind.STRING)

  override fun serialize(encoder: Encoder, value: Url) {
    encoder.encodeString(value.toString())
  }

  override fun deserialize(decoder: Decoder): Url {
    return Url.parse(decoder.decodeString())
  }

  object DataScheme : KSerializer<Url.DataScheme> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Url.DataScheme", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: Url.DataScheme) {
      encoder.encodeString(value.toString())
    }

    override fun deserialize(decoder: Decoder): Url.DataScheme {
      return Url.DataScheme(decoder.decodeString())
    }
  }

  object Absolute : KSerializer<Url.Absolute> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Url.Absolute", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: Url.Absolute) {
      encoder.encodeString(value.toString())
    }

    override fun deserialize(decoder: Decoder): Url.Absolute {
      return Url.Absolute(decoder.decodeString())
    }
  }

  object RootRelative : KSerializer<Url.RootRelative> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Url.RootRelative", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: Url.RootRelative) {
      encoder.encodeString(value.toString())
    }

    override fun deserialize(decoder: Decoder): Url.RootRelative {
      return Url.RootRelative(decoder.decodeString())
    }
  }

  object Relative : KSerializer<Url.Relative> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Url.Relative", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: Url.Relative) {
      encoder.encodeString(value.toString())
    }

    override fun deserialize(decoder: Decoder): Url.Relative {
      return Url.Relative(decoder.decodeString())
    }
  }

}

