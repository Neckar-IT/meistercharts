package com.meistercharts.demo.descriptors

import com.meistercharts.algorithms.layers.AbstractLayer
import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.algorithms.layers.LayerType
import com.meistercharts.algorithms.layers.addClearBackground
import com.meistercharts.algorithms.painter.UrlPaintable
import com.meistercharts.canvas.StyleDsl
import com.meistercharts.canvas.devicePixelRatio
import com.meistercharts.canvas.paintable.Paintable
import com.meistercharts.canvas.saved
import com.meistercharts.demo.ChartingDemo
import com.meistercharts.demo.ChartingDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.configurableDouble
import com.meistercharts.demo.configurableEnum
import com.meistercharts.demo.configurableList
import com.meistercharts.model.Direction
import com.meistercharts.model.Size
import it.neckar.open.kotlin.lang.round
import it.neckar.open.formatting.decimalFormat
import it.neckar.open.unit.other.px

/**
 *
 */
class ImageQualityDemoDescriptor(
  private val url: String = "https://a.tile.openstreetmap.de/12/2138/1420.png",
  private val width: @px Double = 256.0,
  private val height: @px Double = 256.0
) : ChartingDemoDescriptor<Nothing> {
  override val name: String = "Image Quality"
  override val description: String = "## A demo that shows how an image is painted"
  override val category: DemoCategory = DemoCategory.LowLevelTests

  override fun createDemo(configuration: PredefinedConfiguration<Nothing>?): ChartingDemo {
    return ChartingDemo {
      meistercharts {

        configure {
          val urlPaintable = UrlPaintable.fixedSize(url, Size(width, height))
          val urlPaintable2 = UrlPaintable.naturalSize(url)

          layers.addClearBackground()
          val layer = QualityImageLayer(urlPaintable, urlPaintable2)
          layers.addLayer(layer)

          configurableDouble("x", layer.style::x) {
            max = 500.0
          }

          configurableDouble("y", layer.style::y) {
            max = 500.0
          }

          configurableEnum("rounding mode", layer.style.roundingMode, RoundingMode.values()) {
            onChange {
              layer.style.roundingMode = it
              markAsDirty()
            }
          }

          configurableList("scale", layer.style.scale, listOf(0.5, 0.8, 1.0, 1.1, 1.2, 1.5, 2.0, 1.0 / chartSupport.devicePixelRatio, chartSupport.devicePixelRatio)) {
            onChange {
              layer.style.scale = it
              markAsDirty()
            }
          }
        }
      }
    }
  }
}

private class QualityImageLayer(
  val paintable0: Paintable,
  val paintable1: Paintable,
  styleConfiguration: Style.() -> Unit = {}
) : AbstractLayer() {
  val style: Style = Style().also(styleConfiguration)

  override val type: LayerType
    get() = LayerType.Content

  override fun paint(paintingContext: LayerPaintingContext) {
    val gc = paintingContext.gc

    gc.saved {
      gc.scale(style.scale, style.scale)
      paintable0.paint(paintingContext, style.roundingMode.round(style.x), style.roundingMode.round(style.y))
    }
    gc.saved {
      paintable1.paint(paintingContext, style.roundingMode.round(style.x) + 400, style.roundingMode.round(style.y))
    }

    gc.fillText("Device Pixel Ratio: ${decimalFormat.format(paintingContext.chartSupport.devicePixelRatio)}", gc.width, 0.0, Direction.TopRight, 10.0, 10.0)
    gc.fillText("Image0 size: ${paintable0.boundingBox(paintingContext).size}", gc.width, 0.0, Direction.TopRight, 25.0, 25.0)
    gc.fillText("Image1 size: ${paintable1.boundingBox(paintingContext).size}", gc.width, 0.0, Direction.TopRight, 50.0, 50.0)
  }

  @StyleDsl
  class Style {
    var x = 0.0
    var y = 0.0
    var scale = 1.0
    var roundingMode = RoundingMode.None
  }
}

private enum class RoundingMode {
  None {
    override fun round(y: Double): Double {
      return y
    }
  },
  Round {
    override fun round(y: Double): Double {
      return y.round()
    }
  },
  RoundPlusHalf {
    override fun round(y: Double): Double {
      return y.round() + 0.5
    }
  };

  abstract fun round(y: Double): Double
}

