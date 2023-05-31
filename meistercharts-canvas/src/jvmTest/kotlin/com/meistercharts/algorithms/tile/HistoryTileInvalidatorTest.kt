package com.meistercharts.algorithms.tile

import assertk.*
import assertk.assertions.*
import com.meistercharts.charts.ChartId
import com.meistercharts.model.Zoom
import org.junit.jupiter.api.Test

class HistoryTileInvalidatorTest {
  @Test
  fun testXHashCode() {
    val hashCodes = listOf(
      TileIdentifier(
        chartId = ChartId(17),
        tileIndex = TileIndex(1, 2, 3, 4),
        zoom = Zoom(1.0, 1.0)
      ).xDataHashCode(),


      TileIdentifier(
        chartId = ChartId(17),
        tileIndex = TileIndex(1, 3, 3, 4),
        zoom = Zoom(1.0, 1.0)
      ).xDataHashCode(),


      TileIdentifier(
        chartId = ChartId(17),
        tileIndex = TileIndex(2, 3, 3, 4),
        zoom = Zoom(1.0, 1.0)
      ).xDataHashCode(),


      TileIdentifier(
        chartId = ChartId(17),
        tileIndex = TileIndex(2, 2, 3, 4),
        zoom = Zoom(1.0, 1.0)
      ).xDataHashCode(),
    )

    assertThat(hashCodes.toSet()).hasSize(hashCodes.size)
  }
}
