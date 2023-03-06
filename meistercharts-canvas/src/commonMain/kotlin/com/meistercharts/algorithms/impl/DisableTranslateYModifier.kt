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
import com.meistercharts.model.Distance
import com.meistercharts.model.Zoom

/**
 * Disables panning on the y-axis
 */
class DisableTranslateYModifier(private val delegate: ZoomAndTranslationModifier) : ZoomAndTranslationModifier {
  @ContentArea
  override fun modifyTranslation(@ContentArea translation: Distance, calculator: ChartCalculator): Distance {
    return delegate.modifyTranslation(translation, calculator).withY(0.0)
  }

  override fun modifyZoom(zoom: Zoom, calculator: ChartCalculator): Zoom {
    return delegate.modifyZoom(zoom, calculator)
  }
}
