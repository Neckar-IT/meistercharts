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
package com.meistercharts.algorithms.tile

import assertk.*
import assertk.assertions.*
import com.meistercharts.algorithms.ChartCalculator
import com.meistercharts.algorithms.InternalCalculations
import com.meistercharts.algorithms.impl.DefaultChartState
import com.meistercharts.annotations.ContentArea
import com.meistercharts.model.Coordinates
import com.meistercharts.model.Distance
import com.meistercharts.model.Size
import org.junit.jupiter.api.Test

/**
 */
class TilesCalculationTest {
  @Test
  internal fun testIt() {
    val chartState = DefaultChartState()
    chartState.contentAreaSize = Size(800.0, 600.0)

    val calculator = ChartCalculator(chartState)

    assertThat(calculator.chartState.contentAreaSize.width).isEqualTo(800.0)
    assertThat(calculator.chartState.contentAreaSize.height).isEqualTo(600.0)
  }

  @Test
  internal fun testTileCoordinatesCalc() {
    assertThat(InternalCalculations.calculateTileOrigin(TileIndex(0, 0), Size.zero))
      .isEqualTo(Coordinates.origin)

    assertThat(InternalCalculations.calculateTileOrigin(TileIndex(0, 0), Size.of(100.0, 100.0)))
      .isEqualTo(Coordinates.origin)

    assertThat(InternalCalculations.calculateTileOrigin(TileIndex(1, 0), Size.of(100.0, 100.0)))
      .isEqualTo(Coordinates.of(100.0, 0.0))

    assertThat(InternalCalculations.calculateTileOrigin(TileIndex(2, 0), Size.of(100.0, 100.0)))
      .isEqualTo(Coordinates.of(200.0, 0.0))

    assertThat(InternalCalculations.calculateTileOrigin(TileIndex(2, -7), Size.of(100.0, 100.0)))
      .isEqualTo(Coordinates.of(200.0, -700.0))
  }

  @Test
  internal fun testWithCalculator() {
    val chartState = DefaultChartState()
    chartState.contentAreaSize = Size(800.0, 600.0)

    val tileSize = Size(200.0, 150.0)
    val calculator = ChartCalculator(chartState)

    assertThat(calculator.tileIndex2contentArea(TileIndex(0, 0), tileSize)).isEqualTo(Coordinates.of(0.0, 0.0))
    assertThat(calculator.tileIndex2contentArea(TileIndex(1, 0), tileSize)).isEqualTo(Coordinates.of(200.0, 0.0))
    assertThat(calculator.tileIndex2contentArea(TileIndex(0, 1), tileSize)).isEqualTo(Coordinates.of(0.0, 150.0))

    chartState.zoomX = 2.0
    chartState.zoomY = 2.0

    assertThat(calculator.tileIndex2contentArea(TileIndex(0, 0), tileSize)).isEqualTo(Coordinates.of(0.0, 0.0))
    assertThat(calculator.tileIndex2contentArea(TileIndex(1, 0), tileSize)).isEqualTo(Coordinates.of(100.0, 0.0))
    assertThat(calculator.tileIndex2contentArea(TileIndex(0, 1), tileSize)).isEqualTo(Coordinates.of(0.0, 75.0))

    //now translate
    chartState.windowTranslation = Distance.of(17.0, 14.0)

    //translation does not have any effects
    assertThat(calculator.tileIndex2contentArea(TileIndex(0, 0), tileSize)).isEqualTo(Coordinates.of(0.0, 0.0))
    assertThat(calculator.tileIndex2contentArea(TileIndex(1, 0), tileSize)).isEqualTo(Coordinates.of(100.0, 0.0))
    assertThat(calculator.tileIndex2contentArea(TileIndex(0, 1), tileSize)).isEqualTo(Coordinates.of(0.0, 75.0))
  }

  @Test
  internal fun testWithCalculatorWindow() {
    val chartState = DefaultChartState()
    chartState.contentAreaSize = Size(800.0, 600.0)

    val tileSize = Size(200.0, 150.0)
    val calculator = ChartCalculator(chartState)

    assertThat(calculator.tileIndex2window(TileIndex(0, 0), tileSize)).isEqualTo(Coordinates.of(0.0, 0.0))
    assertThat(calculator.tileIndex2window(TileIndex(1, 0), tileSize)).isEqualTo(Coordinates.of(200.0, 0.0))
    assertThat(calculator.tileIndex2window(TileIndex(0, 1), tileSize)).isEqualTo(Coordinates.of(0.0, 150.0))

    chartState.zoomX = 2.0
    chartState.zoomY = 2.0

    //Window coordinates are not dependent of the zoom factor
    assertThat(calculator.tileIndex2window(TileIndex(0, 0), tileSize)).isEqualTo(Coordinates.of(0.0, 0.0))
    assertThat(calculator.tileIndex2window(TileIndex(1, 0), tileSize)).isEqualTo(Coordinates.of(200.0, 0.0))
    assertThat(calculator.tileIndex2window(TileIndex(0, 1), tileSize)).isEqualTo(Coordinates.of(0.0, 150.0))


    //now translate the content to bottom/left
    chartState.windowTranslation = Distance.of(17.0, 14.0)

    assertThat(calculator.tileIndex2window(TileIndex(0, 0), tileSize)).isEqualTo(Coordinates.of(17.0, 14.0))
    assertThat(calculator.tileIndex2window(TileIndex(1, 0), tileSize)).isEqualTo(Coordinates.of(217.0, 14.0))
    assertThat(calculator.tileIndex2window(TileIndex(0, 1), tileSize)).isEqualTo(Coordinates.of(17.0, 164.0))
  }

