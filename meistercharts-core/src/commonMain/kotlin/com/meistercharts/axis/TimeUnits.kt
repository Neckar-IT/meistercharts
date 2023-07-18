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
package com.meistercharts.axis

import it.neckar.open.unit.si.ms

/**
 * The time units used for timeline axis
 */
enum class TimeUnits(@ms val span: Double, val unit: String) {
  Microsecond(0.001, "μs"),
  Millisecond(1.0, "ms"),
  Second(1_000.0, "s"),
  Minute(60 * Second.span, "min"),
  Hour(60 * Minute.span, "h"),
  Day(24 * Hour.span, "d"),
  Week(7 * Day.span, "week"),
  Month(30 * Day.span, "M"),
  Year(365 * Day.span, "y");

  companion object {
    fun getTimeUnitsAscending(): List<TimeUnits> = entries

    fun getTimeUnitsDescending(): List<TimeUnits> = entries.reversed()

    /**
     * Get the largest [TimeUnits] whose span is the same ore less than [span].
     * Returns the largest [TimeUnits] if there is no [TimeUnits] whose span is the same or less than [span].
     */
    fun sameOrLessThan(@ms span: Double): TimeUnits {
      val timeUnits = getTimeUnitsDescending()
      for (timeUnit in timeUnits) {
        if (timeUnit.span <= span) {
          return timeUnit
        }
      }
      return timeUnits.last()
    }

    /**
     * Get the smallest [TimeUnits] whose span is the same or greater than [span].
     * Returns the smallest [TimeUnits] if there is no [TimeUnits] whose span is the same or greater than [span].
     */
    fun sameOrGreaterThan(@ms span: Double): TimeUnits {
      val timeUnits = getTimeUnitsAscending()
      for (timeUnit in timeUnits) {
        if (timeUnit.span >= span) {
          return timeUnit
        }
      }
      return timeUnits.last()
    }
  }
}
