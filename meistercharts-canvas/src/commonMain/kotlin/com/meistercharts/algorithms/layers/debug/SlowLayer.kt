package com.meistercharts.algorithms.layers.debug

import com.meistercharts.algorithms.layers.AbstractLayer
import com.meistercharts.algorithms.layers.Layer
import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.algorithms.layers.LayerType
import it.neckar.open.kotlin.lang.random
import it.neckar.open.time.nowMillis
import it.neckar.open.unit.si.ms

/**
 * Delays the execution for some time
 */
class SlowLayer(
  @ms val targetPaintTime: Double,
  /**
   * This value is added or subtracted from the target time to get random values
   */
  @ms val plusMinus: Double = 0.0
) : AbstractLayer() {

  override val type: LayerType
    get() = LayerType.Content

  override fun paint(paintingContext: LayerPaintingContext) {
    val startTime = nowMillis()
    val target = startTime + targetPaintTime + (random.nextDouble() * 2.0 * plusMinus - plusMinus / 2.0)

    @Suppress("ControlFlowWithEmptyBody", "EmptyWhileBlock")
    while (nowMillis() < target) {
    }
  }
}
