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
 * Formats a percentage value using a delegate.
 * Multiplies the given value with 100 and formats the value
 * using the delegate. Appends a percentage sign ("%").
 */
@Deprecated("use asPercentageFormat - as soon as precision has been removed!")
class PercentageFormat(val delegate: NumberFormat) : NumberFormat {
  override fun format(value: Double, i18nConfiguration: I18nConfiguration, whitespaceConfig: WhitespaceConfig): String {
    return delegate.format(value * 100.0, i18nConfiguration, whitespaceConfig) + "${whitespaceConfig.smallSpace}%"
  }

  override val precision: Double
    get() = delegate.precision / 100.0
}

/**
 * Uses this as
 */
fun NumberFormat.asPercentageFormat(): WithUnitFormat {
  return this.withConversion { it * 100.0 }.appendUnit("%")
}
