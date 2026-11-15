/*
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

import com.meistercharts.demo.configurable
import kotlin.uuid.Uuid
import it.neckar.geometry.AxisOrientationY
import com.meistercharts.algorithms.layers.AbstractLayer
import com.meistercharts.algorithms.layers.FillBackgroundLayer
import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.algorithms.layers.LayerType
import com.meistercharts.canvas.saved
import com.meistercharts.canvas.strokeBoundingBox
import com.meistercharts.demo.MeisterchartsDemo
import com.meistercharts.demo.MeisterchartsDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.DemoQuality
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.VariabilityType
import com.meistercharts.demo.configurableBoolean
import com.meistercharts.demo.configurableDouble
import com.meistercharts.design.neckarit.NeckarItFlowPaintable
import it.neckar.geometry.Size

/**
 *
 */
class NeckarITFlowDemoDescriptor : MeisterchartsDemoDescriptor<Nothing> {
  override val uuid: Uuid = Uuid.parse("2a232a51-6ee7-4197-b9c9-931afe4144c5")
  override val name: String = "Neckar IT Flow"
  override val category: DemoCategory = DemoCategory.NeckarIT
  override val quality: DemoQuality = DemoQuality.High
  override val variabilityType: VariabilityType = VariabilityType.Stable

  override fun prepareDemo(configuration: PredefinedConfiguration<Nothing>?): MeisterchartsDemo {

    return MeisterchartsDemo(this) {
      meistercharts {
        configure {
          chartSupport.rootChartState.axisOrientationY = AxisOrientationY.OriginAtBottom

          layers.addLayer(FillBackgroundLayer() {
            secondary()
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

          configurable("Show bounding box", layer::showBoundingBox)

          configurable("Translation X", layer::translateX) {
            max = 1000.0
          }
          configurable("Translation Y", layer::translateY) {
            max = 1000.0
          }

          configurable("Width", layer::width) {
            max = 2000.0
          }
          configurable("Height Y", layer::height) {
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
