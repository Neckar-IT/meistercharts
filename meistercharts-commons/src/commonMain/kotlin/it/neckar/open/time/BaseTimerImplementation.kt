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

import it.neckar.open.collections.fastForEach
import it.neckar.open.collections.fastForEachDelete
import it.neckar.open.collections.mutableSortedListOf
import it.neckar.open.dispose.Disposable
import it.neckar.open.unit.other.Sorted
import it.neckar.open.unit.si.ms
import kotlin.time.Duration
import kotlin.time.DurationUnit

/**
 * Base class for timer implementations.
 * Call [update] to call all callbacks that are due.
 */
abstract class BaseTimerImplementation : TimerImplementation {
  /**
   * Calls all callbacks that are due
   */
  fun update(now: @ms Double) {
    //Verify if somebody should be called
    handleDelayCallbacks(now)
    handleRepeatCallbacks(now)
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

  private fun handleDelayCallbacks(now: @ms Double) {
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

  private fun handleRepeatCallbacks(now: @ms Double) {
    repeatCallbacks.fastForEach {
      if (it.targetTime <= now) {
        it.callback()
        it.targetTime += it.delay
      } else {
        //The list is sorted, so we can stop here
        return
      }
    }

    repeatCallbacks.sort() //manual sort, because we have changed the targetTime
  }

  override fun delay(delay: Duration, callback: () -> Unit): Disposable {
    //Check if it should be called immediately
    if (delay <= Duration.ZERO) {
      callback()
      return Disposable {}
    }


    @ms val delayInMillis = delay.toDouble(DurationUnit.MILLISECONDS)

    val entry = DelayEntry(nowMillis() + delayInMillis, callback)
    delayCallbacks.add(entry)
    return Disposable { delayCallbacks.remove(entry) }
  }

  override fun repeat(delay: Duration, callback: () -> Unit): Disposable {
    @ms val delayInMillis = delay.toDouble(DurationUnit.MILLISECONDS)
    require(delayInMillis >= 1) { "delay must be at least 1 millisecond but was $delayInMillis" }

    val entry = RepeatEntry(delayInMillis, nowMillis() + delayInMillis, callback)
    repeatCallbacks.add(entry)
    repeatCallbacks.sort()

    return Disposable { repeatCallbacks.remove(entry) }
  }

  /**
   * An entry for a delay callback
   */
  private data class DelayEntry(
    /**
     * The earliest time, when the callback should be called
     */
    val targetTime: @ms Double,
    val callback: () -> Unit,
  ) : Comparable<DelayEntry> {
    override fun compareTo(other: DelayEntry): Int {
      return targetTime.compareTo(other.targetTime)
    }
  }

  /**
   * An entry for a delay callback
   */
  private class RepeatEntry(
    /**
     * The delay
     */
    val delay: @ms Double,
    /**
     * The earliest time, when the callback should be called (again).
     * This value is updated after each call.
     */
    var targetTime: @ms Double,
    val callback: () -> Unit,
  ) : Comparable<RepeatEntry> {
    override fun compareTo(other: RepeatEntry): Int {
      return targetTime.compareTo(other.targetTime)
    }
  }
}
