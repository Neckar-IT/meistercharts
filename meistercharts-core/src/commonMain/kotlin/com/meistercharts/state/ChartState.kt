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
package com.meistercharts.state

import com.meistercharts.axis.Axis
import com.meistercharts.axis.AxisInversionInformation
import com.meistercharts.axis.AxisOrientationX
import com.meistercharts.axis.AxisOrientationY
import com.meistercharts.annotations.ContentArea
import com.meistercharts.annotations.Domain
import com.meistercharts.annotations.DomainRelative
import com.meistercharts.annotations.Window
import com.meistercharts.annotations.Zoomed
import com.meistercharts.geometry.Coordinates
import com.meistercharts.geometry.Distance
import com.meistercharts.model.Insets
import com.meistercharts.model.Size
import com.meistercharts.model.Zoom
import it.neckar.open.unit.number.MayBeZero
import it.neckar.open.unit.number.Positive

/**
 * Describes the state of the chart component.
 * The information from the chart state are then used to calculate the exact
 * conversion from @[DomainRelative] to [Window] (and backwards).
 *
 * Attention: Conversion from [Domain] to [DomainRelative] are done within
 * a [com.meistercharts.model.ValueRange] and out of the scope of the [ChartState]}.
 *
 */
interface ChartState {
  /**
   * Returns the window translation on the x-axis.
   * Explained further here: [windowTranslation]
   */
  val windowTranslationX: @Zoomed Double

  /**
   * Returns the window translation on the y-axis.
   * Explained further here: [windowTranslation]
   */
  val windowTranslationY: @Zoomed Double

  /**
   * Returns the window translation (panning).
   *
   * The window translation is always returned in pixels relative to the window origin (top left corner).
   *
   * A positive x value represents a movement of the chart to the right:
   * The visual representation of the chart starts further right.
   *
   * A positive y value represents a movement of the chart to the bottom:
   * The visual representation of the chart starts further down.
   */
  val windowTranslation: @Zoomed Distance

  /**
   * Returns the zoom for the x-axis
   * The zoom is always > 0
   */
  val zoomX: @Positive Double

  /**
   * Returns the zoom for the y-axis
   * The zoom is always > 0
   */
  val zoomY: @Positive Double

  /**
   * Returns the zoom.
   * The higher the zoom factor, the less content is visible.
   *
   * Memory hook: Same as photographing. The higher the zoom factor the more details are visible.
   *
   * The zoom is always > 0
   */
  val zoom: Zoom

  /**
   * Returns the width of the complete contentArea (*not* the window).
   * This method is used to convert [DomainRelative] to [ContentArea]
   */
  val contentAreaWidth: @ContentArea @MayBeZero Double

  /**
   * Returns the height of the complete contentArea (*not* the window).
   * This method is used to convert [DomainRelative] to [ContentArea]
   */
  val contentAreaHeight: @ContentArea @MayBeZero Double

  /**
   * Returns the size of the complete contentArea (*not* the window).
   * This method is used to convert [DomainRelative] to [ContentArea]
   */
  val contentAreaSize: @ContentArea @MayBeZero Size

  /**
   * Returns the width of the complete window (the part that is visible to the user)
   */
  val windowWidth: @MayBeZero @Zoomed Double

  /**
   * Returns the height of the complete window (the part that is visible to the user)
   */
  val windowHeight: @MayBeZero @Zoomed Double

  /**
   * Returns the size of the complete window (the part that is visible to the user)
   */
  val windowSize: @MayBeZero @Zoomed Size

  /**
   * Returns the content viewport margin.
   * The content view port margin describes the margin *around* within the window that is kept free from the content.
   * That area can be used for additional information (e.g. axis, labels, legends, toolbars...)
   */
  val contentViewportMargin: @Zoomed Insets

  val contentViewportMarginTop: @Zoomed @MayBeZero Double
  val contentViewportMarginRight: @Zoomed @MayBeZero Double
  val contentViewportMarginBottom: @Zoomed @MayBeZero Double
  val contentViewportMarginLeft: @Zoomed @MayBeZero Double

  /**
   * Returns true if at least one size ([windowSize] or [contentAreaSize]) has one value
   * that is zero.
   *
   * If this method returns true,
   * - the canvas is not visible
   * - most of the calculations do not make any sense
   * - and will return NaN
   */
  val hasAnyZeroSize: Boolean
    get() {
      return windowSize.atLeastOneZero() || contentAreaSize.atLeastOneZero()
    }


