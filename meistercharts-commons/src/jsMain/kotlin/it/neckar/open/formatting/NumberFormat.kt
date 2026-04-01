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
import kotlin.math.pow

/*
 * See [https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Number/toLocaleString]
 */
actual class DecimalFormat internal actual constructor(
  /** Possible values are from 0 to 20 */
  actual override val maximumFractionDigits: Int,
  /** Possible values are from 0 to 20 */
  actual override val minimumFractionDigits: Int,
  /** Possible values are from 1 to 21 */
  actual override val minimumIntegerDigits: Int,
  /** Whether thousand separators should be used */
  actual override val useGrouping: Boolean,
) : NumberFormat, DecimalFormatDescriptor {

  /**
   * Options passed to toLocaleString (see [https://developer.mozilla.org/de/docs/Web/JavaScript/Reference/Global_Objects/Number/toLocaleString])
   *
   * This object is interpreted by the browser.
   */
  @Suppress("unused")
  private val formatOptions = object {
    @JsName("minimumIntegerDigits")
    val minimumIntegerDigits = this@DecimalFormat.minimumIntegerDigits
    @JsName("minimumFractionDigits")
    val minimumFractionDigits = this@DecimalFormat.minimumFractionDigits
    @JsName("maximumFractionDigits")
    val maximumFractionDigits = this@DecimalFormat.maximumFractionDigits
    @JsName("useGrouping")
    val useGrouping = this@DecimalFormat.useGrouping
  }

  actual override fun format(value: Double, i18nConfiguration: I18nConfiguration, whitespaceConfig: WhitespaceConfig): String {
    //add 0.0 to avoid "-0.0"
    return (value + 0.0).asDynamic().toLocaleString(i18nConfiguration.formatLocale.locale, formatOptions) as String
  }

  override val precision: Double = 10.0.pow(-maximumFractionDigits)
}

/*
* See [https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Number/toExponential]
*/
actual class ExponentialFormat actual constructor(
  /** Possible values are from 0 to 20 */
  val maximumFractionDigits: Int,
  /** Possible values are from 0 to 20 */
  val minimumFractionDigits: Int,
  /** Possible values are from 1 to 21 */
  val minimumIntegerDigits: Int,
  /** Whether thousand separators should be used */
  val useGrouping: Boolean
) : NumberFormat {

  actual override fun format(value: Double, i18nConfiguration: I18nConfiguration, whitespaceConfig: WhitespaceConfig): String {
    return value.asDynamic().toExponential(maximumFractionDigits) as String
  }

  override val precision: Double = 10.0.pow(-this.maximumFractionDigits)
}
