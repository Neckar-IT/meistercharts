package com.meistercharts.history.generator

import com.meistercharts.history.HistoryEnum
import com.meistercharts.history.HistoryEnumSet
import it.neckar.open.unit.other.pct
import it.neckar.open.unit.si.ms
import kotlin.random.Random

/**
 * Generates values.
 * Very useful for sample/demo data
 */
fun interface EnumValueGenerator {
  /**
   * Generates a value for the input [timestamp]
   */
  fun generate(timestamp: @ms Double, historyEnum: HistoryEnum): HistoryEnumSet

  companion object {
    /**
     * Returns the constant value
     */
    fun always(always: HistoryEnumSet): EnumValueGenerator {
      return EnumValueGenerator { timestamp, enum ->
        always
      }
    }

    /**
     * Generates random values for the given enum
     *
     * @param randomGenerator the random generator that is used
     */
    fun random(
      /**
       * If set to a value > 0, this generator also creates NoValue
       */
      noValuePercentage: @pct Double = 0.05,
      randomGenerator: Random = it.neckar.open.kotlin.lang.random,
    ): EnumValueGenerator {
      return EnumValueGenerator { _, enum ->
        if (noValuePercentage > 0.0 && randomGenerator.nextDouble() <= noValuePercentage) {
          return@EnumValueGenerator HistoryEnumSet.NoValue
        }

        val index = randomGenerator.nextInt(0, enum.valuesCount)
        HistoryEnumSet.forEnumValue(index)
      }
    }

    ///**
    // * Generates normally distributed random values within the given [valueRange] - around the *center* of the given value range
    // *
    // * @param sigmaAbsolute the sigma to be used for the normal distribution
    // */
    //fun normality(
    //  valueRange: ValueRange,
    //  /**
    //   * The sigma - by default 2% of the value range delta
    //   */
    //  sigmaAbsolute: Double = valueRange.delta * 0.02,
    //): RandomNormalDecimalValueGenerator {
    //  return RandomNormalDecimalValueGenerator(valueRange, sigmaAbsolute)
    //}

    ///**
    // * Generates a sine curve within the given [valueRange]
    // *
    // * @param angleIncrement is the increment to be used each time the [generate] function is called
    // */
    //fun sine(
    //  valueRange: ValueRange,
    //  angleIncrement: @rad Double = PI / 100.0,
    //): SineDecimalValueGenerator {
    //  return SineDecimalValueGenerator(valueRange, angleIncrement)
    //}

    ///**
    // * Generates a cosine curve within the given [valueRange]
    // *
    // * @param angleIncrement is the increment to be used each time the [generate] function is called
    // */
    //fun cosine(
    //  valueRange: ValueRange,
    //  angleIncrement: @rad Double = PI / 100.0,
    //): CosineDecimalValueGenerator {
    //  return CosineDecimalValueGenerator(valueRange, angleIncrement)
    //}
  }
}
