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
import com.meistercharts.annotations.Window
import com.meistercharts.canvas.ChartSupport
import com.meistercharts.canvas.DirtyReason
import com.meistercharts.canvas.MouseCursor
import com.meistercharts.canvas.MouseCursorSupport
import com.meistercharts.canvas.events.CanvasMouseEventHandler
import com.meistercharts.canvas.fill
import com.meistercharts.canvas.mouseCursorSupport
import com.meistercharts.canvas.paintMark
import com.meistercharts.canvas.saved
import com.meistercharts.color.Color
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.DemoQuality
import com.meistercharts.demo.MeisterchartsDemo
import com.meistercharts.demo.MeisterchartsDemoDescriptor
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.VariabilityType
import com.meistercharts.design.Theme
import com.meistercharts.events.EventConsumption
import it.neckar.events.MouseMoveEvent
import it.neckar.geometry.Direction
import it.neckar.open.collections.fastForEachIndexed
import it.neckar.open.kotlin.lang.toIntFloor

/**
 *
 */
class MouseCursorDemoDescriptor : MeisterchartsDemoDescriptor<Nothing> {
  override val uuid: Uuid = Uuid.parse("9c9ca5fa-584c-4a5b-874e-0da633d77283")
  override val name: String = "Mouse Cursor"
  override val category: DemoCategory = DemoCategory.Interaction
  override val quality: DemoQuality = DemoQuality.High
  override val variabilityType: VariabilityType = VariabilityType.Unstable

  override fun prepareDemo(configuration: PredefinedConfiguration<Nothing>?): MeisterchartsDemo {
    return MeisterchartsDemo(this) {
      meistercharts {
        configure {
          layers.addClearBackground()

          //All available mouse cursors
          val mouseCursors = MouseCursor.entries

          val myLayer = object : AbstractLayer() {
            override val type: LayerType = LayerType.Content

            val size = 35.0

            override val mouseEventHandler: CanvasMouseEventHandler = object : CanvasMouseEventHandler {
              override fun onMove(event: MouseMoveEvent, chartSupport: ChartSupport): EventConsumption {
                val mouseCursorSupport = chartSupport.mouseCursorSupport

                mouseCursorSupport.cursorProperty("MyLayer", MouseCursorSupport.Priority.Default).value = event.coordinates?.let {
                  val y = it.y - 10.0

                  val index = (y / (size + 5)).toIntFloor()
                  mouseCursors.getOrNull(index)
                }

                //Enforce repaint to be able to see the cursor location
                chartSupport.markAsDirty(reason = DirtyReason.UserInteraction)

                return EventConsumption.Consumed
              }
            }

            override fun paint(paintingContext: LayerPaintingContext) {
              val gc = paintingContext.gc

              gc.saved {
                gc.translate(10.0, 10.0)

                mouseCursors.fastForEachIndexed { index, mouseCursor ->
                  gc.fill(Theme.chartColors.resolve().valueAt(index))
                  gc.fillRect(0.0, 0.0, gc.width - 20.0, size)
                  gc.fill(Color.white)
                  gc.fillText(mouseCursor.name, 0.0, size / 2.0, Direction.CenterLeft, 5.0, 5.0)
                  gc.translate(0.0, size + 5.0)
                }
              }

              @Window val location = paintingContext.chartSupport.mouseEvents.mousePosition
              if (location != null) {
                gc.paintMark(location = location, color = Color.red())
              }
            }
          }

          layers.addLayer(myLayer)
        }

        //layerSupport.chartSupport.canvas.mouseCursor.toJavaFx(), enumEntries()
      }
    }
  }
}
