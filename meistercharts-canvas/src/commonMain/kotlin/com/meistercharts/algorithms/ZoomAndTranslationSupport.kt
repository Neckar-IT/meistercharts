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
package com.meistercharts.algorithms

import com.meistercharts.algorithms.axis.AxisSelection
import com.meistercharts.algorithms.impl.ZoomAndTranslationDefaults
import com.meistercharts.annotations.ContentArea
import com.meistercharts.annotations.ContentAreaRelative
import com.meistercharts.annotations.DomainRelative
import it.neckar.open.unit.number.Positive
import com.meistercharts.annotations.Window
import com.meistercharts.annotations.Zoomed
import com.meistercharts.model.Coordinates
import com.meistercharts.model.Distance
import com.meistercharts.model.Zoom
import it.neckar.open.kotlin.lang.or0ifNaN
import it.neckar.open.kotlin.lang.or1ifNaN
import it.neckar.open.unit.other.px
import kotlin.jvm.JvmOverloads

/**
 * Helper class that offers panning and zooming support
 *
 */
class ZoomAndTranslationSupport(
  val chartCalculator: ChartCalculator,
  val chartState: MutableChartState,
  /**
   * The zoom and pan modifier
   */
  var zoomAndTranslationModifier: ZoomAndTranslationModifier,
  /**
   * Provides the defaults for zoom and pan
   */
  val zoomAndTranslationDefaults: ZoomAndTranslationDefaults,
  /**
   * The change factor is used to calculate the new zoom level
   */
  val zoomChangeFactor: @Positive Double = ZoomLevelCalculator.SQRT_2
) {

  @Deprecated("Tests only!")
  constructor(
    chartState: MutableChartState,
    zoomAndPanModifier: ZoomAndTranslationModifier,
    zoomAndTranslationDefaults: ZoomAndTranslationDefaults,
    zoomChangeFactor: Double = ZoomLevelCalculator.SQRT_2
  ) : this(ChartCalculator(chartState), chartState, zoomAndPanModifier, zoomAndTranslationDefaults, zoomChangeFactor)

  init {
    require(chartCalculator.chartState === chartState) {
      "Chart state of chart calculator must be the same as the chart state"
    }

    resetToDefaults()
  }

  fun translateWindowX(deltaX: @Zoomed Double) {
    moveWindow(deltaX, 0.0)
  }

  fun translateWindowY(deltaY: @Zoomed Double) {
    moveWindow(0.0, deltaY)
  }

  fun translateWindow(axisSelection: AxisSelection, deltaX: @Zoomed Double, deltaY: @Zoomed Double) {
    @Zoomed var relevantDeltaX = 0.0
    if (axisSelection.containsX) {
      relevantDeltaX = deltaX
    }

    @Zoomed var relevantDeltaY = 0.0
    if (axisSelection.containsY) {
      relevantDeltaY = deltaY
    }

    moveWindow(relevantDeltaX, relevantDeltaY)
  }

  /**
   * Move the window by the relative amount
   */
  fun moveWindowRelative(@ContentAreaRelative deltaX: Double, @ContentAreaRelative deltaY: Double) {
    @Zoomed val deltaXZoomed = chartCalculator.contentAreaRelative2zoomedX(deltaX).or0ifNaN()
    @Zoomed val deltaYZoomed = chartCalculator.contentAreaRelative2zoomedY(deltaY).or0ifNaN()
    moveWindow(deltaXZoomed, deltaYZoomed)
  }

  /**
   * Move the window by the given pixels
   */
  fun moveWindow(deltaX: @Zoomed Double, deltaY: @Zoomed Double) {
    setWindowTranslation(chartState.windowTranslation.plus(deltaX, deltaY))
  }

  fun moveWindow(delta: @Zoomed Distance) {
    moveWindow(delta.x, delta.y)
  }

  fun setWindowTranslationX(translationX: @Zoomed Double) {
    setWindowTranslation(Distance.of(translationX, chartState.windowTranslationY))
  }

  fun setWindowTranslationY(translationY: @Zoomed Double) {
    setWindowTranslation(Distance.of(chartState.windowTranslationX, translationY))
  }

  fun setWindowTranslation(windowTranslation: @Zoomed Distance) {
    chartState.windowTranslation = zoomAndTranslationModifier
      .modifyTranslation(windowTranslation, chartCalculator)
      .avoidNaN()
  }

  /**
   * Updates the zoom. Keeps the given zoom center
   */
  @JvmOverloads
  fun setZoom(zoom: Zoom, @Window @px zoomCenter: Coordinates? = null) {
    setZoom(zoom.scaleX, zoom.scaleY, zoomCenter)
  }

  @JvmOverloads
  fun setZoom(
    newZoomFactorX: @Positive Double = chartState.zoomX,
    newZoomFactorY: @Positive Double = chartState.zoomY,
    @Window zoomCenter: Coordinates? = null
  ) {
    if (zoomCenter == null) {
      //Simple zooming mode without a zoom center
      chartState.zoom = Zoom.of(newZoomFactorX, newZoomFactorY)
      return
    }

    //Remember the location under the zoom center before applying the zoom change
    val (@ContentAreaRelative x, @ContentAreaRelative y) = chartCalculator.window2contentAreaRelative(zoomCenter.x, zoomCenter.y)

    //ensure the limits of the zoom are respected
    chartState.zoom = zoomAndTranslationModifier.modifyZoom(Zoom(newZoomFactorX, newZoomFactorY), chartCalculator)

    //which relative value is now under the zoom center? Necessary to calculate the correction
    val (@ContentAreaRelative x1, @ContentAreaRelative y1) = chartCalculator.window2contentAreaRelative(zoomCenter.x, zoomCenter.y)

    //Move the window so that the old content area relative is again placed under the zoom center
    moveWindowRelative(x1 - x, y1 - y)
  }

  @JvmOverloads
  fun modifyZoom(
    zoomIn: Boolean,
    axisSelection: AxisSelection,
    @Window @px zoomCenterX: Double = chartState.windowWidth * 0.5,
    @Window @px zoomCenterY: Double = chartState.windowHeight * 0.5,
    zoomChangeFactor: Double = this.zoomChangeFactor
  ) {
    modifyZoom(zoomIn, axisSelection, Coordinates(zoomCenterX, zoomCenterY), zoomChangeFactor)
  }

  /**
   * Modifies the zoom
   */
  fun modifyZoom(
    zoomIn: Boolean,
    axisSelection: AxisSelection,
    @Window @px zoomCenter: Coordinates? = chartState.windowCenter,
    zoomChangeFactor: Double = this.zoomChangeFactor
  ) {
    modifyZoom(zoomIn, axisSelection, zoomCenter, zoomChangeFactor, zoomChangeFactor)
  }

  /**
   * Modifies the zoom - supports different factors for x and y
   */
  fun modifyZoom(
    zoomIn: Boolean,
    axisSelection: AxisSelection,
    @Window @px zoomCenter: Coordinates? = chartState.windowCenter,
    zoomChangeFactorX: @Positive Double = this.zoomChangeFactor,
    zoomChangeFactorY: @Positive Double = this.zoomChangeFactor
  ) {
    val oldZoomFactors = chartState.zoom

    var newZoomFactorX = oldZoomFactors.scaleX
    var newZoomFactorY = oldZoomFactors.scaleY

    if (zoomIn) {
      //Zoom in
      if (axisSelection.containsX) {
        newZoomFactorX *= zoomChangeFactorX
      }
      if (axisSelection.containsY) {
        newZoomFactorY *= zoomChangeFactorY
      }
    } else {
      //Zoom out
      if (axisSelection.containsX) {
        newZoomFactorX /= zoomChangeFactorX
      }
      if (axisSelection.containsY) {
        newZoomFactorY /= zoomChangeFactorY
      }
    }

    setZoom(newZoomFactorX, newZoomFactorY, zoomCenter)
  }

  fun modifyZoom(
    /**
     * The existing zoom is multiplied with this zoom factor
     */
    zoomChangeFactor: Double,

    axisSelection: AxisSelection = AxisSelection.Both,
    @Window @px zoomCenter: Coordinates? = chartState.windowCenter,
  ) {
    modifyZoom(zoomChangeFactor, zoomChangeFactor, axisSelection, zoomCenter)
  }

  fun modifyZoom(
    /**
     * The existing zoom is multiplied with this zoom factor
     */
    zoomChangeFactorX: Double,
    zoomChangeFactorY: Double,

    axisSelection: AxisSelection = AxisSelection.Both,
    @Window @px zoomCenter: Coordinates? = chartState.windowCenter,
  ) {
    val oldZoomFactors = chartState.zoom

    var newZoomFactorX = oldZoomFactors.scaleX
    var newZoomFactorY = oldZoomFactors.scaleY

    if (axisSelection.containsX) {
      newZoomFactorX *= zoomChangeFactorX
    }
    if (axisSelection.containsY) {
      newZoomFactorY *= zoomChangeFactorY
    }

    setZoom(newZoomFactorX, newZoomFactorY, zoomCenter)
  }

  /**
   * Resets zoom and translation to the default values
   * @param zoomAndTranslationDefaults The amount of overscan Results in a smaller zoom
   */
  @JvmOverloads
  fun resetToDefaults(zoomAndTranslationDefaults: ZoomAndTranslationDefaults = this.zoomAndTranslationDefaults) {
    resetZoom(zoomAndTranslationDefaults)
    resetWindowTranslation(zoomAndTranslationDefaults)
  }

  fun resetZoom(zoomAndTranslationDefaults: ZoomAndTranslationDefaults = this.zoomAndTranslationDefaults, @Window @px zoomCenter: Coordinates? = null) {
    //Reduce the zoom accordingly and translate to center
    setZoom(zoomAndTranslationDefaults.defaultZoom(chartCalculator), zoomCenter)
  }

  /**
   * Resets the window translation
   */
  fun resetWindowTranslation(zoomAndTranslationDefaults: ZoomAndTranslationDefaults = this.zoomAndTranslationDefaults) {
    setWindowTranslation(zoomAndTranslationDefaults.defaultTranslation(chartCalculator))
  }

  fun resetWindowTranslationX(zoomAndTranslationDefaults: ZoomAndTranslationDefaults = this.zoomAndTranslationDefaults) {
    @Zoomed val correctionX = zoomAndTranslationDefaults.defaultTranslation(chartCalculator).x
    setWindowTranslationX(correctionX)
  }

  fun resetWindowTranslationY(zoomAndTranslationDefaults: ZoomAndTranslationDefaults = this.zoomAndTranslationDefaults) {
    @Zoomed val correctionY = zoomAndTranslationDefaults.defaultTranslation(chartCalculator).y
    setWindowTranslationY(correctionY)
  }

  /**
   * Modifies the zoom and pan to show exactly the given range.
   */
  fun fitX(start: @DomainRelative Double, end: @DomainRelative Double) {
    if (chartState.hasZeroSize) {
      // nothing useful to be done here
      return
    }
    @ContentArea val startContentArea = chartCalculator.domainRelative2contentAreaX(start)
    @ContentArea val endContentArea = chartCalculator.domainRelative2contentAreaX(end)

    require(startContentArea <= endContentArea) { "Start <$start> ($startContentArea) must be left of end <$end> ($endContentArea)" }

    @ContentArea val targetVisibleContentAreaWidth = endContentArea - startContentArea

    val newZoomFactorX = (chartState.windowWidth / targetVisibleContentAreaWidth).or1ifNaN()
    setZoom(newZoomFactorX = newZoomFactorX)

    setWindowTranslationX(-chartCalculator.domainRelative2zoomedX(start))
  }

  /**
   * Modifies the zoom and pan to show exactly the given range.
   */
  fun fitY(start: @DomainRelative Double, end: @DomainRelative Double) {
    if (chartState.hasZeroSize) {
      // nothing useful to be done here
      return
    }
    @ContentArea val startContentArea = chartCalculator.domainRelative2contentAreaY(start)
    @ContentArea val endContentArea = chartCalculator.domainRelative2contentAreaY(end)

    require(startContentArea <= endContentArea) { "Start <$start> ($startContentArea) must be left of end <$end> ($endContentArea)" }

    @ContentArea val targetVisibleContentAreaHeight = endContentArea - startContentArea

    val newZoomFactorY = (chartState.windowHeight / targetVisibleContentAreaHeight).or1ifNaN()
    setZoom(newZoomFactorY = newZoomFactorY)

    setWindowTranslationY(-chartCalculator.domainRelative2zoomedY(start))
  }
}
