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
package com.meistercharts.algorithms.painter.stripe.enums

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.canvas.mock.MockLayerPaintingContext
import com.meistercharts.history.DataSeriesId
import com.meistercharts.history.EnumDataSeriesIndex
import com.meistercharts.history.HistoryConfiguration
import com.meistercharts.history.HistoryEnum
import com.meistercharts.history.HistoryEnumOrdinal
import com.meistercharts.history.HistoryEnumSet
import com.meistercharts.history.historyConfiguration
import com.meistercharts.loop.PaintingLoopIndex
import org.junit.jupiter.api.Test

/**
 * Characterization test for the layout phase of [RectangleEnumStripePainter].
 *
 * Captures the current segment output of the stripe-painter layout phase (segment boundaries and per-segment values)
 * so a de-boxing refactor of the painting variables can be verified to be behavior-preserving.
 *
 * Only the layout phase (layoutBegin/layoutValueChange/layoutFinish) is exercised - the paint phase is not,
 * because the segment buffer produced during layout is the state affected by the storage change.
 *
 * The `geometricalCenter` return of each layout call is [Double.NaN] here because the active timestamp is always
 * [Double.NaN]; capturing it also documents that contract.
 */
class EnumStripePainterLayoutTest {
  private val dataSeriesIndex: EnumDataSeriesIndex = EnumDataSeriesIndex.zero

  private val historyConfiguration: HistoryConfiguration = historyConfiguration {
    enumDataSeries(DataSeriesId(17), "Enum", HistoryEnum.createSimple("Simple", listOf("zero", "one", "two", "three", "four")))
  }

  @Test
  fun `consecutive equal values merge into one segment`() {
    val painter = RectangleEnumStripePainter { aggregationMode = EnumAggregationMode.ByOrdinal }
    val paintingContext: LayerPaintingContext = MockLayerPaintingContext()

    var geometricalCenter = Double.NaN

    painter.layoutBegin(paintingContext, height = 22.0, dataSeriesIndex = dataSeriesIndex, historyConfiguration = historyConfiguration)
    geometricalCenter = painter.layoutValueChange(paintingContext, dataSeriesIndex, 0.0, 100.0, 5000.0, 6000.0, Double.NaN, HistoryEnumSet.first, HistoryEnumOrdinal.First)
    //Same value as before -> only extends the current segment, no new segment
    geometricalCenter = painter.layoutValueChange(paintingContext, dataSeriesIndex, 100.0, 200.0, 6000.0, 7000.0, Double.NaN, HistoryEnumSet.first, HistoryEnumOrdinal.First)
    geometricalCenter = painter.layoutValueChange(paintingContext, dataSeriesIndex, 200.0, 300.0, 7000.0, 8000.0, Double.NaN, HistoryEnumSet.second, HistoryEnumOrdinal.Second)
    geometricalCenter = painter.layoutFinish(paintingContext, dataSeriesIndex)
    assertThat(geometricalCenter).isEqualTo(Double.NaN)

    val segments = painter.paintingVariables().forDataSeriesIndex(dataSeriesIndex).segments

    //segment[0]: the initial NoValue segment that precedes the first real value
    //segment[1]: the merged "first" value spanning 0..200 (two equal value changes)
    //segment[2]: the "second" value spanning 200..300, flushed by layoutFinish
    assertThat(segments.size).isEqualTo(3)

    assertThat(HistoryEnumSet(segments[0].enumSet)).isEqualTo(HistoryEnumSet.NoValue)
    assertThat(segments[0].endX).isEqualTo(0.0)

    assertThat(HistoryEnumSet(segments[1].enumSet)).isEqualTo(HistoryEnumSet.first)
    assertThat(HistoryEnumOrdinal(segments[1].ordinal)).isEqualTo(HistoryEnumOrdinal.First)
    assertThat(segments[1].startX).isEqualTo(0.0)
    assertThat(segments[1].endX).isEqualTo(200.0)

    assertThat(HistoryEnumSet(segments[2].enumSet)).isEqualTo(HistoryEnumSet.second)
    assertThat(HistoryEnumOrdinal(segments[2].ordinal)).isEqualTo(HistoryEnumOrdinal.Second)
    assertThat(segments[2].startX).isEqualTo(200.0)
    assertThat(segments[2].endX).isEqualTo(300.0)
  }

