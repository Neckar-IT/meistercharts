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
package com.meistercharts.algorithms.time

import com.meistercharts.algorithms.time.TimeZoneOffsetProvider
import it.neckar.open.kotlin.lang.round
import it.neckar.open.time.TimeZone
import it.neckar.open.unit.si.ms
import kotlin.js.Date

actual class DefaultTimeZoneOffsetProvider : TimeZoneOffsetProvider {
  override fun timeZoneOffset(timestamp: Double, timeZone: TimeZone): Double {
    //Round the time stamp to avoid time zone offsets with fragments of millis
    val timestampRounded = timestamp.round()

    //adapted from https://stackoverflow.com/questions/36112774/calculate-the-utc-offset-given-a-timezone-string-in-javascript
    val date = Date(timestampRounded)
    //this will produce something like 2020/12/10 22:00:04; the locale 'ja' ensures that the sequence is year, month, day of month, hour, minute and second
    val localeString = date.toLocaleString("ja", dateLocaleOptions { this.timeZone = timeZone.zoneId })
    //split the string at every '/', ':' or space character in order to retrieve an array that contains year, month, day of month, hour, minute and second
    val split = localeString.split("""[/\s:]""".toRegex())
    //convert all strings to integers
    val dateParts = split.map { it.toInt() }.toIntArray()
    //ensure that the month is 0-indexed
    dateParts[1] = dateParts[1] - 1
    //treat the time-zone specific date as a UTC timestamp
    @ms val zonedTimestamp = Date.UTC(dateParts[0], dateParts[1], dateParts[2], dateParts[3], dateParts[4], dateParts[5], date.getMilliseconds())
    //The time-zone offset is the difference between the UTC-timestamp and the zone-specific timestamp.
    return (timestampRounded - zonedTimestamp)
  }

}
