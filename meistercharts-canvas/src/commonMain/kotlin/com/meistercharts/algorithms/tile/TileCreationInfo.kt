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
package com.meistercharts.algorithms.tile

import com.meistercharts.Meistercharts
import it.neckar.open.unit.si.ms

/**
 * Contains information about the tile creation
 */
data class TileCreationInfo(
  /**
   * The time when the tile has been created
   */
  val creationTime: @ms Double = Meistercharts.renderLoop.currentFrameTimestamp,
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
 * @property id the ID
 */
@Suppress("unused")
data class TileCreationInfoKey<out T>(val id: String) {
}
