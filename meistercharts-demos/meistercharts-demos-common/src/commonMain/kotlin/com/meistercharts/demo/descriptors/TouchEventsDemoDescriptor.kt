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
import com.meistercharts.canvas.DirtyReason
import com.meistercharts.canvas.events.CanvasTouchEventHandler
import com.meistercharts.canvas.fillStyle
import com.meistercharts.color.Color
import com.meistercharts.color.ColorProvider
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.DemoQuality
import com.meistercharts.demo.MeisterchartsDemo
import com.meistercharts.demo.MeisterchartsDemoDescriptor
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.VariabilityType
import com.meistercharts.events.EventConsumption
import com.meistercharts.zoom.ZoomAndTranslationDefaults
import it.neckar.events.TouchCancelEvent
import it.neckar.events.TouchEndEvent
import it.neckar.events.TouchMoveEvent
import it.neckar.events.TouchStartEvent
import it.neckar.geometry.Coordinates


/**
 */
class TouchEventsDemoDescriptor : MeisterchartsDemoDescriptor<Nothing> {
  override val uuid: Uuid = Uuid.parse("ce3edaa3-f750-4e68-a402-aef82ebcc23b")
  override val name: String = "Touch Event Handling"
  override val description: String = "Demonstrates touch events handled by layers."
  override val category: DemoCategory = DemoCategory.Interaction
  override val quality: DemoQuality = DemoQuality.High
  override val variabilityType: VariabilityType = VariabilityType.Unstable

  override fun prepareDemo(configuration: PredefinedConfiguration<Nothing>?): MeisterchartsDemo {
    return MeisterchartsDemo(this) {
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

  private var color: ColorProvider = touchCancelColor
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
    val touchStartColor: ColorProvider = Color.green
    val touchEndColor: ColorProvider = Color.red
    val touchMoveColor: ColorProvider = Color.orange
    val touchCancelColor: ColorProvider = Color.gray
  }
}
