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
package com.meistercharts.algorithms.axis

import com.meistercharts.algorithms.time.timeZoneOffsetProvider
import it.neckar.open.time.TimeZone
import it.neckar.open.unit.si.ms
import com.soywiz.klock.DateTime
import com.soywiz.klock.DateTimeTz
import com.soywiz.klock.TimezoneOffset

/**
 *
 */

/**
 * Const values
 */

/**
 * The smallest supported timestamp (01-01-01T00:00:00.000) by the klock library
 */
const val klockSmallestSupportedTimestamp: @ms Double = -62135596800000.0

/**
 * The greatest supported timestamp (9999-12-31T23:59:59.999) by the klock library
 */
const val klockGreatestSupportedTimestamp: @ms Double = 253402300799999.0


/**
 * Returns true if this is within the supported range of klock timestamps
 */
fun Double.isInKlockSupportedRange(): Boolean {
  return this in klockSmallestSupportedTimestamp..klockGreatestSupportedTimestamp
}

/**
 * Converts this [DateTime] instance to an [DateTimeTz] instance associated with the given [timeZone].
 *
 * This is interpreted as *local* time
 */
fun DateTime.local2DateTimeTz(timeZone: TimeZone): DateTimeTz {
  return DateTimeTz.local(this, TimezoneOffset(timeZoneOffsetProvider.timeZoneOffset(this.unixMillisDouble, timeZone)))
}

/**
 * Converts this [DateTime] instance to an [DateTimeTz] instance associated with the given [timeZone].
 *
 * This is interpreted as *UTC* time
 */
fun DateTime.utc2DateTimeTz(timeZone: TimeZone): DateTimeTz {
  return DateTimeTz.utc(this, TimezoneOffset(timeZoneOffsetProvider.timeZoneOffset(this.unixMillisDouble, timeZone)))
}

/**
 * Returns the minutes of the day
 */
val DateTimeTz.minutesOfDay: Int
  get() {
    return hours * 60 + minutes
  }

/**
 * Returns the seconds of the day
 */
val DateTimeTz.secondsOfDay: Int
  get() {
    return hours * 60 * 60 + minutes * 60 + seconds
  }
