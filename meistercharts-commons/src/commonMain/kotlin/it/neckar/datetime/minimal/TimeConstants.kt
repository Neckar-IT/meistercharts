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

import it.neckar.open.unit.other.Approximation
import it.neckar.open.unit.si.ms

object TimeConstants {
  /**
   * A timestamp that may serve as a reference point in time.
   *
   * 2001-09-09T01:46:40.000
   */
  const val referenceTimestamp: @ms Double = 1.7040672E12 //Beware that changing this constant may break pixel-related regression tests!

  /**
   * Milli seconds per second
   */
  const val millisPerSecond: @ms Double = 1000.0

  /**
   * Number of milliseconds in a standard minute.
   */
  const val millisPerMinute: @ms Double = 60 * millisPerSecond

  /**
   * Number of milliseconds in a standard hour.
   */
  const val millisPerHour: @ms Double = 60 * millisPerMinute

  /**
   * Number of milliseconds in a standard day.
   */
  const val millisPerDay: @ms Double = 24 * millisPerHour

  const val millisPerYear: @ms Double = 365 * millisPerDay

  @Approximation
  val millisPerDecade: @ms Double = 10 * millisPerYear

  /**
   * Number of milliseconds in a century - approximation.
   */

  @Approximation
  val millisPerCentury: @ms Double = 10 * millisPerDecade
}
