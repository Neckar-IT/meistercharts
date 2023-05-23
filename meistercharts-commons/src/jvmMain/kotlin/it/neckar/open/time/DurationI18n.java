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

import javax.annotation.Nonnull;
import java.util.Locale;

/**
 * Contains the strings for duration formatting
 *
 */
public enum DurationI18n {
  ENGLISH(Locale.ENGLISH, "days", "hours", "minutes", "seconds", "day", "hour", "minute", "second"),
  GERMAN(Locale.GERMAN, "Tage", "Stunden", "Minuten", "Sekunden", "Tag", "Stunde", "Minute", "Sekunde");

  @Nonnull
  private final Locale language;

  @Nonnull
  private final String daysString;
  @Nonnull
  private final String hoursString;
  @Nonnull
  private final String minutesString;
  @Nonnull
  private final String secondsString;

  @Nonnull
  private final String dayString;
  @Nonnull
  private final String hourString;
  @Nonnull
  private final String minuteString;
  @Nonnull
  private final String secondString;

  /**
   * @noinspection ConstructorWithTooManyParameters
   */
  DurationI18n(@Nonnull Locale language, @Nonnull String daysString, @Nonnull String hoursString, @Nonnull String minutesString, @Nonnull String secondsString, @Nonnull String dayString, @Nonnull String hourString, @Nonnull String minuteString, @Nonnull String secondString) {
    this.language = language;
    this.daysString = daysString;
    this.hoursString = hoursString;
    this.minutesString = minutesString;
    this.secondsString = secondsString;
    this.dayString = dayString;
    this.hourString = hourString;
    this.minuteString = minuteString;
    this.secondString = secondString;
  }

  @Nonnull
  public String getDaysString() {
    return daysString;
  }

  @Nonnull
  public String getHoursString() {
    return hoursString;
  }

  @Nonnull
  public String getMinutesString() {
    return minutesString;
  }

  @Nonnull
  public String getSecondsString() {
    return secondsString;
  }

  @Nonnull
  public String getDayString() {
    return dayString;
  }

  @Nonnull
  public String getHourString() {
    return hourString;
  }

  @Nonnull
  public String getMinuteString() {
    return minuteString;
  }

  @Nonnull
  public String getSecondString() {
    return secondString;
  }

  @Nonnull
  public Locale getLanguage() {
    return language;
  }

  /**
   * Returns the best duration i18n for the given language. Will return {@link #ENGLISH} as fallback
   */
  @Nonnull
  public static DurationI18n get(@Nonnull Locale language) {
    for (DurationI18n durationI18n : values()) {
      if (durationI18n.getLanguage().getLanguage().equals(language.getLanguage())) {
        return durationI18n;
      }
    }

    return ENGLISH;
  }
}
