package com.meistercharts.canvas.layout.cache

/**
 * Implementing classes must only be used within a layout cache.
 * They will be reused for each layout (they have only vars).
 *
 * They should be resettet before each layout.
 */
interface LayoutVariable {
  /**
   * Resets all values to the defaults.
   *
   * This method should be called to ensure that no old values are used accidentally.
   * The painting code should work just fine without calling this method.
   */
  fun reset()
}
