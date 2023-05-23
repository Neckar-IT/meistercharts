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

import com.meistercharts.algorithms.ValueRange
import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.annotations.Domain
import it.neckar.open.unit.number.MayBeNegative
import com.meistercharts.annotations.Zoomed
import com.meistercharts.canvas.ConfigurationDsl
import com.meistercharts.canvas.paintable.AbstractResizablePaintable
import com.meistercharts.canvas.paintable.Paintable
import com.meistercharts.model.Rectangle
import com.meistercharts.model.RotationDirection
import com.meistercharts.model.Size
import com.meistercharts.provider.ValueRangeProvider
import it.neckar.open.provider.DoubleProvider
import it.neckar.open.unit.si.rad
import kotlin.math.min

/**
 * Paints a gauge
 */
class GaugePaintable(
  val valueRangeProvider: ValueRangeProvider,
  val currentValueProvider: @Domain DoubleProvider,
  initialSize: Size,
  styleConfigurer: Style.() -> Unit = {},
) : Paintable,
  AbstractResizablePaintable(
    initialSize,
    { size -> Rectangle.centered(size) }
  ) {

  val style: Style = Style().also(styleConfigurer)

  /**
   * The painting variables that have been calculated
   */
  private val paintingVariables = object {
    /**
     * The radius
     */
    var radius: @Zoomed Double = 0.0

    /**
     * The start angle. 0 is at the right side
     */
    var startAngle: @rad Double = 0.0

    /**
     * The extended rotation - positive values rotate clockwise
     */
    var extendWithRotationDirection: @rad @MayBeNegative Double = 0.0

    /**
     * The value range
     */
    var valueRange: @Domain ValueRange = ValueRange.percentage

    /**
     * Update the painting variables
     */
    fun calculate() {
      radius = min(size.width, size.height) / 2.0
      startAngle = style.startAt

      // all calculations are made in clockwise direction, we therefore need a
      // factor that converts from the configured rotation direction to clockwise
      extendWithRotationDirection = style.rotationDirection.toClockwise(style.extend)

      valueRange = valueRangeProvider()
    }
  }

  override fun paint(paintingContext: LayerPaintingContext, x: Double, y: Double) {
    paintingVariables.calculate()

    paintingContext.gc.translate(x, y)
    //to the requested coordinates

    val radius = paintingVariables.radius
    val startAngle = paintingVariables.startAngle
    val extendWithRotationDirection = paintingVariables.extendWithRotationDirection
    val valueRange = paintingVariables.valueRange

    //Paints the base
    style.basePainter.paintBase(this, paintingContext, radius, startAngle, extendWithRotationDirection, valueRange)
    // paint current value

    style.valuePainter.paintCurrentValue(
      this,
      paintingContext,
      radius = radius,
      startAngle = startAngle,
      extendWithRotationDirection = extendWithRotationDirection,
      valueRange = valueRange,
      value = currentValueProvider()
    )
  }

  @ConfigurationDsl
  class Style {
    /**
     * Paints the base
     */
    var basePainter: GaugeBasePainter = ModernCompassPainter()

    /**
     * Paints the current value
     */
    var valuePainter: GaugeValuePainter = ArrowValuePainter()

    /**
     * Start degrees from which the value range is calculated
     */
    @rad
    var startAt: Double = 0.0

    /**
     * Extend of degrees the value range is being mapped to.
     * The direction is configured using [rotationDirection]
     */
    @rad
    var extend: Double = kotlin.math.PI

    /**
     * Mathematical direction: Positive -> against clock, Negative -> with clock
     */
    var rotationDirection: RotationDirection = RotationDirection.Clockwise
  }
}
