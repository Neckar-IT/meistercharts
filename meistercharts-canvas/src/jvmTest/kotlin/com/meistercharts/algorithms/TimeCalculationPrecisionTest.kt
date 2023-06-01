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
package com.meistercharts.algorithms

import assertk.*
import assertk.assertions.*
import com.meistercharts.algorithms.impl.DefaultChartState
import com.meistercharts.algorithms.tile.MainIndex
import com.meistercharts.algorithms.tile.SubIndex
import com.meistercharts.algorithms.tile.TileIndex
import com.meistercharts.annotations.ContentArea
import com.meistercharts.model.Size
import it.neckar.open.formatting.formatUtc
import it.neckar.open.kotlin.lang.floor
import it.neckar.open.time.TimeConstants
import it.neckar.open.time.toDoubleMillis
import it.neckar.open.unit.si.ms
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import kotlin.time.Duration.Companion.milliseconds

class TimeCalculationPrecisionTest {
  @Disabled
  @Test
  fun testSample() {
    /**
     * Zeitpunkt an Origin
     */
    val referenceTimestamp: @ms Double = 0.0

    /**
     * Millis, welche bei Zoom 1.0 1000 Pixeln entsprechen bei Zoom 1.0
     *
     * Einheit: Millis/1000 Pixel
     */
    @ms val contentAreaDuration = 60_000.0 //Umrechnungsfaktor zwischen kalkulatorischen Pixeln und Zeit
    //@ms val contentAreaDuration = 1000.0 * 60 * 60 * 24 //Umrechnungsfaktor zwischen kalkulatorischen Pixeln und Zeit
    //@ms val contentAreaDuration = 1000.0 * 60 * 60 * 24 * 365 //Umrechnungsfaktor zwischen kalkulatorischen Pixeln und Zeit

    @ms val myTime = ZonedDateTime.of(2023, 5, 14, 16, 55, 7, 0, ZoneId.systemDefault()).toDoubleMillis()
    @ms val deltaTimeToReference = myTime - referenceTimestamp

    @ContentArea val distanceInPixelsFromOrigin = deltaTimeToReference / contentAreaDuration * 1000.0
    println("distanceInPixelsFromOrigin: $distanceInPixelsFromOrigin")

    //Ergibt sich aus physical Pixels + Zoom-Factor usw.
    //@ContentArea val tileSize = 17.34
    @ContentArea val tileSize = 400.0

    val naiveTileIndex = (distanceInPixelsFromOrigin / tileSize).floor().toInt()
    println("naiveTileIndex: $naiveTileIndex") //Int.MAX_VALUE!

    @ContentArea val quadrantSize = 1_000.0
    val quadrantIndex: Int = (distanceInPixelsFromOrigin / quadrantSize).floor().toInt()

    println("quadrantIndex: $quadrantIndex")
    val tileIndex: Int = ((distanceInPixelsFromOrigin % quadrantSize) / tileSize).floor().toInt()
    println("tileIndex: $tileIndex")

    println("----------")
    println("reverse: ${tileIndex * tileSize + quadrantIndex * quadrantSize}")

    @ContentArea val minSupportedTileSize = quadrantSize / Int.MAX_VALUE
    println("minSupportedTileSize: $minSupportedTileSize ContentArea px")

    @ms val minSupportedTileTime = minSupportedTileSize * contentAreaDuration / 1000
    println("minSupportedTileTime: $minSupportedTileTime ms")
    println("Hz: ${1000.0 / minSupportedTileTime} Hz")


    @ms val timePerQuadrant = quadrantSize * contentAreaDuration / 1000.0
    println("timePerQuadrant: $timePerQuadrant ${timePerQuadrant.milliseconds}")

    println("Oldest Time: ${Instant.ofEpochMilli((referenceTimestamp + Int.MIN_VALUE * timePerQuadrant).toLong())}")
    println("Newest Time: ${Instant.ofEpochMilli((referenceTimestamp + Int.MAX_VALUE * timePerQuadrant).toLong())}")




    return

    /**
     * Das ist der Abstand in "kalkulatorischen Pixeln" (bei Zoom 1.0) vom Referenz-Zeitpunkt
     */
    //val contentAreaCoordinateX = 100_000_351_350.0 //
    //
    //val tileWidth = 0.0004 //je höher der Zoom, desto kleiner!
    //
    //run {
    //  val div = contentAreaCoordinateX / tileWidth
    //  println("naive tile index: $div ${div.toInt()}")
    //}
    //
    //
    //val quadrantSize = 1_000_000.0
    //val quadrantIndexAsDouble = contentAreaCoordinateX / (quadrantSize / tileWidth)
    //val quadrantIndex = quadrantIndexAsDouble.floor()
    //println("quad index: $quadrantIndexAsDouble")
    //println("quad index as int: ${quadrantIndex.floor()}")
    //
    //val quadrantOrigin = quadrantIndex * quadrantSize
    //val distanceInQuadrant = contentAreaCoordinateX - quadrantOrigin
    //
    //val index = distanceInQuadrant / tileWidth
    //
    //println("index: $index")
  }

