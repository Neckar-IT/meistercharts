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
package com.meistercharts.algorithms.layers.slippymap

import assertk.*
import assertk.assertions.*
import com.meistercharts.calc.ChartCalculator
import it.neckar.geometry.AxisOrientationY
import com.meistercharts.state.DefaultChartState
import com.meistercharts.tile.SubIndex
import com.meistercharts.tile.TileIndex
import com.meistercharts.annotations.DomainRelative
import com.meistercharts.maps.Latitude
import com.meistercharts.maps.Longitude
import com.meistercharts.maps.LatitudeBottomEdge
import com.meistercharts.maps.LatitudeTopEdge
import com.meistercharts.maps.LongitudeLeftEdge
import com.meistercharts.maps.LongitudeRightEdge
import com.meistercharts.maps.SlippyMapCenter
import com.meistercharts.maps.SlippyMapDefaultZoom
import com.meistercharts.maps.calculateSlippyMapContentAreaSize
import com.meistercharts.maps.computeLongitude
import com.meistercharts.maps.computeSlippyMapTileIndex
import com.meistercharts.maps.domainRelative2latitude
import com.meistercharts.maps.domainRelative2longitude
import com.meistercharts.maps.ensureSlippyMapBounds
import com.meistercharts.maps.tilesPerRowOrColumn
import com.meistercharts.maps.toDomainRelativeX
import com.meistercharts.maps.toDomainRelativeY
import com.meistercharts.maps.toSlippyMapZoom
import it.neckar.geometry.Size
import com.meistercharts.model.Zoom
import org.junit.jupiter.api.Test
import kotlin.math.pow
import kotlin.math.roundToInt

internal class SlippyMapTest {

  @Test
  internal fun testBorders() {
    assertThat(LatitudeTopEdge.value).isCloseTo(85.0511, 0.001)
    assertThat(LatitudeBottomEdge.value).isCloseTo(-85.0511, 0.001)
  }

  @Test
  fun longitudeLatitude2SlippyMapTileIndex() {
    // (0.0/0.0) should always be at the middle of all tiles
    assertThat(computeSlippyMapTileIndex(Latitude(0.0), Longitude(0.0), 0)).isEqualTo(TileIndex.of(0, 0))
    assertThat(computeSlippyMapTileIndex(Latitude(0.0), Longitude(0.0), 1)).isEqualTo(TileIndex.of(1, 1))
    assertThat(computeSlippyMapTileIndex(Latitude(0.0), Longitude(0.0), 2)).isEqualTo(TileIndex.of(2, 2))
    assertThat(computeSlippyMapTileIndex(Latitude(0.0), Longitude(0.0), 3)).isEqualTo(TileIndex.of(4, 4))
    assertThat(computeSlippyMapTileIndex(Latitude(0.0), Longitude(0.0), 18)).isEqualTo(TileIndex.of((2.0.pow(18) / 2.0).roundToInt(), (2.0.pow(18) / 2.0).roundToInt()))
  }

  @Test
  fun longitudeRoundTrip() {
    assertThat(computeLongitude(SubIndex(0), 2).value).isEqualTo(-180.0)
    assertThat(computeLongitude(SubIndex(1), 2).value).isEqualTo(-90.0)
    assertThat(computeLongitude(SubIndex(2), 2).value).isEqualTo(0.0)
    assertThat(computeLongitude(SubIndex(3), 2).value).isEqualTo(90.0)
    assertThat(computeLongitude(SubIndex(4), 2).value).isEqualTo(180.0)
    assertThat(computeLongitude(SubIndex(5), 2).value).isEqualTo(270.0)

    for (zoom in 0..18) {
      val tilesPerRow = tilesPerRowOrColumn(zoom)
      for (tileSubIndexX in 0 until tilesPerRow) {
        val longitude = computeLongitude(SubIndex(tileSubIndexX), zoom)
        val tileIndex = computeSlippyMapTileIndex(Latitude(0.0), longitude, zoom)

        assertThat(tileIndex.xAsDouble(), tileIndex.toString()).isCloseTo(tileSubIndexX.toDouble(), 0.001)
      }
    }
  }

