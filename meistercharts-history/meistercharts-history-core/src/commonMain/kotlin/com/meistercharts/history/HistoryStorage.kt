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
import it.neckar.open.collections.fastForEach
import it.neckar.open.collections.fastMapNotNull
import it.neckar.open.dispose.OnDispose
import it.neckar.open.formatting.formatUtc
import it.neckar.open.unit.other.Inclusive
import it.neckar.open.unit.si.ms

/**
 * Returns history buckets for given descriptors.
 *
 * Everything named "*Storage" returns history buckets for fixed descriptors.
 * Use [query] when querying for time ranges.
 *
 */
interface HistoryStorage : OnDispose {
  /**
   * Returns a list of buckets that span the given time
   *
   * This method will return a list of buckets that span at least the complete time range that has been requested - usually more.
   */
  fun query(start: @Inclusive @ms Double, end: @Inclusive @ms Double, samplingPeriod: SamplingPeriod): List<HistoryBucket> {
    val range = HistoryBucketRange.find(samplingPeriod)

    //Collect the buckets
    val descriptors = HistoryBucketDescriptor.forRange(start, end, range)

    check(descriptors.isNotEmpty()) { "Descriptors must not be empty. Start: ${start.formatUtc()} - ${end.formatUtc()}" }
    check(descriptors.first().start <= start) {
      "Invalid start. Was: ${descriptors.first().start.formatUtc()} but must be before ${start.formatUtc()}"
    }
    check(descriptors.last().end >= end) {
      "Invalid end. Was: ${descriptors.last().end.formatUtc()} but must be after ${end.formatUtc()}"
    }

    return get(descriptors)
  }

  /**
   * Queries the time range. Start and end inclusive
   */
  fun query(timeRange: @ms @Inclusive TimeRange, samplingPeriod: SamplingPeriod): List<HistoryBucket> {
    return query(timeRange.start, timeRange.end, samplingPeriod)
  }

  /**
   * Returns the bucket for the given descriptor.
   *
   * Returns null if no history bucket is stored for the given descriptor
   */
  fun get(descriptor: HistoryBucketDescriptor): HistoryBucket?

  /**
   * Returns the buckets for the given descriptors.
   *
   * The returned list contains only the history buckets that have been found.
   * The returned list may contain less elements than the given descriptors
   */
  fun get(descriptors: List<HistoryBucketDescriptor>): List<HistoryBucket> {
    return descriptors.fastMapNotNull {
      get(it)
    }
  }

  /**
   * Returns min and max values for the given time range and sampling period
   */
  fun queryMinMax(timeRange: TimeRange, samplingPeriod: SamplingPeriod, dataSeriesndices: List<DecimalDataSeriesIndex>): MinMaxValues {
    val builder = MinMaxValues.Builder(dataSeriesndices)

    query(timeRange, samplingPeriod).fastForEach { bucket ->
      val chunk = bucket.chunk

      for (timeStampIndexAsInt in 0 until chunk.timeStampsCount) {
        val timeStampIndex = TimestampIndex(timeStampIndexAsInt)
        @ms val time = chunk.timestampCenter(timeStampIndex)

        if (time < timeRange.start) {
          //Skip all data points that are not visible on this tile yet
          continue
        }
        if (time > timeRange.end) {
          //Skip all data points that are no longer visible on this tile
          break
        }

        builder.appendFrom(chunk, timeStampIndex)
      }
    }

    return builder.build()
  }
}
