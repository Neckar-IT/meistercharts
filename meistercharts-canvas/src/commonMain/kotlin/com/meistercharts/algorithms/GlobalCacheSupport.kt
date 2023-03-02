package com.meistercharts.algorithms

import com.meistercharts.algorithms.tile.GlobalTilesCache
import com.meistercharts.charts.ChartId

/**
 * Supports handling of global caches.
 *
 * Every time a chart is disposed, this object is used to clean up all global caches
 */
object GlobalCacheSupport {
  /**
   * Cleans all global caches for the given chart id.
   * This method is *only* called from the ChartSupport.
   */
  fun cleanup(chartId: ChartId) {
    GlobalTilesCache.clear(chartId)
  }
}
