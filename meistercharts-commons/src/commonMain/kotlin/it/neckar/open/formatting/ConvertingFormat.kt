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

import it.neckar.open.i18n.I18nConfiguration
import it.neckar.open.kotlin.lang.WhitespaceConfig

/**
 * Number format that converts values from one unit into another (e.g. from meter to feet)
 */
class ConvertingFormat(
  /**
   * The delegate that is used to format the converted value
   */
  val delegate: NumberFormat,
  /**
   * Converts the value
   */
  val conversion: (value: Double) -> Double
) : NumberFormat {
  override fun format(value: Double, i18nConfiguration: I18nConfiguration, whitespaceConfig: WhitespaceConfig): String {
    val convertedValue = conversion(value)
    return delegate.format(convertedValue, i18nConfiguration, whitespaceConfig)
  }
}

/**
 * Creates a new number format that converts the given value before using this to format that converted value
 */
fun NumberFormat.withConversion(conversion: (value: Double) -> Double): ConvertingFormat {
  return ConvertingFormat(this, conversion)
}
