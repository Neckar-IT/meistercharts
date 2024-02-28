package it.neckar.datetime.minimal

import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

/**
 * Represents a year
 */
@JvmInline
@Serializable
value class Year(val value: Int) : Comparable<Year> {
  /**
   * Returns true if this year is a leap year.
   *
   * ATTENTION: This is a very basic implementation that only works for "normal" values.
   * We ignore the introduction of leap years in 1582. And assume these have existed for all the time
   */
  fun isLeapYear(): Boolean {
    return (value % 4 == 0 && value % 100 != 0) || (value % 400 == 0)
  }

  override fun compareTo(other: Year): Int {
    return value.compareTo(other.value)
  }

  operator fun plus(n: Int): Year {
    return Year(value + n)
  }

  operator fun minus(other: Year): Year {
    return Year(value - other.value)
  }

  operator fun minus(other: Int): Year {
    return Year(value - other)
  }

  override fun toString(): String {
    return value.toString()
  }
}
