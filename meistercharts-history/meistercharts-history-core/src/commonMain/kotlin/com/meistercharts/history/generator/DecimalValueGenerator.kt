package com.meistercharts.history.generator

import com.meistercharts.algorithms.ValueRange
import com.meistercharts.algorithms.coerceIn
import com.meistercharts.animation.Easing
import it.neckar.open.kotlin.lang.cos
import it.neckar.open.kotlin.lang.random
import it.neckar.open.kotlin.lang.randomNormal
import it.neckar.open.kotlin.lang.sin
import it.neckar.open.time.nowMillis
import it.neckar.open.provider.DoubleProvider
import it.neckar.open.unit.other.pct
import it.neckar.open.unit.si.ms
import it.neckar.open.unit.si.rad
import kotlin.math.PI
import kotlin.random.Random

/**
 * Generates values.
 * Very useful for sample/demo data
 */
fun interface DecimalValueGenerator {
  /**
   * Generates a value for the input [timestamp]
   */
  fun generate(timestamp: @ms Double): Double

  companion object {
    /**
     * Returns the constant value
     */
    fun always(always: Double): ConstantDecimalValueGenerator {
      return ConstantDecimalValueGenerator(always)
    }

    /**
     * Generates random values within the given [valueRange]
     *
     * @param randomGenerator the random generator that is used
     */
    fun random(
      valueRange: ValueRange,
      randomGenerator: Random = random,
    ): RandomDecimalValueGenerator {
      return RandomDecimalValueGenerator(valueRange, randomGenerator)
    }

    /**
     * Generates normally distributed random values within the given [valueRange] - around the *center* of the given value range
     *
     * @param sigmaAbsolute the sigma to be used for the normal distribution
     */
    fun normality(
      valueRange: ValueRange,
      /**
       * The sigma - by default 2% of the value range delta
       */
      sigmaAbsolute: Double = valueRange.delta * 0.02,
    ): RandomNormalDecimalValueGenerator {
      return RandomNormalDecimalValueGenerator(valueRange, sigmaAbsolute)
    }

    /**
     * Generates a sine curve within the given [valueRange]
     *
     * @param angleIncrement is the increment to be used each time the [generate] function is called
     */
    fun sine(
      valueRange: ValueRange,
      angleIncrement: @rad Double = PI / 100.0,
    ): SineDecimalValueGenerator {
      return SineDecimalValueGenerator(valueRange, angleIncrement)
    }

    /**
     * Generates a cosine curve within the given [valueRange]
     *
     * @param angleIncrement is the increment to be used each time the [generate] function is called
     */
    fun cosine(
      valueRange: ValueRange,
      angleIncrement: @rad Double = PI / 100.0,
    ): CosineDecimalValueGenerator {
      return CosineDecimalValueGenerator(valueRange, angleIncrement)
    }
  }
}

/**
 * Generates a single value
 */
class ConstantDecimalValueGenerator(val always: Double) : DecimalValueGenerator {
  override fun generate(timestamp: Double): Double {
    return always
  }
}

/**
 * Generates random values within the given [valueRange]
 *
 * @param randomGenerator the random generator that is used
 */
class RandomDecimalValueGenerator(
  val valueRange: ValueRange,
  val randomGenerator: Random = random,
) : DecimalValueGenerator {


  override fun generate(timestamp: Double): Double {
    return randomGenerator.nextDouble(valueRange.start, valueRange.end)
  }
}

/**
 * Generates normally distributed random values within the given [valueRange] - around the *center* of the given value range
 *
 * @param sigma the sigma to be used for the normal distribution
 */
class RandomNormalDecimalValueGenerator(
  val valueRange: ValueRange,
  val sigma: Double,
) : DecimalValueGenerator {
  private val center: Double = valueRange.center()

  override fun generate(timestamp: Double): Double {
    return randomNormal(center, sigma).coerceIn(valueRange)
  }
}

