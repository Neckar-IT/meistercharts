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
package com.meistercharts.algorithms.painter

import it.neckar.open.annotations.Hot
import it.neckar.open.collections.DoubleArrayList
import it.neckar.open.collections.IntArrayList
import it.neckar.open.collections.fastForEach
import it.neckar.open.collections.last
import it.neckar.open.unit.other.px

/**
 * Solves the collision-free label placement problem along the y axis *exactly*.
 *
 * Given labels with preferred center positions (added in ascending order via [addLabel]), the solver
 * computes center positions ([placedCenterYAt]) that:
 * - keep the given order
 * - do not overlap: `y[i+1] - y[i] >= height[i]/2 + spacing + height[i+1]/2`
 * - keep every label completely inside `min..max`
 * - minimize the sum of squared deviations from the preferred centers.
 *
 * The chain constraints are transformed into isotonic constraints (`z[i+1] >= z[i]`) by subtracting the
 * cumulative minimum center distances `c[i]` from both positions and preferred positions. The resulting
 * isotonic regression is solved exactly with the pool-adjacent-violators algorithm (PAVA): sweeping once
 * from top to bottom, consecutive labels whose transformed preferred positions would violate the ordering
 * are merged into clusters placed at the mean of their transformed preferred positions - so each cluster
 * is packed tightly and centered on the centroid of its preferred positions.
 *
 * In the transformed space the window bounds reduce to two constants (binding for the first/last label);
 * they are applied by clamping the monotone solution, which preserves both order and optimality.
 *
 * The caller must ensure that the labels fit into `min..max` (see [requiredSpace]) - the solver fails fast
 * on infeasible input instead of degrading silently.
 *
 * All buffers are reused between calls - a warmed-up solver does not allocate.
 */
class VerticalLabelPlacementSolver {
  /**
   * The preferred center y positions - in ascending order
   */
  private val preferredCenters = DoubleArrayList()

  /**
   * The label heights - same indices as [preferredCenters]
   */
  private val heights = DoubleArrayList()

  /**
   * The cumulative minimum center distances `c[i]`: the exact center of label `i` when all labels
   * are packed tightly, relative to the center of the first label. Recomputed on every [solve].
   */
  private val cumulativeMinDistances = DoubleArrayList()

  /**
   * PAVA cluster stack: number of labels per cluster
   */
  private val clusterSizes = IntArrayList()

  /**
   * PAVA cluster stack: sum of the transformed preferred positions per cluster
   */
  private val clusterSums = DoubleArrayList()

  /**
   * The solved center y positions - same indices as [preferredCenters]. Filled by [solve].
   */
  private val placedCenters = DoubleArrayList()

  /**
   * The number of labels that have been added
   */
  val labelCount: Int
    get() = preferredCenters.size

  /**
   * Removes all labels
   */
  @Hot
  fun clear() {
    preferredCenters.clear()
    heights.clear()
    placedCenters.clear()
  }

  /**
   * Adds a label. Labels must be added in ascending order of [preferredCenterY].
   */
  @Hot
  fun addLabel(preferredCenterY: @px Double, height: @px Double) {
    require(preferredCenterY.isFinite()) { "preferredCenterY must be finite but was $preferredCenterY" }
    require(height.isFinite() && height >= 0.0) { "height must be finite and >= 0 but was $height" }
    if (preferredCenters.isNotEmpty()) {
      require(preferredCenterY >= preferredCenters.last()) {
        "labels must be added in ascending order of preferredCenterY: $preferredCenterY < ${preferredCenters.last()}"
      }
    }

    preferredCenters.add(preferredCenterY)
    heights.add(height)
  }

