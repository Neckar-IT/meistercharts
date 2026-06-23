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
 * A cache for the decimal formats
 */
object DecimalFormatsCache {
  /**
   * Cache for decimal formats. The key is a [DecimalFormatKey] data class (not the
   * Int hash of the parameters) so that two distinct configurations with a colliding
   * Int hash do not return each other's cached format.
   */
  private val cache = cache<DecimalFormatKey, CachedNumberFormat>("DecimalFormatsCache", 50)

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
    val key = DecimalFormatKey(maximumFractionDigits, minimumFractionDigits, minimumIntegerDigits, useGrouping)

    return cache.getOrStore(key) {
      DecimalFormat(maximumFractionDigits, minimumFractionDigits, minimumIntegerDigits, useGrouping).cached()
    }
  }
}

/**
 * Cache key for [DecimalFormatsCache] / [PercentageFormatsCache]. Uses the data-class
 * generated equals/hashCode so cache lookup is keyed on the full configuration tuple,
 * not on a hash that could collide.
 */
internal data class DecimalFormatKey(
  val maximumFractionDigits: Int,
  val minimumFractionDigits: Int,
  val minimumIntegerDigits: Int,
  val useGrouping: Boolean,
)
