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
import com.meistercharts.algorithms.layers.autoHideAfter
import com.meistercharts.algorithms.layers.text.TextLayer
import com.meistercharts.algorithms.layers.visibleIf
import com.meistercharts.canvas.ChartSupport
import com.meistercharts.canvas.DirtyReason
import com.meistercharts.canvas.events.CanvasMouseEventHandler
import com.meistercharts.canvas.registerDirtyListener
import com.meistercharts.canvas.timerSupport
import com.meistercharts.demo.ChartingDemo
import com.meistercharts.demo.ChartingDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.events.EventConsumption
import com.meistercharts.events.MouseClickEvent
import it.neckar.open.observable.ObservableBoolean
import it.neckar.logging.LoggerFactory
import kotlin.time.Duration.Companion.milliseconds

/**
 * Demos that visualizes the functionality of the FPS layer
 */
class HideAfterTimeoutLayerDemoDescriptor : ChartingDemoDescriptor<Nothing> {
  override val name: String = "Hide After Timeout"
  override val description: String = "Click on the canvas. A message will appear.\n\nThis text will be hidden after a 500 ms"
  override val category: DemoCategory = DemoCategory.Interaction

  override fun createDemo(configuration: PredefinedConfiguration<Nothing>?): ChartingDemo {
    return ChartingDemo {
      meistercharts {
        configure {
          layers.addClearBackground()

          val visibleProperty = ObservableBoolean()
          visibleProperty.consumeImmediately {
            logger.debug("Visibility changed to $it")
          }

          visibleProperty.registerDirtyListener(this, DirtyReason.ConfigurationChanged)


          layers.addLayer(object : AbstractLayer() {
            override val type: LayerType
              get() = LayerType.Content

            override fun paint(paintingContext: LayerPaintingContext) {
            }

            override val mouseEventHandler: CanvasMouseEventHandler? = object : CanvasMouseEventHandler {
              override fun onClick(event: MouseClickEvent, chartSupport: ChartSupport): EventConsumption {
                visibleProperty.value = true
                return EventConsumption.Consumed
              }
            }
          })

          layers.addLayer(
            TextLayer({ _, _ -> listOf("Hello World") })
              .visibleIf(visibleProperty)
              .autoHideAfter(500.0.milliseconds, chartSupport.timerSupport)
          )
        }
      }
    }
  }

  companion object {
    private val logger = LoggerFactory.getLogger("com.meistercharts.demo.descriptors.HideAfterTimeoutLayerDemoDescriptor")
  }
}

