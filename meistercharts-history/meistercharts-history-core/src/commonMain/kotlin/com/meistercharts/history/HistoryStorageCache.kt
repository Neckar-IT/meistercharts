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
package com.meistercharts.history

import com.meistercharts.history.impl.HistoryChunk
import com.meistercharts.history.impl.RecordingType
import it.neckar.open.async.Async
import it.neckar.open.time.nowMillis
import it.neckar.open.unit.si.ms
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.DurationUnit

/**
 * Caches history store calls.
 *
 * Attention: This class should only be used to schedule many, small changes.
 * This happens for example when a chart is updated in real time ("recording").
 * Do not use this method to add large chunks. Add large chunks directly to the storage.
 */
class HistoryStorageCache(
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
    scheduledChunk?.let { currentlyScheduled ->
      //Check if it necessary to add the currently scheduled chunk first

      if (currentlyScheduled.firstTimestamp > chunk.lastTimestamp) {
        //adding an old chunk - insert the current chunk immediately
        insertScheduledChunk(samplingPeriod)
      }

      if (currentlyScheduled.timeStampsCount >= 600) {
        //chunk is full, insert immediately
        insertScheduledChunk(samplingPeriod)
      }
    }

    scheduledChunk = scheduledChunk?.let {
      it.merge(chunk, it.firstTimestamp, chunk.lastTimestamp + 1)
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
