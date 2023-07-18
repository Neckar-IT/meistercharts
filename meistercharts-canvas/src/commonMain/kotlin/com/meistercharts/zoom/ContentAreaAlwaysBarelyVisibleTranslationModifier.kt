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
import com.meistercharts.zoom.ZoomAndTranslationModifier
import com.meistercharts.annotations.ContentArea
import com.meistercharts.annotations.Zoomed
import com.meistercharts.geometry.Distance
import com.meistercharts.model.Zoom
import it.neckar.open.unit.other.px

/**
 * Default implementations that limits the panning depending on the zoom level.
 * This modifier ensures the content area is always barely visible when panned.
 *
 * See "[internal/closed/charting/meistercharts-canvas/doc/translation/ContentAlwaysBarelyVisible.svg] for a visualization how this class works.
 *
 */
class ContentAreaAlwaysBarelyVisibleTranslationModifier(
  val delegate: ZoomAndTranslationModifier
) : ZoomAndTranslationModifier {
  /**
   * Modifies the min/max panning.
   * A visualization that describes the limits can be found in "DefaultPanLimiter.svg"
   */
  @px
  override fun modifyTranslation(@Zoomed @px translation: Distance, calculator: ChartCalculator): @Zoomed Distance {
    @Zoomed val minX = calculator.contentAreaRelative2zoomedX(-1.0)
    @Zoomed val minY = calculator.contentAreaRelative2zoomedY(-1.0)

    //TODO is this correct? What happens when the zoom is not 1.0?
    @ContentArea val maxX = calculator.chartState.contentAreaWidth
    @ContentArea val maxY = calculator.chartState.contentAreaHeight

    return delegate.modifyTranslation(translation, calculator)
      .withMin(minX, minY)
      .withMax(maxX, maxY)
  }

  override fun modifyZoom(zoom: Zoom, calculator: ChartCalculator): Zoom {
    return delegate.modifyZoom(zoom, calculator)
  }
}
