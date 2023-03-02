package com.meistercharts.algorithms.tile

import com.meistercharts.canvas.currentFrameTimestamp
import it.neckar.open.unit.si.ms

/**
 * Contains information about the tile creation
 */
data class TileCreationInfo(
  /**
   * The time when the tile has been created
   */
  val creationTime: @ms Double = currentFrameTimestamp,
  /**
   * Contains true if the tile itself is empty.
   * Can be used to avoid painting of empty tiles
   */
  val isEmpty: Boolean = false,
  /**
   * Additional values that have been used during the creation of the tile
   */
  val values: Map<TileCreationInfoKey<*>, Any?> = mapOf()
) {

  /**
   * Returns the entry for the given key
   */
  fun <T> get(key: TileCreationInfoKey<T>): T? {
    return values[key] as T
  }
}

/**
 * Represents a key for a value
 *
 * @param T represents the type of the object that will be returned under the given key
 */
@kotlin.Suppress("unused")
data class TileCreationInfoKey<out T>(val id: String) {
}