  @Test
  fun testMaxValues() {
    assertThat(TimeConstants.referenceTimestamp.formatUtc()).isEqualTo("2024-01-01T00:00:00.000")

    val contentAreaTimeRange = TimeRange.oneMinuteSinceReference

    assertThat(contentAreaTimeRange.start.formatUtc()).isEqualTo("2024-01-01T00:00:00.000")
    assertThat(contentAreaTimeRange.end.formatUtc()).isEqualTo("2024-01-01T00:01:00.000")
    assertThat(contentAreaTimeRange.delta).isEqualTo(60_000.0) //60 seconds per 1000 pixels

    val chartState = DefaultChartState()
    chartState.contentAreaWidth = 1000.0

    val tileSize = Size.of(314.96062992125985, 314.96062992125985) //a little less than 1/3 of the content area width

    @ms val millisPerTile = 18_897.63779527559
    assertThat(contentAreaTimeRange.delta / 1000.0 * tileSize.width).isEqualTo(millisPerTile) //millis per tile


    assertThat(Int.MAX_VALUE * millisPerTile).isEqualTo(40_582_368_132_283.46) //the max duration using only an int index
    assertThat(40_582_368_132_283.46.milliseconds.inWholeDays).isEqualTo(469_703) //days when using int
    assertThat(40_582_368_132_283.46.milliseconds.inWholeDays / 365).isEqualTo(1286) //years when using int


    //Origin
    TileChartCalculator(chartState, TileIndex.Origin, tileSize).let { tileChartCalculator ->
      assertThat(tileChartCalculator.zoomed2contentAreaX(tileSize.width)).isEqualTo(tileSize.width) //zoom is 1.0

      assertThat(tileChartCalculator.tileOrigin2contentAreaX()).isEqualTo(0.0)
      assertThat(tileChartCalculator.origin2contentAreaRelativeX()).isEqualTo(0.0)
      assertThat(tileChartCalculator.tileOrigin2timeX(contentAreaTimeRange).formatUtc()).isEqualTo("2024-01-01T00:00:00.000")
    }

    //Max
    TileChartCalculator(chartState, TileIndex.Max, tileSize).let { tileChartCalculator ->
      assertThat(tileChartCalculator.zoomed2contentAreaX(tileSize.width)).isEqualTo(tileSize.width) //zoom is 1.0

      assertThat(tileChartCalculator.tileOrigin2contentAreaX()).isEqualTo(6.7637280251937E14)
      assertThat(tileChartCalculator.origin2contentAreaRelativeX()).isEqualTo(6.7637280251937E11)
      assertThat(tileChartCalculator.tileOrigin2timeX(contentAreaTimeRange).formatUtc()).isEqualTo("+1288028-01-20T01:59:22.200")
    }

    //Min
    TileChartCalculator(chartState, TileIndex.Min, tileSize).let { tileChartCalculator ->
      assertThat(tileChartCalculator.zoomed2contentAreaX(tileSize.width)).isEqualTo(tileSize.width) //zoom is 1.0

      assertThat(tileChartCalculator.tileOrigin2contentAreaX()).isEqualTo(-6.76372802519685E14)
      assertThat(tileChartCalculator.origin2contentAreaRelativeX()).isEqualTo(-6.76372802519685E11)
      assertThat(tileChartCalculator.tileOrigin2timeX(contentAreaTimeRange).formatUtc()).isEqualTo("-1283981-12-12T22:00:18.896")
    }

    assertThat(TileChartCalculator(chartState, TileIndex(0, 0, 0, 0), tileSize).visibleTimeRangeXinTile(contentAreaTimeRange).format()).isEqualTo("2024-01-01T00:00:00.000 - 2024-01-01T00:00:18.897")
    assertThat(TileChartCalculator(chartState, TileIndex(0, 1, 0, 0), tileSize).visibleTimeRangeXinTile(contentAreaTimeRange).format()).isEqualTo("2024-01-01T00:00:18.897 - 2024-01-01T00:00:37.795")


    TileIndex(MainIndex.Max, SubIndex.Max, MainIndex.Zero, SubIndex.Zero).let { tileIndex ->
      assertThat(tileIndex.xAsDouble()).isEqualTo(2147483647999.0)
      assertThat(tileIndex.xAsDouble()).isGreaterThan(Integer.MAX_VALUE.toDouble())
      assertThat(tileIndex.xAsDouble()).isEqualTo(Integer.MAX_VALUE * 1000.0 + 999)
    }

    //Estimate integer duration

    assertThat(TileChartCalculator(chartState, TileIndex(MainIndex.Max, SubIndex.Max, MainIndex.Zero, SubIndex.Zero), tileSize).visibleTimeRangeXinTile(contentAreaTimeRange).format()).isEqualTo("+1288028-01-20T01:59:22.200 - +1288028-01-20T01:59:41.096")
    assertThat(TileChartCalculator(chartState, TileIndex(MainIndex.Min, SubIndex.Min, MainIndex.Zero, SubIndex.Zero), tileSize).visibleTimeRangeXinTile(contentAreaTimeRange).format()).isEqualTo("-1283981-12-12T22:00:18.896 - -1283981-12-12T22:00:37.792")
  }
}