  @Test
  internal fun testChartContent2TileIndex() {
    @ContentArea val tileSize = Size.of(100.0, 100.0)
    assertThat(InternalCalculations.calculateTileIndex(Coordinates(0, 0), tileSize))
      .isEqualTo(TileIndex.origin)

    assertThat(InternalCalculations.calculateTileIndex(Coordinates(99, 0), tileSize))
      .isEqualTo(TileIndex.origin)
    assertThat(InternalCalculations.calculateTileIndex(Coordinates(100, 0), tileSize))
      .isEqualTo(TileIndex.of(1, 0))
    assertThat(InternalCalculations.calculateTileIndex(Coordinates(101, 0), tileSize))
      .isEqualTo(TileIndex.of(1, 0))

    assertThat(InternalCalculations.calculateTileIndex(Coordinates(199, 0), tileSize))
      .isEqualTo(TileIndex.of(1, 0))
    assertThat(InternalCalculations.calculateTileIndex(Coordinates(200, 0), tileSize))
      .isEqualTo(TileIndex.of(2, 0))
    assertThat(InternalCalculations.calculateTileIndex(Coordinates(201, 0), tileSize))
      .isEqualTo(TileIndex.of(2, 0))

    assertThat(InternalCalculations.calculateTileIndex(Coordinates(-1, 0), tileSize))
      .isEqualTo(TileIndex.of(-1, 0))
    assertThat(InternalCalculations.calculateTileIndex(Coordinates(-99, 0), tileSize))
      .isEqualTo(TileIndex.of(-1, 0))
    assertThat(InternalCalculations.calculateTileIndex(Coordinates(-100, 0), tileSize))
      .isEqualTo(TileIndex.of(-1, 0))
    assertThat(InternalCalculations.calculateTileIndex(Coordinates(-101, 0), tileSize))
      .isEqualTo(TileIndex.of(-2, 0))


    //Y coord

    assertThat(InternalCalculations.calculateTileIndex(Coordinates(0, 99), tileSize))
      .isEqualTo(TileIndex.of(0, 0))
    assertThat(InternalCalculations.calculateTileIndex(Coordinates(0, 100), tileSize))
      .isEqualTo(TileIndex.of(0, 1))
    assertThat(InternalCalculations.calculateTileIndex(Coordinates(0, 101), tileSize))
      .isEqualTo(TileIndex.of(0, 1))
  }

  @Test
  internal fun testCalculateInTileCoords() {
    val tileSize = Size.of(100.0, 100.0)

    assertThat(InternalCalculations.calculateCoordsInTile(Coordinates(0, 0), tileSize))
      .isEqualTo(Coordinates(0, 0))

    assertThat(InternalCalculations.calculateCoordsInTile(Coordinates(1, 2), tileSize))
      .isEqualTo(Coordinates(1, 2))

    assertThat(InternalCalculations.calculateCoordsInTile(Coordinates(100, 2000), tileSize))
      .isEqualTo(Coordinates(0, 0))
    assertThat(InternalCalculations.calculateCoordsInTile(Coordinates(107, 2013), tileSize))
      .isEqualTo(Coordinates(7, 13))
  }

  @Test
  internal fun testTileCoordinates() {
    val tileSize = Size.of(100.0, 100.0)

    assertThat(InternalCalculations.contentArea2TileCoordinates(Coordinates(0, 0), tileSize))
      .isEqualTo(TileCoordinates.of(TileIndex(0, 0), Coordinates(0.0, 0.0)))

    assertThat(InternalCalculations.contentArea2TileCoordinates(Coordinates(1, 2), tileSize))
      .isEqualTo(TileCoordinates.of(TileIndex(0, 0), Coordinates(1.0, 2.0)))

    assertThat(InternalCalculations.contentArea2TileCoordinates(Coordinates(101, 2), tileSize))
      .isEqualTo(TileCoordinates.of(TileIndex(1, 0), Coordinates(1.0, 2.0)))
  }
}
