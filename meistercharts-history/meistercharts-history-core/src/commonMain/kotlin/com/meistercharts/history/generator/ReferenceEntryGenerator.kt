package com.meistercharts.history.generator

import com.meistercharts.history.ReferenceEntryId
import it.neckar.open.unit.other.pct
import it.neckar.open.unit.si.ms
import kotlin.random.Random
import kotlin.time.Duration

/**
 * Generates values for the history.
 * Very useful for sample/demo data
 */
fun interface ReferenceEntryGenerator {
  /**
   * Generates a value for the input [timestamp]
   */
  fun generate(timestamp: @ms Double): ReferenceEntryId

  companion object {
    /**
     * Returns the constant value
     */
    fun always(always: ReferenceEntryId): ReferenceEntryGenerator {
      return ReferenceEntryGenerator { always }
    }

    /**
     * Returns increasing values for each provided [step]
     */
    fun increasing(
      step: Duration,
      max: ReferenceEntryId = ReferenceEntryId(100_000),
    ): ReferenceEntryGenerator {
      return ReferenceEntryGenerator { timestamp ->
        val baseValue = (timestamp / step.inWholeMilliseconds)
        val id = baseValue % max.id
        return@ReferenceEntryGenerator ReferenceEntryId((id % Int.MAX_VALUE).toInt())
      }
    }

    /**
     * Generates random values
     * @param randomGenerator the random generator that is used
     */
    fun random(
      /**
       * If set to a value > 0, this generator also creates NoValue
       */
      noValuePercentage: @pct Double = 0.05,
      /**
       * The max value that is generated at most
       */
      max: ReferenceEntryId = ReferenceEntryId(100_000),
      randomGenerator: Random = it.neckar.open.kotlin.lang.random,
    ): ReferenceEntryGenerator {
      return ReferenceEntryGenerator {
        if (noValuePercentage > 0.0 && randomGenerator.nextDouble() <= noValuePercentage) {
          return@ReferenceEntryGenerator ReferenceEntryId.NoValue
        }

        val id = randomGenerator.nextInt(0, max.id)
        return@ReferenceEntryGenerator ReferenceEntryId(id)
      }
    }
  }
}
