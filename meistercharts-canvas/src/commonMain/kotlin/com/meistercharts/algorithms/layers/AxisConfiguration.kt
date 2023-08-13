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
package com.meistercharts.algorithms.layers

import com.meistercharts.axis.AxisEndConfiguration
import com.meistercharts.color.Color
import it.neckar.open.annotations.JavaFriendly
import com.meistercharts.annotations.Zoomed
import com.meistercharts.canvas.ChartSupport
import com.meistercharts.font.FontMetrics
import com.meistercharts.canvas.ConfigurationDsl
import com.meistercharts.canvas.i18nConfiguration
import com.meistercharts.canvas.textService
import com.meistercharts.design.Theme
import com.meistercharts.font.FontDescriptorFragment
import com.meistercharts.model.Insets
import it.neckar.geometry.Orientation
import it.neckar.geometry.Side
import com.meistercharts.model.Vicinity
import it.neckar.open.kotlin.lang.asProvider
import it.neckar.open.provider.BooleanProvider
import it.neckar.open.formatting.CachedNumberFormat
import it.neckar.open.formatting.decimalFormat
import it.neckar.open.i18n.I18nConfiguration
import it.neckar.open.i18n.TextService
import it.neckar.open.unit.other.px

/**
 * Provides the axis title - returns null if there is no axis title set
 */
typealias AxisTitleProvider = (textService: TextService, i18nConfiguration: I18nConfiguration) -> String?

/**
 * Extracts the [TextService] and [I18nConfiguration] from the provided chart support
 */
operator fun AxisTitleProvider.invoke(chartSupport: ChartSupport): String? {
  return this.invoke(chartSupport.textService, chartSupport.i18nConfiguration)
}


/**
 * The style configuration for an axis
 */
@ConfigurationDsl
open class AxisConfiguration {
  /**
   * The range that is painted
   */
  var paintRange: PaintRange = PaintRange.Continuous

  /**
   * The size of the axis (width on vertical axis, height on horizontal axis).
   * Does *not* include the [margin]
   */
  @px
  var size: Double = 90.0

  /**
   * The side where the axis is used.
   * Title and other values are painted depending on the side.
   */
  var side: Side = Side.Left

  /**
   * The margin of the axis (how far the axis is away from the side).
   * This property "moves" the axis itself - it does *not* limit the "length" of the axis
   *
   * * The left value is taken into account if [side] is [Side.Left]
   * * The right value is taken into account if [side] is [Side.Right]
   * * The top value is taken into account if [side] is [Side.Top]
   * * The bottom value is taken into account if [side] is [Side.Bottom]
   */
  @px
  var margin: Insets = Insets.empty

  /**
   * The passpartout for the axis.
   * If set, the axis (line) will not be drawn in the area of the set insets.
   *
   * Beware that
   * * the left and right values are taken into account if [side] is [Side.Top] or [Side.Bottom]
   * * the top and bottom values are taken into account if [side] is [Side.Left] or [Side.Right]
   */
  @Deprecated("Use content viewport instead")
  var axisPasspartout: () -> @Zoomed Insets = { Insets.empty }

  /**
   * Provides the label for the axis
   */
  var titleProvider: AxisTitleProvider? = null

  /**
   * Sets the title
   */
  @JavaFriendly
  fun setTitle(title: String) {
    titleProvider = { _, _ -> title }
  }

  /**
   * Whether the title is shown
   */
  var titleVisible: BooleanProvider = BooleanProvider.True

  /**
   * The width to draw the line that marks the axis
   */
  @px
  var axisLineWidth: Double = 1.0

  /**
   * Hide the axis line
   */
  fun hideAxisLine() {
    axisLineWidth = 0.0
  }

  fun showAxisLine() {
    axisLineWidth = 1.0
  }

  /**
   * The width of the ticks to paint
   */
  var tickLineWidth: Double = 1.0

  /**
   * Hide the ticks (does not hide the tick labels)
   */
  fun hideTicks() {
    tickLineWidth = 0.0
    tickLength = 0.0
  }

  fun showTicks() {
    tickLineWidth = 1.0
    tickLength = 5.0
  }

  /**
   * Calculates the preferred viewport margin top.
   */
  fun calculatePreferredViewportMarginTop(): @px Double {
    return when (side) {
      Side.Left, Side.Right -> {
        //We need half the tick size to be sure the top ticks are still visible
        FontMetrics[tickFont.withDefaultValues()].totalHeight / 2.0
      }

      Side.Top, Side.Bottom -> 0.0 //no space needed for top/bottom
    }
  }

  fun calculatePreferredViewportMarginBottom(): @px Double {
    //Same as top
    return calculatePreferredViewportMarginTop()
  }

  /**
   * Returns true if the ticks are visible
   */
  fun ticksVisible(): Boolean {
    return tickLineWidth > 0.0 && tickLength > 0.0
  }

  /**
   * Returns true if there is a non-blank title
   */
  fun hasNonBlankTitle(chartSupport: ChartSupport): Boolean {
    return titleProvider?.invoke(chartSupport).isNullOrBlank().not()
  }

  /**
   * The orientation of the axis in accordance with its [side].
   * The orientation describes the direction of the axis.
   *
   * E.g.: [Side.Left] is represented by [Orientation.Vertical]
   */
  val orientation: Orientation
    get() {
      return when (side) {
        Side.Left, Side.Right -> Orientation.Vertical
        Side.Top, Side.Bottom -> Orientation.Horizontal
      }
    }

  /**
   * The length of a tick
   */
  @px
  var tickLength: Double = 5.0

  /**
   * The gap between the tick and the text of the tick
   */
  @px
  var tickLabelGap: Double = 2.0

  /**
   * The color to be used for the axis-line and the ticks
   */
  var lineColor: () -> Color = Theme.axisLineColor().asProvider()

  /**
   * The color for the tick labels
   */
  var tickLabelColor: () -> Color = Theme.axisTickColor().asProvider()

  /**
   * The color to be used for the title of the axis
   */
  var titleColor: () -> Color = Theme.axisTitleColor().asProvider()

  /**
   * The font to be used for the ticks of the axis
   */
  var tickFont: FontDescriptorFragment = Theme.axisTickFont()

  /**
   * The font that is used for the title
   */
  var titleFont: FontDescriptorFragment = Theme.axisTitleFont()

  /**
   * The space that is added to the width (vertical axis) or height (horizontal axis) of the text box of the title
   */
  var titleGap: @px Double = 6.0

  /**
   * The format to be used for the values of the ticks
   */
  var ticksFormat: CachedNumberFormat = decimalFormat

  /**
   * How the axis displays values at its ends
   */
  var axisEndConfiguration: AxisEndConfiguration = AxisEndConfiguration.Default

  /**
   * The orientation used for the ticks
   */
  var tickOrientation: Vicinity = Vicinity.Inside

  /**
   * The (optional) background color
   */
  var background: () -> Color? = { null }

  /**
   * Describes the range where the value axis is painted
   */
  enum class PaintRange {
    /**
     * Only paint the value axis inside the content area
     */
    ContentArea,

    /**
     * Keep painting the value axis outside the content area
     */
    Continuous
  }
}
