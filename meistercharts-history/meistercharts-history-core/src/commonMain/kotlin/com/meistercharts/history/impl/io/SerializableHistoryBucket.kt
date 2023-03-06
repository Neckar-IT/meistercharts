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

import it.neckar.open.unit.other.Sorted
import it.neckar.open.unit.si.ms
import com.meistercharts.history.HistoryBucket
import com.meistercharts.history.HistoryBucketDescriptor
import com.meistercharts.history.HistoryBucketRange
import com.meistercharts.history.HistoryConfiguration
import com.meistercharts.history.impl.HistoryChunk
import com.meistercharts.history.impl.HistoryValues
import com.meistercharts.history.impl.RecordingType
import kotlinx.serialization.Serializable

/**
 * This is a copy of [HistoryBucket] that is serializable.
 * Serializing the HistoryBucket itself does currently not work with the message
 * "Class HistoryBucket have constructor parameters which are not properties and therefore it is not serializable automatically"
 */
@Serializable
data class SerializableHistoryBucket(
  /**
   * The descriptor of the bucket
   */
  val descriptor: SerializableHistoryBucketDescriptor,
  ///**
  // * The history chunk
  // */
  val chunk: SerializableHistoryChunk
) {

  fun toHistoryBucket(): HistoryBucket {
    return HistoryBucket(descriptor.toHistoryBucketDescriptor(),chunk.toHistoryChunk())
  }
}

/**
 * Convert to a serializable history bucket
 */
fun HistoryBucket.toSerializable(): SerializableHistoryBucket {
  return SerializableHistoryBucket(descriptor.toSerializable(), chunk.toSerializable())
}


@Deprecated("No longer required. Serialize HistoryBucketDescriptor directly")
@Serializable
data class SerializableHistoryBucketDescriptor(
  val index: Double,
  val bucketRange: HistoryBucketRange
) {

  fun toHistoryBucketDescriptor(): HistoryBucketDescriptor {
    return HistoryBucketDescriptor.forIndex(index, bucketRange)
  }
}

fun HistoryBucketDescriptor.toSerializable(): SerializableHistoryBucketDescriptor {
  return SerializableHistoryBucketDescriptor(index, bucketRange)
}

@Deprecated("No longer required, serialize HistoryChunk directly")
@Serializable
data class SerializableHistoryChunk(
  /**
   * The history configuration - contains the IDs, decimal places and display names
   *
   * Each entry in [values] contains as many values as there are data series ids stored in the configuration.
   */
  val configuration: HistoryConfiguration,

  /**
   * The time stamps for the history chunk. Each entry in [values] corresponds to
   * one timestamp within this array.
   */
  val timeStamps: @ms @Sorted DoubleArray,

  /**
   * Contains the values.
   * Contains one entry for each timestamp in [timeStamps]. Each entry contains one value for each data series id stored in the [configuration]
   *
   * ATTENTION: Must not be modified!
   */
  val values: HistoryValues,

  /**
   * The type of the history chunk
   */
  val recordingType: RecordingType,
) {

  fun toHistoryChunk(): HistoryChunk {
    return HistoryChunk(configuration, timeStamps, values, recordingType)
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other == null || this::class != other::class) return false

    other as SerializableHistoryChunk

    if (configuration != other.configuration) return false
    if (!timeStamps.contentEquals(other.timeStamps)) return false
    if (values != other.values) return false
    if (recordingType != other.recordingType) return false

    return true
  }

  override fun hashCode(): Int {
    var result = configuration.hashCode()
    result = 31 * result + timeStamps.contentHashCode()
    result = 31 * result + values.hashCode()
    result = 31 * result + recordingType.hashCode()
    return result
  }
}

fun HistoryChunk.toSerializable(): SerializableHistoryChunk {
  return SerializableHistoryChunk(configuration, timeStamps, values, recordingType)
}
