package com.meistercharts.demo.descriptors

import com.meistercharts.algorithms.layers.AbstractLayer
import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.algorithms.layers.LayerType
import com.meistercharts.algorithms.layers.addClearBackground
import com.meistercharts.algorithms.painter.Color
import com.meistercharts.annotations.Window
import com.meistercharts.annotations.Zoomed
import com.meistercharts.canvas.fillRect
import com.meistercharts.canvas.paintLocation
import com.meistercharts.demo.ChartingDemo
import com.meistercharts.demo.ChartingDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.configurableDouble
import com.meistercharts.demo.configurableEnum
import com.meistercharts.model.Direction
import it.neckar.open.formatting.format

/**
 * A demo for [fillRect] with extreme values
 */
class FillRectExtremeDemoDescriptor : ChartingDemoDescriptor<Nothing> {
  override val name: String = "Fill Rect Extreme"

  override val description: String = """
  This demo shows erroneous behavior for large values probably due to floats being used by the native Canvas RenderingContext.<BR/><BR/>
  fun CanvasRenderingContext.fillRect(<BR/>
  &nbsp;&nbsp;&nbsp;<b>x</b>: @px Double,<BR/>
  &nbsp;&nbsp;&nbsp;<b>y</b>: @px Double,<BR/>
  &nbsp;&nbsp;&nbsp;<b>width</b>: @MayBeNegative @px @Zoomed Double,<BR/>
  &nbsp;&nbsp;&nbsp;<b>height</b>: @MayBeNegative @px @Zoomed Double,<BR/>
  &nbsp;&nbsp;&nbsp;<b>anchorDirection</b>: Direction,<BR/>
  &nbsp;&nbsp;&nbsp;<b>anchorGapHorizontal</b>: @px Double = 0.0<BR/>
  &nbsp;&nbsp;&nbsp;<b>anchorGapVertical</b>: @px Double = 0.0<BR/>
  )
  """.trimIndent()

  override val category: DemoCategory = DemoCategory.Primitives

  override fun createDemo(configuration: PredefinedConfiguration<Nothing>?): ChartingDemo {
    return ChartingDemo {
      meistercharts {
        configure {
          layers.addClearBackground()
          val layer = MyFillRectLayer()
          layers.addLayer(layer)

          configurableDouble("Y", layer::y) {
            min = -10_000_001_000.0
            max = -10_000_000_000.0
            step = 1.0
          }

          configurableDouble("Height", layer::height) {
            min = 10_000_000_000.0
            max = 10_000_001_000.0
            step = 1.0
          }

          configurableEnum("Anchor direction", layer::anchorDirection, enumValues())

          configurableDouble("Anchor gap Horizontal", layer::anchorGapHorizontal) {
            min = -50.0
            max = 300.0
          }
          configurableDouble("Anchor gap Vertical", layer::anchorGapVertical) {
            min = -50.0
            max = 300.0
          }
        }
      }
    }
  }

  private class MyFillRectLayer : AbstractLayer() {

    override val type: LayerType
      get() = LayerType.Content

    var x: @Window Double = 100.0
    var y: @Window Double = -10_000_000_000.0
    var width: @Zoomed Double = 200.0
    var height: @Zoomed Double = 10_000_000_000.0
    var anchorDirection: Direction = Direction.TopLeft
    var anchorGapHorizontal: @Zoomed Double = 0.0
    var anchorGapVertical: @Zoomed Double = 0.0

    private val fill = Color("#FF4500BB")

    override fun paint(paintingContext: LayerPaintingContext) {
      val gc = paintingContext.gc
      gc.fill(Color.black);
      gc.fillText("y: ${y.format(useGrouping = true)}", 10.0, 10.0, Direction.TopLeft)
      gc.fillText("height: ${height.format(useGrouping = true)}", 10.0, 30.0, Direction.TopLeft)
      gc.paintLocation(x, y, Color.black)
      gc.paintLocation(x + width, y + height, Color.black)
      gc.fill(fill)
      gc.fillRect(x, y, width, height, anchorDirection, anchorGapHorizontal, anchorGapVertical)
    }
  }
}

