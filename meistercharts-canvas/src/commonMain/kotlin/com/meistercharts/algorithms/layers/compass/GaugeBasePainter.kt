package com.meistercharts.algorithms.layers.compass

import com.meistercharts.algorithms.ValueRange
import com.meistercharts.algorithms.layers.LayerPaintingContext
import it.neckar.open.unit.number.MayBeNegative
import com.meistercharts.annotations.Zoomed
import it.neckar.open.unit.si.rad

/**
 * Paints the base for a gauge (background and surrounding - not the current value)
 *
 */
fun interface GaugeBasePainter {
  /**
   * Paints the background not(!) the current value
   */
  fun paintBase(
    gaugePaintable: GaugePaintable,
    paintingContext: LayerPaintingContext,

    radius: @Zoomed Double,
    startAngle: @rad Double,
    extendWithRotationDirection: @rad @MayBeNegative Double,
    valueRange: ValueRange,
  )
}
