package com.meistercharts.canvas

import it.neckar.open.unit.other.fps
import it.neckar.open.unit.si.ms

/**
 * Contains statistics about the paints
 */
class PaintStatisticsSupport(
  /**
   * The maximum amount of stored repaint stats
   */
  val maxStatsSize: Int = 100
) {
  private val _paintingStatsList: MutableList<PaintingStats> = mutableListOf()

  /**
   * The average duration
   */
  @ms
  val averageDuration: Double
    get() {
      return _paintingStatsList.asSequence()
        .map {
          it.layerPaintDurations.total
        }
        .average()
    }

  /**
   * Calculates the *real* FPS that have been painted - over the last 100 repaints.
   * This value does *not* take paints into account that have been skipped, since the canvas has not been marked as dirty.
   */
  @fps
  val fps: Double
    get() {
      val first = _paintingStatsList.firstOrNull()
      val last = _paintingStatsList.lastOrNull()

      if (first == null || last == null) {
        return 0.0
      }

      @ms val deltaMillis = last.frameTimestamp - first.frameTimestamp
      return _paintingStatsList.size / deltaMillis * 1000.0
    }

  val paintingStatsList: List<PaintingStats>
    get() = _paintingStatsList

  /**
   * Returns the last repaint stats object
   */
  val lastPaintingStats: PaintingStats?
    get() = _paintingStatsList.lastOrNull()

  /**
   * Stores new repaint stats
   */
  fun store(paintingStats: PaintingStats) {
    _paintingStatsList.add(paintingStats)

    //Ensure the list does not grow endless
    while (_paintingStatsList.size > maxStatsSize) {
      _paintingStatsList.removeAt(0)
    }
  }
}
