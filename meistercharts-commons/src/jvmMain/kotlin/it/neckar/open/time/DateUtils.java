/**
 * Copyright (C) cedarsoft GmbH.
 *
 * Licensed under the GNU General Public License version 3 (the "License")
 * with Classpath Exception; you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 *         http://www.cedarsoft.org/gpl3ce
 *         (GPL 3 with Classpath Exception)
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
package it.neckar.open.time;

import static org.apache.commons.lang3.time.DurationFormatUtils.formatDuration;

import java.text.NumberFormat;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.Period;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.chrono.Chronology;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.FormatStyle;
import java.time.temporal.Temporal;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.jetbrains.annotations.NotNull;

import it.neckar.open.unit.si.ms;
import it.neckar.open.unit.si.ns;
import it.neckar.open.unit.si.s;

/**
 * Date related utils
 */
public class DateUtils {
  /**
   * Pattern to format a duration as HH:mm
   */
  @Nonnull
  public static final String PATTERN_HH_MM = "HH:mm";

  /**
   * Converts the millis to a local date with the given zone
   *
   * @param millis the milli seconds
   * @return the local date for the given zone id
   */
  @Nonnull
  public static LocalDate toLocalDate(long millis, @Nonnull ZoneId zone) {
    return LocalDateTime.ofInstant(Instant.ofEpochMilli(millis), zone).toLocalDate();
  }

  @Nonnull
  public static ZonedDateTime toZonedDateTime(long millis) {
    return toZonedDateTime(millis, ZoneId.systemDefault());
  }

  @Nonnull
  public static ZonedDateTime toZonedDateTime(long millis, @Nonnull ZoneId zone) {
    return ZonedDateTime.ofInstant(Instant.ofEpochMilli(millis), zone);
  }

  @Nonnull
  public static OffsetDateTime toOffsetDateTime(long millis, @Nonnull ZoneId zone) {
    return OffsetDateTime.ofInstant(Instant.ofEpochMilli(millis), zone);
  }

  @Nonnull
  public static String formatDurationWords(@Nonnull Duration duration) {
    return formatDurationWords(duration.toMillis());
  }

  /**
   * Interpret the millis as duration and format them words
   */
  @Nonnull
  public static String formatDurationWords(@ms long millis) {
    return formatDurationWords(millis, Locale.getDefault());
  }

  @Nonnull
  public static String formatDurationWords(@ms long millis, @Nonnull Locale language) {
    return formatDurationWords(millis, DurationI18n.get(language));
  }

  @Nonnull
  private static String formatDurationWords(final long durationMillis, @Nonnull DurationI18n durationI18n) {
    if (durationMillis < 0) {
      return durationMillis + " ms";
    }

    // This method is generally replaceable by the format method, but
    // there are a series of tweaks and special cases that require
    // trickery to replicate.
    String duration = formatDuration(durationMillis, "d' " + durationI18n.getDaysString() + " 'H' " + durationI18n.getHoursString() + " 'm' " + durationI18n.getMinutesString() + " 's' " + durationI18n.getSecondsString() + "'");
    {
      // this is a temporary marker on the front. Like ^ in regexp.
      duration = " " + duration;
      String tmp = StringUtils.replaceOnce(duration, " 0 " + durationI18n.getDaysString(), StringUtils.EMPTY);
      if (tmp.length() != duration.length()) {
        duration = tmp;
        tmp = StringUtils.replaceOnce(duration, " 0 " + durationI18n.getHoursString(), StringUtils.EMPTY);
        if (tmp.length() != duration.length()) {
          duration = tmp;
          tmp = StringUtils.replaceOnce(duration, " 0 " + durationI18n.getMinutesString(), StringUtils.EMPTY);
          duration = tmp;
          if (tmp.length() != duration.length()) {
            duration = StringUtils.replaceOnce(tmp, " 0 " + durationI18n.getSecondsString(), StringUtils.EMPTY);
          }
        }
      }
      if (!duration.isEmpty()) {
        // strip the space off again
        duration = duration.substring(1);
      }
    }
    String tmp = StringUtils.replaceOnce(duration, " 0 " + durationI18n.getSecondsString(), StringUtils.EMPTY);
    if (tmp.length() != duration.length()) {
      duration = tmp;
      tmp = StringUtils.replaceOnce(duration, " 0 " + durationI18n.getMinutesString(), StringUtils.EMPTY);
      if (tmp.length() != duration.length()) {
        duration = tmp;
        tmp = StringUtils.replaceOnce(duration, " 0 " + durationI18n.getHoursString(), StringUtils.EMPTY);
        if (tmp.length() != duration.length()) {
          duration = StringUtils.replaceOnce(tmp, " 0 " + durationI18n.getDaysString(), StringUtils.EMPTY);
        }
      }
    }
    // handle plurals
    duration = " " + duration;
    duration = StringUtils.replaceOnce(duration, " 1 " + durationI18n.getSecondsString(), " 1 " + durationI18n.getSecondString());
    duration = StringUtils.replaceOnce(duration, " 1 " + durationI18n.getMinutesString(), " 1 " + durationI18n.getMinuteString());
    duration = StringUtils.replaceOnce(duration, " 1 " + durationI18n.getHoursString(), " 1 " + durationI18n.getHourString());
    duration = StringUtils.replaceOnce(duration, " 1 " + durationI18n.getHoursString(), " 1 " + durationI18n.getDayString());
    return duration.trim();
  }


