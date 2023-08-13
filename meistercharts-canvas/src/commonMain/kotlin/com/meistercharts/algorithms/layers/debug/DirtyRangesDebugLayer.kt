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

import com.meistercharts.time.TimeRange
import com.meistercharts.algorithms.layers.AbstractLayer
import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.algorithms.layers.LayerType
import com.meistercharts.color.Color
import com.meistercharts.annotations.Window
import com.meistercharts.annotations.Zoomed
import com.meistercharts.canvas.StrokeLocation
import com.meistercharts.canvas.ConfigurationDsl
import com.meistercharts.history.SamplingPeriod
import com.meistercharts.history.downsampling.DownSamplingDirtyRangesCollector
import it.neckar.geometry.Direction
import it.neckar.open.collections.fastForEach
import it.neckar.open.collections.fastForEachIndexed

/**
 * Paint the dirty ranges
 */
class DirtyRangesDebugLayer(
  val configuration: Configuration,
  additionalConfiguration: Configuration.() -> Unit = {}
) : AbstractLayer() {

  constructor(
    downSamplingDirtyRangesCollector: DownSamplingDirtyRangesCollector,
    contentAreaTimeRange: TimeRange,
    additionalConfiguration: Configuration.() -> Unit = {}
  ): this(Configuration(downSamplingDirtyRangesCollector, contentAreaTimeRange), additionalConfiguration)

  init {
    configuration.additionalConfiguration()
  }

  override val type: LayerType = LayerType.Notification

  override fun paint(paintingContext: LayerPaintingContext) {
    val gc = paintingContext.gc
    val chartCalculator = paintingContext.chartCalculator

    val visibleTimeRange = chartCalculator.visibleTimeRangeXinWindow(configuration.contentAreaTimeRange)


    SamplingPeriod.entries
      .fastForEachIndexed { index, period ->
        val dirtyTimeRanges = configuration.downSamplingDirtyRangesCollector[period]

        @Window val y = configuration.insetsTop + index * configuration.barHeight

        dirtyTimeRanges?.timeRanges?.fastForEach { dirtyTimeRange ->
          if (!visibleTimeRange.isOverlapping(dirtyTimeRange)) {
            return@fastForEach
          }

          @Window val start = chartCalculator.time2windowX(dirtyTimeRange.start, configuration.contentAreaTimeRange)
          @Window val end = chartCalculator.time2windowX(dirtyTimeRange.end, configuration.contentAreaTimeRange)
          @Zoomed val width = end - start

          gc.fill(configuration.barColor)
          gc.fillRect(start, y, width, configuration.barHeight)
          gc.stroke(Color.orange)
          gc.strokeRect(start, y, width, configuration.barHeight, StrokeLocation.Inside)
        }

        //paint the lane border
        gc.stroke(Color.silver)
        gc.strokeRect(0.0, y, gc.width, configuration.barHeight)

        gc.fill(configuration.barLabelColor)
        gc.fillText(period.label, 0.0, y + configuration.barHeight / 2.0, Direction.CenterLeft)
      }
  }


  @ConfigurationDsl
  class Configuration(
    val downSamplingDirtyRangesCollector: DownSamplingDirtyRangesCollector,
    val contentAreaTimeRange: TimeRange,
  ) {
    var insetsTop: @Zoomed Double = 50.0

    /**
     * The height of one bar
     */
    val barHeight: @Zoomed Double = 20.0

    val barColor: Color = Color.orange

    val barLabelColor: Color = Color.red
  }
}

