package com.meistercharts.demo

import com.meistercharts.history.generator.DecimalValueGenerator
import com.meistercharts.algorithms.ValueRange
import com.meistercharts.animation.Easing
import it.neckar.open.unit.number.Positive
import it.neckar.open.kotlin.lang.random
import it.neckar.open.unit.si.ms
import kotlin.time.Duration.Companion.seconds
import kotlin.time.DurationUnit

/**
 * Builds a [DecimalValueGenerator] that takes a timestamp as an argument
 */
class TimeBasedValueGeneratorBuilder(config: TimeBasedValueGeneratorBuilder.() -> Unit) {

  /**
   * The value to start with
   */
  var startValue: Double = ValueRange.default.center()

  /**
   * The minimum difference between the last generated value of one [period] and the next [period]
   */
  var minDeviation: @Positive Double = ValueRange.default.delta * 0.05

  /**
   * The maximum difference between the last generated value of one [period] and the next [period]
   */
  var maxDeviation: @Positive Double = ValueRange.default.delta * 0.15

  /**
   * The easing to be used to generate all values during a [period]
   */
  var easing: Easing = Easing.linear

  /**
   * The time that should pass between the previously generated value and the next target value
   */
  var period: @ms @Positive Double = 10.seconds.toDouble(DurationUnit.MILLISECONDS)

  /**
   * The range in which the generated values must lie
   */
  var valueRange: ValueRange = ValueRange.default

  init {
    this.config()
  }

  private var built: Boolean = false

  /**
   * Call this function to create a [DecimalValueGenerator] that takes a timestamp as an argument
   *
   * Beware: this function may only be called once!
   */
  fun build(): DecimalValueGenerator {
    check(!built) { "build() may only called once" }
    built = true

    require(minDeviation >= 0) { "$minDeviation < 0" }
    require(maxDeviation > 0) { "$maxDeviation <= 0" }
    require(minDeviation < maxDeviation) { "$minDeviation >= $maxDeviation" }
    require(period > 0) { "period must be greater than 0 but was <$period>" }
    require(valueRange.contains(startValue)) { "value-range $valueRange does not contain start value $startValue" }

    return object : DecimalValueGenerator {
      private var periodStartValue = startValue
      private var periodStartTimestamp: @ms Double = 0.0
      private var periodEndValue: Double = periodStartValue
      private var periodEndTimestamp: @ms Double = 0.0

      override fun generate(timestamp: @ms Double): Double {
        check(valueRange.contains(periodStartValue)) { "$valueRange does not contain period start value $periodStartValue" }
        check(valueRange.contains(periodEndValue)) { "$valueRange does not contain period end value $periodEndValue" }

        if (timestamp > periodEndTimestamp) {
          //start of a new period -> compute its desired end-value
          periodStartTimestamp = timestamp
          periodEndTimestamp = timestamp + period
          periodStartValue = periodEndValue
          var loopCounter = 0
          do {
            ++loopCounter
            val sign = when {
              periodEndValue <= valueRange.start -> 1.0 //periodEndValue must get larger so the sign must be positive
              periodEndValue >= valueRange.end -> -1.0 //periodEndValue must get smaller so the sign must be negative
              else -> if (random.nextBoolean()) -1.0 else 1.0
            }
            periodEndValue += sign * (random.nextDouble(minDeviation, maxDeviation))
          } while (loopCounter < 5 && !valueRange.contains(periodEndValue))
          periodEndValue = periodEndValue.coerceAtMost(valueRange.end)
          periodEndValue = periodEndValue.coerceAtLeast(valueRange.start)
        }
        return periodStartValue + easing((timestamp - periodStartTimestamp) / period) * (periodEndValue - periodStartValue)
      }
    }
  }
}

fun timeBasedValueGenerator(config: TimeBasedValueGeneratorBuilder.() -> Unit): DecimalValueGenerator {
  return TimeBasedValueGeneratorBuilder(config).build()
}

