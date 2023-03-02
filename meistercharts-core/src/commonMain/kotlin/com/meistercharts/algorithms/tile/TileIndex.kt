package com.meistercharts.algorithms.tile

import it.neckar.open.formatting.NumberFormat
import it.neckar.open.formatting.intFormat

/**
 * The index of a tile
 */
data class TileIndex(
  val x: Int,
  val y: Int
) {

  override fun toString(): String {
    return "$x/$y"
  }

  fun format(format: NumberFormat = intFormat): String {
    return "${format.format(x.toDouble())}/${format.format(y.toDouble())}"
  }

  companion object {
    val origin: TileIndex = TileIndex(0, 0)

    fun of(x: Int, y: Int): TileIndex {
      return TileIndex(x, y)
    }
  }
}
