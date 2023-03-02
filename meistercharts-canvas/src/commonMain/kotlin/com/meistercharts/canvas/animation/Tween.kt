package com.meistercharts.canvas.animation

import com.meistercharts.animation.Easing
import it.neckar.open.unit.other.pct
import it.neckar.open.unit.si.ms

/**
 * Supports tweening (generating in-between values)
 */
data class Tween(
  /**
   * The time when the tween has started
   */
  val startTime: @ms Double,

  /**
   * The tween definition
   */
  val definition: TweenDefinition,
) {

  /**
   * Inline constructor
   */
  constructor(
    startTime: @ms Double,

    duration: @ms Double,
    interpolator: Easing = Easing.linear,

    repeatType: AnimationRepeatType = AnimationRepeatType.Once
  ) : this(startTime, TweenDefinition(duration, interpolator, repeatType))

  val duration: @ms Double by definition::duration
  val interpolator: Easing by definition::interpolator
  val repeatType: AnimationRepeatType by definition::repeatType

  /**
   * Returns the end time - or null if [repeatType] is set to a repeating value
   */
  val endTime: @ms Double?
    get() {
      if (repeatType.repeating) {
        return null
      }

      return startTime + duration
    }

  /**
   * Returns the elapsed time since start.
   *
   * Just uses the real delta. Does *not* use [repeatType].
   */
  fun elapsedTime(timestamp: @ms Double): @ms Double = timestamp - startTime

  /**
   * Returns true if the tween has been finished.
   * Always returns false if the [repeatType] is repeating
   */
  fun isFinished(timestamp: @ms Double): Boolean {
    return endTime?.let {
      it < timestamp
    } ?: false
  }

  /**
   * Returns the interpolated value for the given time stamp
   */
  fun interpolate(timestamp: @ms Double): @pct Double {
    @pct val elapsedRatio = elapsedRatioForTime(timestamp)

    return interpolator(elapsedRatio)
  }

  /**
   * Returns the elapsed ratio for the given timestamp
   */
  fun elapsedRatioForTime(timestamp: @ms Double): @pct Double {
    //The elapsed time
    @ms val elapsedTime = elapsedTime(timestamp)

    return definition.elapsedRatioForDuration(elapsedTime)
  }

  companion object {
    fun constant(valueToReturn: @pct Double): Tween {
      return Tween(0.0, TweenDefinition(0.0, { valueToReturn }, AnimationRepeatType.Repeat))
    }
  }
}
