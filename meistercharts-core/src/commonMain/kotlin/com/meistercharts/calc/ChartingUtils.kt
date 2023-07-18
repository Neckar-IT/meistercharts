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
package com.meistercharts.calc

import it.neckar.open.unit.other.px

/**
 * Contains several helper classes
 *
 */
object ChartingUtils {
  /**
   * Ensure that a line lies fully within the given min / max.
   * This method should be used when a line is placed at the edge of the window. It ensures that the complete
   * line is visible.
   *
   * If placing a line at the edge of the canvas only half of the line width is visible by default.
   */
  @px
  fun lineWithin(@px lineCenter: Double, @px min: Double, @px max: Double, @px lineWidth: Double): Double {
    require(max >= min) { "max must not be less than min" }

    return if (max - min < lineWidth) lineCenter else lineCenter.coerceIn(min + lineWidth / 2.0, max - lineWidth / 2.0)
  }
}
