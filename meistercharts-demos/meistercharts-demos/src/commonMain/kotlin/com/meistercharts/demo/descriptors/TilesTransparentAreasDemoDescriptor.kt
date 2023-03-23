package com.meistercharts.demo.descriptors

import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.algorithms.layers.TilesLayer
import com.meistercharts.algorithms.layers.addBackgroundChecker
import com.meistercharts.algorithms.layers.addClearBackground
import com.meistercharts.algorithms.painter.Color
import com.meistercharts.algorithms.tile.Tile
import com.meistercharts.algorithms.tile.TileIdentifier
import com.meistercharts.algorithms.tile.TileProvider
import com.meistercharts.canvas.CanvasRenderingContext
import com.meistercharts.canvas.SnapConfiguration
import com.meistercharts.canvas.pixelSnapSupport
import com.meistercharts.charts.ContentViewportGestalt
import com.meistercharts.demo.ChartingDemo
import com.meistercharts.demo.ChartingDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.configurableColorPicker
import com.meistercharts.demo.configurableEnum
import com.meistercharts.demo.configurableInsetsSeparate
import com.meistercharts.demo.configurableList
import com.meistercharts.model.Insets
import com.meistercharts.model.Size

/**
 */
class TilesTransparentAreasDemoDescriptor : ChartingDemoDescriptor<Nothing> {
  override val name: String = "Tiles transparent background"
  override val category: DemoCategory = DemoCategory.Primitives

  override fun createDemo(configuration: PredefinedConfiguration<Nothing>?): ChartingDemo {
    return ChartingDemo {
      val possibleSizes = listOf(
        Size(200.0, 100.0),
        Size(100.0, 200.0),
        Size(100.0, 100.0),
        Size(101.0, 99.0)
      )

      meistercharts {
        val contentViewportGestalt = ContentViewportGestalt(Insets.all15)
        contentViewportGestalt.configure(this@meistercharts)

        configure {
          layers.addClearBackground()
          val tileProvider = BackgroundTileProvider()

          layers.addBackgroundChecker()
          layers.addLayer(TilesLayer(tileProvider))

          configurableList("Tile Size", Size(200.0, 200.0), possibleSizes) {
            onChange {
              tileProvider.tileSize = it
              markAsDirty()
            }
          }

          configurableColorPicker("background color", tileProvider::backgroundColor) {
          }

          configurableInsetsSeparate("Content Viewport", contentViewportGestalt::contentViewportMargin)

          configurableEnum("SnapConfiguration", chartSupport.pixelSnapSupport.snapConfiguration, SnapConfiguration.values()) {
            onChange {
              chartSupport.pixelSnapSupport.snapConfiguration = it
              markAsDirty()
            }
          }
        }
      }
    }
  }
}

private class BackgroundTileProvider : TileProvider {
  var backgroundColor: Color = Color.green.withAlpha(0.5)

  override var tileSize: Size = Size.of(200.0, 200.0)

  override fun getTile(identifier: TileIdentifier): Tile {
    return object : Tile {
      override val identifier: TileIdentifier
        get() = identifier

      override val tileSize: Size
        get() = this@BackgroundTileProvider.tileSize

      override fun paint(gc: CanvasRenderingContext, paintingContext: LayerPaintingContext) {
        gc.fill(backgroundColor)
        gc.fillRect(0.0, 0.0, tileSize.width, tileSize.height)
      }
    }
  }
}
