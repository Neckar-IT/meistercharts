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
import it.neckar.geometry.AxisSelection
import com.meistercharts.annotations.ContentAreaRelative
import com.meistercharts.annotations.Zoomed
import com.meistercharts.model.Insets
import it.neckar.open.provider.DoubleProvider

/**
 * Builder for a zoom and pan modifiers
 *
 */
class ZoomAndTranslationModifiersBuilder(config: ZoomAndTranslationModifiersBuilder.() -> Unit = {}) {
  init {
    config()
  }

  /**
   * The current modifier that can be wrapped
   */
  var current: ZoomAndTranslationModifier = ZoomAndTranslationModifier.none

  fun disableZoomingAndTranslation(): ZoomAndTranslationModifiersBuilder {
    current = DisableZoomAndTranslationModifier(current)
    return this
  }

  fun disableTranslationX(): ZoomAndTranslationModifiersBuilder {
    current = DisableTranslateXModifier(current)
    return this
  }

  fun disableTranslationY(): ZoomAndTranslationModifiersBuilder {
    current = DisableTranslateYModifier(current)
    return this
  }

  fun disableZoomX(): ZoomAndTranslationModifiersBuilder {
    current = DisableZoomXModifier(current)
    return this
  }

  fun disableZoomY(): ZoomAndTranslationModifiersBuilder {
    current = DisableZoomYModifier(current)
    return this
  }

  fun disableZoom(): ZoomAndTranslationModifiersBuilder {
    current = DisableZoomModifier(current)
    return this
  }

  fun contentAlwaysBarelyVisible(): ZoomAndTranslationModifiersBuilder {
    current = ContentAreaAlwaysBarelyVisibleTranslationModifier(current)
    return this
  }

  fun contentAlwaysCompletelyVisible(
    axisSelection: AxisSelection = AxisSelection.Both,
    boundsProvider: @ContentAreaRelative BoundsProvider = DefaultBoundsProvider,
    /**
     * The margin around the content. Usually the content viewport margin.
     * Should only be overwritten under special circumstances
     */
    marginProvider: (calculator: ChartCalculator) -> @Zoomed Insets = { it.chartState.contentViewportMargin },
  ): ZoomAndTranslationModifiersBuilder {
    current = ContentAreaAlwaysCompletelyVisibleTranslationModifier(axisSelection, marginProvider, boundsProvider, delegate = current)
    return this
  }

  fun onlyPositiveTranslation(): ZoomAndTranslationModifiersBuilder {
    current = OnlyPositiveTranslationModifier(current)
    return this
  }

  fun maxZoom(maxZoomFactorX: DoubleProvider, maxZoomFactorY: DoubleProvider): ZoomAndTranslationModifiersBuilder {
    current = MaxZoomModifier(maxZoomFactorX, maxZoomFactorY, current)
    return this
  }

  fun minZoom(minZoomFactorX: DoubleProvider, minZoomFactorY: DoubleProvider): ZoomAndTranslationModifiersBuilder {
    current = MinZoomModifier(minZoomFactorX, minZoomFactorY, current)
    return this
  }

  fun maxZoom(maxZoomFactorX: Double, maxZoomFactorY: Double): ZoomAndTranslationModifiersBuilder {
    current = MaxZoomModifier(maxZoomFactorX, maxZoomFactorY, current)
    return this
  }

  fun minZoom(minZoomFactorX: Double, minZoomFactorY: Double): ZoomAndTranslationModifiersBuilder {
    current = MinZoomModifier(minZoomFactorX, minZoomFactorY, current)
    return this
  }

  fun build(): ZoomAndTranslationModifier {
    return current
  }
}
