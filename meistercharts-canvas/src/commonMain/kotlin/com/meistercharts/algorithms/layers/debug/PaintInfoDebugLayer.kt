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
package com.meistercharts.algorithms.layers.debug

import com.meistercharts.algorithms.layers.AbstractLayer
import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.algorithms.layers.LayerType
import com.meistercharts.algorithms.layers.Layers
import it.neckar.open.formatting.dateTimeFormat
import it.neckar.logging.LoggerFactory
import it.neckar.logging.debug

/**
 * This layer debugs the paint events
 */
class PaintInfoDebugLayer : AbstractLayer() {
  private val dateFormat = dateTimeFormat

  override val type: LayerType
    get() = LayerType.Notification

  override fun paint(paintingContext: LayerPaintingContext) {
    val chartState = paintingContext.chartSupport.currentChartState

    logger.debug {
      """Repaint called @ ${dateFormat.format(paintingContext.frameTimestamp, paintingContext.i18nConfiguration)} (delta: ${paintingContext.frameTimestampDelta}) ms
        |   Content Area Size: ${chartState.contentAreaSize}
        |   Window translation: ${chartState.windowTranslation}
        |   Zoom: ${chartState.zoom}
      """.trimMargin()
    }
  }

  companion object {
    private val logger = LoggerFactory.getLogger("com.meistercharts.events.gesture.PinchGestureSupport")
  }
}


/**
 * Registers a new paint info debug layer
 */
fun Layers.addPaintInfoDebug() {
  addLayer(PaintInfoDebugLayer())
}
