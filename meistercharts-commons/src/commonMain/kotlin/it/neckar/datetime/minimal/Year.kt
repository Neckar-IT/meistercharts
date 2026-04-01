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
