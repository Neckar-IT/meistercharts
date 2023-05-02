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

import com.meistercharts.algorithms.layers.addClearBackground
import com.meistercharts.algorithms.layers.debug.EventsDebugLayer
import com.meistercharts.algorithms.layers.debug.KeyEventFilter
import com.meistercharts.algorithms.layers.debug.MouseEventFilter
import com.meistercharts.algorithms.layers.debug.PointerEventFilter
import com.meistercharts.algorithms.layers.debug.TouchEventFilter
import com.meistercharts.demo.ChartingDemo
import com.meistercharts.demo.ChartingDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.configurableBoolean
import com.meistercharts.demo.section

/**
 * A demo for the [EventsDebugLayer]
 */
class EventsDebugLayerDemoDescriptor : ChartingDemoDescriptor<Nothing> {
  override val name: String = "Events Debugging"
  override val category: DemoCategory = DemoCategory.Layers
  override val description: String = "Paints all events processed by the EventsDebugLayer."

  override fun createDemo(configuration: PredefinedConfiguration<Nothing>?): ChartingDemo {
    val mouseEventFilter = MouseEventFilter()
    val keyEventFilter = KeyEventFilter()
    val pointerEventFilter = PointerEventFilter()
    val touchEventFilter = TouchEventFilter()

    return ChartingDemo {
      meistercharts {
        configure {
          val eventsDebugLayer = EventsDebugLayer()
          eventsDebugLayer.data.mouseEventFilter = mouseEventFilter::filter
          eventsDebugLayer.data.keyEventFilter = keyEventFilter::filter
          eventsDebugLayer.data.pointerEventFilter = pointerEventFilter::filter
          eventsDebugLayer.data.touchEventFilter = touchEventFilter::filter

          layers.addClearBackground()
          layers.addLayer(eventsDebugLayer)

          declare {
            button("Clear") {
              eventsDebugLayer.clearEventQueue()
              markAsDirty()
            }
          }

          section("Mouse events")
          configurableBoolean("Mouse move", mouseEventFilter::filterMouseMove)
          configurableBoolean("Mouse drag", mouseEventFilter::filterMouseDrag)
          configurableBoolean("Mouse click", mouseEventFilter::filterMouseClick)
          configurableBoolean("Mouse down", mouseEventFilter::filterMouseDown)
          configurableBoolean("Mouse up", mouseEventFilter::filterMouseUp)
          configurableBoolean("Mouse double-click", mouseEventFilter::filterMouseDoubleClick)
          configurableBoolean("Mouse wheel", mouseEventFilter::filterMouseWheel)

          section("Key events")
          configurableBoolean("Key up", keyEventFilter::filterKeyUp)
          configurableBoolean("Key down", keyEventFilter::filterKeyDown)
          configurableBoolean("Key type", keyEventFilter::filterKeyType)

          section("Pointer events")
          configurableBoolean("Pointer leave", pointerEventFilter::filterPointerLeave)
          configurableBoolean("Pointer out", pointerEventFilter::filterPointerOut)
          configurableBoolean("Pointer cancel", pointerEventFilter::filterPointerCancel)
          configurableBoolean("Pointer up", pointerEventFilter::filterPointerUp)
          configurableBoolean("Pointer move", pointerEventFilter::filterPointerMove)
          configurableBoolean("Pointer down", pointerEventFilter::filterPointerDown)
          configurableBoolean("Pointer enter", pointerEventFilter::filterPointerEnter)
          configurableBoolean("Pointer over", pointerEventFilter::filterPointerOver)

          section("Touch events")
          configurableBoolean("Touch cancel", touchEventFilter::filterTouchCancel)
          configurableBoolean("Touch move", touchEventFilter::filterTouchMove)
          configurableBoolean("Touch end", touchEventFilter::filterTouchEnd)
          configurableBoolean("Touch start", touchEventFilter::filterTouchStart)
        }
      }
    }
  }
}

