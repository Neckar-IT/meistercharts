package it.neckar.open.time

import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

/**
 * Represents a year
 */
//@JvmInline
@Serializable
@Deprecated("use [com.soywiz.klock.Year] year instead")
@JvmInline
value class Year(val year: Int) : Comparable<Year> {
  override operator fun compareTo(other: Year): Int {
    return year.compareTo(other.year)
  }

  override fun toString(): String {
    return "$year"
  }

  /**
   * Adds the given number
   */
  operator fun plus(additionalYears: Int): Year {
    return Year(year + additionalYears)
  }

  @Suppress("ObjectPropertyName")
  companion object {
    val _2020: Year = Year(2020)
    val _2021: Year = Year(2021)
    val _2022: Year = Year(2022)
    val _2023: Year = Year(2023)
    val _2024: Year = Year(2024)
    val _2025: Year = Year(2025)

    fun of(year: Int): Year {
      return Year(year)
    }
  }
}
