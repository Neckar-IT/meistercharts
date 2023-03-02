package com.meistercharts.algorithms.impl

import com.meistercharts.algorithms.MutableChartState
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
