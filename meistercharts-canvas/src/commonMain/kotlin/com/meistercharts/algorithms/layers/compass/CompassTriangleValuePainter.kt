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

import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.calc.domain2rad
import com.meistercharts.canvas.ConfigurationDsl
import com.meistercharts.canvas.LineJoin
import com.meistercharts.canvas.fill
import com.meistercharts.canvas.stroke
import com.meistercharts.color.Color
import com.meistercharts.color.ColorProvider
import it.neckar.geometry.Coordinates
import it.neckar.geometry.PolarCoordinates
import com.meistercharts.range.ValueRange
import it.neckar.open.kotlin.lang.asProvider
import it.neckar.open.unit.other.px
import it.neckar.open.unit.si.rad
import kotlin.math.PI

/**
 * Paints a compass needle
 */
class CompassTriangleValuePainter(
  styleConfiguration: Style.() -> Unit = {}
) : GaugeValuePainter {
  val style: Style = Style().also(styleConfiguration)

  override fun paintCurrentValue(gaugePaintable: GaugePaintable, paintingContext: LayerPaintingContext, radius: Double, startAngle: Double, extendWithRotationDirection: Double, valueRange: ValueRange, value: Double) {
    val gc = paintingContext.gc

    val currentValueRad: @rad Double = domain2rad(value, valueRange, startAngle, extendWithRotationDirection)

    //The outer point
    val radiusAtTip = (radius - style.gap).coerceAtLeast(0.0)
    val radiusAtBase = (radiusAtTip - style.height).coerceAtLeast(0.0)

    val tip = PolarCoordinates(radiusAtTip, currentValueRad).toCartesian()

    @rad val baseLeftAngle = currentValueRad - style.baseWidthRad / 2.0
    @rad val baseRightAngle = currentValueRad + style.baseWidthRad / 2.0

    val baseLeft = PolarCoordinates(radiusAtBase, baseLeftAngle).toCartesian()
    val baseRight = PolarCoordinates(radiusAtBase, baseRightAngle).toCartesian()


    gc.beginPath()
    gc.moveTo(baseLeft)
    gc.lineTo(tip)
    gc.lineTo(baseRight)

    gc.arcCenter(Coordinates.origin, radiusAtBase, baseRightAngle, baseLeftAngle - baseRightAngle)
    gc.closePath()

    gc.fill(style.fill)
    gc.fill()

    if (style.lineWidth > 0.0) {
      gc.lineWidth = style.lineWidth
      gc.lineJoin = LineJoin.Round
      gc.stroke(style.stroke)
      gc.stroke()
    }
  }

  @ConfigurationDsl
  class Style {
    /**
     * The gap to the outline of the gauge
     */
    var gap: @px Double = 18.0

    /**
     * The height of the triangle
     */
    var height: @px Double = 42.0

    /**
     * The width of the base arrow in rad
     */
    var baseWidthRad: @rad Double = PI / 25.0

    var lineWidth: @px Double = 5.0

    var stroke: ColorProvider = Color("#185ba6").asProvider()

    var fill: ColorProvider = Color.white
  }
}