  @Test
  fun `most-time aggregation compares the ordinal not the set`() {
    val painter = RectangleEnumStripePainter { aggregationMode = EnumAggregationMode.MostTime }
    val paintingContext: LayerPaintingContext = MockLayerPaintingContext()

    var geometricalCenter = Double.NaN

    painter.layoutBegin(paintingContext, height = 22.0, dataSeriesIndex = dataSeriesIndex, historyConfiguration = historyConfiguration)
    geometricalCenter = painter.layoutValueChange(paintingContext, dataSeriesIndex, 0.0, 100.0, 5000.0, 6000.0, Double.NaN, HistoryEnumSet(0b101), HistoryEnumOrdinal.First)
    //Different set, but same most-time ordinal -> no new segment in MostTime mode
    geometricalCenter = painter.layoutValueChange(paintingContext, dataSeriesIndex, 100.0, 200.0, 6000.0, 7000.0, Double.NaN, HistoryEnumSet(0b111), HistoryEnumOrdinal.First)
    //Different most-time ordinal -> new segment
    geometricalCenter = painter.layoutValueChange(paintingContext, dataSeriesIndex, 200.0, 300.0, 7000.0, 8000.0, Double.NaN, HistoryEnumSet(0b111), HistoryEnumOrdinal.Second)
    geometricalCenter = painter.layoutFinish(paintingContext, dataSeriesIndex)
    assertThat(geometricalCenter).isEqualTo(Double.NaN)

    val segments = painter.paintingVariables().forDataSeriesIndex(dataSeriesIndex).segments

    assertThat(segments.size).isEqualTo(3)

    assertThat(HistoryEnumOrdinal(segments[1].ordinal)).isEqualTo(HistoryEnumOrdinal.First)
    assertThat(segments[1].startX).isEqualTo(0.0)
    assertThat(segments[1].endX).isEqualTo(200.0)

    assertThat(HistoryEnumOrdinal(segments[2].ordinal)).isEqualTo(HistoryEnumOrdinal.Second)
    assertThat(segments[2].startX).isEqualTo(200.0)
    assertThat(segments[2].endX).isEqualTo(300.0)
  }

  @Test
  fun `a new painting loop resets the segments`() {
    val painter = RectangleEnumStripePainter { aggregationMode = EnumAggregationMode.ByOrdinal }

    var geometricalCenter = Double.NaN

    val firstLoop: LayerPaintingContext = MockLayerPaintingContext(loopIndex = PaintingLoopIndex(0))
    painter.layoutBegin(firstLoop, height = 22.0, dataSeriesIndex = dataSeriesIndex, historyConfiguration = historyConfiguration)
    geometricalCenter = painter.layoutValueChange(firstLoop, dataSeriesIndex, 0.0, 100.0, 5000.0, 6000.0, Double.NaN, HistoryEnumSet.first, HistoryEnumOrdinal.First)
    geometricalCenter = painter.layoutFinish(firstLoop, dataSeriesIndex)
    assertThat(painter.paintingVariables().forDataSeriesIndex(dataSeriesIndex).segments.size).isEqualTo(2)

    val secondLoop: LayerPaintingContext = MockLayerPaintingContext(loopIndex = PaintingLoopIndex(1))
    painter.layoutBegin(secondLoop, height = 22.0, dataSeriesIndex = dataSeriesIndex, historyConfiguration = historyConfiguration)
    geometricalCenter = painter.layoutValueChange(secondLoop, dataSeriesIndex, 0.0, 100.0, 5000.0, 6000.0, Double.NaN, HistoryEnumSet.second, HistoryEnumOrdinal.Second)
    geometricalCenter = painter.layoutFinish(secondLoop, dataSeriesIndex)
    assertThat(geometricalCenter).isEqualTo(Double.NaN)

    val segments = painter.paintingVariables().forDataSeriesIndex(dataSeriesIndex).segments
    assertThat(segments.size).isEqualTo(2)
    assertThat(HistoryEnumSet(segments[1].enumSet)).isEqualTo(HistoryEnumSet.second)
  }
}
