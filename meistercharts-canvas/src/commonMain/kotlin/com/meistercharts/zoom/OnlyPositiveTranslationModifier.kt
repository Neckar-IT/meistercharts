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

import com.meistercharts.calc.ChartCalculator
import it.neckar.geometry.AxisOrientationX
import it.neckar.geometry.AxisOrientationY
import com.meistercharts.annotations.Zoomed
import it.neckar.geometry.Distance
import com.meistercharts.model.Zoom

/**
 * Limiter that ensures only positive values are visible
 * [Visualization](src/main/doc/translation/OnlyPositivePanLimiter.svg)
 *
 */
class OnlyPositiveTranslationModifier(val delegate: ZoomAndTranslationModifier) : ZoomAndTranslationModifier {
  /**
   * Modifies the min/max panning.
   * A visualization that describes the limits can be found in "DefaultPanLimiter.svg"
   */
  override fun modifyTranslation(@Zoomed translation: Distance, calculator: ChartCalculator): @Zoomed Distance {
    val minY = when (calculator.chartState.axisOrientationY) {
      AxisOrientationY.OriginAtTop -> calculator.contentAreaRelative2zoomedY(-1.0)
      AxisOrientationY.OriginAtBottom -> calculator.chartState.contentAreaHeight - calculator.contentAreaRelative2zoomedY(1.0)
    }

    val maxY = when (calculator.chartState.axisOrientationY) {
      AxisOrientationY.OriginAtTop -> 0.0
      AxisOrientationY.OriginAtBottom -> calculator.chartState.contentAreaHeight
    }


    val minX = when (calculator.chartState.axisOrientationX) {
      AxisOrientationX.OriginAtLeft  -> calculator.contentAreaRelative2zoomedX(-1.0)
      AxisOrientationX.OriginAtRight -> calculator.chartState.contentAreaWidth - calculator.contentAreaRelative2zoomedX(1.0)
    }

    val maxX = when (calculator.chartState.axisOrientationX) {
      AxisOrientationX.OriginAtLeft  -> 0.0
      AxisOrientationX.OriginAtRight -> calculator.chartState.contentAreaWidth
    }

    return delegate.modifyTranslation(translation, calculator)
      .withMin(minX, minY)
      .withMax(maxX, maxY)
  }

  override fun modifyZoom(zoom: Zoom, calculator: ChartCalculator): Zoom {
    return delegate.modifyZoom(zoom, calculator)
  }
}
