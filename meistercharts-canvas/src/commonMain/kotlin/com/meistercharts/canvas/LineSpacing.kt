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
package com.meistercharts.canvas

import it.neckar.open.unit.other.pct

/**
 * Represents a line spacing
 */
class LineSpacing(@pct val percentage: Double) {

  /**
   * Returns the percentage of the spacing between the lines
   */
  @pct
  val spacePercentage: Double
    get() = percentage - 1.0


  companion object {
    /**
     * "Single" line spacing - 115%
     */
    val Single: LineSpacing = LineSpacing(1.15)
    val OneAndHalf: LineSpacing = LineSpacing(1.5)
    val Double: LineSpacing = LineSpacing(2.00)
    val Triple: LineSpacing = LineSpacing(3.00)

    /**
     * Available default values
     */
    val available: List<LineSpacing> = listOf(Single, OneAndHalf, Double, Triple)
  }
}