  /**
   * Returns the center of the window
   */
  val windowCenter: @Window @MayBeZero Coordinates
    get() {
      return Coordinates(windowWidth / 2.0, windowHeight / 2.0)
    }

  /**
   * Returns the axis orientation for the x-axis
   */
  val axisOrientationX: AxisOrientationX

  /**
   * Returns the axis orientation for the y-axis
   */
  val axisOrientationY: AxisOrientationY

  /**
   * Returns the axis orientation for the given axis
   */
  fun axisOrientation(axis: Axis): AxisInversionInformation {
    return when (axis) {
      Axis.X -> axisOrientationX
      Axis.Y -> axisOrientationY
    }
  }

  /**
   * Returns true if the given x location is within the window
   */
  fun isInWindowX(x: @Window Double): Boolean {
    return x >= 0 && x < windowWidth
  }

  /**
   * Returns true if the given y location is within the window
   */
  fun isInWindowY(y: @Window Double): Boolean {
    return y >= 0 && y < windowHeight
  }

  companion object {
    /**
     * Throws exceptions on each method.
     * Can be used as null object
     */
    val NoOp: ChartState = object : ChartState {
      override val windowTranslationX: Double
        get() = throw UnsupportedOperationException("Noop")

      override
      val windowTranslationY: Double
        get() = throw UnsupportedOperationException("Noop")

      override
      val windowTranslation: Distance
        get() = throw UnsupportedOperationException("Noop")

      override
      val zoomX: Double
        get() = throw UnsupportedOperationException("Noop")

      override
      val zoomY: Double
        get() = throw UnsupportedOperationException("Noop")

      override
      val zoom: Zoom
        get() = throw UnsupportedOperationException("Noop")

      override
      val contentAreaWidth: Double
        get() = throw UnsupportedOperationException("Noop")

      override
      val contentAreaHeight: Double
        get() = throw UnsupportedOperationException("Noop")

      override
      val contentAreaSize: Size
        get() = throw UnsupportedOperationException("Noop")

      override
      val windowWidth: Double
        get() = throw UnsupportedOperationException("Noop")

      override
      val windowHeight: Double
        get() = throw UnsupportedOperationException("Noop")

      override
      val windowSize: Size
        get() = throw UnsupportedOperationException("Noop")
      override val contentViewportMarginTop: Double
        get() = throw UnsupportedOperationException("Noop")
      override val contentViewportMarginRight: Double
        get() = throw UnsupportedOperationException("Noop")
      override val contentViewportMarginBottom: Double
        get() = throw UnsupportedOperationException("Noop")
      override val contentViewportMarginLeft: Double
        get() = throw UnsupportedOperationException("Noop")

      override val contentViewportMargin: Insets
        get() = throw UnsupportedOperationException("Noop")

      override
      val axisOrientationX: AxisOrientationX
        get() = throw UnsupportedOperationException("Noop")

      override
      val axisOrientationY: AxisOrientationY
        get() = throw UnsupportedOperationException("Noop")
    }
  }
}

/**
 * Returns the width of the content view port
 */
val ChartState.contentViewportWidth: @Zoomed Double
  get() {
    return windowWidth - contentViewportMarginLeft - contentViewportMarginRight
  }

/**
 * Returns the height of the content view port
 */
val ChartState.contentViewportHeight: @Zoomed Double
  get() {
    return windowHeight - contentViewportMarginTop - contentViewportMarginBottom
  }

/**
 * Returns a new chart state that delegates all calls to
 * this but has a custom content size set
 */
fun ChartState.withContentAreaSize(@ContentArea sizeOverride: Size): ChartState {
  return ContentAreaSizeOverrideChartState(sizeOverride, this)
}

/**
 * Returns a new chart state that delegates all calls to this but has a
 * custom window size set
 */
fun ChartState.withWindowSize(@Zoomed sizeOverride: Size): ChartState {
  return WindowSizeOverrideChartState(sizeOverride, this)
}

/**
 * Returns a new chart state that delegates all calls to
 * this but has a custom zoom set
 */
fun ChartState.withZoom(zoomOverride: Zoom): ChartState {
  return ZoomOverrideChartState(zoomOverride, this)
}

/**
 * Returns a new instance with the given translation (absolute)
 */
fun ChartState.withTranslation(translationOverride: Distance): ChartState {
  return TranslationOverrideChartState(translationOverride, this)
}

/**
 * Returns a new instance with the given translation added to the current translation.
 */
