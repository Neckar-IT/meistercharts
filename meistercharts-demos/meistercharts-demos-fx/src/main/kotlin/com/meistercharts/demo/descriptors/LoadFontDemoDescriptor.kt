/**
 * Copyright 2023 Neckar IT GmbH, Mössingen, Germany
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
