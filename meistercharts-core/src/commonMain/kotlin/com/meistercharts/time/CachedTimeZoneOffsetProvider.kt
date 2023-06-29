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

import it.neckar.open.collections.Cache
import it.neckar.open.collections.cache
import it.neckar.open.kotlin.lang.floor
import it.neckar.open.time.TimeZone
import it.neckar.open.unit.si.ms
import kotlin.time.Duration.Companion.minutes
import kotlin.time.DurationUnit

/**
 * A time-zone-offset provider that returns cached values.
 */
interface CachedTimeZoneOffsetProvider : TimeZoneOffsetProvider {
  /**
   * Returns the current cache size
   */
  val currentCacheSize: Int
}

/**
 * A time-zone-offset provider that returns cached values computed by its [delegate].
 */
class DefaultCachedTimeZoneOffsetProvider(
  val delegate: TimeZoneOffsetProvider,
  /**
   * The maximum size of the cache
   */
  cacheSize: Int = 500,
) : CachedTimeZoneOffsetProvider {

  init {
    require(delegate !is CachedTimeZoneOffsetProvider) { "cannot cache an already cached time-zone-offset provider" }
  }

  private val cache: Cache<Int, @ms Double> = cache("Cache for CachedTimeZoneOffsetProvider", cacheSize)

  override val currentCacheSize: Int
    get() = cache.size

  override fun timeZoneOffset(timestamp: @ms Double, timeZone: TimeZone): @ms Double {
    val alignedTimestamp = alignTimestampToTimeZones(timestamp)
    //Calculate the hash code to avoid instantiation of objects.
    val key = 31 * alignedTimestamp.hashCode() + timeZone.hashCode()
    return cache.getOrStore(key) {
      delegate.timeZoneOffset(timestamp, timeZone)
    }
  }

  companion object {
    private val fifteenMinutes: @ms Double = 15.minutes.toDouble(DurationUnit.MILLISECONDS)

    internal fun alignTimestampToTimeZones(timestamp: @ms Double): @ms Double {
      //While most time zones differ from Coordinated Universal Time (UTC) by a number
      //of full hours, there are also a few time zones with both 30-minute and 45-minute
      //offsets. So a precision of 15 minutes should suffice.
      //We use floor() here because a time change (switch from summer to winter savings time)
      //typically occurs to the full hour.
      return (timestamp / fifteenMinutes).floor() * fifteenMinutes
    }
  }
}

/**
 * Creates a [CachedTimeZoneOffsetProvider] with this [TimeZoneOffsetProvider] being its delegate
 */
fun TimeZoneOffsetProvider.cached(cacheSize: Int = 500): CachedTimeZoneOffsetProvider {
  return DefaultCachedTimeZoneOffsetProvider(this, cacheSize)
}
