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
import kotlin.streams.toList

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
