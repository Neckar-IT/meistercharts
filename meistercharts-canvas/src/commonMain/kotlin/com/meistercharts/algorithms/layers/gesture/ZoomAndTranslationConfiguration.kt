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
package com.meistercharts.algorithms.layers.gesture

import com.meistercharts.algorithms.axis.AxisSelection
import com.meistercharts.canvas.MeisterChartsBuilderDsl

/**
 * Contains the configuration for the [ZoomAndTranslationLayer]
 */
@MeisterChartsBuilderDsl
class ZoomAndTranslationConfiguration {
  /**
   * Whether to reset on double click
   */
  var resetOnDoubleClick: Boolean = true

  /**
   * Whether to reset on double tap
   */
  var resetOnDoubleTap: Boolean = true

  /**
   * Whether mouse wheel zoom is enabled
   */
  var mouseWheelZoom: Boolean = true

  /**
   * The mouse wheel zoom configuration
   */
  var mouseWheelZoomConfiguration: MouseWheelZoomConfiguration = MouseWheelZoomConfiguration()

  /**
   * Whether to translate on (mouse) drag
   */
  var translateOnMouseDrag: Boolean = true

  /**
   * Whether to translate on (touch screen) drag
   */
  var translateOnTouchDrag: Boolean = true

  /**
   * Whether to zoom on (touch screen) pinch
   */
  var zoomOnPinch: Boolean = true

  /**
   * The axis that can be translated
   */
  var translateAxisSelection: AxisSelection = AxisSelection.Both

  /**
   * Whether to enable the rubber band zoom
   */
  var rubberBandZoom: Boolean = false

  /**
   * Enables the rubber band zoom (and disables translate on drag)
   */
  fun enableRubberBandZoom() {
    rubberBandZoom = true
    translateOnMouseDrag = false
  }

  /**
   * Enable zooming without modifiers
   */
  fun zoomWithoutModifier() {
    mouseWheelZoomConfiguration = MouseWheelZoomConfiguration.withoutModifiers
  }

  /**
   * Configures the zoom and translation layer
   */
  fun configure(zoomAndTranslationLayer: ZoomAndTranslationLayer) {
    if (resetOnDoubleClick) {
      zoomAndTranslationLayer.resetToDefaultsOnDoubleClick()
    }

    if (resetOnDoubleClick) {
      zoomAndTranslationLayer.resetToDefaultsOnDoubleTap()
    }

    if (mouseWheelZoom) {
      zoomAndTranslationLayer.zoomOnMouseWheel(mouseWheelZoomConfiguration)
    }

    if (translateOnMouseDrag) {
      zoomAndTranslationLayer.translateOnMouseDrag(translateAxisSelection)
    }

    if (translateOnTouchDrag) {
      zoomAndTranslationLayer.translateOnTouchDrag(translateAxisSelection)
    }

    if (zoomOnPinch) {
      zoomAndTranslationLayer.zoomOnPinch()
    }

    if (rubberBandZoom) {
      zoomAndTranslationLayer.rubberBandZoom()
    }
  }
}
