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
import com.meistercharts.algorithms.painter.Arrows
import com.meistercharts.color.Color
import com.meistercharts.annotations.DomainRelative
import com.meistercharts.canvas.ConfigurationDsl
import com.meistercharts.model.PolarCoordinates
import it.neckar.open.unit.si.rad
import kotlin.math.PI

/**
 * Paints an arrow
 */
class ArrowValuePainter(
  styleConfiguration: Style.() -> Unit = {}
) : GaugeValuePainter {
  val style: Style = Style().also(styleConfiguration)
  override fun paintCurrentValue(gaugePaintable: GaugePaintable, paintingContext: LayerPaintingContext, radius: Double, startAngle: Double, extendWithRotationDirection: Double, valueRange: ValueRange, value: Double) {
    val gc = paintingContext.gc

    @DomainRelative val domainRelative = valueRange.toDomainRelative(value)

    @rad val currentValue = (startAngle + domainRelative * extendWithRotationDirection)
    gc.lineWidth = style.pointerLineWidth
    gc.stroke(style.pointerColor)

    val end = PolarCoordinates(radius, currentValue).toCartesian()

    gc.translate(end.x, end.y)
    gc.rotateRadians(currentValue + PI / 2.0)
    gc.stroke(Arrows.toTop(radius, 20.0, 10.0))
  }

  @ConfigurationDsl
  class Style {
    /**
     * The pointer stroke width
     */
    var pointerLineWidth: Double = 3.0

    /**
     * The color to paint the pointer
     */
    var pointerColor: Color = Color.red
  }
}
