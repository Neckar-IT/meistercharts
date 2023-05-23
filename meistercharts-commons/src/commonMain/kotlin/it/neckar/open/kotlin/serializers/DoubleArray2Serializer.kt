package it.neckar.open.kotlin.serializers

import it.neckar.open.collections.DoubleArray2
import it.neckar.open.collections.fastForEach
import it.neckar.open.kotlin.lang.fromBase64
import it.neckar.open.kotlin.lang.toBase64
import it.neckar.open.kotlin.bytearray.ByteArrayBuilder
import it.neckar.open.kotlin.bytearray.ByteArrayReader
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

/**
 * Serializer for DoubleArray2
 */
object DoubleArray2Serializer : KSerializer<DoubleArray2> {
  override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("DoubleArray2", PrimitiveKind.STRING)

  override fun serialize(encoder: Encoder, value: DoubleArray2) {
    encoder.encodeString(toByteArray(value).toBase64())
  }

  override fun deserialize(decoder: Decoder): DoubleArray2 {
    return parse(decoder.decodeString().fromBase64())
  }

  fun toByteArray(values: DoubleArray2): ByteArray {
    val builder = ByteArrayBuilder()

    val width = values.width
    val height = values.height

    builder.s16BE(width)
    builder.s16BE(height)

    if (width == 0 || height == 0) {
      //Return immediately - the array is empty
      return builder.toByteArray()
    }

    values.data.fastForEach {
      builder.f64BE(it)
    }

    return builder.toByteArray()
  }

  /**
   * Parses a byte array into a values array
   */
  fun parse(values: ByteArray): DoubleArray2 {
    val reader = ByteArrayReader(values, 0)

    val width = reader.s16BE()
    val height = reader.s16BE()

    if (width == 0 || height == 0) {
      //Array is empty
      return DoubleArray2(width, height, 0.0)
    }

    return DoubleArray2(width, height) {
      reader.f64BE()
    }
  }

}
