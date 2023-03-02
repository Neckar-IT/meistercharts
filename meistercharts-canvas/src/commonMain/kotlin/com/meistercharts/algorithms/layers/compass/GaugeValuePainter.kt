package com.meistercharts.algorithms.layers.compass

import com.meistercharts.algorithms.ValueRange
import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.annotations.Domain
import it.neckar.open.unit.number.MayBeNegative
import com.meistercharts.annotations.Zoomed
import it.neckar.open.unit.si.rad

/**
 * Paints the current value for a gauge
 *
 */
fun interface GaugeValuePainter {
  /**
   * Paints the current value
   */
  fun paintCurrentValue(
    gaugePaintable: GaugePaintable,
    paintingContext: LayerPaintingContext,
    radius: @Zoomed Double,
    startAngle: @rad Double,
    extendWithRotationDirection: @rad @MayBeNegative Double,
    valueRange: ValueRange,
    /**
     * The current value
     */
    value: @Domain Double
  )

}
