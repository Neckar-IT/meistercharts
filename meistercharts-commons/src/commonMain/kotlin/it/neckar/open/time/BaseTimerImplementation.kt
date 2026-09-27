/*
 * Copyright (C) 2013-2026 Neckar IT GmbH, Mössingen, Germany
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Linking this library statically or dynamically with other modules is
 * making a combined work based on this library. Thus, the terms and
 * conditions of the GNU General Public License cover the whole combination.
 *
 * As a special exception, the copyright holders of this library give you
 * permission to link this library with independent modules, regardless of
 * the license terms of these independent modules, and to copy and distribute
 * the resulting combined work under terms of your choice, provided that every
 * copy of the combined work is accompanied by a complete copy of the source
 * code of this library.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package it.neckar.open.time

import it.neckar.open.collections.fastForEachDelete
import it.neckar.open.collections.mutableSortedListOf
import it.neckar.open.dispose.Disposable
import it.neckar.open.unit.other.Sorted
import kotlin.time.ComparableTimeMark
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.TimeSource

/**
 * Base class for timer implementations.
 * Call [update] to call all callbacks that are due.
 *
 * Due times are marks of [monotonicTimeSource]: a wall clock adjustment neither fires nor stalls a callback.
 * When a test or a demo replaces [monotonicTimeSource], every pending callback keeps its remaining delay, counted from the next
 * [update], [delay] or [repeat].
 */
abstract class BaseTimerImplementation : TimerImplementation {
  /**
   * Calls all callbacks that are due
   */
  fun update() {
    val now = markNow()
    handleDelayCallbacks(now)
    handleRepeatCallbacks(now)
  }

  /**
   * The source the due times of all pending callbacks are marks of.
   */
  private var timeSource: TimeSource.WithComparableMarks = monotonicTimeSource

  /**
   * A mark of [monotonicTimeSource]. Carries the pending due times over first, if the source has been replaced:
   * marks of two sources cannot be compared.
   */
  private fun markNow(): ComparableTimeMark {
    val current = monotonicTimeSource
    if (current === timeSource) {
      return current.markNow()
    }

    //One reading per source moves every due time by the same offset, which keeps the order of both lists
    val previousNow = timeSource.markNow()
    val now = current.markNow()
    delayCallbacks.forEach { it.targetTime = now + (it.targetTime - previousNow) }
    repeatCallbacks.forEach { it.targetTime = now + (it.targetTime - previousNow) }
    timeSource = current
    return now
  }

  @Sorted
  private val delayCallbacks = mutableSortedListOf<DelayEntry>()

  /**
   * Contains the repeat callbacks.
   * Attention: This list will be sorted every time something (might) have changed.
   *
   * The [RepeatEntry]s are mutable to avoid allocations.
   */
  @Sorted
  private val repeatCallbacks = mutableListOf<RepeatEntry>()

  private fun handleDelayCallbacks(now: ComparableTimeMark) {
    delayCallbacks.fastForEachDelete {
      if (it.targetTime <= now) {
        it.callback()
        true
      } else {
        //The list is sorted, so we can stop here
        return
      }
    }
  }

  private fun handleRepeatCallbacks(now: ComparableTimeMark) {
    var fired = false

    for (entry in repeatCallbacks) {
      if (entry.targetTime > now) {
        //The list is sorted, so no later entry is due either
        break
      }
      entry.callback()
      entry.targetTime = entry.targetTime + entry.delay
      fired = true
    }

    if (fired) {
      //Re-sort because firing a callback mutated its targetTime and may have broken the ascending order.
      repeatCallbacks.sort()
    }
  }

  override fun delay(delay: Duration, callback: () -> Unit): Disposable {
    //Check if it should be called immediately
    if (delay <= Duration.ZERO) {
      callback()
      return Disposable {}
    }

    val entry = DelayEntry(markNow() + delay, callback)
    delayCallbacks.add(entry)
    return Disposable { delayCallbacks.remove(entry) }
  }

  override fun repeat(delay: Duration, callback: () -> Unit): Disposable {
    require(delay >= 1.milliseconds) { "delay must be at least 1 millisecond but was $delay" }

    val entry = RepeatEntry(delay, markNow() + delay, callback)
    repeatCallbacks.add(entry)
    repeatCallbacks.sort()

    return Disposable { repeatCallbacks.remove(entry) }
  }

  /**
   * An entry for a delay callback
   */
  private class DelayEntry(
    /**
     * The earliest time, when the callback should be called
     */
    var targetTime: ComparableTimeMark,
    val callback: () -> Unit,
  ) : Comparable<DelayEntry> {
    override fun compareTo(other: DelayEntry): Int {
      return targetTime.compareTo(other.targetTime)
    }
  }

  /**
   * An entry for a repeat callback
   */
  private class RepeatEntry(
    /**
     * The delay
     */
    val delay: Duration,
    /**
     * The earliest time, when the callback should be called (again).
     * This value is updated after each call.
     */
    var targetTime: ComparableTimeMark,
    val callback: () -> Unit,
  ) : Comparable<RepeatEntry> {
    override fun compareTo(other: RepeatEntry): Int {
      return targetTime.compareTo(other.targetTime)
    }
  }
}
