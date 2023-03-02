package com.meistercharts.demo.descriptors

import com.meistercharts.algorithms.axis.AxisOrientationY
import com.meistercharts.algorithms.layers.AbstractLayer
import com.meistercharts.algorithms.layers.FillBackgroundLayer
import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.algorithms.layers.LayerType
import com.meistercharts.canvas.saved
import com.meistercharts.canvas.strokeBoundingBox
import com.meistercharts.demo.ChartingDemo
import com.meistercharts.demo.ChartingDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.configurableBoolean
import com.meistercharts.demo.configurableDouble
import com.meistercharts.design.neckarit.NeckarItFlowPaintable
import com.meistercharts.model.Size

/**
 *
 */
class NeckarITFlowDemoDescriptor : ChartingDemoDescriptor<Nothing> {
  override val name: String = "Neckar IT Flow"
  override val category: DemoCategory = DemoCategory.NeckarIT

  override fun createDemo(configuration: PredefinedConfiguration<Nothing>?): ChartingDemo {

    return ChartingDemo {
      meistercharts {
        configure {
          chartSupport.rootChartState.axisOrientationY = AxisOrientationY.OriginAtBottom

          layers.addLayer(FillBackgroundLayer() {
            dark()
          })

          val layer = object : AbstractLayer() {
            var translateX: Double = 0.0
            var translateY: Double = 0.0
            var width = 900.0
            var height = 300.0
            var showBoundingBox: Boolean = true

            override val type: LayerType = LayerType.Content

            override fun paint(paintingContext: LayerPaintingContext) {
              val gc = paintingContext.gc
              val chartCalculator = paintingContext.chartCalculator

              val size = Size.of(width, height)

              val paintable = NeckarItFlowPaintable(size)
              gc.saved {
                paintable.paint(paintingContext, translateX, translateY)
              }

              if (showBoundingBox) {
                paintable.strokeBoundingBox(paintingContext, translateX, translateY, true)
              }

              markAsDirty()
            }
          }
          layers.addLayer(layer)

          configurableBoolean("Show bounding box", layer::showBoundingBox)

          configurableDouble("Translation X", layer::translateX) {
            max = 1000.0
          }
          configurableDouble("Translation Y", layer::translateY) {
            max = 1000.0
          }

          configurableDouble("Width", layer::width) {
            max = 2000.0
          }
          configurableDouble("Height Y", layer::height) {
            max = 1500.0
          }
          declare {
            button("Optimal height") {
              layer.height = NeckarItFlowPaintable.optimalHeight(layer.width)
              markAsDirty()
            }
          }
        }
      }
    }
  }
}
