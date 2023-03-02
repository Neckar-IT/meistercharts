package com.meistercharts.demo.descriptors

import com.meistercharts.algorithms.layers.AbstractLayer
import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.algorithms.layers.LayerType
import com.meistercharts.algorithms.layers.addClearBackground
import com.meistercharts.algorithms.painter.Color
import com.meistercharts.canvas.SnapConfiguration
import com.meistercharts.canvas.paintTextBox
import com.meistercharts.canvas.pixelSnapSupport
import com.meistercharts.canvas.saved
import com.meistercharts.demo.ChartingDemo
import com.meistercharts.demo.ChartingDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.configurableBoolean
import com.meistercharts.demo.configurableDouble
import com.meistercharts.demo.configurableInt
import com.meistercharts.model.Direction
import it.neckar.open.kotlin.lang.fastFor

/**
 */
class AntialiasingRectDemoDescriptor : ChartingDemoDescriptor<Nothing> {
  override val name: String = "Anti Aliasing Rects"
  override val category: DemoCategory = DemoCategory.LowLevelTests

  override fun createDemo(configuration: PredefinedConfiguration<Nothing>?): ChartingDemo {
    return ChartingDemo {
      meistercharts {
        configure {
          layers.addClearBackground()
          chartSupport.pixelSnapSupport.snapConfiguration = SnapConfiguration.Both

          val layer = object : AbstractLayer() {
            override val type: LayerType
              get() = LayerType.Content

            var totalWidth: Double = 500.0
            var rectanglesCount: Int = 40
            var snapPositionPhysical: Boolean = true
            var snapLengthPhysical: Boolean = true

            override fun paint(paintingContext: LayerPaintingContext) {
              val snapConfiguration = paintingContext.snapConfiguration
              val gc = paintingContext.gc
              gc.translate(10.0, 10.0)

              val widthPerRect = totalWidth / rectanglesCount

              val snappedWidth = if (snapLengthPhysical) {
                snapConfiguration.snapXSize(widthPerRect)
              } else {
                widthPerRect
              }

              rectanglesCount.fastFor { index ->
                gc.saved {
                  gc.translate(widthPerRect * index, 0.0)

                  if (snapPositionPhysical) {
                    gc.snapPhysicalTranslation()
                  }

                  gc.fill(Color.blue)
                  gc.fillRect(0.0, 0.0, snappedWidth, 250.0 + index * 10.0)
                }
              }

              //556,09 - 3

              gc.fill(Color.black)

              gc.translateToCenter()
              gc.paintTextBox(
                listOf(
                  "exact width: $widthPerRect",
                  "snapped width: $snappedWidth",
                  "factor: ${gc.scaleX}",
                ), Direction.Center
              )


            }
          }
          layers.addLayer(layer)

          configurableDouble("Total width", layer::totalWidth) {
            max = 700.0
          }
          configurableInt("Rects count", layer::rectanglesCount) {
            max = 100
          }

          configurableBoolean("Snap position physically", layer::snapPositionPhysical)
          configurableBoolean("Snap length physically", layer::snapLengthPhysical)
        }
      }
    }
  }
}
