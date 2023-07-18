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
package com.meistercharts.state

import com.meistercharts.annotations.ContentArea
import com.meistercharts.annotations.Zoomed
import it.neckar.open.kotlin.lang.isPositiveOrZero

/**
 * Abstract base class for a chart state.
 * Implements some abstract methods.
 *
 */
abstract class AbstractChartState : MutableChartState {
  override var contentViewportMarginTop: Double
    get() = contentViewportMargin.top
    set(value) {
      contentViewportMargin = contentViewportMargin.withTop(value)
    }

  override var contentViewportMarginRight: Double
    get() = contentViewportMargin.right
    set(value) {
      contentViewportMargin = contentViewportMargin.withRight(value)
    }
  override var contentViewportMarginBottom: Double
    get() = contentViewportMargin.bottom
    set(value) {
      contentViewportMargin = contentViewportMargin.withBottom(value)
    }

  override var contentViewportMarginLeft: Double
    get() = contentViewportMargin.left
    set(value) {
      contentViewportMargin = contentViewportMargin.withLeft(value)
    }

  override var zoomX: Double
    get() = zoom.scaleX
    set(value) {
      require(value.isFinite()) { "Invalid value <$value>" }
      zoom = zoom.withX(value)
    }

  override var zoomY: Double
    get() = zoom.scaleY
    set(value) {
      require(value.isFinite()) { "Invalid value <$value>" }
      zoom = zoom.withY(value)
    }

  override var windowTranslationX: @Zoomed Double
    get() = windowTranslation.x
    set(value) {
      require(value.isFinite()) { "Invalid value <$value>" }
      windowTranslation = windowTranslation.withX(value)
    }

  override var windowTranslationY: @Zoomed Double
    get() = windowTranslation.y
    set(value) {
      require(value.isFinite()) { "Invalid value <$value>" }
      windowTranslation = windowTranslation.withY(value)
    }

  @ContentArea
  override var contentAreaWidth: Double
    get() = contentAreaSize.width
    set(value) {
      require(value.isPositiveOrZero()) { "Invalid content area width <$value>" }
      contentAreaSize = contentAreaSize.withWidth(value)
    }

  @ContentArea
  override var contentAreaHeight: Double
    get() = contentAreaSize.height
    set(value) {
      require(value.isPositiveOrZero()) { "Invalid content area height <$value>" }
      contentAreaSize = contentAreaSize.withHeight(value)
    }

  @Zoomed
  override var windowWidth: Double
    get() = windowSize.width
    set(value) {
      require(value.isPositiveOrZero()) { "Invalid window area width <$value>" }
      windowSize = windowSize.withWidth(value)
    }

  @Zoomed
  override var windowHeight: Double
    get() = windowSize.height
    set(value) {
      require(value.isPositiveOrZero()) { "Invalid window area height <$value>" }
      windowSize = windowSize.withHeight(value)
    }
}
