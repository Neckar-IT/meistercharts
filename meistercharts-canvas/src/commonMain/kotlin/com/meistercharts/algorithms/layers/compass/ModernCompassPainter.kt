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
import com.meistercharts.annotations.Domain
import com.meistercharts.annotations.Zoomed
import com.meistercharts.calc.domain2rad
import com.meistercharts.canvas.CanvasRenderingContext
import com.meistercharts.canvas.ConfigurationDsl
import com.meistercharts.canvas.fill
import com.meistercharts.font.FontDescriptorFragment
import com.meistercharts.canvas.saved
import com.meistercharts.canvas.stroke
import com.meistercharts.color.Color
import com.meistercharts.color.ColorProvider
import it.neckar.geometry.Coordinates
import it.neckar.geometry.Direction
import it.neckar.geometry.PolarCoordinates
import com.meistercharts.range.ValueRange
import it.neckar.open.formatting.CachedNumberFormat
import it.neckar.open.formatting.NumberFormat
import it.neckar.open.formatting.cached
import it.neckar.open.formatting.intFormat
import it.neckar.open.i18n.I18nConfiguration
import it.neckar.open.kotlin.lang.WhitespaceConfig
import it.neckar.open.unit.number.MayBeNegative
import it.neckar.open.unit.other.px
import it.neckar.open.unit.si.rad
import kotlin.math.PI

/**
 */
class ModernCompassPainter(
  styleConfiguration: Style.() -> Unit = {}
) : GaugeBasePainter {

  val style: Style = Style().also(styleConfiguration)

  override fun paintBase(
    gaugePaintable: GaugePaintable, paintingContext: LayerPaintingContext,
    radius: @Zoomed Double,
    startAngle: @rad Double,
    extendWithRotationDirection: @rad @MayBeNegative Double,
    valueRange: ValueRange,
  ) {
    val gc = paintingContext.gc

    //The radius of the inner circle
    val innerCircleRadius = radius - style.outerCircleWidth


    //The outer color
    gc.fill(style.outerCircleColor)
    gc.fillOvalCenter(0.0, 0.0, radius * 2, radius * 2)

    //The inner circle
    gc.fill(style.innerCircleColor)
    gc.fillOvalCenter(0.0, 0.0, innerCircleRadius * 2, innerCircleRadius * 2)

    //inner border
    gc.stroke(style.lineColor)
    gc.lineWidth = style.innerBorderWidth
    gc.strokeOvalCenter(0.0, 0.0, innerCircleRadius * 2, innerCircleRadius * 2)

    //outer ring
    gc.lineWidth = style.outerBorderLineWidth
    gc.strokeOvalCenter(0.0, 0.0, radius * 2, radius * 2)
    gc.strokeOvalCenter(0.0, 0.0, (radius - style.outerRingWidth) * 2, (radius - style.outerRingWidth) * 2)

    //paint the degree markers (small ticks)
    paintTicks(gc, gaugePaintable, startAngle, valueRange, radius, extendWithRotationDirection, CompassTickType.Minor)
    paintTicks(gc, gaugePaintable, startAngle, valueRange, radius, extendWithRotationDirection, CompassTickType.Medium)
    paintTicks(gc, gaugePaintable, startAngle, valueRange, radius, extendWithRotationDirection, CompassTickType.Major)
  }

  private fun paintTicks(
    gc: CanvasRenderingContext,
    gaugePaintable: GaugePaintable,

    startAngle: @rad Double,
    valueRange: ValueRange,
    radius: @px Double,
    extendWithRotationDirection: @rad @MayBeNegative Double,

    tickType: CompassTickType
  ) {
    //Only set the gc settings once at the start (especially important for the font)
    gc.font(style.tickFont(tickType))
    gc.lineWidth = style.tickWidth(tickType)

    style.ticksProvider.ticks(gaugePaintable, tickType)
      .forEach { tickValue: @Domain Double ->
        paintTick(gc, startAngle, valueRange, tickValue, radius, extendWithRotationDirection, tickType)
      }
  }

  private fun paintTick(
    gc: CanvasRenderingContext,

    startAngle: @rad Double,
    valueRange: ValueRange,

    tickValue: @Domain Double,
    radius: @px Double,
    extendWithRotationDirection: @rad @MayBeNegative Double,

    tickType: CompassTickType
  ) {
    val tickAngle = domain2rad(tickValue, valueRange, startAngle, extendWithRotationDirection)

    @px val startPolarRadius = radius - style.outerRingWidth

    val startCartesianX = PolarCoordinates.toCartesianX(startPolarRadius, tickAngle)
    val startCartesianY = PolarCoordinates.toCartesianY(startPolarRadius, tickAngle)
    val endCartesianX = PolarCoordinates.toCartesianX(radius, tickAngle)
    val endCartesianY = PolarCoordinates.toCartesianY(radius, tickAngle)

    gc.stroke(style.tickColor)
    gc.strokeLine(startCartesianX, startCartesianY, endCartesianX, endCartesianY)


    if (tickType == CompassTickType.Major || tickType == CompassTickType.Medium) {
      val formattedTick = style.valueFormat.format(tickValue)

      gc.saved {
        gc.translate(startCartesianX, startCartesianY)
        gc.rotateRadians(tickAngle + PI / 2.0)
        gc.fill(style.labelsColor)
        gc.fillText(formattedTick, Coordinates.origin, Direction.TopCenter, style.labelsGap)
      }
    }
  }

  @ConfigurationDsl
  class Style {
    /**
     * Returns the ticks
     */
    var ticksProvider: CompassTicksProvider = DefaultCompassTicksProvider()

    /**
     * The width of the outer border lines
     */
    var outerBorderLineWidth: Double = 2.0

    /**
     * The width of the inner border
     */
    var innerBorderWidth: Double = 3.0

    /**
     * The bg color of the outer circle
     */
    var outerCircleColor: Color = Color.web("#DCDCDC")

    var outerCircleWidth: Double = 60.0

    /**
     * The width of the outer ring (that contains the ticks)
     */
    var outerRingWidth: Double = 10.0

    /**
     * The color of the lines
     */
    var lineColor: Color = Color.web("#7B7B7B")


    /**
     * The color to paint the background
     */
    var innerCircleColor: ColorProvider = Color.white

    /**
     * The color to paint the ticks
     */
    var tickColor: Color = lineColor

    /**
     * The color to paint the tick labels
     */
    var labelsColor: Color = tickColor

    /**
     * The gap between the compass rose and the labels
     */
    var labelsGap: Double = 3.0

    /**
     * Font used for the tick labels
     */
    var tickFont: (CompassTickType) -> FontDescriptorFragment = { tickType ->
      when (tickType) {
        CompassTickType.Major -> FontDescriptorFragment.XL
        CompassTickType.Medium -> FontDescriptorFragment.DefaultSize
        else                   -> FontDescriptorFragment.empty
      }
    }

    var tickWidth: (tickType: CompassTickType) -> Double = { tickType ->
      when (tickType) {
        CompassTickType.Major -> 3.0
        CompassTickType.Medium -> 3.0
        else                   -> 1.0
      }
    }

    /**
     * Format for formatting values
     */
    var valueFormat: CachedNumberFormat = object : NumberFormat {
      override fun format(value: Double, i18nConfiguration: I18nConfiguration, whitespaceConfig: WhitespaceConfig): String {
        return formatDegreesAsDirection(value) ?: intFormat.format(value, i18nConfiguration, whitespaceConfig)
      }

      override val precision: Double = intFormat.precision
    }.cached()

  }
}
