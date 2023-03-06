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
package com.meistercharts.fonts

/**
 * Special implementation that optimizes the center alignment to be visually pleasing.
 *
 * This is especially important if aligning (number) values to ticks
 */
object FontCenterAlignmentStrategy {

  /**
   * Calculates the offset from the center for the given height of the capital "h"
   */
  fun calculateCenterOffset(capitalHHeight: Double): Double {
    return -capitalHHeight * 0.02 //hard coded value to improve the position of the center line
  }
}
