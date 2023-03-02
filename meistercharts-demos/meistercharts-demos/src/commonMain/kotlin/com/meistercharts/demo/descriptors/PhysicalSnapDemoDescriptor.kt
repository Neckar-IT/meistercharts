package com.meistercharts.demo.descriptors

import com.meistercharts.algorithms.layers.AbstractLayer
import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.algorithms.layers.LayerType
import com.meistercharts.algorithms.layers.addClearBackground
import com.meistercharts.canvas.CanvasRenderingContext
import com.meistercharts.canvas.PaintingUtils
import com.meistercharts.canvas.paintTextBox
import com.meistercharts.canvas.saved
import com.meistercharts.demo.ChartingDemo
import com.meistercharts.demo.ChartingDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.configurableBoolean
import com.meistercharts.demo.configurableDouble
import com.meistercharts.model.Direction

/**
 */
class PhysicalSnapDemoDescriptor : ChartingDemoDescriptor<Nothing> {
  override val name: String = "Physical Snap"

  //language=HTML
  override val category: DemoCategory = DemoCategory.LowLevelTests

  override fun createDemo(configuration: PredefinedConfiguration<Nothing>?): ChartingDemo {
    return ChartingDemo {
      meistercharts {
        configure {
          layers.addClearBackground()

          val layer = object : AbstractLayer() {
            override val type: LayerType
              get() = LayerType.Content

            var snapToPhysical = true

            var translationX: Double = 7.123
            var translationY: Double = 9.922

            override fun paint(paintingContext: LayerPaintingContext) {
              val gc = paintingContext.gc
              gc.translate(translationX, translationY)

              gc.translate(gc.width * 0.25, gc.height * 0.25)
              gc.saved {
                //Snap to physical
                if (snapToPhysical) {
                  gc.snapPhysicalTranslation()
                }

                strokeLines(gc)
                paintInfoBox(gc, "+0.5 Physical Pixels")
              }

              //0.5 pixel physical offset!
              gc.translate(gc.width * 0.25, gc.height * 0.25)
              gc.saved {
                if (snapToPhysical) {
                  gc.snapPhysicalTranslation(0.5, 0.5)
                }

                strokeLines(gc)
                paintInfoBox(gc, "+0.5 Physical Pixels")
              }
            }

            private fun strokeLines(gc: CanvasRenderingContext) {
              gc.lineWidth = PaintingUtils.snapSize(1.0, snapToPhysical)

              gc.strokeLine(0.0, 100.0, 0.0, -100.0)
              gc.strokeLine(100.0, 0.0, -100.0, 0.0)

              gc.strokeRect(-50.0, -50.0, 100.0, 100.0)
              gc.strokeRect(-25.0, -25.0, 50.0, 50.0)
            }

            private fun paintInfoBox(gc: CanvasRenderingContext, label: String) {
              gc.paintTextBox(
                listOf(
                  label,
                  "Line width: ${gc.lineWidth}",
                  "Physical translation: ${gc.translationPhysical.format()}",
                  "Translation: ${gc.translation.format()}",
                  "Scale: ${gc.scale.format()}",
                  "Native Translation: ${gc.nativeTranslation?.format()}",

                  ), Direction.TopLeft, 5.0
              )
            }
          }
          layers.addLayer(layer)

          configurableBoolean("snap to physical pixels", layer::snapToPhysical)

          configurableDouble("Translation X", layer::translationX) {
            min = -200.0
            max = 200.0
          }
          configurableDouble("Translation Y", layer::translationY) {
            min = -200.0
            max = 200.0
          }

        }
      }
    }
  }
}
