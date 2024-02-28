package it.neckar.open.formatting

import it.neckar.open.i18n.CurrentI18nConfiguration
import it.neckar.open.i18n.I18nConfiguration
import it.neckar.open.kotlin.lang.WhitespaceConfig
import it.neckar.open.kotlin.lang.asProvider

/**
 * Number format that appends a unit
 */
class WithUnitFormat(
  /**
   * The delegate that is used for format the value
   */
  val delegate: NumberFormat,

  /**
   * The unit that is appended
   */
  val unit: () -> String?,
) : NumberFormat {

  constructor(delegate: NumberFormat, unit: String) : this(delegate, unit.asProvider())

  override fun format(value: Double, i18nConfiguration: I18nConfiguration, whitespaceConfig: WhitespaceConfig): String {
    return formatWithUnit(value, i18nConfiguration, whitespaceConfig)
  }

  /**
   * Formats the provided value.
   * This is a helper method that can be used from the [NumberFormat].
   *
   * ATTENTION: The values are *not* cached!
   * In most cases it is preferred to use a [CachedNumberFormat] instead.
   */
  fun formatWithUnit(value: Double, i18nConfiguration: I18nConfiguration = CurrentI18nConfiguration, whitespaceConfig: WhitespaceConfig = WhitespaceConfig.NonBreaking): String {
    val formattedValue = delegate.format(value, i18nConfiguration)

    return appendUnit(formattedValue, unit(), whitespaceConfig)
  }

  companion object {
    /**
     * Helper method to format a value with a unit.
     * This method can be used instead of a [WithUnitFormat] instance to avoid unnecessary object creation.
     */
    fun formatWithUnit(value: Double, valueFormat: NumberFormat, unitLabel: String, i18nConfiguration: I18nConfiguration = CurrentI18nConfiguration, whitespaceConfig: WhitespaceConfig = WhitespaceConfig.NonBreaking): String {
      val formattedValue = valueFormat.format(value, i18nConfiguration)
      return appendUnit(formattedValue, unitLabel, whitespaceConfig)
    }

    /**
     * Appends the unit to the given string - if a unit is provided
     */
    fun appendUnit(formattedValue: String, unitLabel: String?, whitespaceConfig: WhitespaceConfig = WhitespaceConfig.NonBreaking): String {
      if (unitLabel.isNullOrBlank()) {
        return formattedValue //do not try to append anything if the unit is null or empty
      }

      return "$formattedValue${whitespaceConfig.smallSpace}$unitLabel"
    }
  }
}

/**
 * Creates a new number format that appends the given unit
 */
fun NumberFormat.appendUnit(unit: String): WithUnitFormat {
  return WithUnitFormat(this, unit)
}

/**
 * Appends the provided unit.
 * The number format is automatically cached
 */
fun NumberFormat.appendUnit(unitProvider: () -> String?): CachedNumberFormat {
  return WithUnitFormat(this, unitProvider)
    .cached { unitProvider().hashCode() }
}
