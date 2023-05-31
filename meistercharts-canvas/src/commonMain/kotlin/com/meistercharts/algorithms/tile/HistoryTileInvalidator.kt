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

import com.meistercharts.algorithms.layers.PaintingPropertyKey
import com.meistercharts.canvas.ChartSupport
import com.meistercharts.canvas.paintingProperties
import com.meistercharts.history.HistoryUpdateInfo
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
    tiles: Iterable<CanvasTile>,

    /**
     * The chart support
     */
    chartSupport: ChartSupport
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
  override fun historyHasBeenUpdated(updateInfo: HistoryUpdateInfo, tiles: Iterable<CanvasTile>, chartSupport: ChartSupport): HistoryTilesInvalidationResult {
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

  override fun historyHasBeenUpdated(updateInfo: HistoryUpdateInfo, tiles: Iterable<CanvasTile>, chartSupport: ChartSupport): HistoryTilesInvalidationResult {
    val relevantSamplingPeriod = chartSupport.paintingProperties.retrieveOrNull(PaintingPropertyKey.SamplingPeriod)
    if (relevantSamplingPeriod == null) {
      // Do not recalculate - no layout occurred yet
      return HistoryTilesInvalidationResult.None
    }

    var tilesInvalidated = false

    //Sort the tiles by x - all tiles in the same column and of the same zoom are invalided together
    val groupedByTileX = tiles.groupBy { it.identifier.xDataHashCode() }

    updateInfo.updatedTimeRanges.fastForEach { updatedTimeRange ->
      //Iterate over all groups - calculate for each group
      groupedByTileX.values.forEach { tilesInColumn ->
        require(tilesInColumn.isNotEmpty())

        //Find the first tile that is not empty
        val firstTile = tilesInColumn.firstOrNull {
          it.creationInfo != null
        } ?: return@forEach //skip the column, if no tile could be found that contains any data - all tiles have been cleared already

        //Extract the painted time range (that contains the gaps)
        val timeRangeToPaintKey = firstTile.creationInfo?.get(HistoryCanvasTilePainter.timeRangeToPaintKey) ?: throw IllegalStateException("No <timeRangeToPaintKey> found")

        //Check if there are any changes for this tile
        if (updatedTimeRange.start <= timeRangeToPaintKey.end && updatedTimeRange.end >= timeRangeToPaintKey.start) {
          //We have relevant changes in that column, therefore clear all tiles
          tilesInColumn.fastForEach { tile ->
            val snapshotClearResult = tile.clearSnapshot()

            if (snapshotClearResult == SnapshotClearResult.Cleared) {
              tilesInvalidated = true
            }
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
