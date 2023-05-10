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
package com.meistercharts.history

import com.meistercharts.algorithms.TimeRange
import com.meistercharts.history.impl.HistoryChunk
import com.meistercharts.history.impl.HistoryValues
import com.meistercharts.history.impl.RecordingType
import it.neckar.open.collections.fastForEach
import it.neckar.open.formatting.formatUtc
import it.neckar.open.unit.number.MayBeNaN
import it.neckar.open.unit.other.Exclusive
import it.neckar.open.unit.other.Inclusive
import it.neckar.open.unit.other.Sorted
import it.neckar.open.unit.si.ms
import kotlin.contracts.contract

/**
 * Represents a container for history data that has clear borders that can be calculated in constant time.
 * The borders are placed on "even" borders depending on the resolution of the given descriptor
 *
 * This is a high level object that should be used when working with the history.
 *
 *
 * Distinction to the other classes:
 * * [HistoryValues] does *only* contain the values - no timestamps
 * * [HistoryChunk] contains the [HistoryConfiguration], the [HistoryValues] *and* the timestamps.
 * * [com.meistercharts.history.HistoryBucket] contains a [HistoryChunk] and a [com.meistercharts.history.HistoryBucketDescriptor]. Is placed on "event" borders!
 */
data class HistoryBucket(
  /**
   * The descriptor of the bucket
   */
  val descriptor: HistoryBucketDescriptor,
  /**
   * The history chunk
   */
  val chunk: HistoryChunk,
) {

  val bucketRange: HistoryBucketRange
    get() {
      return descriptor.bucketRange
    }

  /**
   * The sampling period
   */
  val samplingPeriod: SamplingPeriod
    get() {
      return bucketRange.samplingPeriod
    }

  /**
   * The start of the bucket
   */
  @Inclusive
  val start: @ms Double
    get() {
      return descriptor.start
    }

  /**
   * The end of the bucket - exclusive
   */
  @Exclusive
  val end: @ms Double
    get() {
      return descriptor.end
    }

  init {
    if (!chunk.isEmpty()) {
      require(chunk.firstTimestamp >= descriptor.start) {
        "Invalid chunk start. Was <${chunk.firstTimestamp.formatUtc()}> but expected at least <${descriptor.start.formatUtc()}>"
      }
      require(chunk.lastTimestamp < descriptor.end) {
        "Invalid chunk end. Was <${chunk.lastTimestamp.formatUtc()}> but expected less than <${descriptor.end.formatUtc()}>"
      }
    }
  }

  /**
   * Returns true if this bucket contains data for the given time range
   */
  fun overlaps(timeRange: TimeRange): Boolean {
    return start < timeRange.end && end > timeRange.start
  }

  /**
   * Returns true if the provided timestamp lays within the time range of this bucket
   */
  fun contains(timestamp: @ms Double): Boolean {
    return start <= timestamp && end > timestamp
  }

  override fun toString(): String {
    return "HistoryBucket(descriptor=$descriptor)"
  }
}

/**
 * Find a value for the provided timestamp.
 * Will return the value:
 * * at the timestamp
 * * directly after the timestamp
 *
 * Will return [Double.NaN] if no good value could be found
 */
fun @Sorted List<HistoryBucket>.findDecimalValueAt(dataSeriesIndex: DecimalDataSeriesIndex, timestamp: @ms Double): @MayBeNaN Double {
  var foundValue: @MayBeNaN Double = Double.NaN

  find(timestamp) { bucket: HistoryBucket, timestampIndex: TimestampIndex ->
    foundValue = bucket.chunk.getDecimalValue(dataSeriesIndex, timestampIndex)
  }

  return foundValue
}

fun @Sorted List<HistoryBucket>.findEnumValueAt(dataSeriesIndex: EnumDataSeriesIndex, timestamp: @ms Double): @MayBeNoValueOrPending HistoryEnumSet {
  var foundValue: @MayBeNoValueOrPending HistoryEnumSet = HistoryEnumSet.NoValue

  find(timestamp) { bucket: HistoryBucket, timestampIndex: TimestampIndex ->
    foundValue = bucket.chunk.getEnumValue(dataSeriesIndex, timestampIndex)
  }

  return foundValue
}

fun @Sorted List<HistoryBucket>.findReferenceEntryIdValueAt(dataSeriesIndex: ReferenceEntryDataSeriesIndex, timestamp: @ms Double): @MayBeNoValueOrPending ReferenceEntryId {
  var found: @MayBeNoValueOrPending ReferenceEntryId = ReferenceEntryId.NoValue

  find(timestamp) { bucket: HistoryBucket, timestampIndex: TimestampIndex ->
    found = bucket.chunk.getReferenceEntryId(dataSeriesIndex, timestampIndex)
  }

  return found
}

/**
 * Finds the provided timestamp in a list of history buckets.
 * Calls the provided [callback] for the found timestamp index and bucket.
 *
 * If the [timestamp] is a direct hit, returns the timestamp itself
 * Else:
 * The callback will be called with the timestamp index *before* the provided [timestamp] - if within the span defined by the [SamplingPeriod] of the [HistoryBucket].
 */
fun @Sorted List<HistoryBucket>.find(timestamp: @ms Double, callback: (bucket: HistoryBucket, timestampIndex: TimestampIndex) -> Unit) {
  contract {
    callsInPlace(callback, kotlin.contracts.InvocationKind.AT_MOST_ONCE)
  }

  fastForEach { bucket ->
    val chunk = bucket.chunk

    val bestTimestampIndexFor = chunk.bestTimestampIndexFor(timestamp)
    if (bestTimestampIndexFor.found) {
      //Direct hit, just return the value
      return callback(bucket, TimestampIndex(bestTimestampIndexFor.index))
    }

    val nearIndex = bestTimestampIndexFor.nearIndex

    if (nearIndex <= 0) {
      //Too early, not relevant
      return@fastForEach
    }

    val timestampIndex = TimestampIndex(nearIndex - 1) //use the value before
    if (timestampIndex.value >= chunk.timeStampsCount) {
      //Too late, not relevant, no value will be found
      return@fastForEach
    }

    //The timestamp that has been found
    @ms val relevantTimestamp = chunk.timestampCenter(timestampIndex)

    //The distance between the relevant timestamp and the queried timestamp
    @ms val distance = timestamp - relevantTimestamp

    require(distance > 0.0) {
      "?????? $distance ${relevantTimestamp.formatUtc()} ${timestamp.formatUtc()}"
    }

    @ms val maxDistance = bucket.descriptor.bucketRange.distance
    when {
      relevantTimestamp >= timestamp + maxDistance -> {
        //Too large, no more value will be found (even in later buckets)
        return
      }

      distance < maxDistance -> {
        //Found relevantTimestamp within the max distance
        return callback(bucket, timestampIndex)
      }

      else -> {
        return@fastForEach
      }
    }
  }

  return
}
