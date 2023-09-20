/**
 * Copyright 2023 Neckar IT GmbH, Mössingen, Germany
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.meistercharts.whatsat

import com.meistercharts.algorithms.layout.BoxIndex
import com.meistercharts.maps.MapCoordinates
import com.meistercharts.tile.TileIndex
import it.neckar.open.annotations.TestOnly

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
    @TestOnly("Currently only used in tests. Maybe introduce a real POI object later")
    val pointOfInterest: ResultElementType<String> = ResultElementType(type = "POI")

    val mapCoordinates: ResultElementType<MapCoordinates> = ResultElementType("mapCoordinates")
    val tileIndex: ResultElementType<TileIndex> = ResultElementType("tileIndex")

    val category: ResultElementType<BoxIndex> = ResultElementType("category")
  }
}
