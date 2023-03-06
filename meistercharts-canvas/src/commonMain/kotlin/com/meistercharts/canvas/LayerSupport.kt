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

import com.meistercharts.algorithms.layers.Layers
import com.meistercharts.charts.ChartId
import com.meistercharts.events.KeyEventBroker
import com.meistercharts.events.MouseEventBroker
import com.meistercharts.events.PointerEventBroker
import com.meistercharts.events.TouchEventBroker
import it.neckar.open.observable.ReadOnlyObservableObject

/**
 * Supports the dirty state for a canvas.
 * Paints only if necessary
 *
 * The layer support itself does not have any information about the axis or the painting strategy.
 * But it has the possibility to zoom and pan.
 */
@MeisterChartsBuilderDsl
interface LayerSupport : PaintListener {
  /**
   * The canvas that is painted on
   */
  val chartSupport: ChartSupport

  /**
   * Returns the chart id
   */
  val chartId: ChartId
    get() {
      return chartSupport.chartId
    }

  /**
   * Contains the layers that are responsible for rendering the content
   */
  val layers: Layers

  /**
   * Interface to mouse events of the canvas
   *
   * Attention: In mose cases [com.meistercharts.algorithms.layers.Layer.mouseEventHandler] should be overridden instead
   */
  val mouseEvents: MouseEventBroker

  /**
   * Interface to key events of the canvas
   *
   * Attention: In mose cases [com.meistercharts.algorithms.layers.Layer.keyEventHandler] should be overridden instead
   */
  val keyEvents: KeyEventBroker

  /**
   * Interface to pointer events of the canvas
   */
  @Deprecated("Pointer events are not supported")
  val pointerEvents: PointerEventBroker

  /**
   * Interface to touch events of the canvas
   * Attention: In mose cases [com.meistercharts.algorithms.layers.Layer.touchEventHandler] should be overridden instead
   */
  val touchEvents: TouchEventBroker

  /**
   * Supports statistics about the repaints.
   * ATTENTION: By default the statistics are *not* recorded.
   */
  val paintStatisticsSupport: PaintStatisticsSupport

  /**
   * If set to true the repaint statistics are recorded
   */
  var recordPaintStatistics: Boolean

  /**
   * Returns the debug configuration
   */
  val debug: DebugConfiguration
    get() {
      return chartSupport.debug
    }

  /**
   * Marks the canvas as dirty - the canvas will be repainted on the next tick.
   *
   * This method can be called often
   */
  fun markAsDirty()
}

/**
 * Registers a dirty listeners for the given [layerSupport]
 */
fun ReadOnlyObservableObject<Any?>.registerDirtyListener(layerSupport: LayerSupport) {
  consume {
    layerSupport.markAsDirty()
  }
}
