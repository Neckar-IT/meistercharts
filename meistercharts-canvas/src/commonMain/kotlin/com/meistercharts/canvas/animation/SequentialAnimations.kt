/**
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
