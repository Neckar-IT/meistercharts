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

import java.util.Locale
import javax.annotation.Nonnull

/**
 * Contains the strings for duration formatting
 *
 */
enum class DurationI18n(
  val language: Locale,
  val daysString: String,
  val hoursString: String,
  val minutesString: String,
  val secondsString: String,
  val dayString: String,
  val hourString: String,
  val minuteString: String,
  val secondString: String,
) {
  ENGLISH(Locale.ENGLISH, "days", "hours", "minutes", "seconds", "day", "hour", "minute", "second"),
  GERMAN(Locale.GERMAN, "Tage", "Stunden", "Minuten", "Sekunden", "Tag", "Stunde", "Minute", "Sekunde");

  companion object {
    /**
     * Returns the best duration i18n for the given language. Will return [.ENGLISH] as fallback
     */
    @Nonnull
    operator fun get(language: Locale): DurationI18n {
      return entries.firstOrNull { it.language.language == language.language }
        ?: ENGLISH
    }
  }
}