  @Test
  fun testLong2DomainRelativeAndBack() {
    verifyLong2DomainRelativeRound(LongitudeLeftEdge, 0.0)
    verifyLong2DomainRelativeRound(LongitudeRightEdge, 1.0)
    verifyLong2DomainRelativeRound(Longitude(94.0), 0.7611111111111111)
    verifyLong2DomainRelativeRound(Longitude(11.0), 0.5305555555555556)
  }

  fun verifyLong2DomainRelativeRound(longitude: Longitude, expectedDomainRelative: @DomainRelative Double) {
    val domainRelative = longitude.toDomainRelativeX()
    assertThat(domainRelative, "").isCloseTo(expectedDomainRelative, 0.000001)

    //roundtrip
    assertThat(domainRelative2longitude(domainRelative).value).isCloseTo(longitude.value, 0.000001)
  }

  @Test
  fun testLatitude2DomainRelativeAndBack() {
    verifyLatitude2DomainRelativeRound(LatitudeTopEdge, 0.0)
    verifyLatitude2DomainRelativeRound(LatitudeBottomEdge, 1.0)
    assertThat(Latitude(80.0).toDomainRelativeY()).isEqualTo(0.11225939796299506)
    assertThat(Latitude(-80.0).toDomainRelativeY()).isEqualTo(0.8877406020370049)
    assertThat(Latitude(0.0).toDomainRelativeY()).isEqualTo(0.5)
    assertThat(Latitude(11.0).toDomainRelativeY()).isEqualTo(0.46925498967190327)
    assertThat(Latitude(-11.0).toDomainRelativeY()).isEqualTo(0.5307450103280967)

    verifyLatitude2DomainRelativeRound(Latitude(80.0), 0.11225939796299506)
    verifyLatitude2DomainRelativeRound(Latitude(-80.0), 0.8877406020370049)

    verifyLatitude2DomainRelativeRound(Latitude(0.0), 0.5)
    verifyLatitude2DomainRelativeRound(Latitude(11.0), 0.46925498967190327)
    verifyLatitude2DomainRelativeRound(Latitude(-11.0), 0.5307450103280967)
  }

  fun verifyLatitude2DomainRelativeRound(latitude: Latitude, expectedDomainRelative: @DomainRelative Double) {
    val domainRelative = latitude.toDomainRelativeY()
    assertThat(domainRelative, "domain relative").isCloseTo(expectedDomainRelative, 0.000001)

    //roundtrip
    assertThat(domainRelative2latitude(domainRelative).value).isCloseTo(latitude.value, 0.000001)
  }

  @Test
  fun longitude2DomainRelative() {
    assertThat(LongitudeLeftEdge.toDomainRelativeX()).isCloseTo(0.0, 0.000001)
    assertThat(LongitudeRightEdge.toDomainRelativeX()).isCloseTo(1.0, 0.000001)
    assertThat(Longitude(0.5 * LongitudeLeftEdge.value + 0.5 * LongitudeRightEdge.value).toDomainRelativeX()).isCloseTo(0.5, 0.000001)
    val tilesPerRow = tilesPerRowOrColumn(SlippyMapDefaultZoom)
    assertThat(tilesPerRow).isEqualTo(512)
    val longitudePerTile = 360.0 / tilesPerRow
    for (tileSubIndexX in 0 until tilesPerRow) {
      val longitude = computeLongitude(SubIndex(tileSubIndexX), SlippyMapDefaultZoom)
      assertThat(longitude.value).isCloseTo(LongitudeLeftEdge.value + tileSubIndexX * longitudePerTile, 0.000001)
      val domainRelativeX = tileSubIndexX / tilesPerRow.toDouble()
      assertThat(longitude.toDomainRelativeX(), "x=$tileSubIndexX, longitude=$longitude").isCloseTo(domainRelativeX, 0.000001)
    }
  }

  @Test
  fun latitude2DomainRelative() {
    assertThat(LatitudeTopEdge.toDomainRelativeY()).isCloseTo(0.0, 0.000001)
    assertThat(LatitudeBottomEdge.toDomainRelativeY()).isCloseTo(1.0, 0.000001)
    assertThat(Latitude(0.5 * LatitudeTopEdge.value + 0.5 * LatitudeBottomEdge.value).toDomainRelativeY()).isCloseTo(0.5, 0.000001) // equator
  }

