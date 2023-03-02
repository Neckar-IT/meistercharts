package com.meistercharts.demo.descriptors

import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.algorithms.layers.slippymap.PaintableOnSlippyMap
import com.meistercharts.algorithms.layers.slippymap.SlippyMapCenter
import com.meistercharts.algorithms.painter.UrlPaintable
import com.meistercharts.canvas.paintLocation
import com.meistercharts.canvas.paintable.CombinedPaintable
import com.meistercharts.canvas.paintable.Paintable
import com.meistercharts.charts.MapWithPaintablesGestalt
import com.meistercharts.demo.ChartingDemo
import com.meistercharts.demo.ChartingDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.model.Distance
import com.meistercharts.model.Rectangle
import com.meistercharts.model.Size
import com.meistercharts.resources.svg.SvgPaintableProviders

/**
 *
 */
class MapWithNeckarITLocationDemoDescriptor : ChartingDemoDescriptor<Nothing> {
  override val name: String = "Map with Neckar IT logo"
  override val description: String = "POC how to add a Neckar IT logo"

  override val category: DemoCategory = DemoCategory.Layers

  override fun createDemo(configuration: PredefinedConfiguration<Nothing>?): ChartingDemo {
    return ChartingDemo {
      meistercharts {

        val logoSize = Size(65, 45)
        val logo = UrlPaintable.fixedSize("https://neckar.it/logo/nit-logo-n-pos.png", logoSize)

        val mapMarker = SvgPaintableProviders.mapMarker

        val markerSize = Size.PX_120
        val secondaryOffset = Distance.of(
          -10.0,
          -20.0
        )

        /**
         * ATTENTION:
         * This code is just a POC. It is not production ready currently!
         */

        val combinedPaintable = CombinedPaintable(mapMarker.get(markerSize), logo, secondaryOffset).atBottomCenter()
        val neckarItLogoPaintable = PaintableOnSlippyMap(SlippyMapCenter.neckarItCenter.latitude, SlippyMapCenter.neckarItCenter.longitude, combinedPaintable)

        val mapGestalt = MapWithPaintablesGestalt(chartId, MapWithPaintablesGestalt.Data().also {
          it.paintables = listOf(neckarItLogoPaintable)
        })

        mapGestalt.configure(this)
      }
    }
  }
}

/**
 * Returns a new paintable that wraps the given paintable and moves the alignment point to the center (from top left)
 */
private fun Paintable.atCenter(): Paintable {
  val originalPaintable = this

  return object : Paintable {
    override fun boundingBox(paintingContext: LayerPaintingContext): Rectangle {
      return originalPaintable.boundingBox(paintingContext).let {
        it.move(-it.getWidth() / 2.0, -it.getHeight() / 2.0)
      }
    }

    override fun paint(paintingContext: LayerPaintingContext, x: Double, y: Double) {
      val boundingBox = boundingBox(paintingContext)

      paintingContext.gc.translate(-boundingBox.getWidth() / 2.0, -boundingBox.getHeight() / 2.0)
      paintingContext.gc.paintLocation(x, y)
      originalPaintable.paint(paintingContext, x, y)
    }
  }
}

private fun Paintable.atBottomCenter(): Paintable {
  val originalPaintable = this

  return object : Paintable {
    override fun boundingBox(paintingContext: LayerPaintingContext): Rectangle {
      return originalPaintable.boundingBox(paintingContext).let {
        it.move(-it.getWidth() / 2.0, -it.getHeight())
      }
    }

    override fun paint(paintingContext: LayerPaintingContext, x: Double, y: Double) {
      val boundingBox = boundingBox(paintingContext)

      paintingContext.gc.translate(-boundingBox.getWidth() / 2.0, -boundingBox.getHeight())
      paintingContext.gc.paintLocation(x, y)
      originalPaintable.paint(paintingContext, x, y)
    }
  }
}

