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

import com.meistercharts.time.TimeRange
import com.meistercharts.color.Color
import com.meistercharts.canvas.ConfigurationDsl
import com.meistercharts.history.HistoryUpdateInfo
import it.neckar.open.unit.other.px

/**
 * Visualizes the updates to the history
 */
class HistoryUpdatesVisualizationLayer(val contentAreaTimeRange: TimeRange) : AbstractLayer() {
  override val type: LayerType = LayerType.Notification

  val style: Style = Style()

  var lastUpdateInfo: HistoryUpdateInfo? = null

  override fun paint(paintingContext: LayerPaintingContext) {
    lastUpdateInfo?.let { lastUpdateInfo ->
      val gc = paintingContext.gc
      val chartCalculator = paintingContext.chartCalculator

      lastUpdateInfo.updatedTimeRanges.fastForEach {
        val startX = chartCalculator.time2windowX(it.start, contentAreaTimeRange)
        val endX = chartCalculator.time2windowX(it.end, contentAreaTimeRange)

        gc.fill(style.fillColor)
        gc.fillRect(startX, 0.0, (endX - startX).coerceAtLeast(style.minimumWidth), gc.height)
      }
    }
  }

  @ConfigurationDsl
  class Style {
    /**
     * The minimum width that is visualized
     */
    var minimumWidth: @px Double = 1.0

    var fillColor: Color = Color("#FF000055")
  }
}
