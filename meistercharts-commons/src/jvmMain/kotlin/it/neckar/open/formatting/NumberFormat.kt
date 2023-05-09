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
  override val maximumFractionDigits: Int,
  override val minimumFractionDigits: Int,
  override val minimumIntegerDigits: Int,
  override val useGrouping: Boolean
) : NumberFormat, DecimalFormatDescriptor {

  private val numberFormatCache = NumberFormatCache("DecimalFormat(Fractions: min: $minimumFractionDigits, max: $maximumFractionDigits; Min int: $minimumIntegerDigits; grouping: $useGrouping)") {
    this.maximumFractionDigits = this@DecimalFormat.maximumFractionDigits
    this.minimumFractionDigits = this@DecimalFormat.minimumFractionDigits
    this.minimumIntegerDigits = this@DecimalFormat.minimumIntegerDigits
    this.isGroupingUsed = this@DecimalFormat.useGrouping
  }

  override fun format(value: Double, i18nConfiguration: I18nConfiguration, whitespaceConfig: WhitespaceConfig): String {
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

  override fun format(value: Double, i18nConfiguration: I18nConfiguration, whitespaceConfig: WhitespaceConfig): String {
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
