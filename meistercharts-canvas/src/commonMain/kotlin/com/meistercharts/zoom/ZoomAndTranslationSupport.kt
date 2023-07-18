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
package com.meistercharts.zoom

import com.meistercharts.annotations.ContentArea
import com.meistercharts.annotations.ContentAreaRelative
import com.meistercharts.annotations.DomainRelative
import com.meistercharts.annotations.Window
import com.meistercharts.annotations.Zoomed
import com.meistercharts.axis.AxisSelection
import com.meistercharts.calc.ChartCalculator
import com.meistercharts.calc.ZoomLevelCalculator
import com.meistercharts.geometry.Coordinates
import com.meistercharts.geometry.Distance
import com.meistercharts.model.Zoom
import com.meistercharts.state.MutableChartState
import it.neckar.logging.Level
import it.neckar.logging.Logger
import it.neckar.logging.LoggerFactory
import it.neckar.logging.log
import it.neckar.open.kotlin.lang.or0ifNaN
import it.neckar.open.kotlin.lang.or1ifNaN
import it.neckar.open.unit.number.Positive
import it.neckar.open.unit.other.pct
import it.neckar.open.unit.other.px

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
  var zoomAndTranslationDefaults: ZoomAndTranslationDefaults,
  /**
   * The change factor is used to calculate the new zoom level
   */
  val zoomChangeFactor: @Positive Double = ZoomLevelCalculator.SQRT_2,
) {

  @Deprecated("Tests only!")
  constructor(
    chartState: MutableChartState,
    zoomAndPanModifier: ZoomAndTranslationModifier,
    zoomAndTranslationDefaults: ZoomAndTranslationDefaults,
    zoomChangeFactor: Double = ZoomLevelCalculator.SQRT_2,
  ) : this(ChartCalculator(chartState), chartState, zoomAndPanModifier, zoomAndTranslationDefaults, zoomChangeFactor)

  init {
    require(chartCalculator.chartState === chartState) {
      "Chart state of chart calculator must be the same as the chart state"
    }

    resetToDefaults(reason = UpdateReason.Initial)
  }

  fun translateWindowX(deltaX: @Zoomed Double, reason: UpdateReason) {
    moveWindow(deltaX, 0.0, reason = reason)
  }

  fun translateWindowY(deltaY: @Zoomed Double, reason: UpdateReason) {
    moveWindow(0.0, deltaY, reason = reason)
  }

  fun translateWindow(axisSelection: AxisSelection, deltaX: @Zoomed Double, deltaY: @Zoomed Double, reason: UpdateReason) {
    @Zoomed var relevantDeltaX = 0.0
    if (axisSelection.containsX) {
      relevantDeltaX = deltaX
    }

    @Zoomed var relevantDeltaY = 0.0
    if (axisSelection.containsY) {
      relevantDeltaY = deltaY
    }

    moveWindow(relevantDeltaX, relevantDeltaY, reason = reason)
  }

  /**
   * Move the window by the relative amount
   */
  fun moveWindowRelative(@ContentAreaRelative deltaX: Double, @ContentAreaRelative deltaY: Double, reason: UpdateReason) {
    @Zoomed val deltaXZoomed = chartCalculator.contentAreaRelative2zoomedX(deltaX).or0ifNaN()
    @Zoomed val deltaYZoomed = chartCalculator.contentAreaRelative2zoomedY(deltaY).or0ifNaN()
    moveWindow(deltaXZoomed, deltaYZoomed, reason = reason)
  }

  /**
   * Move the window by the given pixels
   */
  fun moveWindow(deltaX: @Zoomed Double, deltaY: @Zoomed Double, reason: UpdateReason) {
    setWindowTranslation(chartState.windowTranslation.plus(deltaX, deltaY), reason = reason)
  }

  fun moveWindow(delta: @Zoomed Distance, reason: UpdateReason) {
    moveWindow(delta.x, delta.y, reason = reason)
  }

  fun setWindowTranslationX(translationX: @Zoomed Double, reason: UpdateReason) {
    setWindowTranslation(Distance.of(translationX, chartState.windowTranslationY), reason = reason)
  }

  fun setWindowTranslationY(translationY: @Zoomed Double, reason: UpdateReason) {
    setWindowTranslation(Distance.of(chartState.windowTranslationX, translationY), reason = reason)
  }

  fun setWindowTranslation(windowTranslation: @Zoomed Distance, axisSelection: AxisSelection = AxisSelection.Both, reason: UpdateReason) {
    when {
      reason == UpdateReason.Animation -> Level.TRACE //the animation events happen a lot of times, do not debug these
      else -> Level.DEBUG
    }.let {
      logger.log(it) { "Setting window translation to $windowTranslation because ${reason.label()}" }
    }

    chartState.setWindowTranslation(
      zoomAndTranslationModifier
        .modifyTranslation(windowTranslation, chartCalculator)
        .avoidNaN(),
      axisSelection = axisSelection
    )
  }

  /**
   * Updates the zoom. Keeps the given zoom center
   */
  fun setZoom(zoom: Zoom, @Window @px zoomCenter: Coordinates? = null, axisSelection: AxisSelection = AxisSelection.Both, reason: UpdateReason) {
    setZoom(zoom.scaleX, zoom.scaleY, zoomCenter, axisSelection = axisSelection, reason = reason)
  }

  fun setZoom(
    newZoomFactorX: @Positive Double = chartState.zoomX,
    newZoomFactorY: @Positive Double = chartState.zoomY,
    @Window zoomCenter: Coordinates? = null,
    axisSelection: AxisSelection = AxisSelection.Both,
    reason: UpdateReason,
  ) {
    logger.debug("Setting zoom to $newZoomFactorX, $newZoomFactorY with center $zoomCenter because ${reason.label()}")

    if (zoomCenter == null) {
      //Simple zooming mode without a zoom center
      chartState.setZoom(newZoomFactorX, newZoomFactorY, axisSelection = axisSelection)
      return
    }

    //Remember the location under the zoom center before applying the zoom change
    val (@ContentAreaRelative x, @ContentAreaRelative y) = chartCalculator.window2contentAreaRelative(zoomCenter.x, zoomCenter.y)

    //ensure the limits of the zoom are respected
    chartState.setZoom(zoomAndTranslationModifier.modifyZoom(Zoom(newZoomFactorX, newZoomFactorY), chartCalculator), axisSelection = axisSelection)

    //which relative value is now under the zoom center? Necessary to calculate the correction
    val (@ContentAreaRelative x1, @ContentAreaRelative y1) = chartCalculator.window2contentAreaRelative(zoomCenter.x, zoomCenter.y)

    //Move the window so that the old content area relative is again placed under the zoom center
    moveWindowRelative(x1 - x, y1 - y, reason = reason)
  }

  fun modifyZoom(
    zoomIn: Boolean,
    axisSelection: AxisSelection,
    @Window @px zoomCenterX: Double = chartState.windowWidth * 0.5,
    @Window @px zoomCenterY: Double = chartState.windowHeight * 0.5,
    zoomChangeFactor: Double = this.zoomChangeFactor,
    reason: UpdateReason,
  ) {
    modifyZoom(zoomIn, axisSelection, Coordinates(zoomCenterX, zoomCenterY), zoomChangeFactor, reason = reason)
  }

  /**
   * Modifies the zoom
   */
  fun modifyZoom(
    zoomIn: Boolean,
    axisSelection: AxisSelection,
    @Window @px zoomCenter: Coordinates? = chartState.windowCenter,
    zoomChangeFactor: Double = this.zoomChangeFactor,
    reason: UpdateReason,
  ) {
    modifyZoom(zoomIn, axisSelection, zoomCenter, zoomChangeFactor, zoomChangeFactor, reason = reason)
  }

  /**
   * Modifies the zoom - supports different factors for x and y
   */
  fun modifyZoom(
    zoomIn: Boolean,
    axisSelection: AxisSelection,
    @Window @px zoomCenter: Coordinates? = chartState.windowCenter,
    zoomChangeFactorX: @Positive Double = this.zoomChangeFactor,
    zoomChangeFactorY: @Positive Double = this.zoomChangeFactor,
    reason: UpdateReason,
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

    setZoom(newZoomFactorX, newZoomFactorY, zoomCenter, reason = reason)
  }

  fun modifyZoom(
    /**
     * The existing zoom is multiplied with this zoom factor
     */
    zoomChangeFactor: Double,

    axisSelection: AxisSelection = AxisSelection.Both,
    @Window @px zoomCenter: Coordinates? = chartState.windowCenter,
    reason: UpdateReason,
  ) {
    modifyZoom(zoomChangeFactor, zoomChangeFactor, axisSelection, zoomCenter, reason = reason)
  }

  fun modifyZoom(
    /**
     * The existing zoom is multiplied with this zoom factor
     */
    zoomChangeFactorX: Double,
    zoomChangeFactorY: Double,

    axisSelection: AxisSelection = AxisSelection.Both,
    @Window @px zoomCenter: Coordinates? = chartState.windowCenter,
    reason: UpdateReason,
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

    setZoom(newZoomFactorX, newZoomFactorY, zoomCenter, reason = reason)
  }

  /**
   * Resets zoom and translation to the default values
   */
  fun resetToDefaults(
    zoomAndTranslationDefaults: ZoomAndTranslationDefaults = this.zoomAndTranslationDefaults,
    axisSelection: AxisSelection = AxisSelection.Both,
    reason: UpdateReason,
  ) {
    resetZoom(zoomAndTranslationDefaults, axisSelection = axisSelection, reason = reason)
    resetWindowTranslation(zoomAndTranslationDefaults, axisSelection = axisSelection, reason = reason)
  }

  fun resetZoom(
    zoomAndTranslationDefaults: ZoomAndTranslationDefaults = this.zoomAndTranslationDefaults,
    @Window @px zoomCenter: Coordinates? = null,
    axisSelection: AxisSelection = AxisSelection.Both,
    reason: UpdateReason,
  ) {
    //Reduce the zoom accordingly and translate to center
    setZoom(zoomAndTranslationDefaults.defaultZoom(chartCalculator), zoomCenter, axisSelection = axisSelection, reason = reason)
  }

  /**
   * Resets the window translation
   */
  fun resetWindowTranslation(zoomAndTranslationDefaults: ZoomAndTranslationDefaults = this.zoomAndTranslationDefaults, axisSelection: AxisSelection = AxisSelection.Both, reason: UpdateReason) {
    setWindowTranslation(zoomAndTranslationDefaults.defaultTranslation(chartCalculator), axisSelection = axisSelection, reason = reason)
  }

  fun resetWindowTranslationX(zoomAndTranslationDefaults: ZoomAndTranslationDefaults = this.zoomAndTranslationDefaults, reason: UpdateReason) {
    @Zoomed val correctionX = zoomAndTranslationDefaults.defaultTranslation(chartCalculator).x
    setWindowTranslationX(correctionX, reason = reason)
  }

  fun resetWindowTranslationY(zoomAndTranslationDefaults: ZoomAndTranslationDefaults = this.zoomAndTranslationDefaults, reason: UpdateReason) {
    @Zoomed val correctionY = zoomAndTranslationDefaults.defaultTranslation(chartCalculator).y
    setWindowTranslationY(correctionY, reason = reason)
  }

  /**
   * Calculates the zoom factor that should be set to fit the provided start/end
   */
  fun calculateFitZoomX(start: @DomainRelative Double, end: @DomainRelative Double): @pct Double {
    if (chartState.hasAnyZeroSize) {
      // nothing useful to be done here
      return 1.0
    }
    @ContentArea val startContentArea = chartCalculator.domainRelative2contentAreaX(start)
    @ContentArea val endContentArea = chartCalculator.domainRelative2contentAreaX(end)

    require(startContentArea <= endContentArea) { "Start <$start> ($startContentArea) must be left of end <$end> ($endContentArea)" }

    @ContentArea val targetVisibleContentAreaWidth = endContentArea - startContentArea

    return (chartState.windowWidth / targetVisibleContentAreaWidth).or1ifNaN()
  }

  /**
   * Calculates the window translation x to fit the provided start
   */
  fun calculateFitWindowTranslationX(start: @DomainRelative Double): @Window Double {
    return -chartCalculator.domainRelative2zoomedX(start)
  }


  /**
   * Modifies the zoom and pan to show exactly the given range.
   */
  fun fitX(start: @DomainRelative Double, end: @DomainRelative Double, reason: UpdateReason) {
    if (chartState.hasAnyZeroSize) {
      // nothing useful to be done here
      return
    }

    setZoom(newZoomFactorX = calculateFitZoomX(start, end), reason = reason)
    setWindowTranslationX(calculateFitWindowTranslationX(start), reason = reason)
  }

  /**
   * Modifies the zoom and pan to show exactly the given range.
   */
  fun fitY(start: @DomainRelative Double, end: @DomainRelative Double, reason: UpdateReason) {
    if (chartState.hasAnyZeroSize) {
      // nothing useful to be done here
      return
    }
    @ContentArea val startContentArea = chartCalculator.domainRelative2contentAreaY(start)
    @ContentArea val endContentArea = chartCalculator.domainRelative2contentAreaY(end)

    require(startContentArea <= endContentArea) { "Start <$start> ($startContentArea) must be left of end <$end> ($endContentArea)" }

    @ContentArea val targetVisibleContentAreaHeight = endContentArea - startContentArea

    val newZoomFactorY = (chartState.windowHeight / targetVisibleContentAreaHeight).or1ifNaN()
    setZoom(newZoomFactorY = newZoomFactorY, reason = reason)

    setWindowTranslationY(-chartCalculator.domainRelative2zoomedY(start), reason = reason)
  }

  companion object {
    private val logger: Logger = LoggerFactory.getLogger("com.meistercharts.zoom.ZoomAndTranslationSupport")
  }
}
