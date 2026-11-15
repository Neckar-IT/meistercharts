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

import com.meistercharts.algorithms.layers.AbstractLayer
import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.algorithms.layers.LayerType
import com.meistercharts.algorithms.layers.addClearBackground
import com.meistercharts.algorithms.layers.text.TextPainter
import com.meistercharts.canvas.ChartSupport
import com.meistercharts.canvas.DirtyReason
import com.meistercharts.canvas.events.CanvasMouseEventHandler
import com.meistercharts.canvas.text.LineSpacing
import com.meistercharts.color.Color
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.DemoQuality
import com.meistercharts.demo.MeisterchartsDemo
import com.meistercharts.demo.MeisterchartsDemoDescriptor
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.VariabilityType
import com.meistercharts.events.EventConsumption
import com.meistercharts.font.FontDescriptorFragment
import com.meistercharts.style.BoxStyle
import it.neckar.events.MouseButton
import it.neckar.events.MouseDownEvent
import it.neckar.events.MouseUpEvent
import it.neckar.geometry.Direction
import it.neckar.geometry.HorizontalAlignment
import it.neckar.open.collections.EvictingQueue
import kotlin.uuid.Uuid

/**
 * Verifies the platform mapping from native mouse button codes to [MouseButton].
 *
 * Press each mouse button (including the back/forward side buttons) on the canvas and compare
 * the detected [MouseButton] with the expected value from the table painted on the canvas.
 * On the web targets the events run through `MouseEvent.extractButton()` /
 * `MouseButton.fromDomButtonCode()` - the mapping this demo verifies.
 */
class MouseButtonMappingDemoDescriptor : MeisterchartsDemoDescriptor<Nothing> {
  override val uuid: Uuid = Uuid.parse("6124bdda-2749-427e-8bda-5ec799c19019")
  override val name: String = "Mouse Button Mapping"
  override val quality: DemoQuality = DemoQuality.High
  override val variabilityType: VariabilityType = VariabilityType.Unstable

  override val description: String = "Press each mouse button (including the back/forward side buttons) and compare the detected MouseButton with the expected value"
  override val category: DemoCategory = DemoCategory.Interaction

  override fun prepareDemo(configuration: PredefinedConfiguration<Nothing>?): MeisterchartsDemo {
    return MeisterchartsDemo(this) {
      meistercharts {
        disableZoomAndTranslation()

        configure {
          layers.addClearBackground()
          layers.addLayer(MouseButtonMappingLayer())
        }
      }
    }
  }
}

private class MouseButtonMappingLayer : AbstractLayer() {
  override val type: LayerType = LayerType.Content

  private val painter = TextPainter()

  private var lastDetectedButton: MouseButton? = null

  private val detectedButtonHistory: EvictingQueue<String> = EvictingQueue(8)

  override val mouseEventHandler: CanvasMouseEventHandler = object : CanvasMouseEventHandler {
    override fun onDown(event: MouseDownEvent, chartSupport: ChartSupport): EventConsumption {
      lastDetectedButton = event.button
      detectedButtonHistory.add("mouse down -> ${event.button}")
      chartSupport.markAsDirty(DirtyReason.UserInteraction)
      //Consume to prevent the browser default action (e.g. history navigation for the side buttons)
      return EventConsumption.Consumed
    }

    override fun onUp(event: MouseUpEvent, chartSupport: ChartSupport): EventConsumption {
      return EventConsumption.Consumed
    }
  }

  override fun paint(paintingContext: LayerPaintingContext) {
    val gc = paintingContext.gc

    val lines = buildList {
      add("Press each mouse button on this canvas. The detected MouseButton must match the expected value:")
      add("")
      add("Button pressed        | expected MouseButton")
      add("left                  | Primary")
      add("wheel                 | Middle")
      add("right                 | Secondary")
      add("side button back      | Back")
      add("side button forward   | Forward")
      add("")
      add("Last detected: ${lastDetectedButton ?: "-"}")
      add("")
      addAll(detectedButtonHistory.toList())
    }

    gc.font(FontDescriptorFragment.S)
    painter.paintText(
      gc,
      lines,
      Color.black(),
      BoxStyle.none,
      LineSpacing.Single,
      HorizontalAlignment.Left,
      Direction.TopLeft,
      10.0,
      10.0,
    )
  }
}
