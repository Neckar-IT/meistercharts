package com.meistercharts.demo.descriptors

import com.meistercharts.algorithms.layers.AbstractLayer
import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.algorithms.layers.LayerType
import com.meistercharts.algorithms.layers.addClearBackground
import com.meistercharts.canvas.CanvasRenderingContext
import com.meistercharts.canvas.currentFrameTimestamp
import com.meistercharts.demo.ChartingDemo
import com.meistercharts.demo.ChartingDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.model.Direction
import it.neckar.open.time.nowMillis
import it.neckar.open.formatting.format
import it.neckar.open.formatting.formatUtc
import it.neckar.open.unit.si.ms

/**
 * A demo of frame-related timestamps
 */
class FrameTimestampDemoDescriptor : ChartingDemoDescriptor<Nothing> {
  override val name: String = "Frame Timestamp"
  override val description: String = "Visualizes the values of the different time stamps available"
  override val category: DemoCategory = DemoCategory.Calculations

  override fun createDemo(configuration: PredefinedConfiguration<Nothing>?): ChartingDemo {
    return ChartingDemo {
      meistercharts {
        configure {
          layers.addClearBackground()

          layers.addLayer(object : AbstractLayer() {
            override val type: LayerType = LayerType.Content

            override fun paint(paintingContext: LayerPaintingContext) {
              val gc = paintingContext.gc
              gc.translate(10.0, 10.0)

              paintRow(gc, paintingContext.frameTimestamp, "paintingContext.frameTimestamp")

              gc.translate(0.0, 30.0)

              paintRow(gc, paintingContext.frameTimestampDelta, "paintingContext.frameTimestampDelta", false)
              gc.translate(0.0, 30.0)

              paintRow(gc, currentFrameTimestamp, "currentFrameTimestamp")

              val nowMillis = nowMillis()

              gc.translate(0.0, 30.0)
              paintRow(gc, nowMillis, "nowMillis()")

              gc.translate(0.0, 30.0)
              paintRow(gc, (nowMillis - paintingContext.frameTimestamp), "Δ nowMillis() - frameTimestamp", false)
            }

            private fun paintRow(gc: CanvasRenderingContext, it: @ms Double, description: String, formatAsUtc: Boolean = true) {
              gc.fillText(description, 0.0, 0.0, Direction.TopLeft)
              gc.fillText(it.format(), 440.0, 0.0, Direction.TopRight)
              if (formatAsUtc) {
                gc.fillText(it.formatUtc(), 500.0, 0.0, Direction.TopLeft)
              }
            }
          })
        }
      }
    }
  }
}
