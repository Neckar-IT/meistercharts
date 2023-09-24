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
