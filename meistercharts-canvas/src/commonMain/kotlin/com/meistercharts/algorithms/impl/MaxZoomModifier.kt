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
import com.meistercharts.algorithms.ZoomAndTranslationModifier
import com.meistercharts.annotations.ContentArea
import com.meistercharts.annotations.Zoomed
import com.meistercharts.model.Distance
import com.meistercharts.model.Zoom
import it.neckar.open.provider.DoubleProvider

/**
 * Ensures a max zoom level
 *
 */
class MaxZoomModifier(
  private var maxZoomFactorX: DoubleProvider,
  private var maxZoomFactorY: DoubleProvider,
  private val delegate: ZoomAndTranslationModifier,
) : ZoomAndTranslationModifier {

  constructor(
    maxZoomFactorX: Double,
    maxZoomFactorY: Double,
    delegate: ZoomAndTranslationModifier
  ) : this({ maxZoomFactorX }, { maxZoomFactorY }, delegate)

  override fun modifyTranslation(@Zoomed translation: Distance, calculator: ChartCalculator): @Zoomed Distance {
    return delegate.modifyTranslation(translation, calculator)
  }

  override fun modifyZoom(zoom: Zoom, calculator: ChartCalculator): Zoom {
    return delegate.modifyZoom(zoom, calculator)
      .withMax(maxZoomFactorX(), maxZoomFactorY())
  }
}
