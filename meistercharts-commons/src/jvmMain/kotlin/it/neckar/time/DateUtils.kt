/**
 * Copyright (C) cedarsoft GmbH.
 *
 * Licensed under the GNU General Public License version 3 (the "License")
 * with Classpath Exception; you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.cedarsoft.org/gpl3ce
 * (GPL 3 with Classpath Exception)
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 3 only, as
 * published by the Free Software Foundation. cedarsoft GmbH designates this
 * particular file as subject to the "Classpath" exception as provided
 * by cedarsoft GmbH in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 3 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 3 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact cedarsoft GmbH, 72810 Gomaringen, Germany,
 * or visit www.cedarsoft.com if you need additional information or
 * have any questions.
 */
package it.neckar.open.time

import it.neckar.open.unit.si.ms
import it.neckar.open.unit.si.ns
import it.neckar.open.unit.si.s
import org.apache.commons.lang3.StringUtils
import org.apache.commons.lang3.time.DurationFormatUtils
import java.text.NumberFormat
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.OffsetDateTime
import java.time.Period
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.chrono.Chronology
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder
import java.time.format.FormatStyle
import java.time.temporal.Temporal
import java.util.Locale
import java.util.concurrent.TimeUnit

/**
 * Date related utils
 */
object DateUtils {
  /**
   * Pattern to format a duration as HH:mm
   */
  const val PATTERN_HH_MM: String = "HH:mm"

  /**
   * Converts the millis to a local date with the given zone
   *
   * @param millis the milliseconds
   * @return the local date for the given zone id
   */
  fun toLocalDate(millis: Long, zone: ZoneId?): LocalDate {
    return LocalDateTime.ofInstant(Instant.ofEpochMilli(millis), zone).toLocalDate()
  }

  fun toZonedDateTime(millis: Long): ZonedDateTime {
    return toZonedDateTime(millis, ZoneId.systemDefault())
  }

  fun toZonedDateTime(millis: Long, zone: ZoneId?): ZonedDateTime {
    return ZonedDateTime.ofInstant(Instant.ofEpochMilli(millis), zone)
  }

  fun toOffsetDateTime(millis: Long, zone: ZoneId?): OffsetDateTime {
    return OffsetDateTime.ofInstant(Instant.ofEpochMilli(millis), zone)
  }

  fun formatDurationWords(duration: Duration): String {
    return formatDurationWords(duration.toMillis())
  }

  /**
   * Interpret the millis as duration and format them words
   */
  fun formatDurationWords(millis: @ms Long): String {
    return formatDurationWords(millis, Locale.getDefault())
  }

  fun formatDurationWords(millis: @ms Long, language: Locale): String {
    return formatDurationWords(millis, DurationI18n[language])
  }

  private fun formatDurationWords(
    durationMillis: @ms Long,
    durationI18n: DurationI18n,
  ): String {
    if (durationMillis < 0) return "$durationMillis ms"
    if (durationMillis == 0L) {
      return "0 seconds"
    }

    val durationStr = StringBuilder(
      DurationFormatUtils.formatDuration(
        durationMillis,
        "d' ${durationI18n.daysString} 'H' ${durationI18n.hoursString} 'm' ${durationI18n.minutesString} 's' ${durationI18n.secondsString}'"
      )
    )

    fun StringBuilder.replaceOnce(toReplace: String, replacement: String) =
      StringUtils.replaceOnce(this.toString(), toReplace, replacement).let {
        this.setLength(0)
        this.append(it)
      }

    fun StringBuilder.replaceZeros() {
      listOf(
        durationI18n.daysString,
        durationI18n.hoursString,
        durationI18n.minutesString,
        durationI18n.secondsString
      ).forEach { unit ->
        replaceOnce(" 0 $unit", "")
      }
    }

    fun StringBuilder.handlePlurals() {
      replaceOnce(" 1 ${durationI18n.secondsString}", " 1 ${durationI18n.secondString}")
      replaceOnce(" 1 ${durationI18n.minutesString}", " 1 ${durationI18n.minuteString}")
      replaceOnce(" 1 ${durationI18n.hoursString}", " 1 ${durationI18n.hourString}")
      replaceOnce(" 1 ${durationI18n.daysString}", " 1 ${durationI18n.dayString}")
    }

    durationStr.insert(0, " ")
    durationStr.replaceZeros()
    durationStr.handlePlurals()

    return durationStr.trim().toString()
  }

