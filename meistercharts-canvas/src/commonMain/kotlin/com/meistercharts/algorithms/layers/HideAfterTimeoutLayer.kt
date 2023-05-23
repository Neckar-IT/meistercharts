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

import com.meistercharts.canvas.timerSupport
import it.neckar.open.async.TimerSupport
import it.neckar.open.time.nowMillis
import it.neckar.open.unit.si.ms
import kotlin.time.Duration

/**
 * A layer that is only shown for a given time
 *
 */
class HideAfterTimeoutLayer<T : Layer>(
  delegate: LayerVisibilityAdapterWithState<T>,
  @ms val duration: Duration,
) : DelegatingLayer<LayerVisibilityAdapterWithState<T>>(delegate) {

  private var lastShowTime: Double? = null

  override val type: LayerType
    get() = delegate.type

  init {
    delegate.visibleProperty.consume(false) {
      if (it) {
        lastShowTime = nowMillis()

        timerSupport.delay(duration) {
          delegate.visibleProperty.value = false
        }
      }
    }
  }

  private lateinit var timerSupport: TimerSupport

  override fun initialize(paintingContext: LayerPaintingContext) {
    super.initialize(paintingContext)
    timerSupport = paintingContext.chartSupport.timerSupport
  }
}


/**
 * Wraps this into an [HideAfterTimeoutLayer]
 */
fun <T : Layer> LayerVisibilityAdapterWithState<T>.autoHideAfter(@ms duration: Duration): HideAfterTimeoutLayer<T> {
  return HideAfterTimeoutLayer(this, duration)
}
