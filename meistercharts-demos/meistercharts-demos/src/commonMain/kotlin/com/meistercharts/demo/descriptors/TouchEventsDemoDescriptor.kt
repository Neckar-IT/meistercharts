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

import com.meistercharts.algorithms.impl.ZoomAndTranslationDefaults
import com.meistercharts.algorithms.layers.AbstractLayer
import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.algorithms.layers.LayerType
import com.meistercharts.algorithms.layers.addClearBackground
import com.meistercharts.algorithms.painter.Color
import com.meistercharts.algorithms.painter.RgbaColor
import com.meistercharts.canvas.ChartSupport
import com.meistercharts.canvas.DirtyReason
import com.meistercharts.canvas.events.CanvasTouchEventHandler
import com.meistercharts.demo.ChartingDemo
import com.meistercharts.demo.ChartingDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.model.Coordinates
import com.meistercharts.events.EventConsumption
import com.meistercharts.events.TouchCancelEvent
import com.meistercharts.events.TouchEndEvent
import com.meistercharts.events.TouchMoveEvent
import com.meistercharts.events.TouchStartEvent


/**
 */
class TouchEventsDemoDescriptor : ChartingDemoDescriptor<Nothing> {
  override val name: String = "Touch Event Handling"
  override val description: String = "## Demonstration of touch events handled by layers"
  override val category: DemoCategory = DemoCategory.Interaction

  override fun createDemo(configuration: PredefinedConfiguration<Nothing>?): ChartingDemo {
    return ChartingDemo {
      meistercharts {
        zoomAndTranslationDefaults(ZoomAndTranslationDefaults.tenPercentMargin)

        //contentAreaSizingStrategy = FixedContentAreaWidth(1000.0)

        zoomAndTranslationConfiguration {
          translateOnTouchDrag = false
          zoomOnPinch = false
        }

        configure {
          layers.addClearBackground()
          layers.addLayer(TouchEventsLayer())
        }
      }
    }
  }

}

class TouchEventsLayer : AbstractLayer() {

  override val type: LayerType
    get() = LayerType.Content

  private var color: Color = touchCancelColor
  private var coordinates: Coordinates = Coordinates.none

  override val touchEventHandler: CanvasTouchEventHandler = object : CanvasTouchEventHandler {
    override fun onStart(event: TouchStartEvent, chartSupport: ChartSupport): EventConsumption {
      color = touchStartColor
      coordinates = event.changedTouches.firstOrNull()?.coordinates ?: Coordinates.none
      chartSupport.markAsDirty(DirtyReason.UserInteraction)
      return EventConsumption.Consumed
    }

    override fun onEnd(event: TouchEndEvent, chartSupport: ChartSupport): EventConsumption {
      color = touchEndColor
      coordinates = event.changedTouches.firstOrNull()?.coordinates ?: Coordinates.none
      chartSupport.markAsDirty(DirtyReason.UserInteraction)
      return EventConsumption.Consumed
    }

    override fun onMove(event: TouchMoveEvent, chartSupport: ChartSupport): EventConsumption {
      color = touchMoveColor
      coordinates = event.changedTouches.firstOrNull()?.coordinates ?: Coordinates.none
      chartSupport.markAsDirty(DirtyReason.UserInteraction)
      return EventConsumption.Consumed
    }

    override fun onCancel(event: TouchCancelEvent, chartSupport: ChartSupport): EventConsumption {
      color = touchCancelColor
      coordinates = event.changedTouches.firstOrNull()?.coordinates ?: Coordinates.none
      chartSupport.markAsDirty(DirtyReason.UserInteraction)
      return EventConsumption.Consumed
    }
  }

  override fun paint(paintingContext: LayerPaintingContext) {
    val gc = paintingContext.gc
    gc.fillStyle(color)
    gc.fillOvalCenter(coordinates.x, coordinates.y, 10.0, 10.0)
  }

  companion object {
    val touchStartColor: RgbaColor = Color.green
    val touchEndColor: RgbaColor = Color.red
    val touchMoveColor: RgbaColor = Color.orange
    val touchCancelColor: RgbaColor = Color.gray
  }
}
