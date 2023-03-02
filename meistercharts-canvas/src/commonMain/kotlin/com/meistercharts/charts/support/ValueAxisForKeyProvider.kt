package com.meistercharts.charts.support

import com.meistercharts.algorithms.layers.ValueAxisLayer

/**
 * Provides a value axis for a key
 */
fun interface ValueAxisForKeyProvider<in Key> {
  /**
   * Returns the axis layer for the given key.
   * Should always return the same instance for the same key
   */
  fun getAxisLayer(key: Key): ValueAxisLayer
}
