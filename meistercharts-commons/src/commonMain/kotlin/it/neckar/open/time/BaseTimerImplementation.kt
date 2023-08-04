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
    require(delayInMillis >= 1) { "delay must be at least 1 millisecond but was $delay" }

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
