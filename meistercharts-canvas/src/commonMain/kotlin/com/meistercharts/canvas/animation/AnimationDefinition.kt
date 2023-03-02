package com.meistercharts.canvas.animation

import it.neckar.open.unit.si.ms

/**
 * Describes an animation
 */
interface AnimationDefinition {
  /**
   * The duration of the tween (e.g. 1_000 ms)
   */
  val duration: @ms Double

  /**
   * The type of the animation
   */
  val repeatType: AnimationRepeatType
}


/**
 * A group of animations
 */
class AnimationGroupDefinition(

) : AnimationDefinition {
  override val duration: Double
    get() = 0.0

  override
  val repeatType: AnimationRepeatType
    get() = AnimationRepeatType.Once
}

