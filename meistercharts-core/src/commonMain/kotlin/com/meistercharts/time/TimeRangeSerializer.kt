/**
 * Copyright 2023 Neckar IT GmbH, Mössingen, Germany
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.meistercharts.time

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.serialDescriptor
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.encoding.decodeStructure
import kotlinx.serialization.encoding.encodeStructure

/**
 * Serializer for TimeRange
 */
object TimeRangeSerializer : KSerializer<TimeRange> {
  override val descriptor: SerialDescriptor = buildClassSerialDescriptor("TimeRange") {
    element("start", serialDescriptor<Double>())
    element("end", serialDescriptor<Double>())
  }

  override fun serialize(encoder: Encoder, value: TimeRange) {
    encoder.encodeStructure(descriptor) {
      encodeDoubleElement(descriptor, 0, value.start)
      encodeDoubleElement(descriptor, 1, value.end)
    }
  }

  override fun deserialize(decoder: Decoder): TimeRange {
    var start: Double = Double.NaN
    var end: Double = Double.NaN

    decoder.decodeStructure(descriptor) {
      while (true) {
        when (val index = decodeElementIndex(descriptor)) {
          0 -> start = decodeDoubleElement(descriptor, 0)
          1 -> end = decodeDoubleElement(descriptor, 1)
          CompositeDecoder.DECODE_DONE -> break
          else -> error("Unexpected index: $index")
        }
      }
    }

    return TimeRange(start, end)
  }
}
