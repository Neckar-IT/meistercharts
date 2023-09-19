package it.neckar.datetime.minimal

import kotlin.jvm.JvmInline

/**
 * Represents a day of a month
 */
@JvmInline
value class DayOfMonth(val value: Int) : Comparable<DayOfMonth> {
  operator fun plus(daysToAdd: Int): DayOfMonth {
    return DayOfMonth(value + daysToAdd)
  }

  fun coerceAtMost(max: DayOfMonth): DayOfMonth {
    return DayOfMonth(value.coerceAtMost(max.value))
  }

  override fun compareTo(other: DayOfMonth): Int {
    return value.compareTo(other.value)
  }

  /**
   * Calculates the day of month that is valid for the year and moth
   */
  fun coerceDayOfMonth(year: Year, month: Month): DayOfMonth {
    return coerceAtMost(month.daysInMonth(year))
  }

  companion object {
    val FirstDay: DayOfMonth = DayOfMonth(1)
  }
}
