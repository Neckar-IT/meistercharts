package com.meistercharts.charts

import com.meistercharts.algorithms.impl.FittingInContentViewport
import com.meistercharts.annotations.Zoomed
import com.meistercharts.canvas.MeisterChartBuilder
import com.meistercharts.model.Insets
import it.neckar.open.observable.ObservableObject

/**
 * Configures the content viewport using margins
 */
open class ContentViewportGestalt(contentViewportMargin: @Zoomed Insets) : ChartGestalt {
  /**
   * The current content viewport margin
   */
  val contentViewportMarginProperty: ObservableObject<@Zoomed Insets> = ObservableObject(contentViewportMargin)

  var contentViewportMargin: @Zoomed Insets by contentViewportMarginProperty

  @ChartGestaltConfiguration
  override fun configure(meisterChartBuilder: MeisterChartBuilder) {
    meisterChartBuilder.apply {
      configure {
        contentViewportMarginProperty.consumeImmediately {
          chartSupport.rootChartState.contentViewportMargin = it
          chartSupport.zoomAndTranslationSupport.resetToDefaults()
        }
      }

      zoomAndTranslationDefaults = FittingInContentViewport
    }
  }

  inline fun setMarginTop(newTop: @Zoomed Double) {
    contentViewportMargin = contentViewportMargin.withTop(newTop)
  }

  inline fun setMarginLeft(newLeft: @Zoomed Double) {
    contentViewportMargin = contentViewportMargin.withLeft(newLeft)
  }

  inline fun setMarginBottom(newBottom: @Zoomed Double) {
    contentViewportMargin = contentViewportMargin.withBottom(newBottom)
  }

  inline fun setMarginRight(newRight: @Zoomed Double) {
    contentViewportMargin = contentViewportMargin.withRight(newRight)
  }
}
