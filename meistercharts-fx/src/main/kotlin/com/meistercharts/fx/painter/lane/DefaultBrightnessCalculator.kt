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
package com.meistercharts.fx.painter.lane

/**
 * Default implementation that uses the average of the brightnesses provided by the edges
 *
 */
class DefaultBrightnessCalculator : LanesInformationBuilder.BrightnessCalculator {

  override fun calculateBrightness(lowerEdge: LanesInformation.Edge?, upperEdge: LanesInformation.Edge?): Double {
    var sum = 0.0
    var count = 0.0

    if (lowerEdge != null) {
      sum += lowerEdge.brightnessAbove
      count++
    }

    if (upperEdge != null) {
      sum += upperEdge.brightnessBelow
      count++
    }

    return if (count == 0.0) {
      //Default value if there are no active edges around
      0.0
    } else Math.max(0.0, Math.min(1.0, sum / count))
  }
}
