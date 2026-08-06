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
package it.neckar.open.formatting

import it.neckar.open.unit.number.PositiveOrZero

/**
 * Inserts [millis] milliseconds into the formatted date [formattedWithoutMillis] which has no milliseconds part yet
 */
internal fun insertMillis(formattedWithoutMillis: String, millis: Int): String {
  // Crude workaround: no browser API formats milliseconds directly, so insert them after the seconds for the common
  // locales that use ':' between hour, minute and second.
  try {
    val firstIndexOfSeparator = formattedWithoutMillis.indexOf(":")
    if (firstIndexOfSeparator != -1) {
      val secondIndexOfSeparator = formattedWithoutMillis.indexOf(":", firstIndexOfSeparator + 1)
      if (secondIndexOfSeparator != -1) {
        var indexAfterLastDigit = secondIndexOfSeparator + 1
        while (indexAfterLastDigit < formattedWithoutMillis.length) {
          if (formattedWithoutMillis[indexAfterLastDigit].isDigit().not()) {
            break
          }
          ++indexAfterLastDigit
        }
        val formattedMillisPart = millis.toString().padStart(3, '0')
        return formattedWithoutMillis.substring(0, indexAfterLastDigit) + '.' + formattedMillisPart + formattedWithoutMillis.substring(indexAfterLastDigit)
      }
    }
  } catch (e: Exception) {
    println("failed to format date <$formattedWithoutMillis> with milliseconds: $e")
  }
  return formattedWithoutMillis
}

/**
 * ATTENTION! This method must only be used for positive values
 */
internal fun @PositiveOrZero Int.formatWithLeadingZeros(length: Int = 2): String {
  return this.toString().padStart(length, '0')
}
