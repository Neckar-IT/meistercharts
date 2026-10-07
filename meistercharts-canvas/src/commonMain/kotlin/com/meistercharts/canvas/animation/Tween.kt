/*
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
package com.meistercharts.canvas.animation

import com.meistercharts.animation.Easing
import it.neckar.open.unit.other.pct
import it.neckar.open.unit.si.ms
import kotlin.time.Duration

/**
 * Supports tweening (generating in-between values).
 *
 * Every time is a value of [it.neckar.open.time.monotonicMillis], so a wall clock adjustment leaves the progress unchanged.
 * The times are plain Doubles: a tween is evaluated on every frame, and [Duration] arithmetic allocates on Kotlin/JS.
 */
data class Tween(
  /**
   * The time when the tween has started
   */
  val start: @ms Double,

  /**
   * The tween definition
   */
  val definition: TweenDefinition,
) {
  /**
   * Inline constructor
   */
  constructor(
    start: @ms Double,
    duration: Duration,
    interpolator: Easing = Easing.linear,
    repeatType: AnimationRepeatType = AnimationRepeatType.Once,
  ) : this(start, TweenDefinition(duration, interpolator, repeatType))

  //Plain getters: a property reference delegate allocates on every read on Kotlin/JS
  val duration: Duration
    get() = definition.duration

  val interpolator: Easing
    get() = definition.interpolator

  val repeatType: AnimationRepeatType
    get() = definition.repeatType

  /**
   * Returns the end time - or null if [repeatType] is set to a repeating value
   */
  val end: @ms Double?
    get() {
      if (repeatType.repeating) {
        return null
      }
      return start + definition.durationMillis
    }

  /**
   * Returns the elapsed time.
   *
   * Just uses the real delta. Does *not* use [repeatType].
   */
  fun elapsedTime(frameMonotonicMillis: @ms Double): @ms Double {
    return frameMonotonicMillis - start
  }

  /**
   * Returns true if the tween has been finished.
   * Always returns false if the [repeatType] is repeating
   */
  fun isFinished(frameMonotonicMillis: @ms Double): Boolean {
    if (repeatType.repeating) {
      return false
    }
    return start + definition.durationMillis < frameMonotonicMillis
  }

  /**
   * Returns the interpolated value for the given frame time
   */
  fun interpolate(frameMonotonicMillis: @ms Double): @pct Double {
    @pct val elapsedRatio = elapsedRatioForTime(frameMonotonicMillis)
    return interpolator(elapsedRatio)
  }

  /**
   * Returns the elapsed ratio for the given frame time
   */
  fun elapsedRatioForTime(frameMonotonicMillis: @ms Double): @pct Double {
    return definition.elapsedRatioForDuration(elapsedTime(frameMonotonicMillis))
  }

  companion object {
    /**
     * A tween that always returns [valueToReturn]
     */
    fun constant(valueToReturn: @pct Double): Tween {
      return Tween(0.0, TweenDefinition(Duration.ZERO, { valueToReturn }, AnimationRepeatType.Repeat))
    }
  }
}
