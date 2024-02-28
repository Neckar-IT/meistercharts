package it.neckar.datetime.minimal

import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

/**
 * Represents a month
 */
@JvmInline
@Serializable
value class Month(val value: Int) : Comparable<Month> {
  init {
    require(value in 1..12) {
      "Invalid month: $value"
    }
  }

  /**
   * Returns the day in this month for the provided year
   */
  fun daysInMonth(year: Year): DayOfMonth {
    return when (this.value) {
      1, 3, 5, 7, 8, 10, 12 -> DayOfMonth(31)
      4, 6, 9, 11 -> DayOfMonth(30)
      2 -> if (year.isLeapYear()) DayOfMonth(29) else DayOfMonth(28)
      else -> throw IllegalArgumentException("Invalid month: $this")
    }
  }

  override fun compareTo(other: Month): Int {
    return value.compareTo(other.value)
  }

  /**
   * Returns the amount of days before this month in the provided year.
   *
   * Returns 0 for January.
   */
  fun daysBeforeThisMonth(year: Year): Int {
    return (1 until value).sumOf { Month(it).daysInMonth(year).value }
  }

  companion object {
    val January: Month = Month(1)
    val February: Month = Month(2)
    val March: Month = Month(3)
    val April: Month = Month(4)
    val May: Month = Month(5)
    val June: Month = Month(6)
    val July: Month = Month(7)
    val August: Month = Month(8)
    val September: Month = Month(9)
    val October: Month = Month(10)
    val November: Month = Month(11)
    val December: Month = Month(12)
  }
}
