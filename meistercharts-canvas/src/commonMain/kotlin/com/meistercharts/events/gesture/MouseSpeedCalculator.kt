package com.meistercharts.events.gesture

import com.meistercharts.model.Distance
import it.neckar.open.collections.EvictingQueue
import it.neckar.open.time.nowMillis
import it.neckar.open.unit.other.px
import it.neckar.open.unit.other.px_ms
import it.neckar.open.unit.si.ms
import kotlin.math.E
import kotlin.math.pow

/**
 * Calculates the mouse speed
 */
class MouseSpeedCalculator {
  /**
   * Contains the last speed elements of the last n dragging events
   */
  private val entries = EvictingQueue<Entry>(SpeedQueueSize)

  /**
   * Calculates the speed
   */
  @px_ms
  private fun calculateSpeed(speedExtractor: (Entry) -> Double): Double {
    if (entries.isEmpty()) {
      return 0.0
    }

    // The algorithm is taken from http://mortoray.com/2015/04/08/measuring-finger-mouse-velocity-at-release-time/ .
    var totalTime = 0.0
    for (entry in entries) {
      totalTime += entry.deltaTime
    }

    var speed = 0.0
    for (entry in entries) {
      val alpha = 1.0 - E.pow(-entry.deltaTime / totalTime)
      speed = alpha * speedExtractor(entry) + (1.0 - alpha) * speed
    }
    return speed
  }

  @px_ms
  fun calculateSpeedX(): Double {
    return calculateSpeed { it.speedX }
  }

  @px_ms
  fun calculateSpeedY(): Double {
    return calculateSpeed { it.speedY }
  }

  @px_ms
  fun calculateSpeed(): Speed {
    return Speed(calculateSpeedX(), calculateSpeedY())
  }

  /**
   * Adds a new entry
   * @param deltaTime the delta time
   */
  fun add(@ms deltaTime: Double, @px distance: Distance) {
    add(deltaTime, distance.x, distance.y)
  }

  fun add(@ms deltaTime: Double, @px deltaX: Double, @px deltaY: Double) {
    if (deltaTime <= 0) {
      return
    }

    //Evict all entries that a too old
    @ms val now = nowMillis()
    entries.removeAll { entry -> now - entry.time > MaxAge }

    entries.add(Entry(now, deltaTime, deltaX / deltaTime, deltaY / deltaTime))
  }

  /**
   * Clears the speed calculation
   */
  fun clear() {
    entries.clear()
  }

  companion object {
    /**
     * The size of the queue
     */
    const val SpeedQueueSize: Int = 15

    /**
     * The maximum age for entries before they are disposed
     */
    const val MaxAge: @ms Int = 400 * 1000
  }
}

/**
 * The calculated speed
 */
data class Speed(
  @px_ms val speedX: Double,
  @px_ms val speedY: Double
)

/**
 * One entry for the mouse speed calculator
 */
private class Entry(
  /**
   * The time when this entry has been created
   */
  @ms
  val time: Double,
  /**
   * The time delta this entry covers
   */
  @ms
  val deltaTime: Double,

  /**
   * The speed on the x axis
   */
  @px_ms
  val speedX: Double,
  /**
   * The speed on the y axis
   */
  @px_ms
  val speedY: Double
) {

  override fun toString(): String {
    return "Entry{time=$time, deltaTime=$deltaTime, speedX=$speedX, speedY=$speedY}"
  }
}
