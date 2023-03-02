package com.meistercharts.history.impl.io

import it.neckar.open.collections.DoubleArray2
import it.neckar.open.collections.fastForEach
import it.neckar.open.kotlin.bytearray.ByteArrayBuilder
import it.neckar.open.kotlin.bytearray.ByteArrayReader

/**
 * Serializes an IntArray2
 *
 */
@Deprecated("Use DoubleArray2Serializer instead")
object DoubleArray2SerializerOld {

  /**
   * Converts the values array to an optimized byte array
   */
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
