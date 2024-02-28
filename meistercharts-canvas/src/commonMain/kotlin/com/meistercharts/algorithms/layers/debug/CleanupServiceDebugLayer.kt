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
import com.meistercharts.canvas.fill
import com.meistercharts.canvas.stroke
import com.meistercharts.font.FontDescriptorFragment
import com.meistercharts.history.HistoryBucketDescriptor
import com.meistercharts.history.HistoryBucketDescriptor.Companion.MaxSupportedDescriptorsCount
import com.meistercharts.history.InMemoryHistoryStorage
import it.neckar.geometry.Direction
import it.neckar.open.collections.fastForEachIndexed
import it.neckar.open.unit.si.ms
import kotlin.math.max

/**
 *
 */
class CleanupServiceDebugLayer(
  val historyStorage: InMemoryHistoryStorage,
  val contentAreaTimeRange: TimeRange
) : AbstractLayer() {
  override val type: LayerType = LayerType.Notification

  override fun paint(paintingContext: LayerPaintingContext) {
    val gc = paintingContext.gc
    val calculator = paintingContext.chartCalculator

    val visibleTimeRange = calculator.visibleTimeRangeXinWindow(contentAreaTimeRange)

    val historyBucketRange = historyStorage.naturalSamplingPeriod.toHistoryBucketRange()
    val latest = historyStorage.bookKeeping.latestBound(historyBucketRange) ?: return
    val lastToDelete = latest.previous(historyStorage.maxSizeConfiguration.keptBucketsCount)


    /*
     * The max distance that is supported
     */
    @ms val maxDistance = historyBucketRange.duration * MaxSupportedDescriptorsCount
    val start = max(visibleTimeRange.start, lastToDelete.end - maxDistance)

    val visibleDescriptors = HistoryBucketDescriptor.forRange(start, lastToDelete.end, historyBucketRange)

    visibleDescriptors.fastForEachIndexed { _, descriptor ->
      if (descriptor.end <= lastToDelete.end) {
        gc.fill(Color.red)
        gc.stroke(Color.red)

        @Window val startX = calculator.time2windowX(descriptor.start, contentAreaTimeRange)
        gc.strokeLine(startX, 0.0, startX, 20.0)

        gc.font(FontDescriptorFragment.S)
        gc.fillText("\uD83D\uDDD1", startX, 0.0, Direction.TopLeft, 2.0, 2.0)
      }
    }
  }
}
