/*
 * Copyright (C) 2013-2026 Neckar IT GmbH, Mössingen, Germany
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Linking this library statically or dynamically with other modules is
 * making a combined work based on this library. Thus, the terms and
 * conditions of the GNU General Public License cover the whole combination.
 *
 * As a special exception, the copyright holders of this library give you
 * permission to link this library with independent modules, regardless of
 * the license terms of these independent modules, and to copy and distribute
 * the resulting combined work under terms of your choice, provided that every
 * copy of the combined work is accompanied by a complete copy of the source
 * code of this library.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
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

  fun format(): String {
    return when (value) {
      1 -> "Januar"
      2 -> "Februar"
      3 -> "März"
      4 -> "April"
      5 -> "Mai"
      6 -> "Juni"
      7 -> "Juli"
      8 -> "August"
      9 -> "September"
      10 -> "Oktober"
      11 -> "November"
      12 -> "Dezember"
      else -> throw IllegalArgumentException("Invalid month: $this")
    }
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
