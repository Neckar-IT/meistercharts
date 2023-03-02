package com.meistercharts.canvas.animation

/**
 * Adds several animations sequentially
 */
class SequentialAnimations(
  val animations: List<Animated>
) : Animated {

  init {
    require(animations.isNotEmpty()) { "at least one animation required" }
  }

  internal var currentAnimationIndex = 0
    private set

  override fun animationFrame(frameTimestamp: Double): AnimationState {
    //Search for the correct animation
    while (currentAnimationIndex < animations.size) {
      animateCurrent(frameTimestamp).let {
        if (it == AnimationState.Active) {
          return AnimationState.Active
        }
      }

      currentAnimationIndex++
    }

    return AnimationState.Finished
  }

  /**
   * Animates the current animation.
   * Checks the bounds
   */
  private fun animateCurrent(frameTimestamp: Double): AnimationState {
    return animations.getOrNull(currentAnimationIndex)?.animationFrame(frameTimestamp) ?: AnimationState.Finished
  }
}