/**
 * Generates a sine curve within the given [valueRange]
 *
 * @param angleIncrement is the increment to be used each time the [generate] function is called
 */
class SineDecimalValueGenerator(
  val valueRange: ValueRange,
  private val angleIncrement: @rad Double = PI / 100.0,
) : DecimalValueGenerator {
  private val center: Double = (valueRange.start + valueRange.end) / 2.0
  private var angle: @rad Double = 0.0
  override fun generate(timestamp: Double): Double {
    angle += angleIncrement
    if (angle >= 2 * PI) {
      angle -= 2 * PI
    }
    return center + angle.sin() * valueRange.delta * 0.5
  }
}

/**
 * Generates a cosine curve within the given [valueRange]
 *
 * @param angleIncrement is the increment to be used each time the [generate] function is called
 */
class CosineDecimalValueGenerator(val valueRange: ValueRange, private val angleIncrement: @rad Double = PI / 100.0) : DecimalValueGenerator {
  private val center: Double = (valueRange.start + valueRange.end) / 2.0
  private var angle: @rad Double = 0.0
  override fun generate(timestamp: Double): Double {
    angle += angleIncrement
    if (angle >= 2 * PI) {
      angle -= 2 * PI
    }
    return center + angle.cos() * valueRange.delta * 0.5
  }
}

/**
 * Scales the values generated by [decimalValueGenerator] and adds an offset.
 *
 * @param scale the factor by which all values are multiplied
 * @param offset the value to be added to all generated values
 */
class ScalingDecimalValueGenerator(
  val decimalValueGenerator: DecimalValueGenerator,
  val scale: Double,
  val offset: Double = 0.0,
) : DecimalValueGenerator {
  override fun generate(timestamp: Double): Double {
    return offset + decimalValueGenerator.generate(timestamp) * scale
  }
}

/**
 * Generates a repeating value sequence
 * @param easing the easing to be used
 * @param period the time period of the sequence; the sequence repeats itself after this time period
 * @param shift a time based offset into the sequence
 * @param center the smallest possible value of the sequence is [center] - [deviation] while the largest possible value is [center] + [deviation]
 * @param deviation the smallest possible value of the sequence is [center] - [deviation] while the largest possible value is [center] + [deviation]
 */
fun repeatingValues(
  easing: Easing,
  period: @ms Double,
  shift: @ms Double = 0.0,
  center: @pct Double = 0.5,
  deviation: @pct Double = 1.0 - center * 0.5
): @pct Double {
  //the sine ensures that the values are repeated
  val sine = ((nowMillis() - shift) / period * 2 * PI).sin()
  //sine is in [-1.0, 1.0] but easing needs a value in [0.0, 1.0]
  @pct val sineAdjusted = (sine + 1.0) * 0.5
  //ensure that eased value lies around center
  val result = center - 0.5 * easing(sineAdjusted) * deviation
  check(result <= center + deviation) { "result <$result> should be less than or equal to ${center + deviation}" }
  check(result >= center - deviation) { "result <$result> should be greater than or equal to ${center - deviation}" }
  return result
}

/**
 * Creates a provider that passes the current time to this [DecimalValueGenerator] every time it is invoked
 */
fun DecimalValueGenerator.forNow(): DoubleProvider {
  return DoubleProvider {
    generate(nowMillis())
  }
}

/**
 * Scales the values generated by this and adds an offset.
 *
 * @param scale the factor by which all values are multiplied
 * @param offset the value to be added to all generated values
 */
fun DecimalValueGenerator.scaled(
  scale: Double,
  offset: Double = 0.0,
): ScalingDecimalValueGenerator {
  return ScalingDecimalValueGenerator(this, scale, offset)
}

/**
 * Offsets the values generated by this
 */
fun DecimalValueGenerator.offset(
  offset: Double = 0.0,
): ScalingDecimalValueGenerator {
  return ScalingDecimalValueGenerator(this, 1.0, offset)
}
