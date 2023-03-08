/**
 * Copyright 2023 Neckar IT GmbH, Mössingen, Germany
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
     * Returns a [ReferenceEntryGenerator] that generates increasing [ReferenceEntryId] values based on the given time step.
     *
     * @param step the time step between generated [ReferenceEntryId] values.
     * @param max the maximum value that can be generated (inclusive). Default value is [ReferenceEntryId] with id 100,000.
     * @return a [ReferenceEntryGenerator] that generates increasing [ReferenceEntryId] values based on the given time step.
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
     * Returns a [ReferenceEntryGenerator] that generates random [ReferenceEntryId]s within the range [0, max.id], with an optional chance of generating a [ReferenceEntryId.NoValue].
     *
     * @param noValuePercentage the percentage chance of generating a [ReferenceEntryId.NoValue] value. Default value is 0.05.
     * @param max the maximum value that can be generated (inclusive). Default value is [ReferenceEntryId] with id 100,000.
     * @param randomGenerator the [Random] object used to generate the values. Defaults to [it.neckar.open.kotlin.lang.random].
     * @return a [ReferenceEntryGenerator] that generates random [ReferenceEntryId]s within the specified range, with an optional chance of generating a [ReferenceEntryId.NoValue].
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
