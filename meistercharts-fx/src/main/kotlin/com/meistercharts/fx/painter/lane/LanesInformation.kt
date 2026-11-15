/*
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

import com.meistercharts.range.ValueRange
import com.meistercharts.color.Color
import com.meistercharts.annotations.Domain
import it.neckar.open.unit.other.pct
import com.google.common.collect.ImmutableList

/**
 * Contains edges and positions for a given point in time
 */
class LanesInformation(
  val lanes: ImmutableList<out Lane>,
  val edges: ImmutableList<out Edge>,
  @Domain val valueRange: ValueRange
) {

  constructor(
    lanes: Collection<out Lane>,
    edges: Collection<out Edge>,
    @Domain
    valueRange: ValueRange
  ) : this(ImmutableList.copyOf(lanes), ImmutableList.copyOf(edges), valueRange)

  /**
   * Describes one edge
   */
  data class Edge(
    /**
     * The position of the edge
     */
    @Domain
    val position: Double,
    /**
     * The brightness below the edge
     */
    @pct
    val brightnessBelow: Double,
    /**
     * The brightness above the edge
     */
    @pct
    val brightnessAbove: Double,

    /**
     * The label for the edge.
     * If set to null, the label is not painted
     */
    val label: String?,

    /**
     * The color for the edge
     */
    val color: Color,
  )

  /**
   * Describes one lane
   */
  data class Lane(
    /**
     * The color of the lane
     */
    @pct val brightness: Double,
    /**
     * The start of the pane
     */
    @Domain val lower: Double,
    /**
     * The end of the lane
     */
    @Domain val upper: Double
  )

  companion object {
    val EMPTY: LanesInformation = LanesInformation(ImmutableList.of(), ImmutableList.of(), ValueRange.percentage)
  }
}