fun ChartState.withAdditionalTranslation(additionalTranslation: Distance): ChartState {
  return TranslationAddedChartState(additionalTranslation, this)
}

/**
 * Returns a new instance with the given translation added to the current translation.
 */
fun ChartState.withContentViewportMargin(contentViewportMarginOverride: Insets): ChartState {
  return ContentViewportOverrideChartState(contentViewportMarginOverride, this)
}

/**
 * Returns a new instance with the given axis orientation
 */
fun ChartState.withAxisOrientation(
  axisOrientationXOverride: AxisOrientationX? = null,
  axisOrientationYOverride: AxisOrientationY? = null,
): ChartState {
  return AxisOrientationOverrideChartState(axisOrientationXOverride, axisOrientationYOverride, this)
}

/**
 * Overrides the translation of a chart state.
 */
class TranslationOverrideChartState(
  val translationOverride: Distance,
  delegate: ChartState
) : DelegatingChartState(delegate) {
  override val windowTranslationX: Double
    get() = translationOverride.x

  override val windowTranslationY: Double
    get() = translationOverride.y

  override val windowTranslation: Distance
    get() = translationOverride
}

/**
 * Adds a translation to the current translation of a chart state
 */
class TranslationAddedChartState(
  val additionalTranslation: Distance,
  delegate: ChartState
) : DelegatingChartState(delegate) {
  override val windowTranslationX: Double
    get() = additionalTranslation.x + delegate.windowTranslationX

  override val windowTranslationY: Double
    get() = additionalTranslation.y + delegate.windowTranslationY

  override val windowTranslation: Distance
    get() = additionalTranslation.plus(delegate.windowTranslation)
}

/**
 * Overrides the zoom for a chart state
 */
class ZoomOverrideChartState(
  val zoomOverride: Zoom,
  delegate: ChartState
) : DelegatingChartState(delegate) {
  override val zoom: Zoom
    get() = zoomOverride

  override val zoomX: Double
    get() = zoomOverride.scaleX

  override val zoomY: Double
    get() = zoomOverride.scaleY
}

/**
 * Overrides the content area size
 */
class ContentAreaSizeOverrideChartState(
  val sizeOverride: Size,
  delegate: ChartState,
) : DelegatingChartState(delegate) {

  init {
    require(sizeOverride.bothNotNegative()) { "Invalid size <$sizeOverride>" }
  }

  override val contentAreaSize: Size
    get() = sizeOverride

  override val contentAreaWidth: Double
    get() = sizeOverride.width

  override val contentAreaHeight: Double
    get() = sizeOverride.height
}

class WindowSizeOverrideChartState(
  val sizeOverride: @Zoomed Size,
  delegate: ChartState
) : DelegatingChartState(delegate) {
  init {
    require(sizeOverride.bothNotNegative()) { "Invalid size <$sizeOverride>" }
  }

  override val windowWidth: @Zoomed Double
    get() = sizeOverride.width

  override val windowHeight: @Zoomed Double
    get() = sizeOverride.height

  override val windowSize: @Zoomed Size
    get() = sizeOverride
}

/**
 * Overwrites the axis orientation
 */
class AxisOrientationOverrideChartState(
  val axisOrientationXOverride: AxisOrientationX?,
  val axisOrientationYOverride: AxisOrientationY?,
  delegate: ChartState,
) : DelegatingChartState(delegate) {

  override fun axisOrientation(axis: Axis): AxisInversionInformation {
    return when (axis) {
      Axis.X -> axisOrientationX
      Axis.Y -> axisOrientationY
    }
  }

  override val axisOrientationX: AxisOrientationX
    get() = axisOrientationXOverride ?: super.axisOrientationX

  override val axisOrientationY: AxisOrientationY
    get() = axisOrientationYOverride ?: super.axisOrientationY
}

/**
 * Overwrites the content viewport
 */
class ContentViewportOverrideChartState(
  val contentViewportMarginOverride: Insets,
  delegate: ChartState,
) : DelegatingChartState(delegate) {

  override val contentViewportMarginTop: Double
    get() = contentViewportMargin.top

  override val contentViewportMarginRight: Double
    get() = contentViewportMargin.right

  override val contentViewportMarginBottom: Double
    get() = contentViewportMargin.bottom

  override val contentViewportMarginLeft: Double
    get() = contentViewportMargin.left

  override val contentViewportMargin: Insets
    get() {
      return contentViewportMarginOverride
    }
}
