package com.meistercharts.demo.descriptors

import com.meistercharts.algorithms.layers.AbstractLayer
import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.algorithms.layers.LayerType
import com.meistercharts.algorithms.layers.addClearBackground
import com.meistercharts.algorithms.painter.Color
import com.meistercharts.canvas.LineSpacing
import com.meistercharts.canvas.paintTextBox
import com.meistercharts.demo.ChartingDemo
import com.meistercharts.demo.ChartingDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.model.Direction
import com.meistercharts.model.Distance
import com.meistercharts.model.HorizontalAlignment
import com.meistercharts.style.BoxStyle

/**
 *
 */
class CanvasLowLevelDemoDescriptor : ChartingDemoDescriptor<Nothing> {
  override val name: String = "Canvas Low Level"

  override val category: DemoCategory
    get() = DemoCategory.LowLevelTests

  override fun createDemo(configuration: PredefinedConfiguration<Nothing>?): ChartingDemo {
    return ChartingDemo {
      meistercharts {
        configure {
          layers.addClearBackground()
          layers.addLayer(MyLowLevelLayer())
        }
      }
    }
  }
}

private class MyLowLevelLayer : AbstractLayer() {
  override val type: LayerType = LayerType.Content

  override fun paint(paintingContext: LayerPaintingContext) {
    val gc = paintingContext.gc

    gc.translate(20.0, 30.0)
    gc.translation = Distance(17.0, 22.3)

    gc.paintTextBox(
      listOf(
        "Scale: ${gc.scaleX} / ${gc.scaleY}",
        "Translation: ${gc.translation.format()}"
      ), LineSpacing.Single, HorizontalAlignment.Left, Direction.TopLeft, 5.0, 5.0, BoxStyle.none, Color.black
    )

  }
}
