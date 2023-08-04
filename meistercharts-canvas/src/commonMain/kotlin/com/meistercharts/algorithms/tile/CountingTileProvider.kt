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
import com.meistercharts.annotations.Zoomed
import it.neckar.geometry.Size
import it.neckar.open.collections.incr
import it.neckar.open.unit.si.ms

/**
 * Collects debug information about when and how often tiles are provided
 */
class CountingTileProvider(val delegate: TileProvider) : TileProvider {
  override val tileSize: @Zoomed Size
    get() = delegate.tileSize

  override fun getTile(identifier: TileIdentifier): Tile? {
    lastPaintTimes[identifier] = Meistercharts.renderLoop.currentFrameTimestamp
    providedCount.incr(identifier)
    return delegate.getTile(identifier)
  }

  /**
   * Contains the last time the tile for the given identifier has been provided
   */
  private val lastPaintTimes = mutableMapOf<TileIdentifier, @ms Double>()

  /**
   * Returns the last paint time
   */
  fun lastPaintTime(tileIdentifier: TileIdentifier): @ms Double? {
    return lastPaintTimes[tileIdentifier]
  }

  /**
   * How often a tile has been provided
   */
  private val providedCount = mutableMapOf<TileIdentifier, Int>()

  /**
   * How often a tile has been provided for the given identifier
   */
  fun providedCount(tileIdentifier: TileIdentifier): Int {
    return providedCount[tileIdentifier] ?: 0
  }

}
