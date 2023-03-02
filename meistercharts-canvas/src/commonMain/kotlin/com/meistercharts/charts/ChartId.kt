package com.meistercharts.charts

/**
 * Identifiers a single chart.
 */
data class ChartId
/**
 * Usually [next] should be called instead!
 */
constructor(val id: Int) {
  companion object {
    private var lastId = -1

    /**
     * Creates a new chart id
     */
    fun next(): ChartId {
      lastId++
      return ChartId(lastId)
    }

  }
}
