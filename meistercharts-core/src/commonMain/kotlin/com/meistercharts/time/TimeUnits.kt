package com.meistercharts.time

import it.neckar.open.unit.si.ms

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
    fun getTimeUnitsAscending(): Array<TimeUnits> = values()

    fun getTimeUnitsDescending(): Array<TimeUnits> = getTimeUnitsAscending().reversedArray()

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
