package com.meistercharts.font

import com.meistercharts.Meistercharts

/**
 * Cache for [FontMetrics].
 */
interface FontMetricsCache {
  /**
   * Returns the font metrics for the given font descriptor
   */
  operator fun get(font: FontDescriptor): FontMetrics

  companion object {
    /**
     * Provides access to the font metrics cache.
     * The value is initialized in the Platform.init() method
     */
    fun get(): FontMetricsCache {
      return Meistercharts.fontMetricsCache ?: throw IllegalStateException("No fontMetricsCache set - please call MeisterChartPlatform.init()")
    }
  }
}
