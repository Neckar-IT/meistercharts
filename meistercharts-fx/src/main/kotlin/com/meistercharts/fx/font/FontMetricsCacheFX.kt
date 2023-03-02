package com.meistercharts.fx.font

import com.meistercharts.canvas.FontDescriptor
import com.meistercharts.canvas.FontMetrics
import com.meistercharts.canvas.FontMetricsCache
import it.neckar.open.collections.cache
import com.meistercharts.events.FontLoadedEventBroker

/**
 * Provides exact font metrics (cached) for the JavaFX canvas.
 *
 * Uses a canvas to render the font and measures the pixels.
 */
object FontMetricsCacheFX : FontMetricsCache {
  /**
   * Returns the current cache size (for debugging purposes)
   */
  val cacheSize: Int
    get() {
      return fontMetricsCache.size
    }

  private val fontMetricsCache = cache<FontDescriptor, FontMetrics>("FxCanvasFontMetricsCache", 500)

  /**
   * Is used to calculate the font metrics
   */
  val fontMetricsCalculator: CanvasFontMetricsCalculatorFX = CanvasFontMetricsCalculatorFX()

  init {
    //Clear the cache whenever a font has been loaded.
    FontLoadedEventBroker.onLoaded {
      fontMetricsCache.clear()
    }
  }

  /**
   * Returns the height for the given font family and size
   */
  override fun get(font: FontDescriptor): FontMetrics {
    return fontMetricsCache.getOrStore(font) {
      fontMetricsCalculator.calculateFontMetrics(font)
    }
  }
}
