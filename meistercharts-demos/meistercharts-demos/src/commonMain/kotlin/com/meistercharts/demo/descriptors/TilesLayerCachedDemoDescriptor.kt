package com.meistercharts.demo.descriptors

import com.meistercharts.algorithms.layers.addClearBackground
import com.meistercharts.algorithms.layers.addTiles
import com.meistercharts.algorithms.tile.CachedTileProvider
import com.meistercharts.algorithms.tile.DebugTileProvider
import com.meistercharts.demo.ChartingDemo
import com.meistercharts.demo.ChartingDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.model.Size

/**
 */
class TilesLayerCachedDemoDescriptor : ChartingDemoDescriptor<Nothing> {
  override val name: String = "Tiles layer with Cached tiles provider"
  override val description: String = "## A demo of the tiles layer with a cached tiles provider"
  override val category: DemoCategory = DemoCategory.Layers

  override fun createDemo(configuration: PredefinedConfiguration<Nothing>?): ChartingDemo {
    return ChartingDemo {
      meistercharts {
        configure {
          layers.addClearBackground()
          layers.addTiles(CachedTileProvider(chartId, DebugTileProvider(Size(200.0, 200.0))))
        }
      }
    }
  }
}
