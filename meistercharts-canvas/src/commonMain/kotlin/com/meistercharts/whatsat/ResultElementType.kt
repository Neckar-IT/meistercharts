package com.meistercharts.whatsat

import com.meistercharts.algorithms.layout.BoxIndex
import com.meistercharts.algorithms.tile.TileIndex
import com.meistercharts.model.MapCoordinates

/**
 * Identifies a type for a result element.
 * ATTENTION: Do not instantiate directly! Use the predefined values instead
 *
 *```
 *   type ResultElementType =
 *   "POI" |
 *   "mapCoordinates" |
 *   "tileIndex"
 * ```
 */
@Suppress("DataClassPrivateConstructor")
data class ResultElementType<T> private constructor(val type: String) {
  companion object {
    /**
     * Defines a point of interest.
     */
    @Deprecated("Currently only used in tests. Maybe introduce a real POI object later")
    val pointOfInterest: ResultElementType<String> = ResultElementType(type = "POI")

    val mapCoordinates: ResultElementType<MapCoordinates> = ResultElementType("mapCoordinates")
    val tileIndex: ResultElementType<TileIndex> = ResultElementType("tileIndex")

    val category: ResultElementType<BoxIndex> = ResultElementType("category")
  }
}
