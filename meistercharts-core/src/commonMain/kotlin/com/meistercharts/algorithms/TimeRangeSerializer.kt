package com.meistercharts.algorithms

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.serialDescriptor
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.encoding.decodeStructure
import kotlinx.serialization.encoding.encodeStructure


object TimeRangeSerializer: KSerializer<TimeRange> {
  override val descriptor: SerialDescriptor = buildClassSerialDescriptor("TimeRange") {
    element("start", serialDescriptor<Double>())
    element("end", serialDescriptor<Double>())
  }

  override fun serialize(encoder: Encoder, value: TimeRange) {
    encoder.encodeStructure(TimeRangeSerializer.descriptor) {
      encodeDoubleElement(TimeRangeSerializer.descriptor, 0, value.start)
      encodeDoubleElement(TimeRangeSerializer.descriptor, 1, value.end)
    }
  }

  override fun deserialize(decoder: Decoder): TimeRange {
    var start: Double = Double.NaN
    var end: Double = Double.NaN

    decoder.decodeStructure(TimeRangeSerializer.descriptor) {
      while (true) {
        when (val index = decodeElementIndex(TimeRangeSerializer.descriptor)) {
          0 -> start = decodeDoubleElement(TimeRangeSerializer.descriptor, 0)
          1 -> end = decodeDoubleElement(TimeRangeSerializer.descriptor, 1)
          CompositeDecoder.DECODE_DONE -> break
          else -> error("Unexpected index: $index")
        }
      }
    }

    return TimeRange(start, end)
  }
}
