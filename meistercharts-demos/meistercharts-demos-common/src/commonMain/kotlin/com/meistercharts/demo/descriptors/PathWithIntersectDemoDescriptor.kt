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
import com.meistercharts.algorithms.layers.AbstractLayer
import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.algorithms.layers.LayerType
import com.meistercharts.algorithms.layers.addBackgroundChecker
import com.meistercharts.canvas.fill
import com.meistercharts.color.Color
import com.meistercharts.canvas.saved
import com.meistercharts.canvas.stroke
import com.meistercharts.demo.MeisterchartsDemo
import com.meistercharts.demo.MeisterchartsDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.DemoQuality
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.VariabilityType
import com.meistercharts.demo.configurableBoolean

/**
 *
 */
class PathWithIntersectDemoDescriptor : MeisterchartsDemoDescriptor<Nothing> {
  override val uuid: Uuid = Uuid.parse("7d6c3373-e11a-48d6-b750-8790edddd088")
  override val name: String = "Path with intersect"
  override val category: DemoCategory = DemoCategory.LowLevelTests
  override val quality: DemoQuality = DemoQuality.High
  override val variabilityType: VariabilityType = VariabilityType.Stable

  override fun prepareDemo(configuration: PredefinedConfiguration<Nothing>?): MeisterchartsDemo {
    return MeisterchartsDemo(this) {
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

          configurable("Fill", layer::fill)
          configurable("Stroke", layer::stroke)
          configurable("Clockwise", layer::clockwise)
        }
      }
    }
  }
}
