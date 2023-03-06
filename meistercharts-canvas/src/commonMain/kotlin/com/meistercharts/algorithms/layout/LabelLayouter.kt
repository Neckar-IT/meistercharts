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
package com.meistercharts.algorithms.layout

import com.meistercharts.label.LayoutedLabel
import it.neckar.open.collections.fastForEach
import it.neckar.open.collections.fastForEachFiltered
import it.neckar.open.collections.fastForEachReversed
import it.neckar.open.unit.other.pct
import it.neckar.open.unit.other.px
import kotlin.math.max
import kotlin.math.min

/**
 * Layouter for labels that avoids overlaps.
 * Only layouts on one axis (usually the y axis)
 *
 */
@Deprecated("do not use anymore")
class LabelLayouter constructor(
  /**
   * The spacing between the labels
   */
  @px
  val labelSpacing: Double = 3.0,

  /**
   * The min value for the labels
   */
  @px val min: Double = -Double.MAX_VALUE,
  /**
   * The max value for the labels
   */
  @px val max: Double = Double.MAX_VALUE
) {

  /**
   * Calculates the optimal y positions for the given labels.
   * The given layout info objects are updated with the best coordinates
   */
  fun calculateOptimalPositions(labels: List<LayoutedLabel>) {
    //The other methods expect the label infos to be sorted
    val sorted = ArrayList(labels)
    sorted.sortWith(compareBy {
      it.preferredCenterY
    })

    //Calculate absolute min/max values for each label
    calculateAbsoluteMin(sorted)
    calculateAbsoluteMax(sorted)

    //Layout from button up (simple stack)
    minToMaxLayout(sorted)

    //(re)overlap labels for 50%
    revertPercent(sorted, 0.5)

    //stack from current position from top to bottom
    maxToMinLayout(sorted)

    //Optimize the layout locally (each label within the bounds of its neighbors)
    optimizeLayoutYLocally(sorted)
  }

  /**
   * Sets the min and max value for each label
   */
  private fun calculateAbsoluteMin(labels: List<LayoutedLabel>) {
    var lastMaxY = min

    labels.fastForEach { label ->
      label.absoluteCenterYMin = lastMaxY + label.halfHeight + labelSpacing
      lastMaxY = label.absoluteCenterYMin + label.halfHeight
    }
  }

  private fun calculateAbsoluteMax(labels: List<LayoutedLabel>) {
    var lastMinY = max

    for (i in labels.indices.reversed()) {
      val label = labels[i]

      label.absoluteCenterYMax = lastMinY - label.halfHeight - labelSpacing
      lastMinY = label.absoluteCenterYMax - label.halfHeight
    }
  }

  /**
   * Stack from low y values to high y values
   */
  private fun minToMaxLayout(labels: List<LayoutedLabel>) {
    @px var lastMaxY = min

    labels.fastForEach { label ->
      //Check the min y with the last stored top y
      if (label.actualMinY < lastMaxY + labelSpacing) {
        //We are too low - move up
        label.actualCenterY = lastMaxY + labelSpacing + label.halfHeight
      }

      lastMaxY = label.actualMaxY
    }
  }

  /**
   * Layout from high y values to low y values
   */
  private fun maxToMinLayout(labels: List<LayoutedLabel>) {
    @px var lastMinY = max

    labels.fastForEachReversed { label ->
      //Check the max y with the last stored bottom y
      if (label.actualMaxY > lastMinY - labelSpacing) {
        //We are too high - move down
        label.actualCenterY = lastMinY - labelSpacing - label.halfHeight
      }

      lastMinY = label.actualMinY
    }
  }

  /**
   * Move 50% towards the natural position
   */
  private fun revertPercent(labels: List<LayoutedLabel>, @pct correctionFactor: Double) {
    labels.fastForEachFiltered({
      it.hasModifiedActualY()
    }) { label ->
      @px val delta = label.actualCenterY - label.preferredCenterY
      label.actualCenterY = label.actualCenterY - delta * correctionFactor
    }
  }

  /**
   * Optimize the layout locally
   */
  private fun optimizeLayoutYLocally(labels: List<LayoutedLabel>) {
    if (labels.isEmpty()) {
      return
    }

    for (i in labels.indices) {
      val lowerLabel: LayoutedLabel?
      if (i == 0) {
        lowerLabel = null
      } else {
        lowerLabel = labels[i - 1]
      }
      val middleLabel = labels[i]

      val upperLabel: LayoutedLabel?
      if (i == labels.size - 1) {
        upperLabel = null
      } else {
        upperLabel = labels[i + 1]
      }

      avoidOverlap(lowerLabel, middleLabel, upperLabel)
    }

  }

  /**
   * Avoids overlaps
   */
  internal fun avoidOverlap(lowerLabel: LayoutedLabel?, middleLabel: LayoutedLabel, upperLabel: LayoutedLabel?) {
    if (lowerLabel != null && middleLabel.overlapsActualY(lowerLabel)) {
      //label is overlapping with lower

      //The smallest y value that does not overlap with the lower label
      @px val minY = lowerLabel.actualMaxY + labelSpacing + middleLabel.height / 2.0
      middleLabel.actualCenterY = max(middleLabel.preferredCenterY, minY)
      return
    }

    if (upperLabel != null && middleLabel.overlapsActualY(upperLabel)) {
      //Label is overlapping with upper:

      @px val maxY = upperLabel.actualMinY - labelSpacing - middleLabel.height / 2.0
      middleLabel.actualCenterY = min(middleLabel.preferredCenterY, maxY)
    }
  }
}