  /**
   * Returns the total space the added labels require: the sum of all heights plus [spacing] between them
   * (0.0 for an empty solver). The labels are feasible iff `requiredSpace(spacing) <= max - min`.
   *
   * [solve] fails fast on infeasible input - callers must drop labels until this fits.
   */
  @Hot
  fun requiredSpace(spacing: @px Double): @px Double {
    if (heights.isEmpty()) {
      return 0.0
    }

    var sum = -spacing //n labels require only (n-1) * spacing
    heights.fastForEach { height ->
      sum += height + spacing
    }
    return sum
  }

  /**
   * Solves the placement for all added labels. The results are available via [placedCenterYAt].
   */
  @Hot
  fun solve(
    /**
     * The minimum space between two labels (edge to edge)
     */
    spacing: @px Double,
    /**
     * The upper window bound - no label extends above this value
     */
    min: @px Double,
    /**
     * The lower window bound - no label extends below this value
     */
    max: @px Double,
  ) {
    require(spacing >= 0.0) { "spacing must be >= 0 but was $spacing" }

    placedCenters.clear()

    val count = labelCount
    if (count == 0) {
      return
    }

    @px val requiredSpace = requiredSpace(spacing)
    require(requiredSpace <= max - min) {
      "labels do not fit: required $requiredSpace but only ${max - min} available ($min..$max) - drop labels before solving"
    }

    //Cumulative minimum center distances: c[0] = 0, c[i] = c[i-1] + h[i-1]/2 + spacing + h[i]/2
    cumulativeMinDistances.clear()
    cumulativeMinDistances.add(0.0)
    for (index in 1 until count) {
      cumulativeMinDistances.add(cumulativeMinDistances[index - 1] + heights[index - 1] / 2.0 + spacing + heights[index] / 2.0)
    }

    //PAVA sweep over the transformed preferred positions q[i] = p[i] - c[i]:
    //merge a new cluster into its predecessor while its mean is smaller than the predecessor's mean
    clusterSizes.clear()
    clusterSums.clear()
    for (index in 0 until count) {
      var mergedSize = 1
      var mergedSum = preferredCenters[index] - cumulativeMinDistances[index]

      while (clusterSizes.isNotEmpty() && mergedSum / mergedSize < clusterSums.last() / clusterSizes.last()) {
        mergedSum += clusterSums.removeAt(clusterSums.size - 1)
        mergedSize += clusterSizes.removeAt(clusterSizes.size - 1)
      }

      clusterSizes.add(mergedSize)
      clusterSums.add(mergedSum)
    }

    //The window bounds in the transformed space: both are binding for exactly one label
    //(first label for min, last label for max), all other bounds are implied by monotonicity
    val lowerBound = min + heights[0] / 2.0
    val upperBound = max - heights[count - 1] / 2.0 - cumulativeMinDistances[count - 1]

    //Expand the clusters back: y[i] = clamp(clusterMean) + c[i]
    //Sequential clamping instead of coerceIn: the bounds derive from cumulativeMinDistances whose floating-point
    //association differs from requiredSpace - at an exact fit upperBound can end up ULPs below lowerBound.
    //coerceIn would throw then; clamping sequentially (max bound wins) degrades by ULPs instead.
    var labelIndex = 0
    for (clusterIndex in 0 until clusterSizes.size) {
      val clusterMean = clusterSums[clusterIndex] / clusterSizes[clusterIndex]
      val clampedMean = clusterMean.coerceAtLeast(lowerBound).coerceAtMost(upperBound)

      repeat(clusterSizes[clusterIndex]) {
        placedCenters.add(clampedMean + cumulativeMinDistances[labelIndex])
        labelIndex++
      }
    }
  }

  /**
   * Returns the solved center y position for the label at the given index (same order as [addLabel] calls).
   * [solve] must have been called before.
   */
  @Hot
  fun placedCenterYAt(index: Int): @px Double {
    require(placedCenters.size == labelCount) { "solve() must be called before accessing results" }
    require(index in 0 until placedCenters.size) { "index $index out of bounds for $labelCount labels" }
    return placedCenters[index]
  }
}
