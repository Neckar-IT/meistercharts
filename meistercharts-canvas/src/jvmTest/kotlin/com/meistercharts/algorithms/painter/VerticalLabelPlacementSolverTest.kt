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

import assertk.assertThat
import assertk.assertions.isCloseTo
import assertk.assertions.isEqualTo
import assertk.assertions.isGreaterThanOrEqualTo
import assertk.assertions.isLessThanOrEqualTo
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class VerticalLabelPlacementSolverTest {
  private val solver = VerticalLabelPlacementSolver()

  private fun solveCenters(spacing: Double, min: Double, max: Double, heights: DoubleArray, vararg preferredCenters: Double): DoubleArray {
    solver.clear()
    preferredCenters.forEachIndexed { index, preferredCenterY ->
      solver.addLabel(preferredCenterY = preferredCenterY, height = heights[index])
    }
    solver.solve(spacing = spacing, min = min, max = max)
    return DoubleArray(solver.labelCount) { solver.placedCenterYAt(it) }
  }

  private fun uniformHeights(count: Int, height: Double = 20.0): DoubleArray = DoubleArray(count) { height }

  @Test
  fun testEmpty() {
    solver.clear()
    solver.solve(spacing = 3.0, min = 0.0, max = 400.0)
    assertThat(solver.labelCount).isEqualTo(0)
  }

  @Test
  fun testSingleLabelKeepsPreferredPosition() {
    val centers = solveCenters(spacing = 3.0, min = 0.0, max = 400.0, uniformHeights(1), 100.0)
    assertThat(centers[0]).isEqualTo(100.0)
  }

  @Test
  fun testSingleLabelClampedToMin() {
    //label height 20 -> the center may not be above min + 10
    val centers = solveCenters(spacing = 3.0, min = 0.0, max = 400.0, uniformHeights(1), 5.0)
    assertThat(centers[0]).isEqualTo(10.0)
  }

  @Test
  fun testSingleLabelClampedToMax() {
    val centers = solveCenters(spacing = 3.0, min = 0.0, max = 400.0, uniformHeights(1), 395.0)
    assertThat(centers[0]).isEqualTo(390.0)
  }

  @Test
  fun testIssueExample() {
    //Example from issue #2352: heights 20, spacing 3 (min center distance 23), preferred [100, 105, 110, 300]
    //The first three labels cluster (centroid 105), the fourth stays untouched
    val centers = solveCenters(spacing = 3.0, min = 0.0, max = 400.0, uniformHeights(4), 100.0, 105.0, 110.0, 300.0)

    assertThat(centers[0]).isCloseTo(82.0, 1e-9)
    assertThat(centers[1]).isCloseTo(105.0, 1e-9)
    assertThat(centers[2]).isCloseTo(128.0, 1e-9)
    assertThat(centers[3]).isCloseTo(300.0, 1e-9)
  }

  @Test
  fun testDenseClusterCenteredOnCentroid() {
    //Three labels with identical preferred position: packed tightly, centered on the shared preferred position
    val centers = solveCenters(spacing = 3.0, min = 0.0, max = 400.0, uniformHeights(3), 200.0, 200.0, 200.0)

    assertThat(centers[0]).isCloseTo(177.0, 1e-9)
    assertThat(centers[1]).isCloseTo(200.0, 1e-9)
    assertThat(centers[2]).isCloseTo(223.0, 1e-9)
  }

  @Test
  fun testClusterClampedAtTopEdge() {
    //All labels want to sit at min: the cluster is pushed down so the first label touches min
    val centers = solveCenters(spacing = 3.0, min = 0.0, max = 400.0, uniformHeights(3), 0.0, 0.0, 0.0)

    assertThat(centers[0]).isCloseTo(10.0, 1e-9) //top edge at 0.0
    assertThat(centers[1]).isCloseTo(33.0, 1e-9)
    assertThat(centers[2]).isCloseTo(56.0, 1e-9)
  }

  @Test
  fun testClusterClampedAtBottomEdge() {
    val centers = solveCenters(spacing = 3.0, min = 0.0, max = 400.0, uniformHeights(3), 400.0, 400.0, 400.0)

    assertThat(centers[0]).isCloseTo(344.0, 1e-9)
    assertThat(centers[1]).isCloseTo(367.0, 1e-9)
    assertThat(centers[2]).isCloseTo(390.0, 1e-9) //bottom edge at 400.0
  }

  @Test
  fun testExactFitLeavesNoSlack() {
    //3 labels of height 20 with spacing 3 require exactly 66 - unique feasible layout
    val centers = solveCenters(spacing = 3.0, min = 0.0, max = 66.0, uniformHeights(3), 50.0, 50.0, 60.0)

    assertThat(centers[0]).isCloseTo(10.0, 1e-9)
    assertThat(centers[1]).isCloseTo(33.0, 1e-9)
    assertThat(centers[2]).isCloseTo(56.0, 1e-9)
  }

  @Test
  fun testVaryingHeights() {
    //heights [10, 30, 20], spacing 2, preferred [100, 101, 140]
    //cumulative min distances c = [0, 22, 49]; PAVA merges the first two labels (means 100, 79)
    //to the mean 89.5; the third label (transformed 91) stays separate
    val centers = solveCenters(spacing = 2.0, min = 0.0, max = 400.0, doubleArrayOf(10.0, 30.0, 20.0), 100.0, 101.0, 140.0)

    assertThat(centers[0]).isCloseTo(89.5, 1e-9)
    assertThat(centers[1]).isCloseTo(111.5, 1e-9) //89.5 + 5 + 2 + 15
    assertThat(centers[2]).isCloseTo(140.0, 1e-9) //separate cluster - stays at its preferred position
  }

  @Test
  fun testFarApartLabelsStayAtPreferredPositions() {
    val centers = solveCenters(spacing = 3.0, min = 0.0, max = 400.0, uniformHeights(2), 100.0, 300.0)

    assertThat(centers[0]).isEqualTo(100.0)
    assertThat(centers[1]).isEqualTo(300.0)
  }

  @Test
  fun testUnboundedWindow() {
    //Default bounds of LabelPainter2: -MAX_VALUE..MAX_VALUE must not distort the solution
    val centers = solveCenters(spacing = 3.0, min = -Double.MAX_VALUE, max = Double.MAX_VALUE, uniformHeights(3), 200.0, 200.0, 200.0)

    assertThat(centers[0]).isCloseTo(177.0, 1e-9)
    assertThat(centers[1]).isCloseTo(200.0, 1e-9)
    assertThat(centers[2]).isCloseTo(223.0, 1e-9)
  }

  @Test
  fun testRequiredSpace() {
    solver.clear()
    solver.addLabel(preferredCenterY = 100.0, height = 20.0)
    solver.addLabel(preferredCenterY = 105.0, height = 30.0)
    solver.addLabel(preferredCenterY = 110.0, height = 10.0)

    assertThat(solver.requiredSpace(3.0)).isEqualTo(66.0)
  }

  @Test
  fun testRequiredSpaceEmpty() {
    solver.clear()
    assertThat(solver.requiredSpace(3.0)).isEqualTo(0.0)
  }

  @Test
  fun testInfeasibleInputFailsFast() {
    solver.clear()
    solver.addLabel(preferredCenterY = 10.0, height = 20.0)
    solver.addLabel(preferredCenterY = 11.0, height = 20.0)

    assertThrows<IllegalArgumentException> {
      //two labels of height 20 + spacing 3 require 43 - only 30 available
      solver.solve(spacing = 3.0, min = 0.0, max = 30.0)
    }
  }

  @Test
  fun testUnsortedInputFailsFast() {
    solver.clear()
    solver.addLabel(preferredCenterY = 100.0, height = 20.0)

    assertThrows<IllegalArgumentException> {
      solver.addLabel(preferredCenterY = 99.0, height = 20.0)
    }
  }

  @Test
  fun testResultsAreOverlapFreeAndOrdered() {
    val heights = uniformHeights(10)
    val centers = solveCenters(
      spacing = 3.0, min = 0.0, max = 400.0, heights,
      100.0, 100.1, 100.2, 100.3, 100.4, 100.5, 100.6, 200.0, 200.1, 399.0,
    )

    for (index in centers.indices) {
      //all labels completely inside the window
      assertThat(centers[index] - heights[index] / 2.0).isGreaterThanOrEqualTo(0.0)
      assertThat(centers[index] + heights[index] / 2.0).isLessThanOrEqualTo(400.0)

      if (index > 0) {
        //minimum center distance: half heights + spacing
        assertThat(centers[index] - centers[index - 1]).isGreaterThanOrEqualTo(23.0 - 1e-9)
      }
    }
  }
}
