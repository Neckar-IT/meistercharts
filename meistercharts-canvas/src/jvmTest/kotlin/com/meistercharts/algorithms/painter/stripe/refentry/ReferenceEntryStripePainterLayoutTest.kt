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
package com.meistercharts.algorithms.painter.stripe.refentry

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.canvas.mock.MockLayerPaintingContext
import com.meistercharts.history.DataSeriesId
import com.meistercharts.history.HistoryConfiguration
import com.meistercharts.history.HistoryEnumSet
import com.meistercharts.history.ReferenceEntryData
import com.meistercharts.history.ReferenceEntryDataSeriesIndex
import com.meistercharts.history.ReferenceEntryDifferentIdsCount
import com.meistercharts.history.ReferenceEntryId
import com.meistercharts.history.historyConfiguration
import org.junit.jupiter.api.Test

/**
 * Characterization test for the layout phase of [RectangleReferenceEntryStripePainter].
 *
 * Captures the current segment output of the stripe-painter layout phase (segment boundaries and per-segment values)
 * so a de-boxing refactor of the painting variables can be verified to be behavior-preserving.
 *
 * The `geometricalCenter` return of each layout call is [Double.NaN] here because the active timestamp is always
 * [Double.NaN]; capturing it also documents that contract.
 */
class ReferenceEntryStripePainterLayoutTest {
  private val dataSeriesIndex: ReferenceEntryDataSeriesIndex = ReferenceEntryDataSeriesIndex.zero

  private val historyConfiguration: HistoryConfiguration = historyConfiguration {
    referenceEntryDataSeries(DataSeriesId(23), "RefEntry", null)
  }

  @Test
  fun `equal id count and status merge into one segment`() {
    val painter = RectangleReferenceEntryStripePainter()
    val paintingContext: LayerPaintingContext = MockLayerPaintingContext()

    val entryData: ReferenceEntryData = ReferenceEntryData.create(1, "first")
    var geometricalCenter = Double.NaN

    painter.layoutBegin(paintingContext, height = 22.0, dataSeriesIndex = dataSeriesIndex, historyConfiguration = historyConfiguration)
    geometricalCenter = painter.layoutValueChange(paintingContext, dataSeriesIndex, 0.0, 100.0, 5000.0, 6000.0, Double.NaN, ReferenceEntryId(1), ReferenceEntryDifferentIdsCount(1), HistoryEnumSet.first, entryData)
    //Same id, count and status -> only extends the current segment (value4 is intentionally not part of the change check)
    geometricalCenter = painter.layoutValueChange(paintingContext, dataSeriesIndex, 100.0, 200.0, 6000.0, 7000.0, Double.NaN, ReferenceEntryId(1), ReferenceEntryDifferentIdsCount(1), HistoryEnumSet.first, entryData)
    //Different id -> new segment
    geometricalCenter = painter.layoutValueChange(paintingContext, dataSeriesIndex, 200.0, 300.0, 7000.0, 8000.0, Double.NaN, ReferenceEntryId(2), ReferenceEntryDifferentIdsCount(1), HistoryEnumSet.first, entryData)
    geometricalCenter = painter.layoutFinish(paintingContext, dataSeriesIndex)
    assertThat(geometricalCenter).isEqualTo(Double.NaN)

    val segments = painter.paintingVariables().forDataSeriesIndex(dataSeriesIndex).segments

    assertThat(segments.size).isEqualTo(3)

    assertThat(ReferenceEntryId(segments[0].id)).isEqualTo(ReferenceEntryId.NoValue)
    assertThat(segments[0].data).isNull()
    assertThat(segments[0].endX).isEqualTo(0.0)

    assertThat(ReferenceEntryId(segments[1].id)).isEqualTo(ReferenceEntryId(1))
    assertThat(ReferenceEntryDifferentIdsCount(segments[1].count)).isEqualTo(ReferenceEntryDifferentIdsCount(1))
    assertThat(HistoryEnumSet(segments[1].status)).isEqualTo(HistoryEnumSet.first)
    assertThat(segments[1].data).isEqualTo(entryData)
    assertThat(segments[1].startX).isEqualTo(0.0)
    assertThat(segments[1].endX).isEqualTo(200.0)

    assertThat(ReferenceEntryId(segments[2].id)).isEqualTo(ReferenceEntryId(2))
    assertThat(segments[2].startX).isEqualTo(200.0)
    assertThat(segments[2].endX).isEqualTo(300.0)
  }

  @Test
  fun `a changed status starts a new segment`() {
    val painter = RectangleReferenceEntryStripePainter()
    val paintingContext: LayerPaintingContext = MockLayerPaintingContext()

    var geometricalCenter = Double.NaN

    painter.layoutBegin(paintingContext, height = 22.0, dataSeriesIndex = dataSeriesIndex, historyConfiguration = historyConfiguration)
    geometricalCenter = painter.layoutValueChange(paintingContext, dataSeriesIndex, 0.0, 100.0, 5000.0, 6000.0, Double.NaN, ReferenceEntryId(1), ReferenceEntryDifferentIdsCount(1), HistoryEnumSet.first, null)
    //Same id and count, but different status -> new segment
    geometricalCenter = painter.layoutValueChange(paintingContext, dataSeriesIndex, 100.0, 200.0, 6000.0, 7000.0, Double.NaN, ReferenceEntryId(1), ReferenceEntryDifferentIdsCount(1), HistoryEnumSet.second, null)
    geometricalCenter = painter.layoutFinish(paintingContext, dataSeriesIndex)
    assertThat(geometricalCenter).isEqualTo(Double.NaN)

    val segments = painter.paintingVariables().forDataSeriesIndex(dataSeriesIndex).segments

    assertThat(segments.size).isEqualTo(3)
    assertThat(HistoryEnumSet(segments[1].status)).isEqualTo(HistoryEnumSet.first)
    assertThat(segments[1].endX).isEqualTo(100.0)
    assertThat(HistoryEnumSet(segments[2].status)).isEqualTo(HistoryEnumSet.second)
    assertThat(segments[2].startX).isEqualTo(100.0)
    assertThat(segments[2].endX).isEqualTo(200.0)
  }
}