  /**
   * Formats hour, minute, seconds, millis
   */
  @Nonnull
  public static String formatDurationHHmmSSmmm(@ms long millis) {
    return DurationFormatUtils.formatDurationHMS(millis);
  }

  /**
   * Formats hour, minute, seconds
   */
  @Nonnull
  public static String formatHMS(@ms long millis) {
    return formatDuration(millis, "HH:mm:ss");
  }

  @Nonnull
  public static String asWeeksAndDays(@Nonnull Period period) {
    int days = period.getDays() % 7;
    int weeks = period.getDays() / 7;

    return weeks + " weeks, " + days + " days";
  }

  @Nonnull
  public static String formatHHmm(@Nonnull LocalTime time) {
    return DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT).format(time);
  }

  @s
  private static final long SECONDS_PER_HOUR = 60 * 60;
  @s
  private static final long SECONDS_PER_MINUTE = 60;

  @Nonnull
  public static String formatDurationWordsWithSeconds(@ms Duration duration) {
    return formatDurationWordsWithSeconds(duration.toMillis());
  }

  public static String formatDurationWordsWithSeconds(@ms long millis) {
    StringBuilder sb = new StringBuilder();

    @s long seconds = millis / 1000; // skip milliseconds

    if (seconds >= SECONDS_PER_HOUR || sb.length() > 0) {
      if (sb.length() > 0) {
        sb.append(" ");
      }
      long hours = seconds / SECONDS_PER_HOUR;
      sb.append(NumberFormat.getNumberInstance().format(hours)).append("h");
      seconds -= hours * SECONDS_PER_HOUR;
    }

    NumberFormat twoDigitsFormat = NumberFormat.getNumberInstance();
    //always use two digits if the hour has been set
    if (sb.length() > 0) {
      twoDigitsFormat.setMinimumIntegerDigits(2);
    }

    if (seconds >= SECONDS_PER_MINUTE || sb.length() > 0) {
      if (sb.length() > 0) {
        sb.append(" ");
      }
      long minutes = seconds / SECONDS_PER_MINUTE;

      sb.append(twoDigitsFormat.format(minutes)).append("min");
      seconds -= minutes * SECONDS_PER_MINUTE;
    }

    if (sb.length() > 0) {
      sb.append(" ");
    }
    //always use two digits if the minute has been set
    if (sb.length() > 0) {
      twoDigitsFormat.setMinimumIntegerDigits(2);
    }

    sb.append(twoDigitsFormat.format(seconds));
    sb.append("s");

    return sb.toString();
  }

  /**
   * Returns all nanos from instant
   *
   * @param instant instant to convert in nanos
   * @return nanos from epoch
   */
  @ns
  public static long toNanos(@Nonnull Instant instant) {
    return TimeUnit.SECONDS.toNanos(instant.getEpochSecond()) + instant.getNano();
  }

  @Nonnull
  public static String formatDurationHHmm(@Nonnull Duration duration) {
    return formatDuration(duration.toMillis(), PATTERN_HH_MM);
  }

  @Nonnull
  public static Duration parseDurationHHmm(@Nonnull String formatted) {
    int index = formatted.indexOf(':');
    if (index < 0) {
      throw new IllegalArgumentException("Could not parse <" + formatted + ">");
    }

    String firstPart = formatted.substring(0, index);
    String secondPart = formatted.substring(index + 1);

    return Duration
      .ofHours(Long.parseLong(firstPart))
      .plusMinutes(Long.parseLong(secondPart));
  }

  /**
   * Formats a local date or local date time format
   */
  @NotNull
  public static String formatLocalDateAndOrTime(@Nonnull Temporal temporal) {
    if (temporal instanceof LocalDateTime) {
      return DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT).format(temporal);
    }

    return DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT).format(temporal);
  }

  /**
   * Returns a date time formatter that contains the milli seconds format
   */
  @Nonnull
  public static DateTimeFormatter createTimeMillisFormat(@Nonnull Locale locale) {
    return createMillisFormat(locale, null, FormatStyle.MEDIUM);
  }

  @Nonnull
  public static DateTimeFormatter createDateTimeMillisFormat(@Nonnull Locale locale) {
    return createMillisFormat(locale, FormatStyle.MEDIUM, FormatStyle.MEDIUM);
  }

  @Nonnull
  public static DateTimeFormatter createDateTimeShortMillisFormat(@Nonnull Locale locale) {
    return createMillisFormat(locale, FormatStyle.SHORT, FormatStyle.MEDIUM);
  }

  @Nonnull
  private static DateTimeFormatter createMillisFormat(@Nonnull Locale locale, @Nullable FormatStyle dateStyle, @Nullable FormatStyle timeStyle) {
    Chronology chronology = Chronology.ofLocale(locale);
    String pattern = DateTimeFormatterBuilder.getLocalizedDateTimePattern(dateStyle, timeStyle, chronology, locale).replace(":ss", ":ss.SSS");
    return DateTimeFormatter.ofPattern(pattern, locale);
  }
}
