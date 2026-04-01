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
import it.neckar.open.i18n.convert
import it.neckar.open.kotlin.lang.WhitespaceConfig
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import kotlin.math.pow

/**
 * Decimal format implementation for the JVM
 */
actual class DecimalFormat internal actual constructor(
  actual override val maximumFractionDigits: Int,
  actual override val minimumFractionDigits: Int,
  actual override val minimumIntegerDigits: Int,
  actual override val useGrouping: Boolean,
) : NumberFormat, DecimalFormatDescriptor {

  private val numberFormatCache = NumberFormatCache("DecimalFormat(Fractions: min: $minimumFractionDigits, max: $maximumFractionDigits; Min int: $minimumIntegerDigits; grouping: $useGrouping)") {
    this.maximumFractionDigits = this@DecimalFormat.maximumFractionDigits
    this.minimumFractionDigits = this@DecimalFormat.minimumFractionDigits
    this.minimumIntegerDigits = this@DecimalFormat.minimumIntegerDigits
    this.isGroupingUsed = this@DecimalFormat.useGrouping
  }

  actual override fun format(value: Double, i18nConfiguration: I18nConfiguration, whitespaceConfig: WhitespaceConfig): String {
    return numberFormatCache[i18nConfiguration.formatLocale].format(value + 0.0) //add 0.0 to avoid "-0.0"
  }

  override val precision: Double = 10.0.pow(-maximumFractionDigits)
}

actual class ExponentialFormat actual constructor(
  val maximumFractionDigits: Int,
  val minimumFractionDigits: Int,
  val minimumIntegerDigits: Int,
  val useGrouping: Boolean
) : NumberFormat {

  actual override fun format(value: Double, i18nConfiguration: I18nConfiguration, whitespaceConfig: WhitespaceConfig): String {
    //TODO add cache
    return createExponentialFormat(maximumFractionDigits, minimumFractionDigits, minimumIntegerDigits, i18nConfiguration.formatLocale).format(value)
  }

  override val precision: Double = 10.0.pow(-maximumFractionDigits)

  companion object {
    fun createExponentialFormat(
      maximumFractionDigits: Int,
      minimumFractionDigits: Int,
      minimumIntegerDigits: Int,
      locale: it.neckar.open.i18n.Locale
    ): DecimalFormat {
      require(maximumFractionDigits > -1)
      require(minimumFractionDigits > -1)
      require(minimumIntegerDigits > -1)

      val formatSymbols = DecimalFormatSymbols.getInstance(locale.convert()).also { it.exponentSeparator = "e" }
      val patternExponent = "E0"
      val patternIntegerDigits = "".padEnd(minimumIntegerDigits, '0')
      if (maximumFractionDigits < 1) {
        return DecimalFormat("$patternIntegerDigits$patternExponent", formatSymbols)
      }
      val patternFractionDigits = "".padEnd(minimumFractionDigits, '0').padEnd(maximumFractionDigits, '#')
      return DecimalFormat("$patternIntegerDigits.$patternFractionDigits$patternExponent", formatSymbols)
    }
  }
}
