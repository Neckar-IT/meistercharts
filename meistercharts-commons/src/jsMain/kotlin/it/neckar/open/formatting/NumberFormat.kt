package it.neckar.open.formatting

import it.neckar.open.i18n.I18nConfiguration
import it.neckar.open.kotlin.lang.WhitespaceConfig
import kotlin.math.pow

/*
 * See [https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Number/toLocaleString]
 */
actual class DecimalFormat internal actual constructor(
  /** Possible values are from 0 to 20 */
  override val maximumFractionDigits: Int,
  /** Possible values are from 0 to 20 */
  override val minimumFractionDigits: Int,
  /** Possible values are from 1 to 21 */
  override val minimumIntegerDigits: Int,
  /** Whether thousand separators should be used */
  override val useGrouping: Boolean
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

  override fun format(value: Double, i18nConfiguration: I18nConfiguration, whitespaceConfig: WhitespaceConfig): String {
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

  override fun format(value: Double, i18nConfiguration: I18nConfiguration, whitespaceConfig: WhitespaceConfig): String {
    return value.asDynamic().toExponential(maximumFractionDigits) as String
  }

  override val precision: Double = 10.0.pow(-this.maximumFractionDigits)
}
