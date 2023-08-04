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
import com.meistercharts.annotations.Zoomed
import it.neckar.geometry.Distance
import com.meistercharts.model.Zoom

/**
 * Disables panning and zooming
 */
class DisableZoomAndTranslationModifier(private val delegate: ZoomAndTranslationModifier) : ZoomAndTranslationModifier {
  override fun modifyTranslation(@Zoomed translation: Distance, calculator: ChartCalculator): @Zoomed Distance {
    return delegate.modifyTranslation(translation, calculator).withX(0.0).withY(0.0)
  }

  override fun modifyZoom(zoom: Zoom, calculator: ChartCalculator): Zoom {
    return delegate.modifyZoom(zoom, calculator).withY(1.0).withX(1.0)
  }
}
