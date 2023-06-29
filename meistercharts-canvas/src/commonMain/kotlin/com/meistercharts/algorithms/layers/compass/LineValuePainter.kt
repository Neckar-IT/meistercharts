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
package com.meistercharts.algorithms.layers.compass

import com.meistercharts.model.ValueRange
import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.color.Color
import com.meistercharts.calc.domain2rad
import com.meistercharts.canvas.ConfigurationDsl
import com.meistercharts.geometry.Coordinates
import com.meistercharts.model.PolarCoordinates
import it.neckar.open.unit.other.px
import it.neckar.open.unit.si.rad

/**
 * Paints a simple line
 */
class LineValuePainter(
  styleConfiguration: Style.() -> Unit = {}
) : GaugeValuePainter {
  val style: Style = Style().also(styleConfiguration)

  override fun paintCurrentValue(gaugePaintable: GaugePaintable, paintingContext: LayerPaintingContext, radius: Double, startAngle: Double, extendWithRotationDirection: Double, valueRange: ValueRange, value: Double) {
    val gc = paintingContext.gc

    @rad val currentValueRad = domain2rad(value, valueRange, startAngle, extendWithRotationDirection)

    val end = PolarCoordinates((radius - style.lineGap).coerceAtLeast(0.0), currentValueRad).toCartesian()

    gc.lineWidth = style.pointerWidth
    gc.stroke(style.pointerColor)
    gc.strokeLine(Coordinates.origin, end)
  }

  @ConfigurationDsl
  class Style {
    /**
     * The gap to the outline of the gauge
     */
    var lineGap: @px Double = 3.0

    /**
     * The pointer stroke width
     */
    var pointerWidth: @px Double = 3.0

    /**
     * The color to paint the pointer
     */
    var pointerColor: Color = Color.red
  }
}

