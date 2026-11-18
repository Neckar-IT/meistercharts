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
package com.meistercharts.demo.descriptors.zeitstrahl

import com.meistercharts.algorithms.layers.AbstractLayer
import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.algorithms.layers.LayerType
import com.meistercharts.canvas.ChartSupport
import com.meistercharts.canvas.DirtyReason
import com.meistercharts.canvas.events.CanvasMouseEventHandler
import com.meistercharts.events.EventConsumption
import it.neckar.events.MouseDownEvent
import it.neckar.events.MouseDragEvent
import it.neckar.events.MouseMoveEvent
import it.neckar.events.MouseUpEvent
import it.neckar.events.MouseWheelEvent
import kotlin.math.pow

/**
 * Keeps the width of [viewport] at the canvas width right of the tree column. The mouse wheel
 * - zooms the time around the pointer,
 * - with Alt zooms the row height of [rowScale],
 * - with Shift pans the time,
 * - with Ctrl stays with the browser, because a pinch on a trackpad arrives as Ctrl + wheel.
 *
 * Dragging pans the time.
 */
class DeepTimeViewportInteractionLayer(
  private val viewport: DeepTimeViewport,
  private val rowScale: RowScale,
) : AbstractLayer() {
  override val type: LayerType = LayerType.Content

  /** The x of the last drag event, `null` while no button is down. */
  private var lastDragX: Double? = null

  override fun paint(paintingContext: LayerPaintingContext) {
    viewport.updateWindowWidth(paintingContext.gc.width - ZeitstrahlPocLayout.TreeWidth)
  }

  override val mouseEventHandler: CanvasMouseEventHandler = object : CanvasMouseEventHandler {
    override fun onWheel(event: MouseWheelEvent, chartSupport: ChartSupport): EventConsumption {
      val modifiers = event.modifierCombination
      val notches = event.delta / PixelsPerWheelNotch
      when {
        modifiers.control -> return EventConsumption.Ignored
        modifiers.alt -> rowScale.zoomBy(ZoomFactorPerWheelNotch.pow(-notches))
        modifiers.shift -> viewport.panBy(-event.delta)
        else -> viewport.zoomAround((event.coordinates.x - ZeitstrahlPocLayout.TreeWidth).coerceAtLeast(0.0), ZoomFactorPerWheelNotch.pow(notches))
      }
      chartSupport.markAsDirty(DirtyReason.UserInteraction)
      return EventConsumption.Consumed
    }

    override fun onDown(event: MouseDownEvent, chartSupport: ChartSupport): EventConsumption {
      lastDragX = event.coordinates.x
      return EventConsumption.Ignored
    }

    override fun onDrag(event: MouseDragEvent, chartSupport: ChartSupport): EventConsumption {
      // A button pressed outside the canvas drags into it without a down event here.
      val previousX = lastDragX ?: return EventConsumption.Ignored
      viewport.panBy(event.coordinates.x - previousX)
      lastDragX = event.coordinates.x
      chartSupport.markAsDirty(DirtyReason.UserInteraction)
      return EventConsumption.Consumed
    }

    // A button released outside the canvas sends no up event here; the next move without a button ends the drag.
    override fun onMove(event: MouseMoveEvent, chartSupport: ChartSupport): EventConsumption {
      lastDragX = null
      return EventConsumption.Ignored
    }

    override fun onUp(event: MouseUpEvent, chartSupport: ChartSupport): EventConsumption {
      lastDragX = null
      return EventConsumption.Ignored
    }
  }

  private companion object {
    const val ZoomFactorPerWheelNotch: Double = 1.25

    /** The browser reports one notch of a mouse wheel as 100 px. */
    const val PixelsPerWheelNotch: Double = 100.0
  }
}
