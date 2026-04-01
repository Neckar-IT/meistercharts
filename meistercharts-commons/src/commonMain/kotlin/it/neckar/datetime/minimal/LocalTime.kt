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
 * Represents a local time
 */
data class LocalTime(
  val hour: Int,
  val minute: Int,
  val second: Int,
  val millis: Int = 0, //JS Date does not support nanoseconds, therefore we stick with millis
) : Comparable<LocalTime> {

  init {
    require(hour in 0..23) { "Hour must be between 0 and 23" }
    require(minute in 0..59) { "Minute must be between 0 and 59" }
    require(second in 0..59) { "Second must be between 0 and 59" }
    require(millis in 0..999) { "Millis must be between 0 and 1000.0" }
  }

  fun format(): String {
    val formattedHour = hour.toString().padStart(2, '0')
    val formattedMinute = minute.toString().padStart(2, '0')
    val formattedSecond = second.toString().padStart(2, '0')
    val formattedMillis = millis.toString().padEnd(3, '0')

    return "$formattedHour:$formattedMinute:$formattedSecond.$formattedMillis"
  }

  /**
   * Creates a new instance at the start of the current hour
   */
  fun startStartOfHour(): LocalTime {
    return LocalTime(hour, 0, 0, 0)
  }

  fun startStartOfHour(hour: Int): LocalTime {
    return LocalTime(hour, 0, 0, 0)
  }

  fun plusHours(hoursToAdd: Int): LocalTime {
    val newHour = (hour + hoursToAdd) % 24
    return LocalTime(newHour, minute, second, millis)
  }

  fun plusMinutes(minutesToAdd: Int): LocalTime {
    val totalMinutes = hour * 60 + minute + minutesToAdd
    val newHour = (totalMinutes / 60) % 24
    val newMinute = totalMinutes % 60
    return LocalTime(newHour, newMinute, second, millis)
  }

  fun plusSeconds(secondsToAdd: Int): LocalTime {
    val totalSeconds = (hour * 3600) + (minute * 60) + second + secondsToAdd
    val newHour = (totalSeconds / 3600) % 24
    val newMinute = (totalSeconds % 3600) / 60
    val newSecond = totalSeconds % 60
    return LocalTime(newHour, newMinute, newSecond, millis)
  }

  override fun compareTo(other: LocalTime): Int {
    return when {
      this.hour != other.hour -> this.hour - other.hour
      this.minute != other.minute -> this.minute - other.minute
      this.second != other.second -> this.second - other.second
      else -> this.millis.compareTo(other.millis)
    }
  }

  override fun toString(): String {
    return format()
  }

  companion object {
  }
}

expect fun LocalTime.Companion.fromMillisCurrentTimeZone(millis: @ms Double, expectedTimeZone: TimeZone): LocalTime
