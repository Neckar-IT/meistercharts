package it.neckar.open.formatting

import it.neckar.open.i18n.CurrentI18nConfiguration
import it.neckar.open.i18n.DefaultI18nConfiguration
import it.neckar.open.i18n.I18nConfiguration
import it.neckar.open.kotlin.lang.WhitespaceConfig
import it.neckar.open.unit.currency.EUR

/**
 * Format for numbers
 */
interface NumberFormat {
  /**
   * Formats a number to a string
   */
  fun format(value: Double, i18nConfiguration: I18nConfiguration = CurrentI18nConfiguration, whitespaceConfig: WhitespaceConfig = WhitespaceConfig.NonBreaking): String

  /**
   * The smallest absolute difference between two double values that leads to different results calling [format].
   *
   * Mathematically speaking:
   *
   * given a value v and the precision p > 0 it holds: format(v) != format(v + p) and there is no q > 0 with q < p and format(v) != format(v + q)
   *
   */
  @Deprecated("we need a new solution for the value axis; maybe the precision must be passed to the NumberFormat instead of the NumberFormat providing the precision")
  val precision: Double
    get() {
      return 0.0
    }
}

/**
 * A cached format that can be used to format decimals
 */
val decimalFormat: CachedNumberFormat = DecimalFormatsCache.get(2, 0)

/**
 * Formats integer values
 */
val intFormat: CachedNumberFormat = DecimalFormatsCache.get(0)

/**
 * A decimal format with one fraction digits
 */
val decimalFormat1digit: CachedNumberFormat = DecimalFormatsCache.get(1, 1)

/**
 * A decimal format with two fraction digits
 */
val decimalFormat2digits: CachedNumberFormat = DecimalFormatsCache.get(2, 2)

/**
 * Formats a percentage with up to two fraction digits
 */
val percentageFormat: CachedNumberFormat = PercentageFormat(decimalFormat2digits).cached()

/**
 * Formats a percentage with exactly two fraction digits
 */
val percentageFormat2digits: CachedNumberFormat = PercentageFormat(decimalFormat2digits).cached()

val percentageFormat0digits: CachedNumberFormat = PercentageFormat(intFormat).cached()

val percentageFormat1digits: CachedNumberFormat = PercentageFormat(decimalFormat1digit).cached()

/**
 * Returns the percentage format for the given number of decimals
 */
fun percentageFormat(numberOfDecimals: Int, useGrouping: Boolean = true): CachedNumberFormat {
  return PercentageFormatsCache.get(numberOfDecimals, useGrouping)
}

/**
 * Returns a cached decimal format for the given number of decimals
 */
fun decimalFormat(numberOfDecimals: Int, useGrouping: Boolean = true): CachedNumberFormat {
  return DecimalFormatsCache.get(numberOfDecimals, useGrouping = useGrouping)
}

/**
 * Returns a cached decimal format for the given number of decimals
 */
fun decimalFormat(
  /**
   * The maximum fraction digits
   */
  maximumFractionDigits: Int = 2,
  /**
   * The minimum fraction digits
   */
  minimumFractionDigits: Int = 0,
  /**
   * The minimum integer digits for the format
   */
  minimumIntegerDigits: Int = 1, // must be greater than 0 in JavaScript
  /**
   * Whether to use grouping or not
   */
  useGrouping: Boolean = true
): CachedNumberFormat {
  return DecimalFormatsCache.get(maximumFractionDigits, minimumFractionDigits, minimumIntegerDigits, useGrouping)
}


/**
 * Formats a double with the given number of decimals
 */
fun Double.format(numberOfDecimals: Int = 2, useGrouping: Boolean = true, i18nConfiguration: I18nConfiguration = DefaultI18nConfiguration, whitespaceConfig: WhitespaceConfig = WhitespaceConfig.NonBreaking): String {
  return decimalFormat(numberOfDecimals, useGrouping).format(this, i18nConfiguration, whitespaceConfig)
}

/**
 * Formats a double with the given number of decimals
 */
