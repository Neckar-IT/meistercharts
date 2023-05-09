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
