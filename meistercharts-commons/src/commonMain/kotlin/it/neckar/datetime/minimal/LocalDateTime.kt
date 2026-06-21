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

import it.neckar.open.unit.si.ms

/**
 * Represents a local date + time
 */
data class LocalDateTime(
  val date: LocalDate,
  val time: LocalTime,
) : Comparable<LocalDateTime> {

  inline val year: Year
    get() {
      return date.year
    }
  inline val month: Month
    get() {
      return date.month
    }

  inline val dayOfMonth: DayOfMonth
    get() {
      return date.dayOfMonth
    }

  inline val hour: Int
    get() {
      return time.hour
    }

  inline val minute: Int
    get() {
      return time.minute
    }

  inline val second: Int
    get() {
      return time.second
    }

  inline val millis: Int
    get() {
      return time.millis
    }

  override fun compareTo(other: LocalDateTime): Int {
    return when {
      this.date != other.date -> this.date.compareTo(other.date)
      this.time != other.time -> this.time.compareTo(other.time)
      else -> 0
    }
  }

  /**
   * Creates a new instance at the start of the current hour
   */
  fun atStartOfHour(): LocalDateTime {
    return LocalDateTime(date, time.startOfHour())
  }

  /**
   * Creates a new instance at the start of the provided hour
   */
  fun atStartOfHour(hour: Int): LocalDateTime {
    return LocalDateTime(date, time.startOfHour(hour))
  }

  fun format(): String {
    return "${date.format()}T${time.format()}"
  }

  override fun toString(): String {
    return format()
  }

  companion object {
    /**
     * Creates a new instance
     */
    fun of(
      year: Int, month: Int, day: Int,
      hour: Int, minute: Int, second: Int, millis: Int = 0,
    ): LocalDateTime {
      return LocalDateTime(
        LocalDate(year, month, day),
        LocalTime(hour, minute, second, millis)
      )
    }
  }
}

expect fun LocalDateTime.Companion.fromMillisCurrentTimeZone(millis: @ms Double, expectedTimeZone: TimeZone): LocalDateTime

expect fun LocalDateTime.toMillis(timeZone: TimeZone): @ms Double
