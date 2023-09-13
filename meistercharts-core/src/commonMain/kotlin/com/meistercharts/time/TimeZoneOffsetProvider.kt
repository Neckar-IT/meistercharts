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

import it.neckar.datetime.minimal.TimeZone
import it.neckar.open.unit.si.ms

/**
 * The time-zone-offset provider that has been configured.
 * The design is configured from the MeisterChartsPlatform
 */
var timeZoneOffsetProvider: CachedTimeZoneOffsetProvider = DefaultTimeZoneOffsetProvider().cached()

/**
 * Provider for time zone offsets for a given timestamp and time zone
 */
fun interface TimeZoneOffsetProvider {
  /**
   * Provides the offset for the given time-zone at the given timestamp
   * @param timestamp represents milliseconds since 1 January 1970 UTC
   * @param timeZone the time-zone for which the offset should be provided
   */
  fun timeZoneOffset(timestamp: @ms Double, timeZone: TimeZone): @ms Double
}

/**
 * Always returns 0 as a time-zone offset
 */
object Always0TimeZoneOffsetProvider : TimeZoneOffsetProvider {
  override fun timeZoneOffset(timestamp: @ms Double, timeZone: TimeZone): @ms Double {
    return 0.0
  }

}