  @Test
  fun domainRelative2contentArea() {
    val chartState = DefaultChartState()
    chartState.contentAreaSize = calculateSlippyMapContentAreaSize()
    chartState.windowSize = Size.of(1000.0, 1000.0)
    chartState.axisOrientationY = AxisOrientationY.OriginAtTop
    chartState.zoom = Zoom.of(4.0, 4.0)
    val chartCalculator = ChartCalculator(chartState)

    assertThat(chartCalculator.domainRelative2contentAreaX(0.0)).isCloseTo(0.0, 0.000001)
    assertThat(chartCalculator.domainRelative2contentAreaX(1.0)).isCloseTo(calculateSlippyMapContentAreaSize().width, 0.000001)
    assertThat(chartCalculator.domainRelative2contentAreaY(0.0)).isCloseTo(0.0, 0.000001)
    assertThat(chartCalculator.domainRelative2contentAreaY(1.0)).isCloseTo(calculateSlippyMapContentAreaSize().height, 0.000001)
  }

  @Test
  fun toSlippyMapZoom() {
    assertThat(Zoom(0.25, 0.25).toSlippyMapZoom()).isEqualTo(7)
    assertThat(Zoom(0.5, 0.5).toSlippyMapZoom()).isEqualTo(8)
    assertThat(Zoom(1.0, 1.0).toSlippyMapZoom()).isEqualTo(9)
    assertThat(Zoom(2.0, 2.0).toSlippyMapZoom()).isEqualTo(10)
    assertThat(Zoom(4.0, 4.0).toSlippyMapZoom()).isEqualTo(11)
  }

  @Test
  fun ensureSlippyMapBounds() {
    val slippyMapZoom = 3 // 2^3 x 2^3 = 8 x 8 tiles -> valid indices are 0..7
    assertThat(TileIndex.of(0, 0).ensureSlippyMapBounds(slippyMapZoom)).isEqualTo(TileIndex.of(0, 0))
    assertThat(TileIndex.of(7, 7).ensureSlippyMapBounds(slippyMapZoom)).isEqualTo(TileIndex.of(7, 7))
    assertThat(TileIndex.of(8, 8).ensureSlippyMapBounds(slippyMapZoom)).isEqualTo(TileIndex.of(0, 0))
    assertThat(TileIndex.of(-1, -1).ensureSlippyMapBounds(slippyMapZoom)).isEqualTo(TileIndex.of(7, 7))
    assertThat(TileIndex.of(-2, -2).ensureSlippyMapBounds(slippyMapZoom)).isEqualTo(TileIndex.of(6, 6))
    assertThat(TileIndex.of(-8, -8).ensureSlippyMapBounds(slippyMapZoom)).isEqualTo(TileIndex.of(0, 0))
  }

  @Test
  fun testTilesPerRowOrColumn() {
    assertThat(tilesPerRowOrColumn(0)).isEqualTo(1)
    assertThat(tilesPerRowOrColumn(1)).isEqualTo(2)
    assertThat(tilesPerRowOrColumn(2)).isEqualTo(4)
    assertThat(tilesPerRowOrColumn(19)).isEqualTo(524288)
  }

  @Test
  fun testSlippyMapCenter() {
    val chartState = DefaultChartState()
    chartState.contentAreaSize = calculateSlippyMapContentAreaSize()
    chartState.windowSize = Size.of(1000.0, 1000.0)
    chartState.axisOrientationY = AxisOrientationY.OriginAtTop
    val chartCalculator = ChartCalculator(chartState)

    // top / left
    SlippyMapCenter(LatitudeTopEdge, LongitudeLeftEdge, 9).defaultTranslation(chartCalculator).let {
      assertThat(it.x).isCloseTo(chartState.windowWidth * 0.5, 0.000001)
      assertThat(it.y).isCloseTo(chartState.windowHeight * 0.5, 0.000001)
    }
    // bottom right
    SlippyMapCenter(LatitudeBottomEdge, LongitudeRightEdge, 0).defaultTranslation(chartCalculator).let {
      assertThat(it.x).isCloseTo(-chartCalculator.contentArea2zoomedX(chartState.contentAreaWidth) + chartState.windowWidth * 0.5, 0.000001)
      assertThat(it.y).isCloseTo(-chartCalculator.contentArea2zoomedY(chartState.contentAreaHeight) + chartState.windowHeight * 0.5, 0.000001)
    }
  }

}

