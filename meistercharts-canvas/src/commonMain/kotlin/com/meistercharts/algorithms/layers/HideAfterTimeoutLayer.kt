package com.meistercharts.algorithms.layers

import it.neckar.open.async.TimerSupport
import it.neckar.open.time.nowMillis
import it.neckar.open.unit.si.ms
import kotlin.time.Duration

/**
 * A layer that is only shown for a given time
 *
 */
class HideAfterTimeoutLayer constructor(
  delegate: LayerVisibilityAdapterWithState,
  @ms val duration: Duration,
  private val timerSupport: TimerSupport
) : DelegatingLayer<LayerVisibilityAdapterWithState>(delegate) {

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
}


/**
 * Wraps this into an [HideAfterTimeoutLayer]
 */
fun LayerVisibilityAdapterWithState.autoHideAfter(@ms duration: Duration, timerSupport: TimerSupport): HideAfterTimeoutLayer {
  return HideAfterTimeoutLayer(this, duration, timerSupport)
}
