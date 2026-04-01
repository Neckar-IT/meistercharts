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

import it.neckar.open.collections.cache

/**
 * A cache for the percentage formats
 */
object PercentageFormatsCache {
  /**
   * Cache for decimal formats
   */
  private val cache = cache<Int, CachedNumberFormat>("PercentageFormatsCache", 50)

  /**
   * Returns a (cached) decimal format with a fixed number of decimals
   */
  fun get(numberOfDecimals: Int): CachedNumberFormat {
    return get(numberOfDecimals, numberOfDecimals)
  }

  fun get(numberOfDecimals: Int, useGrouping: Boolean = true): CachedNumberFormat {
    return get(numberOfDecimals, numberOfDecimals, 1, useGrouping)
  }

  fun get(
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
    val hashCode = calculateHashCode(maximumFractionDigits, minimumFractionDigits, minimumIntegerDigits, useGrouping)

    return cache.getOrStore(hashCode) {
      val decimalFormat = DecimalFormatsCache.get(maximumFractionDigits, minimumFractionDigits, minimumIntegerDigits, useGrouping)
      PercentageFormat(decimalFormat).cached()
    }
  }

  /**
   * Calculates the hash code for the given values
   */
  private fun calculateHashCode(
    maximumFractionDigits: Int,
    minimumFractionDigits: Int,
    minimumIntegerDigits: Int,
    useGrouping: Boolean
  ): Int {
    var result = maximumFractionDigits
    result = 31 * result + minimumFractionDigits
    result = 31 * result + minimumIntegerDigits
    result = 31 * result + useGrouping.hashCode()
    return result
  }

}
