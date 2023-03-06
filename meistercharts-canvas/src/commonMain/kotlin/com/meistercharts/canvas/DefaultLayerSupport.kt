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
package com.meistercharts.canvas

import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.algorithms.layers.Layers
import com.meistercharts.algorithms.layers.text.TextLayer
import com.meistercharts.canvas.events.CanvasMouseEventHandler
import com.meistercharts.canvas.events.MouseEvent2CanvasHandler
import it.neckar.open.collections.fastForEach
import it.neckar.open.kotlin.lang.consumeUntil
import com.meistercharts.events.EventConsumption
import com.meistercharts.events.EventConsumption.Consumed
import com.meistercharts.events.EventConsumption.Ignored
import com.meistercharts.events.KeyDownEvent
import com.meistercharts.events.KeyEventBroker
import com.meistercharts.events.KeyEventHandler
import com.meistercharts.events.KeyTypeEvent
import com.meistercharts.events.KeyUpEvent
import com.meistercharts.events.MouseClickEvent
import com.meistercharts.events.MouseDoubleClickEvent
import com.meistercharts.events.MouseDownEvent
import com.meistercharts.events.MouseDragEvent
import com.meistercharts.events.MouseEventBroker
import com.meistercharts.events.MouseMoveEvent
import com.meistercharts.events.MouseUpEvent
import com.meistercharts.events.MouseWheelEvent
import com.meistercharts.events.PointerCancelEvent
import com.meistercharts.events.PointerDownEvent
import com.meistercharts.events.PointerEnterEvent
import com.meistercharts.events.PointerEventBroker
import com.meistercharts.events.PointerEventHandler
import com.meistercharts.events.PointerLeaveEvent
import com.meistercharts.events.PointerMoveEvent
import com.meistercharts.events.PointerOutEvent
import com.meistercharts.events.PointerOverEvent
import com.meistercharts.events.PointerUpEvent
import com.meistercharts.events.TouchCancelEvent
import com.meistercharts.events.TouchEndEvent
import com.meistercharts.events.TouchEventBroker
import com.meistercharts.events.TouchEventHandler
import com.meistercharts.events.TouchMoveEvent
import com.meistercharts.events.TouchStartEvent
import com.meistercharts.events.register
import it.neckar.open.unit.si.ms

/**
 * Default implementation for a layer support
 */
