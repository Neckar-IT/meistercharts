package com.meistercharts.canvas

import com.meistercharts.charts.ChartId
import com.meistercharts.loop.PaintingLoopIndex
import it.neckar.open.unit.number.IsFinite
import it.neckar.open.unit.number.MayBeNaN
import it.neckar.open.unit.si.ms

/**
 * Helper object that holds the current painting context.
 */
object CurrentPaintingContext {
  fun clear() {
    _paintingLoopIndex = null
    _frameTimestamp = Double.NaN
    _chartId = null
  }

  fun fill(chartSupport: ChartSupport, paintingLoopIndex: PaintingLoopIndex, frameTimestamp: @ms @IsFinite Double) {
    _paintingLoopIndex = paintingLoopIndex
    _frameTimestamp = frameTimestamp

    _chartId = chartSupport.chartId
  }

  private var _chartId: ChartId? = null

  /**
   * The current chart id
   */
  val chartId: ChartId
    get() {
      return _chartId ?: throw IllegalStateException("not in frame")
    }

  /**
   * Is set to NaN if the frame timestamp is not available
   */
  @Suppress("ObjectPropertyName")
  private var _frameTimestamp: @ms @MayBeNaN Double = Double.NaN

  /**
   * The current frame timestamp
   */
  val frameTimestamp: @ms @IsFinite Double
    get() {
      return _frameTimestamp.also {
        if (it.isNaN()) throw IllegalStateException("not in frame")
      }
    }

  @Suppress("ObjectPropertyName")
  private var _paintingLoopIndex: PaintingLoopIndex? = null

  /**
   * The current painting loop index
   */
  val paintingLoopIndex: PaintingLoopIndex
    get() {
      return _paintingLoopIndex ?: throw IllegalStateException("not in frame")
    }

}
