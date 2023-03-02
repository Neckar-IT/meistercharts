package com.meistercharts.demo.descriptors

import com.meistercharts.algorithms.layers.AbstractLayer
import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.algorithms.layers.LayerType
import com.meistercharts.algorithms.layers.addBackgroundChecker
import com.meistercharts.algorithms.painter.Color
import com.meistercharts.canvas.saved
import com.meistercharts.demo.ChartingDemo
import com.meistercharts.demo.ChartingDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.configurableBoolean

/**
 *
 */
class PathWithIntersectDemoDescriptor : ChartingDemoDescriptor<Nothing> {
  override val name: String = "Path with intersect"
  override val category: DemoCategory = DemoCategory.LowLevelTests

  override fun createDemo(configuration: PredefinedConfiguration<Nothing>?): ChartingDemo {
    return ChartingDemo {
      meistercharts {
        configure {
          layers.addBackgroundChecker()

          val layer = object : AbstractLayer() {
            override val type: LayerType = LayerType.Content

            var fill: Boolean = true
            var stroke: Boolean = true

            var clockwise: Boolean = false

            override fun paint(paintingContext: LayerPaintingContext) {
              val gc = paintingContext.gc
              gc.translate(100.0, 100.0)

              //Rect with 4 rects "stamped out"
              gc.saved {
                gc.beginPath()
                gc.moveTo(0.0, 0.0);

                if (clockwise) {
                  gc.lineTo(140.0, 0.0);
                  gc.lineTo(140.0, 140.0);
                  gc.lineTo(0.0, 140.0);
                  gc.lineTo(0.0, 0.0);
                } else {
                  gc.lineTo(0.0, 140.0);
                  gc.lineTo(140.0, 140.0);
                  gc.lineTo(140.0, 0.0);
                  gc.lineTo(0.0, 0.0);
                }


                // Then we call rect four times, which adds a rect to our path going clockwise
                gc.rect(20.0, 20.0, 40.0, 40.0);
                gc.rect(80.0, 20.0, 40.0, 40.0);
                gc.rect(20.0, 80.0, 40.0, 40.0);
                gc.rect(80.0, 80.0, 40.0, 40.0);

                if (fill) {
                  gc.fill(Color.orange)
                  gc.fill()
                }

                if (stroke) {
                  gc.stroke(Color.darkgray)
                  gc.stroke()
                }
              }

              gc.translate(200.0, 0.0)

              //Rect with removed inner circle
              gc.saved {
                gc.beginPath()
                gc.moveTo(0.0, 0.0);

                if (clockwise) {
                  gc.lineTo(140.0, 0.0);
                  gc.lineTo(140.0, 140.0);
                  gc.lineTo(0.0, 140.0);
                  gc.lineTo(0.0, 0.0);
                } else {
                  gc.lineTo(0.0, 140.0);
                  gc.lineTo(140.0, 140.0);
                  gc.lineTo(140.0, 0.0);
                  gc.lineTo(0.0, 0.0);
                }

                gc.ovalCenter(70.0, 70.0, 50.0, 50.0)

                if (fill) {
                  gc.fill(Color.orange)
                  gc.fill()
                }

                if (stroke) {
                  gc.stroke(Color.darkgray)
                  gc.stroke()
                }
              }

              //circle with removed inner rect
              gc.translate(200.0, 0.0)

              gc.saved {
                gc.beginPath()

                gc.ovalCenter(70.0, 70.0, 140.0, 140.0)

                gc.moveTo(40.0, 40.0);
                if (clockwise) {
                  gc.lineTo(100.0, 40.0);
                  gc.lineTo(100.0, 100.0);
                  gc.lineTo(40.0, 100.0);
                  gc.lineTo(40.0, 40.0);
                } else {
                  gc.lineTo(40.0, 100.0);
                  gc.lineTo(100.0, 100.0);
                  gc.lineTo(100.0, 40.0);
                  gc.lineTo(40.0, 40.0);
                }


                if (fill) {
                  gc.fill(Color.orange)
                  gc.fill()
                }

                if (stroke) {
                  gc.stroke(Color.darkgray)
                  gc.stroke()
                }
              }
            }
          }
          layers.addLayer(layer)

          configurableBoolean("Fill", layer::fill)
          configurableBoolean("Stroke", layer::stroke)
          configurableBoolean("Clockwise", layer::clockwise)
        }
      }
    }
  }
}
