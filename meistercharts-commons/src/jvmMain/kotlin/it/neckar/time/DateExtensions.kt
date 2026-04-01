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

import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalAdjusters
import java.time.temporal.WeekFields
import java.util.Date
import java.util.Locale
import java.util.stream.Stream
import kotlin.math.abs

/**
 * Contains extension functions for Date related classes
 *
 * Copied from https://github.com/LukasForst/katlib/blob/master/ (MIT License)
 */


/**
 * Returns list of days from [this] date to [to] date (both inclusive).
 */
fun LocalDate.getDateRangeTo(to: LocalDate): List<LocalDate> = this.getDateRangeToAsStream(to).toList()

/**
 * Returns stream of days from [this] date to [to] date (both inclusive).
 */
fun LocalDate.getDateRangeToAsStream(to: LocalDate): Stream<LocalDate> =
  Stream
    .iterate(this) { d -> d.plusDays(1) }
    .limit(this.until(to, ChronoUnit.DAYS) + 1)

/**
 * Returns stream of decreasing days from [this] date to [to] date (both inclusive).
 * This means that [this] date should be later than [to] date
 * to obtain non-empty stream.
 */
fun LocalDate.getInvertedDateRangeToAsStream(to: LocalDate): Stream<LocalDate> =
  Stream
    .iterate(this) { d -> d.minusDays(1) }
    .limit(to.until(this, ChronoUnit.DAYS) + 1)


/**
 * Returns week of year for [this].
 *
 * For such operation, [Locale] of country (therefore one must use [Locale.GERMANY] instead of [Locale.GERMAN]) is required.
 * The default value is set to [Locale.GERMANY] since it uses calendar which starts at Monday.
 */
fun LocalDate.getWeekOfYear(locale: Locale = Locale.GERMANY): Int =
  this.get(WeekFields.of(locale).weekOfYear())

/**
 * Returns [this] LocalDate with week of the year set to [week] and day of the week set to [DayOfWeek.MONDAY].
 * I.e. when [this] date is 23-08-2019 and [week]
 * is 2, the returned date is 07-01-2019.
 *
 * For such operation, [Locale] of country (therefore one must use [Locale.GERMANY] instead of [Locale.GERMAN]) is required.
 * The default value is set to [Locale.GERMANY] since it uses calendar which starts at Monday.
 */
fun LocalDate.setWeekOfYearMonday(week: Int, locale: Locale = Locale.GERMANY): LocalDate = this
  .with(WeekFields.of(locale).weekOfYear(), week.toLong())
  .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))

/**
 * Returns number of days in interval between [this] date and [to] date (both inclusive).
 * This means that this method returns 1 when [this] and [to] are equal.
 */
fun LocalDate.getDaysInInterval(to: LocalDate): Int =
  (ChronoUnit.DAYS.between(this, to).toInt() + 1)
    .takeIf { it > 0 }
    ?: throw IllegalArgumentException("start date $this is smaller than end date $to.")

/**
 * Returns number of days between [this] date and [to] date (exclusive).
 * This means that this method returns 0 when [this] and [to] are equal.
 */
fun LocalDate.getDayDifference(to: LocalDate) =
  ChronoUnit.DAYS.between(this, to).toInt()

/**
 * Convert [Date] to [LocalDate]. [zoneId] parameter sets the zone of the [LocalDate] instance.
 */
fun Date.toLocalDate(zoneId: ZoneId): LocalDate =
  LocalDate.from(Instant.ofEpochMilli(this.time).atZone(zoneId))

/**
 * Convert [Date] to [LocalDate] with zone set to UTC.
 */
fun Date.toUtcLocalDate(): LocalDate = toLocalDate(ZoneId.of("UTC"))


/**
 * Computes duration in milliseconds between two [Instant] instances.
 * */
fun Instant.durationToInMilli(other: Instant): Long = durationInMilli(this, other)

/**
 * Computes duration in milliseconds between two [Instant] instances.
 * */
fun durationInMilli(a: Instant, b: Instant): Long = abs(a.toEpochMilli() - b.toEpochMilli())
