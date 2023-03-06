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
import com.meistercharts.model.Distance
import com.meistercharts.model.Zoom
import it.neckar.open.unit.other.px

/**
 * Modifies/limits the panning and zooming.
 *
 * The implementation can be used to keep the user within a reasonable area.
 *
 * It is suggested to use a [com.meistercharts.algorithms.impl.ZoomAndTranslationModifiersBuilder]
 * to instantiate a new ZoomAndPanModifier.
 *
 */
interface ZoomAndTranslationModifier {
  /**
   * Returns the updated translation
   */
  fun modifyTranslation(@ContentArea @px translation: Distance, calculator: ChartCalculator): @ContentArea Distance

  /**
   * Modifies the zoom.
   */
  fun modifyZoom(zoom: Zoom, calculator: ChartCalculator): Zoom

  companion object {
    /**
     * Default implementation that doesn't modify anything
     */
    val none: ZoomAndTranslationModifier = object : ZoomAndTranslationModifier {
      override fun modifyTranslation(@ContentArea @px translation: Distance, calculator: ChartCalculator): @ContentArea Distance {
        return translation
      }

      override fun modifyZoom(zoom: Zoom, calculator: ChartCalculator): Zoom {
        return zoom
      }
    }
  }
}
