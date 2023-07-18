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

import it.neckar.open.collections.fastForEach
import it.neckar.open.collections.fastForEachReversed
import it.neckar.open.unit.si.ms
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.time.Duration
import kotlin.time.DurationUnit

/**
 * Represents the temporal resolution when recording the history.
 * Describes the distance between two samples.
 *
 */
enum class SamplingPeriod(
  /**
   * The distance between two samples in milliseconds
   */
  @ms
  val distance: Double,
  /**
   * The description
   */
  val label: String,
) {

  /**
   * 1 sample every millisecond
   */
  EveryMillisecond(1.0, "1 ms"),

  /**
   * 1 sample every 10 milliseconds
   */
  EveryTenMillis(10.0, "10 ms"),

  /**
   * 1 sample every 100 milliseconds
   */
  EveryHundredMillis(100.0, "100 ms"),

  /**
   * 1 sample per second
   */
  EverySecond(1_000.0, "1 sec"),

  /**
   * 1 sample per 10 seconds
   */
  EveryTenSeconds(10 * 1_000.0, "10 sec"),

  /**
   * 1 sample per minute
   */
  EveryMinute(60 * 1_000.0, "1 min"),

  /**
   * 1 sample per 10 minutes
   */
  EveryTenMinutes(10 * 60 * 1_000.0, "10 min"),

  /**
   * 1 sample per hour
   */
  EveryHour(60 * 60 * 1_000.0, "1 h"),

  /**
   * 1 sample per 6 hours
   */
  Every6Hours(60 * 60 * 1_000.0 * 6, "6 h"),

  /**
   * 1 sample every 24 hours
   */
  Every24Hours(60 * 60 * 1_000.0 * 24, "24 h"),

  /**
   * 1 sample every 5 days
   */
  Every5Days(60 * 60 * 1_000.0 * 24 * 5, "5 d"),

  /**
   * 1 sample every 30 days
   */
  Every30Days(60 * 60 * 1_000.0 * 24 * 5 * 6, "30 d"),

  /**
   * 1 sample every 90 days
   */
  Every90Days(60 * 60 * 1_000.0 * 24 * 5 * 6 * 3, "90 d"),

  /**
   * 1 sample every 360 days
   */
  Every360Days(60 * 60 * 1_000.0 * 24 * 5 * 6 * 3 * 4, "360 d"),
  ;

  /**
   * Returns the millis value that is the same or greater
   * than as the given one.
   */
  fun sameOrGreater(millis: @ms Double): @ms Double {
    return ceil(millis / distance) * distance
  }

  fun sameOrSmaller(millis: @ms Double): @ms Double {
    return floor(millis / distance) * distance
  }

  fun toHistoryBucketRange(): HistoryBucketRange {
    return HistoryBucketRange.find(this)
  }

  /**
   * Returns the sampling period above (which has a larger distance) - or null if there is none
   */
  fun above(): SamplingPeriod? {
    return when (this) {
      EveryMillisecond -> EveryTenMillis
      EveryTenMillis -> EveryHundredMillis
      EveryHundredMillis -> EverySecond
      EverySecond -> EveryTenSeconds
      EveryTenSeconds -> EveryMinute
      EveryMinute -> EveryTenMinutes
      EveryTenMinutes -> EveryHour
      EveryHour -> Every6Hours
      Every6Hours -> Every24Hours
      Every24Hours -> Every5Days
      Every5Days -> Every30Days
      Every30Days -> Every90Days
      Every90Days -> Every360Days
      Every360Days -> null
    }
  }

  companion object {
    /**
     * Returns the first sampling period that has a distance same or smaller than the given one
     */
    fun withMaxDistance(maxDistance: @ms Double): SamplingPeriod {
      entries.fastForEachReversed {
        if (it.distance <= maxDistance) {
          return it
        }
      }

      return EveryMillisecond
    }

    /**
     * Returns the first sampling period that has a distance same or greater than the given one
     */
    fun withMinDistance(minDistance: @ms Double): SamplingPeriod {
      entries.fastForEach {
        if (it.distance >= minDistance) {
          return it
        }
      }

      return Every360Days
    }

    /**
     * Returns the first sampling period that has a distance same or smaller than the given one
     */
    fun withMaxDuration(maxDuration: @ms Double): SamplingPeriod {
      return withMaxDistance(maxDuration)
    }

    /**
     * Returns the first sampling period that has a distance same or greater than the given one
     */
    fun withMinDuration(minDuration: @ms Double): SamplingPeriod {
      return withMinDistance(minDuration)
    }

    /**
     * Returns the sampling period that matches the distance perfectly.
     * Throws an exception if no perfect hit could be found
     */
    fun getForDistance(@ms distance: Double): SamplingPeriod {
      entries.fastForEach {
        if (it.distance == distance) {
          return it
        }
      }

      throw IllegalArgumentException("No resolution found for the distance <$distance>")
    }
  }
}
