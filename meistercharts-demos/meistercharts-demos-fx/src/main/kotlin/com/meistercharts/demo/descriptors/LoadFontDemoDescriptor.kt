package com.meistercharts.demo.descriptors

import com.meistercharts.algorithms.layers.AbstractLayer
import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.algorithms.layers.LayerType
import com.meistercharts.algorithms.layers.addClearBackground
import com.meistercharts.demo.ChartingDemo
import com.meistercharts.demo.ChartingDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.fx.native
import com.meistercharts.model.Direction
import javafx.scene.text.Font

/**
 *
 */
class LoadFontDemoDescriptor : ChartingDemoDescriptor<Nothing> {
  override val name: String = "Load Font"
  override val category: DemoCategory = DemoCategory.Platform

  override fun createDemo(configuration: PredefinedConfiguration<Nothing>?): ChartingDemo {
    return ChartingDemo {
      meistercharts {

        val oswaldFont: Font = Font.loadFont(javaClass.getResourceAsStream("/com/cedarsoft/charting/demo/Oswald-VariableFont_wght.ttf"), 80.0)
        val oswaldFontLight: Font = Font.loadFont(javaClass.getResourceAsStream("/com/cedarsoft/charting/demo/Oswald-Light.ttf"), 80.0)
        val oswaldFontRegular: Font = Font.loadFont(javaClass.getResourceAsStream("/com/cedarsoft/charting/demo/Oswald-Regular.ttf"), 80.0)
        val oswaldFontBold: Font = Font.loadFont(javaClass.getResourceAsStream("/com/cedarsoft/charting/demo/Oswald-Bold.ttf"), 80.0)

        configure {
          layers.addClearBackground()

          layers.addLayer(object : AbstractLayer() {
            override val type: LayerType = LayerType.Content

            override fun paint(paintingContext: LayerPaintingContext) {
              val gc = paintingContext.gc.native()
              val context = gc.context

              gc.translate(gc.width / 2.0, 0.0)

              context.font = oswaldFont
              gc.fillText("Hello World!!! Oswald vght", 0.0, 0.0, Direction.TopCenter)

              context.font = oswaldFontLight
              gc.translate(0.0, 80.0)
              gc.fillText("Hello World!!! Oswald light", 0.0, 0.0, Direction.TopCenter)

              context.font = oswaldFontRegular
              gc.translate(0.0, 80.0)
              gc.fillText("Hello World!!! Oswald Regular", 0.0, 0.0, Direction.TopCenter)

              context.font = oswaldFontBold
              gc.translate(0.0, 80.0)
              gc.fillText("Hello World!!! Oswald Bold", 0.0, 0.0, Direction.TopCenter)
            }
          })
        }
      }
    }
  }
}
