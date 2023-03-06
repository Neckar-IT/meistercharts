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
package com.meistercharts.algorithms.impl

import com.meistercharts.algorithms.ChartCalculator
import com.meistercharts.model.Distance
import com.meistercharts.model.Zoom

/**
 * Delegates each axis to a delegate
 */
class DelegatingZoomAndTranslationDefaults(
  /**
   * the delegate that is used to calculate the defaults for the x axis
   */
  val xAxisDelegate: ZoomAndTranslationDefaults,
  /**
   * the delegate that is used to calculate the defaults for the y axis
   */
  val yAxisDelegate: ZoomAndTranslationDefaults
) : ZoomAndTranslationDefaults {

  override fun defaultZoom(chartCalculator: ChartCalculator): Zoom {
    return Zoom.of(
      xAxisDelegate.defaultZoom(chartCalculator).scaleX,
      yAxisDelegate.defaultZoom(chartCalculator).scaleY
    )
  }

  override fun defaultTranslation(chartCalculator: ChartCalculator): Distance {
    return Distance.of(
      xAxisDelegate.defaultTranslation(chartCalculator).x,
      yAxisDelegate.defaultTranslation(chartCalculator).y
    )
  }
}
