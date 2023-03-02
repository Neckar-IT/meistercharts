package com.meistercharts.history

import it.neckar.open.time.nowMillis
import it.neckar.open.async.Async
import it.neckar.open.unit.si.ms
import com.meistercharts.history.impl.HistoryChunk
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.DurationUnit

/**
 * Caches history store calls
 */
class HistoryStorageCache constructor(
  /**
   * The history that is cached
   */
  val history: WritableHistoryStorage,

  /**
   * The window for the addition
   */
  val window: Duration = 100.milliseconds,
) {

  private val async = Async().also {
    history.onDispose(it)
  }

  /**
   * The chunk that is scheduled for addition
   */
  var scheduledChunk: HistoryChunk? = null
    private set

  /**
   * The last time a scheduled chunk has been added
   */
  private var lastInsertionTime: @ms Double = 0.0

  /**
   * Schedules the given [chunk] to be stored into the [history].
   */
  fun scheduleForStore(chunk: HistoryChunk, samplingPeriod: SamplingPeriod) {
    scheduledChunk = scheduledChunk?.let {
      it.merge(chunk, it.start, chunk.end + 1)
    } ?: chunk

    @ms val windowMillis = window.toDouble(DurationUnit.MILLISECONDS)

    //Check if we can/should add immediately
    @ms val timeSinceLastInsertion = nowMillis() - lastInsertionTime
    if (timeSinceLastInsertion >= windowMillis) {
      //insert immediately - too long since last insertion
      insertScheduledChunk(samplingPeriod)
      return
    }

    @ms val remainingTime = windowMillis - timeSinceLastInsertion

    async.throttleLast(remainingTime.milliseconds, this) {
      insertScheduledChunk(samplingPeriod)
    }
  }

  /**
   * Clears the cache and discards all chunks scheduled for storage.
   */
  fun clear() {
    scheduledChunk = null
  }

  /**
   * Inserts the scheduled chunk
   */
  private fun insertScheduledChunk(samplingPeriod: SamplingPeriod) {
    scheduledChunk?.let {
      history.storeWithoutCache(it, samplingPeriod)
      //Remember the last insertion time
      lastInsertionTime = nowMillis()
    }
    scheduledChunk = null
  }
}
