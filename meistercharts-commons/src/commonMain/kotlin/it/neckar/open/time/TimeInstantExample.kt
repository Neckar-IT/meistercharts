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
package it.neckar.open.time

import kotlin.time.Instant

/**
 * Contains examples for time in milliseconds (as doubles)
 */
@Suppress("ObjectPropertyName")
object TimeInstantExample {
  val _2020_05_21_15_00_41_500: Instant = TimeMillisExample._2020_05_21_15_00_41_500.toInstant()

  val _2023_11_15_12_00_00_000: Instant = TimeMillisExample._2023_11_15_12_00_00_000.toInstant()

  val _2019_01_07_15_45_19_123_987_345_678_901: Instant = TimeMillisExample._2019_01_07_15_45_19_123_987_345_678_901.toInstant().also {
    requireMatchesFormattedUtc(it, "2019-01-07T15:45:19.123Z")
  }

  val _2022_07_29_09_38_04_141Z: Instant = TimeMillisExample._2022_07_29_09_38_04_141Z.toInstant().also {
    requireMatchesFormattedUtc(it, "2022-07-29T07:38:04.141Z")
  }

  val _2019_01_07_10_11_59_123Z: Instant = TimeMillisExample._2019_01_07_10_11_59_123Z.toInstant().also {
    requireMatchesFormattedUtc(it, "2019-01-07T10:11:59.123Z")
  }
  val _2020_05_21_15_00_41_500Z: Instant = TimeMillisExample._2020_05_21_15_00_41_500Z.toInstant().also {
    requireMatchesFormattedUtc(it, "2020-05-21T15:00:41.500Z")
  }
  val _2024_09_12_06_44_17_500Z: Instant = TimeMillisExample._2024_09_12_06_44_17_500Z.toInstant().also {
    requireMatchesFormattedUtc(it, "2024-09-12T06:44:17.500Z")
  }
  val _2026_04_24_03_10_00_000Z: Instant = TimeMillisExample._2026_04_24_03_10_00_000Z.toInstant().also {
    requireMatchesFormattedUtc(it, "2026-04-24T03:10:00.000Z")
  }
  val _2026_09_30_17_38_12_123Z: Instant = TimeMillisExample._2026_09_30_17_38_12_123Z.toInstant().also {
    requireMatchesFormattedUtc(it, "2026-09-30T17:38:12.000Z")
  }
  init {
    require(_2020_05_21_15_00_41_500.formatUtcForDebug() == "2020-05-21T15:00:41.500Z")
    require(_2019_01_07_15_45_19_123_987_345_678_901.formatUtcForDebug() == "2019-01-07T15:45:19.123Z")
    require(_2022_07_29_09_38_04_141Z.formatUtcForDebug() == "2022-07-29T07:38:04.141Z")
    require(_2019_01_07_10_11_59_123Z.formatUtcForDebug() == "2019-01-07T10:11:59.123Z")
    require(_2020_05_21_15_00_41_500Z.formatUtcForDebug() == "2020-05-21T15:00:41.500Z")
    require(_2024_09_12_06_44_17_500Z.formatUtcForDebug() == "2024-09-12T06:44:17.500Z")
    require(_2026_04_24_03_10_00_000Z.formatUtcForDebug() == "2026-04-24T03:10:00.000Z")
    require(_2026_09_30_17_38_12_123Z.formatUtcForDebug() == "2026-09-30T17:38:12.000Z")
  }

  val entries: List<Instant> = listOf(
    _2020_05_21_15_00_41_500,
    _2019_01_07_15_45_19_123_987_345_678_901,
    _2022_07_29_09_38_04_141Z,
    _2019_01_07_10_11_59_123Z,
    _2020_05_21_15_00_41_500Z,
    _2024_09_12_06_44_17_500Z,
    _2026_04_24_03_10_00_000Z,
    _2026_09_30_17_38_12_123Z,
  )

  /**
   * Helper method to verify the formatted UTC string
   */
  private fun requireMatchesFormattedUtc(timeInMillis: Instant, expectedFormattedUtcString: String) {
    require(timeInMillis.formatUtcForDebug() == expectedFormattedUtcString) {
      "Expected <$expectedFormattedUtcString> but was <${timeInMillis.formatUtcForDebug()}>"
    }
  }
}
