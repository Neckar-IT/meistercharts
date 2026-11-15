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
import com.meistercharts.algorithms.layers.addClearBackground
import com.meistercharts.algorithms.layers.debug.EventsDebugLayer
import com.meistercharts.algorithms.layers.debug.KeyEventFilter
import com.meistercharts.algorithms.layers.debug.MouseEventFilter
import com.meistercharts.algorithms.layers.debug.PointerEventFilter
import com.meistercharts.algorithms.layers.debug.TouchEventFilter
import com.meistercharts.demo.MeisterchartsDemo
import com.meistercharts.demo.MeisterchartsDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.DemoQuality
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.VariabilityType
import com.meistercharts.demo.configurableBoolean
import com.meistercharts.demo.section

/**
 * A demo for the [EventsDebugLayer]
 */
class EventsDebugLayerDemoDescriptor : MeisterchartsDemoDescriptor<Nothing> {
  override val uuid: Uuid = Uuid.parse("d8345c1e-29b9-46c7-a74f-90c6d75ed241")
  override val name: String = "Events Debugging"
  override val category: DemoCategory = DemoCategory.Layers
  override val description: String = "Paints all events processed by the EventsDebugLayer."
  override val quality: DemoQuality = DemoQuality.High
  override val variabilityType: VariabilityType = VariabilityType.Unstable

  override fun prepareDemo(configuration: PredefinedConfiguration<Nothing>?): MeisterchartsDemo {
    val mouseEventFilter = MouseEventFilter()
    val keyEventFilter = KeyEventFilter()
    val pointerEventFilter = PointerEventFilter()
    val touchEventFilter = TouchEventFilter()

    return MeisterchartsDemo(this) {
      meistercharts {
        configure {
          val eventsDebugLayer = EventsDebugLayer()
          eventsDebugLayer.configuration.mouseEventFilter = mouseEventFilter::filter
          eventsDebugLayer.configuration.keyEventFilter = keyEventFilter::filter
          eventsDebugLayer.configuration.pointerEventFilter = pointerEventFilter::filter
          eventsDebugLayer.configuration.touchEventFilter = touchEventFilter::filter

          layers.addClearBackground()
          layers.addLayer(eventsDebugLayer)

          declare {
            button("Clear") {
              eventsDebugLayer.clearEventQueue()
              markAsDirty()
            }
          }

          section("Mouse events")
          configurable("Mouse move", mouseEventFilter::filterMouseMove)
          configurable("Mouse drag", mouseEventFilter::filterMouseDrag)
          configurable("Mouse click", mouseEventFilter::filterMouseClick)
          configurable("Mouse down", mouseEventFilter::filterMouseDown)
          configurable("Mouse up", mouseEventFilter::filterMouseUp)
          configurable("Mouse double-click", mouseEventFilter::filterMouseDoubleClick)
          configurable("Mouse wheel", mouseEventFilter::filterMouseWheel)

          section("Key events")
          configurable("Key up", keyEventFilter::filterKeyUp)
          configurable("Key down", keyEventFilter::filterKeyDown)
          configurable("Key type", keyEventFilter::filterKeyType)

          section("Pointer events")
          configurable("Pointer leave", pointerEventFilter::filterPointerLeave)
          configurable("Pointer out", pointerEventFilter::filterPointerOut)
          configurable("Pointer cancel", pointerEventFilter::filterPointerCancel)
          configurable("Pointer up", pointerEventFilter::filterPointerUp)
          configurable("Pointer move", pointerEventFilter::filterPointerMove)
          configurable("Pointer down", pointerEventFilter::filterPointerDown)
          configurable("Pointer enter", pointerEventFilter::filterPointerEnter)
          configurable("Pointer over", pointerEventFilter::filterPointerOver)

          section("Touch events")
          configurable("Touch cancel", touchEventFilter::filterTouchCancel)
          configurable("Touch move", touchEventFilter::filterTouchMove)
          configurable("Touch end", touchEventFilter::filterTouchEnd)
          configurable("Touch start", touchEventFilter::filterTouchStart)
        }
      }
    }
  }
}

