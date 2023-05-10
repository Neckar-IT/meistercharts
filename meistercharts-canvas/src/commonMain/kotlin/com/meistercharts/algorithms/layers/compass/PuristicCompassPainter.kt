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
import com.meistercharts.algorithms.domain2rad
import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.algorithms.painter.Color
import com.meistercharts.annotations.Domain
import com.meistercharts.annotations.Zoomed
import com.meistercharts.canvas.CanvasRenderingContext
import com.meistercharts.canvas.FontDescriptorFragment
import com.meistercharts.canvas.FontSize
import com.meistercharts.canvas.StyleDsl
import com.meistercharts.canvas.saved
import com.meistercharts.design.Theme
import com.meistercharts.design.corporateDesign
import com.meistercharts.model.Coordinates
import com.meistercharts.model.Direction
import com.meistercharts.model.PolarCoordinates
import it.neckar.open.collections.fastForEach
import it.neckar.open.formatting.CachedNumberFormat
import it.neckar.open.formatting.NumberFormat
import it.neckar.open.formatting.cached
import it.neckar.open.formatting.intFormat
import it.neckar.open.i18n.I18nConfiguration
import it.neckar.open.kotlin.lang.WhitespaceConfig
import it.neckar.open.kotlin.lang.round
import it.neckar.open.unit.number.MayBeNegative
import it.neckar.open.unit.other.px
import it.neckar.open.unit.si.rad
import kotlin.math.PI

/**
 */
class PuristicCompassPainter(
  styleConfiguration: Style.() -> Unit = {}
) : GaugeBasePainter {

  val style: Style = Style().also(styleConfiguration)

  override fun paintBase(gaugePaintable: GaugePaintable, paintingContext: LayerPaintingContext, radius: Double, startAngle: Double, extendWithRotationDirection: @rad @MayBeNegative Double, valueRange: ValueRange) {
    val gc = paintingContext.gc

    gc.fill(style.backgroundColor)
    gc.fillOvalCenter(0.0, 0.0, radius * 2.0, radius * 2.0)

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
    gc.font(style.tickFont(radius, tickType))
    gc.lineWidth = style.tickWidth(radius, tickType)

    style.ticksProvider.ticks(gaugePaintable, tickType)
      .fastForEach { tickValue: @Domain Double ->
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

    tickType: CompassTickType,
  ) {
    val tickAngle = domain2rad(tickValue, valueRange, startAngle, extendWithRotationDirection)

    @px val startPolarRadius = radius - style.tickLength(radius, tickType)

    val startCartesianX = PolarCoordinates.toCartesianX(startPolarRadius, tickAngle)
    val startCartesianY = PolarCoordinates.toCartesianY(startPolarRadius, tickAngle)
    val endCartesianX = PolarCoordinates.toCartesianX(radius, tickAngle)
    val endCartesianY = PolarCoordinates.toCartesianY(radius, tickAngle)

    gc.fill(style.tickColor)
    gc.strokeLine(startCartesianX, startCartesianY, endCartesianX, endCartesianY)


    if (tickType == CompassTickType.Major || tickType == CompassTickType.Medium) {
      if (gc.font.size.size < 8.0) {
        //too small to be decipherable
        return
      }
      val formattedTick = style.valueFormat.format(tickValue)

      gc.saved {
        gc.translate(startCartesianX, startCartesianY)
        gc.rotateRadians(tickAngle + PI / 2.0)
        gc.fill(style.labelsColor)
        gc.fillText(formattedTick, Coordinates.origin, Direction.TopCenter, style.labelsGap)
      }
    }
  }

  @StyleDsl
  class Style {
    /**
     * Returns the ticks
     */
    var ticksProvider: CompassTicksProvider = DefaultCompassTicksProvider()

    /**
     * The background color
     */
    var backgroundColor: Color = Color.white

    /**
     * The color of the lines
     */
    var lineColor: Color = Theme.axisLineColor()

    /**
     * The color to paint the ticks
     */
    var tickColor: Color = Theme.axisTickColor()

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
    var tickFont: (@px Double, CompassTickType) -> FontDescriptorFragment = { radius, tickType ->
      //TODO avoid object allocation
      when (tickType) {
        CompassTickType.Major -> corporateDesign.h1.withSize(FontSize((radius * 0.1).round()))
        CompassTickType.Medium -> corporateDesign.h1.withSize(FontSize((radius * 0.05).round()))
        else                   -> corporateDesign.h1.withSize(FontSize((radius * 0.025).round()))
      }
    }

    /**
     * Width of the tick lines
     */
    var tickWidth: (@px Double, tickType: CompassTickType) -> @Zoomed Double = { radius, tickType ->
      when (tickType) {
        CompassTickType.Major, CompassTickType.Medium -> (radius * 0.0001).round().coerceAtLeast(2.0)
        else                                          -> 1.0
      }
    }

    /**
     * Length of the tick lines
     */
    var tickLength: (@px Double, tickType: CompassTickType) -> @Zoomed Double = { radius, tickType ->
      when (tickType) {
        CompassTickType.Major -> (radius * 0.1).round().coerceAtLeast(10.0)
        else                  -> (radius * 0.05).round().coerceAtLeast(5.0)
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
