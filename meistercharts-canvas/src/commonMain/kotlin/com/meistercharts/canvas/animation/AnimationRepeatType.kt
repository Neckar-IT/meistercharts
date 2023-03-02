package com.meistercharts.canvas.animation

/**
 * Describes the repetition of the animation
 */
enum class AnimationRepeatType(val repeating: Boolean) {

  /**
   * The animation is executed exactly once
   */
  Once(false),

  /**
   * Repeats the animation
   */
  Repeat(true),

  /**
   * Repeats the animation - and reverses the animation every second time.
   *
   * This can be used to simulate a back and forth animation/movement
   */
  RepeatAutoReverse(true);
}
