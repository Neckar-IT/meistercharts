package com.meistercharts.canvas.timer

import com.meistercharts.canvas.ChartRenderLoopListener
import com.meistercharts.canvas.ChartSupport
import it.neckar.open.time.BaseTimerImplementation
import it.neckar.open.unit.si.ms

/**
 * A timer support that uses the canvas render loop to execute the callbacks
 */
class CanvasBasedTimerImplementation : BaseTimerImplementation(), ChartRenderLoopListener {
  override fun render(chartSupport: ChartSupport, frameTimestamp: @ms Double, relativeHighRes: @ms Double) {
    update(frameTimestamp)
  }
}
