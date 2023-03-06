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
import com.meistercharts.algorithms.painter.Color
import com.meistercharts.canvas.FontDescriptorFragment
import com.meistercharts.demo.ChartingDemo
import com.meistercharts.demo.ChartingDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.configurableFont
import com.meistercharts.fx.native

/**
 *
 */
class FontMetricsFXDemoDescriptor : ChartingDemoDescriptor<Nothing> {
  override val name: String = "Fx Font Metrics"
  override val category: DemoCategory = DemoCategory.Platform

  override fun createDemo(configuration: PredefinedConfiguration<Nothing>?): ChartingDemo {
    return ChartingDemo {
      meistercharts {
        configure {
          layers.addClearBackground()

          val myLayer = object : AbstractLayer() {
            var fontFragment = FontDescriptorFragment.L

            override val type: LayerType = LayerType.Content

            override fun paint(paintingContext: LayerPaintingContext) {
              val gcCommon = paintingContext.gc
              gcCommon.translateToCenter()

              val gcFx = gcCommon.native()
              gcFx.font(fontFragment)

              val fxFontMetrics = gcFx.getFxFontMetrics()

              gcFx.stroke(Color.silver)
              gcFx.strokeLine(-10.0, 0.0, 100.0, 0.0)

              //Ascent
              fxFontMetrics.ascent.toDouble().let {
                gcFx.strokeLine(-10.0, -it, 120.0, -it)
              }
              fxFontMetrics.maxAscent.toDouble().let {
                gcFx.strokeLine(-10.0, -it, 120.0, -it)
              }

              fxFontMetrics.descent.toDouble().let {
                gcFx.strokeLine(-10.0, it, 120.0, it)
              }
              fxFontMetrics.maxDescent.toDouble().let {
                gcFx.strokeLine(-10.0, it, 120.0, it)
              }

              gcFx.stroke(Color.black)
              gcFx.context.fillText("xHÁp", 0.0, 0.0)
            }
          }
          layers.addLayer(myLayer)

          configurableFont("font", myLayer::fontFragment)
        }
      }
    }
  }
}
