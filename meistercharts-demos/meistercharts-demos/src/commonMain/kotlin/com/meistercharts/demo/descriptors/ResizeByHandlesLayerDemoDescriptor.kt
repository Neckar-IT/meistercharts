package com.meistercharts.demo.descriptors

import com.meistercharts.algorithms.layers.AbstractLayer
import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.algorithms.layers.LayerType
import com.meistercharts.algorithms.layers.addClearBackground
import com.meistercharts.algorithms.layers.text.addText
import com.meistercharts.algorithms.painter.Color
import com.meistercharts.annotations.Zoomed
import com.meistercharts.canvas.FontDescriptorFragment
import com.meistercharts.canvas.resize.ResizeByHandlesLayer
import com.meistercharts.canvas.resize.ResizeHandler
import com.meistercharts.canvas.resizeHandlesSupport
import com.meistercharts.demo.ChartingDemo
import com.meistercharts.demo.ChartingDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.configurableCoordinatesSeparate
import com.meistercharts.demo.configurableDouble
import com.meistercharts.demo.configurableSizeSeparate
import com.meistercharts.model.Coordinates
import com.meistercharts.model.Direction
import com.meistercharts.model.DirectionBasedBasePointProvider
import com.meistercharts.model.Distance
import com.meistercharts.model.HorizontalAlignment
import com.meistercharts.model.Rectangle
import com.meistercharts.model.Size
import com.meistercharts.model.VerticalAlignment
import com.meistercharts.style.BoxStyle
import it.neckar.open.unit.other.px

/**
 *
 */
class ResizeByHandlesLayerDemoDescriptor : ChartingDemoDescriptor<Nothing> {
  override val name: String = "Resize by Handles Layer"
  override val category: DemoCategory = DemoCategory.Interaction

  override fun createDemo(configuration: PredefinedConfiguration<Nothing>?): ChartingDemo {
    return ChartingDemo {
      meistercharts {
        configure {
          layers.addClearBackground()

          val resizeByHandlesLayer = ResizeByHandlesLayer()

          val contentLayer = object : AbstractLayer() {
            override val type: LayerType = LayerType.Content

            var contentLocation = Coordinates(450.0, 350.0)
            var contentSize = Size(150.0, 200.0)

            var color = Color.blue

            override fun initialize(paintingContext: LayerPaintingContext) {
              super.initialize(paintingContext)

              paintingContext.chartSupport.resizeHandlesSupport.onResize(this, object : ResizeHandler {
                override fun armed(handleDirection: Direction) {
                  color = Color.red
                }

                override fun disarmed() {
                  color = Color.blue
                }

                override fun beginResizing(handleDirection: Direction) {
                  color = Color.orange
                }

                override fun resizing(rawDistance: Distance, handleDirection: Direction, deltaX: Double, deltaY: Double) {
                  //is it necessary to move the location?
                  val deltaLocationX: @px @Zoomed Double
                  val deltaLocationY: @px @Zoomed Double
                  val resizeX: @px @Zoomed Double
                  val resizeY: @px @Zoomed Double

                  if (handleDirection.horizontalAlignment == HorizontalAlignment.Left) {
                    deltaLocationX = deltaX
                    resizeX = -deltaX
                  } else {
                    deltaLocationX = 0.0
                    resizeX = deltaX
                  }

                  if (handleDirection.verticalAlignment == VerticalAlignment.Top) {
                    deltaLocationY = deltaY
                    resizeY = -deltaY
                  } else {
                    deltaLocationY = 0.0
                    resizeY = deltaY
                  }

                  contentLocation = contentLocation.plus(deltaLocationX, deltaLocationY)
                  contentSize = contentSize.plus(resizeX, resizeY)
                  paintingContext.chartSupport.markAsDirty()
                }

                override fun resizingFinished() {
                  color = Color.darkblue
                }
              })
            }

            override fun layout(paintingContext: LayerPaintingContext) {
              super.layout(paintingContext)
              paintingContext.chartSupport.resizeHandlesSupport.setResizable(this, Rectangle(contentLocation, contentSize))
            }

            override fun paint(paintingContext: LayerPaintingContext) {
              val gc = paintingContext.gc
              gc.fill(color)

              //Paint the element
              gc.fillRect(Rectangle(contentLocation, contentSize))
            }
          }
          layers.addLayer(contentLayer)
          layers.addLayer(resizeByHandlesLayer)

          layers.addText({ _, _ ->
            listOf(resizeByHandlesLayer.uiState.toString())
          }) {
            boxStyle = BoxStyle.gray
            anchorDirection = Direction.TopLeft
            anchorPointProvider = DirectionBasedBasePointProvider(Direction.TopLeft)
            font = FontDescriptorFragment.XS
          }

          configurableCoordinatesSeparate("Rect Location", contentLayer::contentLocation)

          configurableSizeSeparate("Rect Size", contentLayer::contentSize) {
            max = 500.0
          }
          configurableDouble("Handle Diameter", resizeByHandlesLayer.style::handleDiameter) {
            max = 50.0
          }
        }
      }
    }
  }
}