  //
  //private fun formatDurationWords(durationMillis: Long,  durationI18n: DurationI18n): String {
  //  if (durationMillis < 0) {
  //    return "$durationMillis ms"
  //  }
  //
  //  // This method is generally replaceable by the format method, but
  //  // there are a series of tweaks and special cases that require
  //  // trickery to replicate.
  //  var duration = DurationFormatUtils.formatDuration(durationMillis, "d' " + durationI18n.daysString + " 'H' " + durationI18n.hoursString + " 'm' " + durationI18n.minutesString + " 's' " + durationI18n.secondsString + "'")
  //  run {
  //
  //    // this is a temporary marker on the front. Like ^ in regexp.
  //    duration = " $duration"
  //    var tmp = StringUtils.replaceOnce(duration, " 0 " + durationI18n.daysString, StringUtils.EMPTY)
  //    if (tmp.length != duration.length) {
  //      duration = tmp
  //      tmp = StringUtils.replaceOnce(duration, " 0 " + durationI18n.hoursString, StringUtils.EMPTY)
  //      if (tmp.length != duration.length) {
  //        duration = tmp
  //        tmp = StringUtils.replaceOnce(duration, " 0 " + durationI18n.minutesString, StringUtils.EMPTY)
  //        duration = tmp
  //        if (tmp.length != duration.length) {
  //          duration = StringUtils.replaceOnce(tmp, " 0 " + durationI18n.secondsString, StringUtils.EMPTY)
  //        }
  //      }
  //    }
  //    if (!duration.isEmpty()) {
  //      // strip the space off again
  //      duration = duration.substring(1)
  //    }
  //  }
  //  var tmp = StringUtils.replaceOnce(duration, " 0 " + durationI18n.secondsString, StringUtils.EMPTY)
  //  if (tmp.length != duration.length) {
  //    duration = tmp
  //    tmp = StringUtils.replaceOnce(duration, " 0 " + durationI18n.minutesString, StringUtils.EMPTY)
  //    if (tmp.length != duration.length) {
  //      duration = tmp
  //      tmp = StringUtils.replaceOnce(duration, " 0 " + durationI18n.hoursString, StringUtils.EMPTY)
  //      if (tmp.length != duration.length) {
  //        duration = StringUtils.replaceOnce(tmp, " 0 " + durationI18n.daysString, StringUtils.EMPTY)
  //      }
  //    }
  //  }
  //  // handle plurals
  //  duration = " $duration"
  //  duration = StringUtils.replaceOnce(duration, " 1 " + durationI18n.secondsString, " 1 " + durationI18n.secondString)
  //  duration = StringUtils.replaceOnce(duration, " 1 " + durationI18n.minutesString, " 1 " + durationI18n.minuteString)
  //  duration = StringUtils.replaceOnce(duration, " 1 " + durationI18n.hoursString, " 1 " + durationI18n.hourString)
  //  duration = StringUtils.replaceOnce(duration, " 1 " + durationI18n.hoursString, " 1 " + durationI18n.dayString)
  //  return duration.trim { it <= ' ' }
  //}

  /**
   * Formats hour, minute, seconds, millis
   */
  fun formatDurationHHmmSSmmm(millis: @ms Long): String {
    return DurationFormatUtils.formatDurationHMS(millis)
  }

  /**
   * Formats hour, minute, seconds
   */

  fun formatHMS(millis: @ms Long): String {
    return DurationFormatUtils.formatDuration(millis, "HH:mm:ss")
  }

  fun asWeeksAndDays(period: Period): String {
    val days = period.days % 7
    val weeks = period.days / 7
    return "$weeks weeks, $days days"
  }

