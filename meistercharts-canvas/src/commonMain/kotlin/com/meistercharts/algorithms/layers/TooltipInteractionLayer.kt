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
package com.meistercharts.algorithms.layers

import com.meistercharts.algorithms.layout.BoxIndex
import com.meistercharts.algorithms.layout.EquisizedBoxLayout
import com.meistercharts.model.category.CategoryIndex
import com.meistercharts.annotations.ContentArea
import com.meistercharts.annotations.ContentAreaRelative
import com.meistercharts.annotations.TimeRelative
import com.meistercharts.annotations.Window
import com.meistercharts.canvas.ChartSupport
import com.meistercharts.canvas.events.CanvasKeyEventHandler
import com.meistercharts.canvas.events.CanvasMouseEventHandler
import com.meistercharts.canvas.events.CanvasTouchEventHandler
import com.meistercharts.events.EventConsumption
import it.neckar.events.KeyCode
import it.neckar.events.KeyDownEvent
import it.neckar.events.KeyStroke
import it.neckar.events.MouseMoveEvent
import it.neckar.events.TouchStartEvent
import com.meistercharts.history.ReferenceEntryDataSeriesIndex
import it.neckar.geometry.Coordinates
import it.neckar.geometry.Orientation

/**
 * Handles the events for the tooltip interactions
 * Handles *ONLY* the interaction - does *not* paint the tooltips
 *
 * Listens to mouse and touch events.
 *
 * This layer should be registered very early (at the bottom) to avoid invalid consumptions of events
 */
class TooltipInteractionLayer<T>(
  val configuration: Configuration<T>,
  additionalConfiguration: Configuration<T>.() -> Unit = {},
) : AbstractLayer() {

  init {
    configuration.additionalConfiguration()
  }

  override val type: LayerType = LayerType.Background

  /**
   * Processes an event at the given location
   */
  private fun processEventAt(eventCoordinates: @Window Coordinates?, chartSupport: ChartSupport) {
    if (eventCoordinates == null) {
      configuration.newTooltipLocationHandler(null, chartSupport)
    }

    configuration.newTooltipLocationHandler(eventCoordinates, chartSupport)
  }

  override val mouseEventHandler: CanvasMouseEventHandler = object : CanvasMouseEventHandler {
    override fun onMove(event: MouseMoveEvent, chartSupport: ChartSupport): EventConsumption {
      processEventAt(event.coordinates, chartSupport)
      return EventConsumption.Ignored //ignore move events
    }
  }

  override val touchEventHandler: CanvasTouchEventHandler = object : CanvasTouchEventHandler {
    override fun onStart(event: TouchStartEvent, chartSupport: ChartSupport): EventConsumption {
      if (event.targetTouches.size != 1) {
        return EventConsumption.Ignored
      }

      processEventAt(event.firstTarget.coordinates, chartSupport)
      return EventConsumption.Ignored //ignore touch events
    }
  }

  override val keyEventHandler: CanvasKeyEventHandler = object : CanvasKeyEventHandler {
    override fun onDown(event: KeyDownEvent, chartSupport: ChartSupport): EventConsumption {
      if (event.keyStroke == KeyStroke(KeyCode.Escape)) {
        //Reset selection on ESC
        configuration.newTooltipLocationHandler(null, chartSupport)
        return EventConsumption.Consumed
      }
      return EventConsumption.Ignored
    }
  }

  override fun paint(paintingContext: LayerPaintingContext) {
    // Noop
  }

  class Configuration<T>(
    /**
     * Is called for new tooltip locations
     */
    val newTooltipLocationHandler: (
      eventCoordinates: @Window Coordinates?,
      chartSupport: ChartSupport,
    ) -> Unit,
  )


  companion object {
    /**
     * Handles selection of categories - on the x-axis
     */
    fun forCategories(
      /**
       * The orientation of the categories.
       *
       * * [Orientation.Horizontal]: The categories are placed horizontally (x values differ for each category)
       * * [Orientation.Vertical]: The categories are placed vertically (y values differ for each category)
       */
      orientation: () -> Orientation,

      /**
       * Provides the current [EquisizedBoxLayout] (usually from painting properties)
       */
      layoutProvider: () -> EquisizedBoxLayout,

      /**
       * This method is called *often* (potentially on every mouse / touch event).
       * Therefore, it must be very, very fast
       */
      selectionSink: (newSelection: CategoryIndex?, chartSupport: ChartSupport) -> Unit,

      ): TooltipInteractionLayer<CategoryIndex> {
      return TooltipInteractionLayer(
        Configuration { coordinates, chartSupport ->
          if (coordinates == null) {
            selectionSink(null, chartSupport)
            return@Configuration
          }

          val layout: EquisizedBoxLayout = layoutProvider()

          //Convert to content area (relative to the content area
          val boxIndex: BoxIndex? = when (orientation()) {
            Orientation.Horizontal -> {
              @ContentArea val xZoomed = chartSupport.chartCalculator.window2contentAreaX(coordinates.x)
              layout.boxIndexFor(xZoomed)
            }

            Orientation.Vertical -> {
              @ContentArea val yZoomed = chartSupport.chartCalculator.window2contentAreaY(coordinates.y)
              layout.boxIndexFor(yZoomed)
            }
          }

          if (boxIndex == null) {
            selectionSink(null, chartSupport)
          } else {
            selectionSink(CategoryIndex(boxIndex.value), chartSupport)
          }
        }
      )
    }

    /**
     * Creates a new tooltip interaction layer for discrete data series
     */
    fun forDiscreteDataSeries(
      /**
       * Provides the current [EquisizedBoxLayout] (usually from painting properties)
       */
      layoutProvider: () -> EquisizedBoxLayout,

      /**
       * This method is called *often* (potentially on every mouse / touch event).
       * Therefore, it must be very, very fast.
       *
       * The boxIndex must be converted to the corresponding [ReferenceEntryDataSeriesIndex] - depending on the visible indices.
       */
      selectionSink: (relativeTime: @TimeRelative Double?, boxIndex: BoxIndex?, chartSupport: ChartSupport) -> Unit,

      ): TooltipInteractionLayer<ReferenceEntryDataSeriesIndex> {
      return TooltipInteractionLayer(
        Configuration { coordinates: @Window Coordinates?, chartSupport ->
          if (coordinates == null) {
            selectionSink(null, null, chartSupport)
            return@Configuration
          }

          val chartCalculator = chartSupport.chartCalculator

          @ContentAreaRelative @TimeRelative val timeRelative = chartCalculator.window2contentAreaRelativeX(coordinates.x)

          //Convert to content area (relative to the content area
          val layout: EquisizedBoxLayout = layoutProvider()

          @ContentArea val yContentArea = chartCalculator.window2contentAreaY(coordinates.y)
          val boxIndex: BoxIndex? = layout.boxIndexFor(yContentArea)

          if (boxIndex == null) {
            selectionSink(timeRelative, null, chartSupport)
          } else {
            selectionSink(timeRelative, boxIndex, chartSupport)
          }
        }
      )
    }
  }
}
