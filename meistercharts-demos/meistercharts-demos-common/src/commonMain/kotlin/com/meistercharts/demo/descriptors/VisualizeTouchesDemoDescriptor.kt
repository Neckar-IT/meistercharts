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


import kotlin.uuid.Uuid
import com.meistercharts.algorithms.layers.AbstractLayer
import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.algorithms.layers.LayerType
import com.meistercharts.algorithms.layers.addClearBackground
import com.meistercharts.canvas.ChartSupport
import com.meistercharts.canvas.events.CanvasTouchEventHandler
import com.meistercharts.canvas.fill
import com.meistercharts.color.Color
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.DemoQuality
import com.meistercharts.demo.MeisterchartsDemo
import com.meistercharts.demo.MeisterchartsDemoDescriptor
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.VariabilityType
import com.meistercharts.design.Theme
import com.meistercharts.events.EventConsumption
import com.meistercharts.font.FontDescriptorFragment
import it.neckar.events.Touch
import it.neckar.events.TouchEndEvent
import it.neckar.events.TouchEvent
import it.neckar.events.TouchMoveEvent
import it.neckar.events.TouchStartEvent
import it.neckar.geometry.Direction
import it.neckar.open.collections.fastForEach

/**
 *
 */
class VisualizeTouchesDemoDescriptor : MeisterchartsDemoDescriptor<Nothing> {
  override val uuid: Uuid = Uuid.parse("694450b4-8a45-4fac-8b79-3f889e0bd073")
  override val name: String = "Visualizes all touches"
  override val quality: DemoQuality = DemoQuality.High
  override val variabilityType: VariabilityType = VariabilityType.Stable

  override val description: String = "Visualizes all active touch points on the canvas."

  override val category: DemoCategory = DemoCategory.Interaction

  override fun prepareDemo(configuration: PredefinedConfiguration<Nothing>?): MeisterchartsDemo {
    return MeisterchartsDemo(this) {
      meistercharts {
        configure {
          this.layers.addClearBackground()

          this.layers.addLayer(object : AbstractLayer() {
            override val type: LayerType = LayerType.Content

            /**
             * Contains the target touches of the last event
             */
            val lastTargetTouches = mutableListOf<Touch>()
            val lastChangedTouches = mutableSetOf<Touch>()

            /**
             * The total number of touches
             */
            var lastTouchesCount = 0

            override fun paint(paintingContext: LayerPaintingContext) {
              val gc = paintingContext.gc

              gc.fill(Color.black)
              gc.fillText("Total Touch Count: $lastTouchesCount", 10.0, 10.0, Direction.TopLeft)
              gc.fillText("Target Touch Count: ${lastTargetTouches.size}", 10.0, 30.0, Direction.TopLeft)

              lastTargetTouches.fastForEach {
                val color = Theme.chartColors.resolve().valueAt(it.touchId.id)
                val location = it.coordinates

                //Paint round border for changed events
                if (lastChangedTouches.contains(it)) {
                  gc.fill(Color.red)
                  gc.fillOvalCenter(location, 80.0)
                }

                gc.fill(color)
                gc.fillOvalCenter(location, 50.0)


                gc.fill(Color.white)
                gc.font(FontDescriptorFragment.XL)
                gc.fillText(it.touchId.id.toString(), it.coordinates, Direction.Center)
              }
            }

            override val touchEventHandler: CanvasTouchEventHandler = object : CanvasTouchEventHandler {
              private fun updateTouches(event: TouchEvent) {
                lastTargetTouches.clear()
                lastTargetTouches.addAll(event.targetTouches)
                lastTouchesCount = event.touches.size
                lastChangedTouches.clear()
                lastChangedTouches.addAll(event.changedTouches)
                markAsDirty()
              }

              override fun onStart(event: TouchStartEvent, chartSupport: ChartSupport): EventConsumption {
                updateTouches(event)
                return EventConsumption.Consumed
              }

              override fun onMove(event: TouchMoveEvent, chartSupport: ChartSupport): EventConsumption {
                updateTouches(event)
                return EventConsumption.Consumed
              }

              override fun onEnd(event: TouchEndEvent, chartSupport: ChartSupport): EventConsumption {
                updateTouches(event)
                return EventConsumption.Consumed
              }
            }
          })
        }
      }
    }
  }
}
