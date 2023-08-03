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
package it.neckar.geometry

import it.neckar.open.unit.si.rad

/**
 * Represents at the rotation direction.
 * To calculate an angle from a given direction to another use
 * [toClockwise] and [toCounterClockwise]
 */
enum class RotationDirection(
  /**
   * The factor that has to be applied when converting from clockwise to counter clockwise
   */
  private val toClockwise: Int,
  /**
   * The factor that has to be applied when converting from counter clockwise to clockwise
   */
  private val toCounterClockwise: Int
) {
  CounterClockwise(-1, 1),
  Clockwise(1, -1);

  /**
   * Factor for calculating from this enum value to Clockwise
   */
  fun toClockwise(@rad value: Double): @rad Double {
    return value * toClockwise
  }

  /**
   * Converts the value to the plattform rotation direction (clockwise)
   */
  fun toPlattformRotationDirection(@rad value: Double): @rad Double {
    return toClockwise(value)
  }

  /**
   * Factor for calculating from this enum value to CounterClockwise
   */
  fun toCounterClockwise(value: @rad Double): @rad Double {
    return value * toCounterClockwise
  }
}
