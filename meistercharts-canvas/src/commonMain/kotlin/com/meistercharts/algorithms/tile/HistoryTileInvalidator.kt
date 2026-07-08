/*
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

import com.meistercharts.algorithms.layers.PaintingPropertyKey
import com.meistercharts.canvas.ChartSupport
import com.meistercharts.canvas.paintingProperties
import com.meistercharts.history.HistoryUpdateInfo
import it.neckar.open.collections.IntMap
import it.neckar.open.collections.fastForEach

/**
 * A service that invalidates the history tiles when the history has been updated
 */
fun interface HistoryTileInvalidator {
  /**
   * Is called whenever the history has been updated
   */
  fun historyHasBeenUpdated(
    /**
     * The update info describing the changes to the history
     */
    updateInfo: HistoryUpdateInfo,

    /**
     * The cached tiles that should be invalidated if necessary
     */
    tiles: Collection<CanvasTile>,

    /**
     * The chart support
     */
    chartSupport: ChartSupport,
  ): HistoryTilesInvalidationResult
}

/**
 * Represents the result of a history tiles invalidation
 */
enum class HistoryTilesInvalidationResult {
  None,
  TilesInvalidated
}

/**
 * Invalidates all tiles - useful for tests/debugging
 */
object InvalidateAll : HistoryTileInvalidator {
  override fun historyHasBeenUpdated(updateInfo: HistoryUpdateInfo, tiles: Collection<CanvasTile>, chartSupport: ChartSupport): HistoryTilesInvalidationResult {
    tiles.forEach {
      it.clearSnapshot()
    }

    return HistoryTilesInvalidationResult.TilesInvalidated
  }
}

/**
 * Default implementation that uses the time range
 */
class DefaultHistoryTileInvalidator : HistoryTileInvalidator {

  /**
   * Reusable buffer that contains the clear decision per tile column (key: [xDataHashCode]).
   * - `true`: the column has relevant changes - all its tiles are cleared
   * - `false`: the column has no relevant changes
   * - missing key: no decision has been made (yet) for that column
   *
   * Kept as field (and cleared per updated time range) to avoid allocating a map, lists and boxed keys on every history update.
   */
  private val columnClearDecisions: IntMap<Boolean> = IntMap()

  override fun historyHasBeenUpdated(updateInfo: HistoryUpdateInfo, tiles: Collection<CanvasTile>, chartSupport: ChartSupport): HistoryTilesInvalidationResult {
    val relevantSamplingPeriod = chartSupport.paintingProperties.retrieveOrNull(PaintingPropertyKey.SamplingPeriod)
    if (relevantSamplingPeriod == null) {
      // Do not recalculate - no layout occurred yet
      return HistoryTilesInvalidationResult.None
    }

    if (tiles.isEmpty()) {
      //Nothing to do, no tiles exist
      return HistoryTilesInvalidationResult.None
    }

    var tilesInvalidated = false

    updateInfo.updatedTimeRanges.fastForEach { updatedTimeRange ->
      columnClearDecisions.clear()

      //Decide for each column (all tiles with the same [xDataHashCode] - same column, same zoom) whether it has relevant changes.
      //The decision is based on the first tile (in iteration order) of the column that is not empty.
      //If no tile of a column contains any data (all tiles have been cleared already), no decision is stored - the column is skipped.
      tiles.forEach { tile ->
        val columnKey = tile.identifier.xDataHashCode()
        if (columnKey in columnClearDecisions) {
          //Decision for this column has already been made
          return@forEach
        }

        //Skip tiles without data - they cannot be used to decide for the column
        val creationInfo = tile.creationInfo ?: return@forEach

        //Extract the painted time range (that contains the gaps)
        val timeRangeToPaint = creationInfo.get(HistoryCanvasTilePainter.timeRangeToPaintKey) ?: throw IllegalStateException("No <timeRangeToPaintKey> found")

        //Check if there are any changes for this column
        columnClearDecisions[columnKey] = updatedTimeRange.start <= timeRangeToPaint.end && updatedTimeRange.end >= timeRangeToPaint.start
      }

      //Clear all tiles of the columns that have relevant changes
      tiles.forEach { tile ->
        if (columnClearDecisions[tile.identifier.xDataHashCode()] == true) {
          val snapshotClearResult = tile.clearSnapshot()

          if (snapshotClearResult == SnapshotClearResult.Cleared) {
            tilesInvalidated = true
          }
        }
      }
    }

    return if (tilesInvalidated) {
      HistoryTilesInvalidationResult.TilesInvalidated
    } else {
      HistoryTilesInvalidationResult.None
    }
  }
}

/**
 * Calculates a hash code for the x related properties.
 * This hash can be used to identify tiles within the same column
 */
internal fun TileIdentifier.xDataHashCode(): Int {
  var result = 0
  result = 31 * result + mainX.hashCode()
  result = 31 * result + subX.hashCode()
  return result
}
