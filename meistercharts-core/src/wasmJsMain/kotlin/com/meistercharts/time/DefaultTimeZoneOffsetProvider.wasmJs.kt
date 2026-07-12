/*
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
import it.neckar.open.kotlin.lang.round
import it.neckar.open.unit.si.ms

actual class DefaultTimeZoneOffsetProvider : TimeZoneOffsetProvider {
  actual override fun timeZoneOffset(timestamp: Double, timeZone: TimeZone): Double {
    //Round the time stamp to avoid time zone offsets with fragments of millis
    val timestampRounded = timestamp.round()

    @ms val zonedTimestamp = jsZonedTimestamp(timestampRounded, timeZone.zoneId)
    //The time-zone offset is the difference between the UTC-timestamp and the zone-specific timestamp.
    return (timestampRounded - zonedTimestamp)
  }
}

/**
 * Treats the time-zone specific date as a UTC timestamp.
 * The locale 'ja' ensures that the sequence is year, month, day of month, hour, minute and second.
 * Adapted from https://stackoverflow.com/questions/36112774/calculate-the-utc-offset-given-a-timezone-string-in-javascript
 */
private fun jsZonedTimestamp(millis: Double, timeZone: String): Double = js(
  """(function() {
    var date = new Date(millis);
    var parts = date.toLocaleString('ja', {timeZone: timeZone}).split(/[\/\s:]/).map(Number);
    return Date.UTC(parts[0], parts[1] - 1, parts[2], parts[3], parts[4], parts[5], date.getMilliseconds());
  })()"""
)
