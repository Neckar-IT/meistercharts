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
