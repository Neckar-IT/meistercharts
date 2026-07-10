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
import assertk.assertions.isFalse
import assertk.assertions.isGreaterThanOrEqualTo
import assertk.assertions.isLessThanOrEqualTo
import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.algorithms.layers.crosswire.CrossWireLayer.LabelIndex
import com.meistercharts.canvas.mock.MockLayerPaintingContext
import com.meistercharts.model.Insets
import com.meistercharts.provider.LabelsProvider
import com.meistercharts.style.BoxStyle
import it.neckar.open.i18n.I18nConfiguration
import it.neckar.open.i18n.TextService
import it.neckar.open.provider.DoublesProvider
import it.neckar.open.provider.MultiProvider
import it.neckar.open.provider.asDoublesProvider1
import org.junit.jupiter.api.Test

/**
 * Tests the layout of [LabelPainter2] end to end (label filtering, deterministic dropping, collision-free placement).
 *
 * The [MockLayerPaintingContext] font metrics result in a line height of 19.0 - with [BoxStyle.none]
 * (no padding) every label is exactly 19.0 high. The default [LabelPainter2.Style.labelSpacing] is 3.0,
 * so the minimum center distance between two labels is 22.0.
 */
class LabelPainter2LayoutTest {
  private val painter = LabelPainter2(snapXValues = false, snapYValues = false)

  private val labelTexts = object : LabelsProvider<LabelIndex> {
    override fun valueAt(index: Int, param1: TextService, param2: I18nConfiguration): String {
      return "label $index"
    }
  }

  private fun layout(
    min: Double,
    max: Double,
    vararg labelLocations: Double,
    labelBoxStyles: MultiProvider<LabelIndex, BoxStyle> = MultiProvider.always(BoxStyle.none),
  ) {
    painter.layout(
      paintingContext = MockLayerPaintingContext(),
      labelLocations = DoublesProvider.forDoubles(*labelLocations).asDoublesProvider1<LayerPaintingContext>(),
      labelBoxStyles = labelBoxStyles,
      labelTexts = labelTexts,
      min = min,
      max = max,
    )
  }

  private fun assertLayoutIsCollisionFreeAndInside(min: Double, max: Double) {
    val labels = painter.layoutedLabels

    labels.forEachIndexed { index, label ->
      assertThat(label.actualMinY, "label $index top").isGreaterThanOrEqualTo(min - 1e-9)
      assertThat(label.actualMaxY, "label $index bottom").isLessThanOrEqualTo(max + 1e-9)

      if (index > 0) {
        assertThat(labels[index].overlapsActualY(labels[index - 1]), "labels ${index - 1}/$index overlap").isFalse()
      }
    }
  }

  @Test
  fun testEmptyInput() {
    layout(min = 0.0, max = 500.0)
    assertThat(painter.layoutedLabels.size).isEqualTo(0)
  }

  @Test
  fun testSingleLabel() {
    layout(min = 0.0, max = 500.0, 250.0)

    assertThat(painter.layoutedLabels.size).isEqualTo(1)
    assertThat(painter.layoutedLabels[0].actualCenterY).isEqualTo(250.0)
  }

  @Test
  fun testDenseClusterIsCollisionFree() {
    //Five labels with nearly identical preferred position
    layout(min = 0.0, max = 500.0, 200.0, 200.1, 200.2, 200.3, 200.4)

    assertThat(painter.layoutedLabels.size).isEqualTo(5)
    assertLayoutIsCollisionFreeAndInside(0.0, 500.0)

    //The cluster is centered on the centroid of the preferred positions
    assertThat(painter.layoutedLabels[2].actualCenterY).isCloseTo(200.2, 1e-9)
  }

  @Test
  fun testClusterAtTopEdge() {
    layout(min = 0.0, max = 500.0, 1.0, 2.0, 3.0)

    assertThat(painter.layoutedLabels.size).isEqualTo(3)
    assertLayoutIsCollisionFreeAndInside(0.0, 500.0)
  }

  @Test
  fun testClusterAtBottomEdge() {
    layout(min = 0.0, max = 500.0, 497.0, 498.0, 499.0)

    assertThat(painter.layoutedLabels.size).isEqualTo(3)
    assertLayoutIsCollisionFreeAndInside(0.0, 500.0)
  }

