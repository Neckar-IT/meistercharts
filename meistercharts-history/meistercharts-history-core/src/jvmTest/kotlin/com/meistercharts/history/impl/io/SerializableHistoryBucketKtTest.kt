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
package com.meistercharts.history.impl.io

import assertk.*
import assertk.assertions.*
import com.meistercharts.history.HistoryBucketDescriptor
import com.meistercharts.history.HistoryBucketRange
import com.meistercharts.history.TimestampIndex
import com.meistercharts.history.downsampling.calculateDownSampled
import com.meistercharts.history.impl.HistoryValues
import com.meistercharts.history.impl.MockSinusHistoryStorage
import com.meistercharts.history.impl.RecordingType
import com.meistercharts.history.impl.historyValues
import it.neckar.open.serialization.roundTrip
import org.junit.jupiter.api.Test

class SerializableHistoryBucketKtTest {
  @Test
  fun toSerializable() {
    val descriptor: HistoryBucketDescriptor = HistoryBucketDescriptor.forTimestamp(1000.0, HistoryBucketRange.OneMinute)

    val mockBucket = MockSinusHistoryStorage().get(descriptor)
    val targetDescriptor: HistoryBucketDescriptor = HistoryBucketDescriptor.forTimestamp(1000.0, HistoryBucketRange.TenMinutes)

    val downSampledBucket = targetDescriptor.calculateDownSampled(listOf(mockBucket))

    assertThat(mockBucket.chunk.recordingType).isEqualTo(RecordingType.Measured)
    assertThat(downSampledBucket.chunk.recordingType).isEqualTo(RecordingType.Calculated)
    assertThat(downSampledBucket.chunk.values.hasMinMax).isTrue()
    assertThat(downSampledBucket.chunk.values.decimalHistoryValues.minValues).isNotNull()
    assertThat(downSampledBucket.chunk.values.decimalHistoryValues.maxValues).isNotNull()
    assertThat(downSampledBucket.chunk.values.enumHistoryValues.mostOfTheTimeValues).isNotNull()
    val serializableHistoryBucket = downSampledBucket.toSerializable()
    val deserializedHistoryBucket = serializableHistoryBucket.toHistoryBucket()
    assertThat(deserializedHistoryBucket.chunk.recordingType).isEqualTo(RecordingType.Calculated)
    assertThat(deserializedHistoryBucket.chunk.values.decimalHistoryValues.minValues).isNotNull() // should be true
    assertThat(deserializedHistoryBucket.chunk.values.decimalHistoryValues.maxValues).isNotNull() // should be true
    assertThat(deserializedHistoryBucket.chunk.values.hasMinMax).isTrue() // should be true
  }
}
