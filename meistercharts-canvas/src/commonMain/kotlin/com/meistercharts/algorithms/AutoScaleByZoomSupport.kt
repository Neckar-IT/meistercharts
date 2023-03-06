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

import com.meistercharts.annotations.ContentArea
import com.meistercharts.annotations.DomainRelative
import com.meistercharts.annotations.Zoomed
import com.meistercharts.canvas.ChartSupport
import com.meistercharts.model.Insets
import com.meistercharts.model.Zoom
import it.neckar.open.kotlin.lang.or1ifInfinite

/**
 * Helper class that is able to calculate auto-scale.
 *
 * ATTENTION: This class *must not* be used for TimeLineChart. Use [com.meistercharts.charts.AutoScaleSupport] instead.
 */
class AutoScaleByZoomSupport(val chartSupport: ChartSupport) {
  /**
   * Optimizes the zoom and translation on the y axis to only show the given data range
   */
  fun autoScaleY(
    minRelative: @DomainRelative Double,
    maxRelative: @DomainRelative Double,
    /**
     * The margin of the content area
     */
    margin: @Zoomed Insets = Insets.empty,
    /**
     * The factor that is used to calculate the additional space that is added to the top and bottom
     * above [minRelative] and [maxRelative].
     */
    additionalSpaceFactor: Double = 0.05
  ) {
    val chartCalculator = chartSupport.chartCalculator
    val chartState = chartCalculator.chartState
    @Zoomed val windowSize = chartState.windowSize

    //The height that should be filled
    @ContentArea val targetHeight = chartState.contentAreaSize.height - margin.offsetHeight

    //The net zoom factor that is valid for @DomainRelative 1.0
    val netZoomFactorY = if (targetHeight <= 0) {
      Zoom.default.scaleY
    } else {
      1.0 / windowSize.height * targetHeight
    }


    //The delta that shall be visible
    @DomainRelative val delta = maxRelative - minRelative

    //The additional space above *and* below
    @DomainRelative val additional = delta * additionalSpaceFactor


    val newZoomFactoryY = (netZoomFactorY / (delta + additional + additional)).or1ifInfinite()

    chartSupport.zoomAndTranslationSupport.let {
      //set the zoom factor to fit the y-axis
      it.setZoom(newZoomFactorY = newZoomFactoryY)
      //fix the translation to make the values on the y axis visible
      it.setWindowTranslationY(-chartSupport.chartCalculator.domainRelative2zoomedY(maxRelative + additional) + margin.top)
    }
  }
}

/**
 * Returns the [AutoScaleByZoomSupport] instance
 */
val ChartSupport.autoScaleByZoomSupport: AutoScaleByZoomSupport
  get() {
    return serviceRegistry.get(AutoScaleByZoomSupport::class) {
      AutoScaleByZoomSupport(this)
    }
  }
