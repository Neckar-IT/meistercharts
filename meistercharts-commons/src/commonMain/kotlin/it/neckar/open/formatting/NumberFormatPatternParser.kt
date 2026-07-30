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

import it.neckar.logging.Logger
import it.neckar.logging.LoggerFactory
import it.neckar.open.collections.Cache
import it.neckar.open.collections.cache


/**
 * Maps patterns to [DecimalFormat] instances
 */
private val decimalFormatFromPatternCache: Cache<String, CachedNumberFormat> = cache("decimalFormatCache", 100)

/**
 * Parses the given [pattern] and returns a [DecimalFormat].
 */
fun decimalFormatFromPattern(pattern: String): CachedNumberFormat {
  return decimalFormatFromPatternCache.getOrStore(pattern) {
    NumberFormatPatternParser.parsePattern(pattern).let {
      decimalFormat(
        it.maximumFractionDigits,
        it.minimumFractionDigits,
        it.minimumIntegerDigits,
        it.useGrouping
      )
    }
  }
}

/**
 * A configuration that can be used to create [NumberFormat] instances from
 */
data class ParsedNumberFormatPattern(
  /**
   * The maximum fraction digits
   */
  val maximumFractionDigits: Int,
  /**
   * The minimum fraction digits
   */
  val minimumFractionDigits: Int,
  /**
   * The minimum integer digits for the format
   */
  val minimumIntegerDigits: Int, // must be greater than 0 in JavaScript
  /**
   * Whether to use grouping or not
   */
  val useGrouping: Boolean
)

/**
 * A parser for number format patterns
 */
object NumberFormatPatternParser {
  private val logger: Logger = LoggerFactory.getLogger("it.neckar.open.formatting.NumberFormatPatternParser")

  /**
   * Maps patterns to [ParsedNumberFormatPattern] instances.
   *
   * We need only a small cache here because there are not many different patterns in a real-life application.
   */
  private val cache: Cache<String, ParsedNumberFormatPattern> = cache("NumberFormatPatternParser", 20)

  /**
   * Parses the given pattern and creates a [ParsedNumberFormatPattern] from it.
   *
   * A pattern may consist of
   *  - &#35; to indicate a digit; zero shows as absent
   *  - 0 to indicate a digit
   *  - . to indicate the beginning of the fraction part
   *  - , to turn on grouping (thousand separator)
   *
   * example: &#35;,0.00&#35; will turn on grouping, has at least one integer digit, a minimum of 2 fraction digits and a maximum of 3 fraction digits.
   */
  fun parsePattern(pattern: String): ParsedNumberFormatPattern {
    val cachedDecimalFormat = cache[pattern]
    if (cachedDecimalFormat != null) {
      return cachedDecimalFormat
    }

    var minimumIntegerDigits = 0
    var minimumFractionDigits = 0
    var maximumFractionDigits = 0
    var useGrouping = false

    var fractionPart = false
    for (i in 0 until pattern.length) {
      val c = pattern[i]
      when (c) {
        ',' -> useGrouping = true
        '.' -> fractionPart = true
        '#' -> if (fractionPart) {
          ++maximumFractionDigits
        }
        '0' -> if (fractionPart) {
          ++minimumFractionDigits
          ++maximumFractionDigits
        } else {
          ++minimumIntegerDigits
        }
        else -> logger.warn("unsupported character $c in pattern $pattern found")
      }
    }
    if (minimumIntegerDigits < 1) {
      minimumIntegerDigits = 1
    }
    if (minimumFractionDigits > maximumFractionDigits) {
      maximumFractionDigits = minimumFractionDigits
    }

    val numberFormatConfiguration = ParsedNumberFormatPattern(maximumFractionDigits, minimumFractionDigits, minimumIntegerDigits, useGrouping)
    cache[pattern] = numberFormatConfiguration
    return numberFormatConfiguration
  }
}
