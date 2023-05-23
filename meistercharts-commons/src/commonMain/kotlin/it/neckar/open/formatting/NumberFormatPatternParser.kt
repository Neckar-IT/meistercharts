package it.neckar.open.formatting

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
        else -> println("unsupported character $c in pattern $pattern found")
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