  fun formatHHmm(time: LocalTime?): String {
    return DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT).format(time)
  }

  private const val SECONDS_PER_HOUR: @s Long = (60 * 60).toLong()
  private const val SECONDS_PER_MINUTE: @s Long = 60

  fun formatDurationWordsWithSeconds(duration: Duration): String {
    return formatDurationWordsWithSeconds(duration.toMillis())
  }

  fun formatDurationWordsWithSeconds(millis: @ms Long): String {
    val sb = StringBuilder()
    var seconds: @s Long = millis / 1000 // skip milliseconds
    if (seconds >= SECONDS_PER_HOUR || sb.isNotEmpty()) {
      if (sb.isNotEmpty()) {
        sb.append(" ")
      }
      val hours = seconds / SECONDS_PER_HOUR
      sb.append(NumberFormat.getNumberInstance().format(hours)).append("h")
      seconds -= hours * SECONDS_PER_HOUR
    }
    val twoDigitsFormat = NumberFormat.getNumberInstance()
    //always use two digits if the hour has been set
    if (sb.length > 0) {
      twoDigitsFormat.setMinimumIntegerDigits(2)
    }
    if (seconds >= SECONDS_PER_MINUTE || sb.length > 0) {
      if (sb.length > 0) {
        sb.append(" ")
      }
      val minutes = seconds / SECONDS_PER_MINUTE
      sb.append(twoDigitsFormat.format(minutes)).append("min")
      seconds -= minutes * SECONDS_PER_MINUTE
    }
    if (sb.length > 0) {
      sb.append(" ")
    }
    //always use two digits if the minute has been set
    if (sb.length > 0) {
      twoDigitsFormat.setMinimumIntegerDigits(2)
    }
    sb.append(twoDigitsFormat.format(seconds))
    sb.append("s")
    return sb.toString()
  }

  /**
   * Returns all nanos from instant
   *
   * @param instant instant to convert in nanos
   * @return nanos from epoch
   */
  fun toNanos(instant: Instant): @ns Long {
    return TimeUnit.SECONDS.toNanos(instant.epochSecond) + instant.nano
  }

  fun formatDurationHHmm(duration: Duration): String {
    return DurationFormatUtils.formatDuration(duration.toMillis(), PATTERN_HH_MM)
  }

  fun parseDurationHHmm(formatted: String): Duration {
    val index = formatted.indexOf(':')
    require(index >= 0) { "Could not parse <$formatted>" }
    val firstPart = formatted.substring(0, index)
    val secondPart = formatted.substring(index + 1)
    return Duration
      .ofHours(firstPart.toLong())
      .plusMinutes(secondPart.toLong())
  }

  /**
   * Formats a local date or local date time format
   */
  fun formatLocalDateAndOrTime(temporal: Temporal?): String {
    return if (temporal is LocalDateTime) {
      DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT).format(temporal)
    } else DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT).format(temporal)
  }

  /**
   * Returns a date time formatter that contains the milliseconds format
   */

  fun createTimeMillisFormat(locale: Locale): DateTimeFormatter {
    return createMillisFormat(locale, null, FormatStyle.MEDIUM)
  }

  fun createDateTimeMillisFormat(locale: Locale): DateTimeFormatter {
    return createMillisFormat(locale, FormatStyle.MEDIUM, FormatStyle.MEDIUM)
  }

  fun createDateTimeShortMillisFormat(locale: Locale): DateTimeFormatter {
    return createMillisFormat(locale, FormatStyle.SHORT, FormatStyle.MEDIUM)
  }

  private fun createMillisFormat(locale: Locale, dateStyle: FormatStyle?, timeStyle: FormatStyle?): DateTimeFormatter {
    val chronology = Chronology.ofLocale(locale)
    val pattern = DateTimeFormatterBuilder.getLocalizedDateTimePattern(dateStyle, timeStyle, chronology, locale).replace(":ss", ":ss.SSS")
    return DateTimeFormatter.ofPattern(pattern, locale)
  }
}
