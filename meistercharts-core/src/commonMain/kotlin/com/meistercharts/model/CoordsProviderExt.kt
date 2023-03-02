package com.meistercharts.model

import it.neckar.open.provider.CoordinatesProvider


/**
 * Creates a new [CoordinatesProvider] that returns the given values
 */
fun CoordinatesProvider.Companion.forValues(values: List<Coordinates>): CoordinatesProvider {
  return object : CoordinatesProvider {
    override fun size(): Int {
      return values.size
    }

    override fun xAt(index: Int): Double {
      return values[index].x
    }

    override fun yAt(index: Int): Double {
      return values[index].y
    }
  }
}