class DefaultLayerSupport(
  /**
   * The canvas that is painted on
   */
  override val chartSupport: ChartSupport
) : LayerSupport {
  /**
   * Contains the layers that are responsible for rendering the content
   */
  override val layers: Layers = Layers(chartSupport.chartId)

  /**
   * Interface to mouse events of the canvas
   */
  override val mouseEvents: MouseEventBroker = chartSupport.mouseEvents

  /**
   * Interface to key events of the canvas
   */
  override val keyEvents: KeyEventBroker = chartSupport.keyEvents

  /**
   * Interface to pointer events of the canvas
   */
  override val pointerEvents: PointerEventBroker = chartSupport.pointerEvents

  /**
   * Interface to touch events of the canvas
   */
  override val touchEvents: TouchEventBroker = chartSupport.touchEvents

  override val paintStatisticsSupport: PaintStatisticsSupport = PaintStatisticsSupport()

  override var recordPaintStatistics: Boolean = false

  init {
    //Register this as paint service
    chartSupport.onPaint(this)

    setUpMouseEventsForLayers()
    setUpKeyEventsForLayers()
    setUpPointerEventsForLayers()
    setUpTouchEventsForLayers()
  }

  private fun setUpMouseEventsForLayers() {
    mouseEvents.register(
      MouseEvent2CanvasHandler(
        chartSupport,
        object : CanvasMouseEventHandler {
          override fun onClick(event: MouseClickEvent, chartSupport: ChartSupport): EventConsumption {
            return layers.layersOrderedForInteraction.consumeUntil(Consumed) {
              it.mouseEventHandler?.onClick(event, chartSupport)
            } ?: Ignored
          }

          override fun onDown(event: MouseDownEvent, chartSupport: ChartSupport): EventConsumption {
            return layers.layersOrderedForInteraction.consumeUntil(Consumed) {
              it.mouseEventHandler?.onDown(event, chartSupport)
            } ?: Ignored
          }

          override fun onUp(event: MouseUpEvent, chartSupport: ChartSupport): EventConsumption {
            return layers.layersOrderedForInteraction.consumeUntil(Consumed) {
              it.mouseEventHandler?.onUp(event, chartSupport)
            } ?: Ignored
          }

          override fun onDoubleClick(event: MouseDoubleClickEvent, chartSupport: ChartSupport): EventConsumption {
            return layers.layersOrderedForInteraction.consumeUntil(Consumed) {
              it.mouseEventHandler?.onDoubleClick(event, chartSupport)
            } ?: Ignored
          }

          override fun onMove(event: MouseMoveEvent, chartSupport: ChartSupport): EventConsumption {
            return layers.layersOrderedForInteraction.consumeUntil(Consumed) {
              it.mouseEventHandler?.onMove(event, chartSupport)
            } ?: Ignored
          }

          override fun onDrag(event: MouseDragEvent, chartSupport: ChartSupport): EventConsumption {
            return layers.layersOrderedForInteraction.consumeUntil(Consumed) {
              it.mouseEventHandler?.onDrag(event, chartSupport)
            } ?: Ignored
          }

          override fun onWheel(event: MouseWheelEvent, chartSupport: ChartSupport): EventConsumption {
            return layers.layersOrderedForInteraction.consumeUntil(Consumed) {
              it.mouseEventHandler?.onWheel(event, chartSupport)
            } ?: Ignored
          }
        }
      )
    )
  }

  private fun setUpKeyEventsForLayers() {
    keyEvents.register(object : KeyEventHandler {
      override fun onDown(event: KeyDownEvent): EventConsumption {
        return layers.layersOrderedForInteraction.consumeUntil(Consumed) {
          it.keyEventHandler?.onDown(event, chartSupport)
        } ?: Ignored
      }

      override fun onUp(event: KeyUpEvent): EventConsumption {
        return layers.layersOrderedForInteraction.consumeUntil(Consumed) {
          it.keyEventHandler?.onUp(event, chartSupport)
        } ?: Ignored
      }

      override fun onType(event: KeyTypeEvent): EventConsumption {
        return layers.layersOrderedForInteraction.consumeUntil(Consumed) {
          it.keyEventHandler?.onType(event, chartSupport)
        } ?: Ignored
      }
    })
  }

  private fun setUpTouchEventsForLayers() {
    touchEvents.register(object : TouchEventHandler {
      override fun onStart(event: TouchStartEvent): EventConsumption {
        return layers.layersOrderedForInteraction.consumeUntil(Consumed) {
          it.touchEventHandler?.onStart(event, chartSupport)
        } ?: Ignored
      }

      override fun onEnd(event: TouchEndEvent): EventConsumption {
        return layers.layersOrderedForInteraction.consumeUntil(Consumed) {
          it.touchEventHandler?.onEnd(event, chartSupport)
        } ?: Ignored
      }

      override fun onMove(event: TouchMoveEvent): EventConsumption {
        return layers.layersOrderedForInteraction.consumeUntil(Consumed) {
          it.touchEventHandler?.onMove(event, chartSupport)
        } ?: Ignored
      }

      override fun onCancel(event: TouchCancelEvent): EventConsumption {
        return layers.layersOrderedForInteraction.consumeUntil(Consumed) {
          it.touchEventHandler?.onCancel(event, chartSupport)
        } ?: Ignored
      }
    })
  }

  private fun setUpPointerEventsForLayers() {
    pointerEvents.register(object : PointerEventHandler {
      override fun onOver(event: PointerOverEvent): EventConsumption {
        return layers.layersOrderedForInteraction.consumeUntil(Consumed) {
          it.pointerEventHandler?.onOver(event, chartSupport)
        } ?: Ignored
      }

      override fun onEnter(event: PointerEnterEvent): EventConsumption {
        return layers.layersOrderedForInteraction.consumeUntil(Consumed) {
          it.pointerEventHandler?.onEnter(event, chartSupport)
        } ?: Ignored
      }

      override fun onDown(event: PointerDownEvent): EventConsumption {
        return layers.layersOrderedForInteraction.consumeUntil(Consumed) {
          it.pointerEventHandler?.onDown(event, chartSupport)
        } ?: Ignored
      }

      override fun onMove(event: PointerMoveEvent): EventConsumption {
        return layers.layersOrderedForInteraction.consumeUntil(Consumed) {
          it.pointerEventHandler?.onMove(event, chartSupport)
        } ?: Ignored
      }

      override fun onUp(event: PointerUpEvent): EventConsumption {
        return layers.layersOrderedForInteraction.consumeUntil(Consumed) {
          it.pointerEventHandler?.onUp(event, chartSupport)
        } ?: Ignored
      }

      override fun onCancel(event: PointerCancelEvent): EventConsumption {
        return layers.layersOrderedForInteraction.consumeUntil(Consumed) {
          it.pointerEventHandler?.onCancel(event, chartSupport)
        } ?: Ignored
      }

      override fun onOut(event: PointerOutEvent): EventConsumption {
        return layers.layersOrderedForInteraction.consumeUntil(Consumed) {
          it.pointerEventHandler?.onOut(event, chartSupport)
        } ?: Ignored
      }

      override fun onLeave(event: PointerLeaveEvent): EventConsumption {
        return layers.layersOrderedForInteraction.consumeUntil(Consumed) {
          it.pointerEventHandler?.onLeave(event, chartSupport)
        } ?: Ignored
      }
    })
  }

  /**
   * Marks the canvas as dirty - the canvas will be painted on the next tick.
   *
   * This method can be called often
   */
  override fun markAsDirty() {
    chartSupport.markAsDirty()
  }

  /**
   * Handlers that are called if there have been resources missing during the paint
   */
  private val missingResourcesHandlers: MutableList<MissingResourcesHandler> = mutableListOf()

  override fun paint(frameTimestamp: @ms Double, delta: @ms Double, paintingLoopIndex: PaintingLoopIndex) {
    val paintingContext = LayerPaintingContext(chartSupport.canvas.gc, this, frameTimestamp, delta, paintingLoopIndex)

    //Clear all painting properties *before* painting.
    //This is necessary to ensure the painting variables are calculated before they are used
    chartSupport.paintingProperties.clear()

    if (layers.isEmpty()) {
      //Show a welcome message to avoid empty charts when no layers are configured
      TextLayer.helloMeisterChart.run {
        layout(paintingContext)
        paint(paintingContext)
      }
    } else {
      if (recordPaintStatistics) {
        paintStatisticsSupport.store(layers.paintLayersWithStats(paintingContext))
      } else {
        layers.paintLayers(paintingContext)
      }

      //handle missing resources
      paintingContext.missingResources.missingURLs.takeIf {
        it.isNotEmpty()
      }?.let { missingUrls ->
        missingResourcesHandlers.fastForEach {
          it.missingResourcesDetected(paintingContext, missingUrls)
        }
      }
    }
  }
}