  @Test
  fun testLocationsOutsideMinMaxAreSkipped() {
    layout(min = 0.0, max = 500.0, -10.0, 250.0, 510.0)

    assertThat(painter.layoutedLabels.size).isEqualTo(1)
    assertThat(painter.layoutedLabels[0].index).isEqualTo(1)
  }

  @Test
  fun testNaNLocationsAreSkipped() {
    layout(min = 0.0, max = 500.0, Double.NaN, 250.0, Double.NaN)

    assertThat(painter.layoutedLabels.size).isEqualTo(1)
    assertThat(painter.layoutedLabels[0].index).isEqualTo(1)
  }

  @Test
  fun testOverflowDropsHighestIndicesFirst() {
    //100.0 available: 4 labels fit (4*19 + 3*3 = 85), 5 do not (5*19 + 4*3 = 107)
    layout(min = 0.0, max = 100.0, 50.0, 10.0, 90.0, 30.0, 70.0)

    assertThat(painter.layoutedLabels.size).isEqualTo(4)

    //The label with the highest index (lowest priority) is dropped - independent of its y location
    val keptIndices = painter.layoutedLabels.map { it.index }.sorted()
    assertThat(keptIndices).isEqualTo(listOf(0, 1, 2, 3))

    assertLayoutIsCollisionFreeAndInside(0.0, 100.0)
  }

  @Test
  fun testExtremeOverflowKeepsOnlyFittingLabels() {
    //Only one label fits: 2 labels require 2*19 + 3 = 41 > 25
    layout(min = 0.0, max = 25.0, 5.0, 10.0, 15.0, 20.0)

    assertThat(painter.layoutedLabels.size).isEqualTo(1)
    assertThat(painter.layoutedLabels[0].index).isEqualTo(0)
    assertLayoutIsCollisionFreeAndInside(0.0, 25.0)
  }

  @Test
  fun testOversizedLabelDoesNotEvictFittingLabels() {
    //Label 0 has 20.0 padding on each side: height 19 + 40 = 59 > 50 available - can never be painted.
    //It must be dropped itself instead of evicting the smaller labels 1 and 2 (2*19 + 3 = 41 <= 50).
    layout(
      min = 0.0, max = 50.0, 10.0, 20.0, 40.0,
      labelBoxStyles = MultiProvider.invoke { index ->
        if (index == 0) BoxStyle(padding = Insets.of(20.0)) else BoxStyle.none
      },
    )

    assertThat(painter.layoutedLabels.map { it.index }.sorted()).isEqualTo(listOf(1, 2))
    assertLayoutIsCollisionFreeAndInside(0.0, 50.0)
  }

  @Test
  fun testNegativeLabelSpacingIsTreatedAsZero() {
    painter.style.labelSpacing = -5.0

    //Three labels of height 19 fit exactly into 57 with spacing 0 (they touch, but do not overlap)
    layout(min = 0.0, max = 57.0, 10.0, 20.0, 30.0)

    val labels = painter.layoutedLabels
    assertThat(labels.size).isEqualTo(3)

    labels.forEachIndexed { index, label ->
      assertThat(label.actualMinY, "label $index top").isGreaterThanOrEqualTo(0.0 - 1e-9)
      assertThat(label.actualMaxY, "label $index bottom").isLessThanOrEqualTo(57.0 + 1e-9)

      if (index > 0) {
        //center distance of at least one label height = no overlap
        assertThat(label.actualCenterY - labels[index - 1].actualCenterY).isGreaterThanOrEqualTo(19.0 - 1e-9)
      }
    }
  }

  @Test
  fun testOrderIsPreserved() {
    layout(min = 0.0, max = 500.0, 300.0, 100.0, 305.0, 95.0, 302.0)

    assertThat(painter.layoutedLabels.size).isEqualTo(5)
    assertLayoutIsCollisionFreeAndInside(0.0, 500.0)

    //Labels are sorted by preferred y - the order of the preferred positions is kept for the actual positions
    assertThat(painter.layoutedLabels.map { it.index }).isEqualTo(listOf(3, 1, 0, 4, 2))
  }
}
