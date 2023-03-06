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

import com.meistercharts.algorithms.ValueRange
import com.meistercharts.algorithms.painter.Color
import com.meistercharts.annotations.Domain
import com.meistercharts.label.LabelFormat
import it.neckar.open.unit.other.pct
import com.google.common.collect.ImmutableList

/**
 * Builder that creates the lanes from a number of edges
 */
class LanesInformationBuilder(
  private val valueRange: ValueRange
) {

  private val edgesBuilder = ImmutableList.builder<LanesInformation.Edge>()

  fun addEdge(@Domain domainValue: Double, @pct brightnessBelow: Double, @pct brightnessAbove: Double, edgeLabel: String, edgeColor: Color): LanesInformationBuilder {
    edgesBuilder.add(LanesInformation.Edge(domainValue, brightnessBelow, brightnessAbove, edgeLabel, edgeColor))
    return this
  }

  fun addEdge(edge: LanesInformation.Edge): LanesInformationBuilder {
    edgesBuilder.add(edge)
    return this
  }

  fun addEdge(@Domain domainValue: Double, @pct brightnessBelow: Double, @pct brightnessAbove: Double, labelFormat: LabelFormat, edgeColor: Color): LanesInformationBuilder {
    return addEdge(domainValue, brightnessBelow, brightnessAbove, labelFormat.format(domainValue), edgeColor)
  }

  @JvmOverloads
  fun build(brightnessCalculator: BrightnessCalculator = DefaultBrightnessCalculator()): LanesInformation {
    val edges = edgesBuilder.build()

    //Creates the lanes using the edges
    val lanesBuilder = ImmutableList.builder<LanesInformation.Lane>()

    for (i in edges.indices) {
      val lowerEdge: LanesInformation.Edge?
      val upperEdge = edges[i]

      if (i > 0) {
        lowerEdge = edges[i - 1]
      } else {
        lowerEdge = null
      }

      @Domain val lower: Double = lowerEdge?.position ?: valueRange.start

      lanesBuilder.add(LanesInformation.Lane(brightnessCalculator.calculateBrightness(lowerEdge, upperEdge), lower, upperEdge.position))


      //Add the last lane
      if (i == edges.size - 1) {
        lanesBuilder.add(LanesInformation.Lane(brightnessCalculator.calculateBrightness(upperEdge, null), upperEdge.position, valueRange.end))
      }
    }

    return LanesInformation(lanesBuilder.build(), edges, valueRange)
  }


  /**
   * Calculates the color for a lane
   */
  fun interface BrightnessCalculator {
    /**
     * Calculate the brightness for the lane
     *
     * @param lowerEdge is null for the first lane
     * @param upperEdge is null for the last lane
     */
    @pct
    fun calculateBrightness(lowerEdge: LanesInformation.Edge?, upperEdge: LanesInformation.Edge?): Double
  }
}
