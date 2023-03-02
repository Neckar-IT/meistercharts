package com.meistercharts.canvas.animation

import com.meistercharts.animation.Easing
import it.neckar.open.unit.other.pct
import it.neckar.open.unit.si.ms

/**
 * Describes an abstract tween. Is *not* runnable
 */
data class TweenDefinition(
  /**
   * The duration of the tween (e.g. 1_000 ms)
   */
  val duration: @ms Double,

  /**
   * The interpolator that is used to interpolate the values
   */
  val interpolator: Easing = Easing.linear,

  /**
   * The type of the animation
   */
  val repeatType: AnimationRepeatType = AnimationRepeatType.Once
) {

  /**
   * Realizes the definition
   */
  fun realize(startTime: @ms Double): Tween {
    return Tween(startTime, this)
  }

  /**
   * Returns the elapsed ratio for the given elapsed time (since the start of the animation)
   */
  fun elapsedRatioForDuration(elapsedTime: @ms Double): @pct Double {
    return when (repeatType) {
      AnimationRepeatType.Once -> {
        (elapsedTime / duration).coerceIn(0.0, 1.0)
      }
      AnimationRepeatType.Repeat -> {
        //Repeat the animation - use module to find the relative elapsed time
        @ms val relativeElapsedTime = elapsedTime % duration
        (relativeElapsedTime / duration).coerceIn(0.0, 1.0)
      }

      AnimationRepeatType.RepeatAutoReverse -> {
        //Repeat the animation - use module to find the relative elapsed time
        //Calculate with duplicate duration to handle auto reverse
        @ms val relativeElapsedTime = elapsedTime % (duration * 2) //0..(duration*2)

        val fixedElapsedTime = if (relativeElapsedTime <= duration) {
          relativeElapsedTime
        } else {
          //revert
          2 * duration - relativeElapsedTime
        }

        (fixedElapsedTime / duration).coerceIn(0.0, 1.0)
      }
    }
  }

}