fun Double.format(
  /**
   * The maximum fraction digits
   */
  maximumFractionDigits: Int = 2,
  /**
   * The minimum fraction digits
   */
  minimumFractionDigits: Int = 0,
  /**
   * The minimum integer digits for the format
   */
  minimumIntegerDigits: Int = 1, // must be greater than 0 in JavaScript
  /**
   * Whether to use grouping or not
   */
  useGrouping: Boolean = true, i18nConfiguration: I18nConfiguration = DefaultI18nConfiguration
): String {
  return decimalFormat(maximumFractionDigits, minimumFractionDigits, minimumIntegerDigits, useGrouping).format(this, i18nConfiguration)
}

/**
 * Formats this value as int
 */
fun Double.formatAsInt(i18nConfiguration: I18nConfiguration = DefaultI18nConfiguration, whitespaceConfig: WhitespaceConfig = WhitespaceConfig.NonBreaking): String {
  return intFormat.format(this, i18nConfiguration, whitespaceConfig)
}

fun Int.format(i18nConfiguration: I18nConfiguration = DefaultI18nConfiguration, whitespaceConfig: WhitespaceConfig = WhitespaceConfig.NonBreaking): String {
  return intFormat.format(this.toDouble(), i18nConfiguration, whitespaceConfig)
}

/**
 * Formats a double as percentage
 */
fun Double.formatAsPercentage(numberOfDecimals: Int = 2, useGrouping: Boolean = true, i18nConfiguration: I18nConfiguration = DefaultI18nConfiguration, whitespaceConfig: WhitespaceConfig = WhitespaceConfig.NonBreaking): String {
  return percentageFormat(numberOfDecimals, useGrouping).format(this, i18nConfiguration, whitespaceConfig)
}

/**
 * Formats this value as an amount of money in EUR.
 *
 * The value is given in cents.
 */
fun @EUR Int.formatEuroCents(i18nConfiguration: I18nConfiguration = DefaultI18nConfiguration, whitespaceConfig: WhitespaceConfig = WhitespaceConfig.NonBreaking): String {
  return decimalFormat2digits.format(this / 100.0, i18nConfiguration, whitespaceConfig) + "${whitespaceConfig.smallSpace}€"
}

/**
 * Formats the given value as euros
 */
fun @EUR Double.formatEuro(i18nConfiguration: I18nConfiguration = DefaultI18nConfiguration, whitespaceConfig: WhitespaceConfig = WhitespaceConfig.NonBreaking): String {
  return decimalFormat2digits.format(this, i18nConfiguration, whitespaceConfig) + "${whitespaceConfig.smallSpace}€"
}

/**
 * Contains the values that are used by the decimal format
 */
interface DecimalFormatDescriptor {
  /**
   * The maximum fraction digits
   */
  val maximumFractionDigits: Int

  /**
   * The minimum fraction digits
   */
  val minimumFractionDigits: Int

  /**
   * The minimum integer digits for the format
   */
  val minimumIntegerDigits: Int

  /**
   * Whether to use grouping or not
   */
  val useGrouping: Boolean
}

/**
 * Decimal format.
 * Do *not* instantiate directly. Use the methods [DecimalFormatsCache] or [decimalFormat] or the constants [decimalFormat1digit], [decimalFormat2digits] instead.
 */
expect class DecimalFormat internal constructor(
  /**
   * The maximum fraction digits
   */
  maximumFractionDigits: Int = 2,
  /**
   * The minimum fraction digits
   */
  minimumFractionDigits: Int = 0,
  /**
   * The minimum integer digits for the format
   */
  minimumIntegerDigits: Int = 1, // must be greater than 0 in JavaScript
  /**
   * Whether to use grouping or not
   */
  useGrouping: Boolean = true
) : NumberFormat, DecimalFormatDescriptor

/**
 * Format that prints exponential
 */
expect class ExponentialFormat(
  /**
   * The maximum fraction digits
   */
  maximumFractionDigits: Int = 2,
  /**
   * The minimum fraction digits
   */
  minimumFractionDigits: Int = 0,
  /**
   * The minimum integer digits for the format
   */
  minimumIntegerDigits: Int = 1, // must be greater than 0 in JavaScript
  /**
   * Whether to use grouping or not
   */
  useGrouping: Boolean = true
) : NumberFormat

