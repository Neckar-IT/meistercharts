package com.meistercharts.canvas

import com.meistercharts.algorithms.layers.LayerPaintingContext

/**
 * Handles missing resources
 */
interface MissingResourcesHandler {
  /**
   * Is called when resources have been detected as missing.
   * This method is called *after* all layers have been painted
   */
  fun missingResourcesDetected(paintingContext: LayerPaintingContext, missingUrls: Set<String>)
}
