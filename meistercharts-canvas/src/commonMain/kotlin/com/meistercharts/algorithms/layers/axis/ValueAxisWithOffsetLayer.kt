/*
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
package com.meistercharts.algorithms.layers.axis

import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.algorithms.layers.LayerType
import com.meistercharts.algorithms.layers.barchart.AbstractAxisLayer
import com.meistercharts.annotations.Domain
import com.meistercharts.canvas.ConfigurationDsl
import com.meistercharts.color.Color
import com.meistercharts.color.ColorProvider
import com.meistercharts.design.Theme
import com.meistercharts.font.FontDescriptorFragmentProvider
import com.meistercharts.provider.ValueRangeProvider
import com.meistercharts.range.ValueRange
import it.neckar.geometry.Direction
import it.neckar.geometry.Orientation
import it.neckar.open.provider.MultiProvider
import it.neckar.open.unit.number.Positive
import it.neckar.open.unit.other.px
import kotlin.jvm.JvmInline
import kotlin.math.pow

/**
 * Paints a value axis with an additional offset to reduce the amount of horizontal space needed to display values
 */
class ValueAxisWithOffsetLayer(
  override val configuration: Configuration,
  additionalConfiguration: Configuration.() -> Unit = {},
) : AbstractAxisLayer() {

  constructor(
    title: String,
    valueRange: ValueRange,
    styleConfiguration: Configuration.() -> Unit = {},
  ) : this(
    Configuration(valueRangeProvider = { valueRange }),
    {
      titleProvider = { _, _ -> title }
      styleConfiguration()
    },
  )

  init {
    configuration.additionalConfiguration()
  }

  override val type: LayerType
    get() = LayerType.Content

  override fun paintingVariables(): ValueAxisWithOffsetPaintingVariables {
    return paintingVariables
  }

  //Painting variable - stores intermediate results required for painting
  private val paintingVariables = object : ValueAxisWithOffsetPaintingVariablesImpl() {

    override fun calculate(paintingContext: LayerPaintingContext) {
      reset()

      contentAreaValueRange = configuration.valueRangeProvider()

      calculateTickFontMetrics(paintingContext, configuration)


      calculateEstimatedTickFormatMaxLength(paintingContext, configuration)
      calculateTitle(paintingContext, configuration)

      calculateAxisStartEnd(paintingContext, configuration)

      calculateDomainStartEndValues(paintingContext, configuration)

      calculateTickLabelsMaxWidth(configuration)
      calculateLocations(paintingContext, configuration)

      storeTicks(calculateTickValues(paintingContext), paintingContext, configuration)
    }

    /**
     * Calculate the tick values that are painted
     */
    private fun calculateTickValues(paintingContext: LayerPaintingContext): @Domain DoubleArray {
      return when (configuration.orientation) {
        Orientation.Vertical -> calculateTickValuesValueRangeVertically(fontHeight = tickFontMetrics.totalHeight)
        Orientation.Horizontal -> calculateTickValuesValueRangeHorizontally(maxFormattedLabelWidth = estimatedTickFormatMaxLength)
      }
    }

    private fun calculateTickValuesValueRangeHorizontally(maxFormattedLabelWidth: @px Double): @Domain DoubleArray {
      //TODO implement me!
      return doubleArrayOf(10.0, 15.0)
    }

    private fun calculateTickValuesValueRangeVertically(fontHeight: @px Double): @Domain DoubleArray {
      //TODO implement me
      return doubleArrayOf(11.0, 16.0)
    }

    /**
     * Calculates the offset index for the given value.
     * The index
     */
    fun offsetIndexForValue(value: Double): OffsetIndex {
      return if (deltaMagnitude < integerDigits) {
        (value / 10.0.pow(integerDigits))
      } else {
        (value / offsetStep)
      }.toInt()
        .let { OffsetIndex(it) }
    }
  }

  override fun paintTicksWithLabelsVertically(paintingContext: LayerPaintingContext, direction: Direction) {
    TODO("Not yet implemented: paintTicksWithLabelsVertically")
  }

  override fun paintTicksWithLabelsHorizontally(paintingContext: LayerPaintingContext, direction: Direction) {
    TODO("Not yet implemented: paintTicksWithLabelsHorizontally")
  }

  @ConfigurationDsl
  open class Configuration(
    /**
     * Provides the value range for the axis
     */
    override var valueRangeProvider: ValueRangeProvider = { ValueRange.default },
  ) : ValueAxisLayer.Configuration() {
    /**
     * The size of the offset area
     */
    var offsetAreaSize: @Positive Double = 30.0

    /**
     * The fills that are used for the offset areas.
     */
    var offsetAreaFills: MultiProvider<OffsetIndex, Color> = MultiProvider.Companion.modulo(Color.web("#DBE1E5"), Color.web("#F3F5F7"))

    /**
     * The colors of the offset area ticks
     */
    var offsetTickLabelColor: ColorProvider = Theme.axisTickColor.provider()

    /**
     * The font for the offset ticks
     */
    var offsetTickFont: FontDescriptorFragmentProvider = Theme.offsetTickFont.provider()

    /**
     * The maximum amount of digits each value tick is allowed to have before an offset is added
     *  For example, when set to 3, a value tick of 23.741 would be shortened to 741 with an offset of +23.000
     *  This also works for decimal numbers
     */
    var spaceForDigits: @Positive Int = 6
  }
}

/**
 * Represents the "global" offset index
 */
@JvmInline
value class OffsetIndex(val value: Int)


inline fun <T> MultiProvider<OffsetIndex, T>.valueAt(index: OffsetIndex): T {
  return this.valueAt(index.value)
}
