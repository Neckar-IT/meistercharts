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

import com.meistercharts.canvas.ChartSupport
import com.meistercharts.canvas.DirtyReason
import com.meistercharts.canvas.ChartRenderLoopListener
import it.neckar.open.dispose.Disposable
import it.neckar.open.dispose.DisposeSupport
import it.neckar.open.dispose.OnDispose
import it.neckar.open.unit.si.ms

/**
 * Represents an animation that is connected to the chart
 *
 */
class ChartAnimation(
  val animated: Animated
) : Disposable, OnDispose, ChartRenderLoopListener {

  private val disposeSupport: DisposeSupport = DisposeSupport()

  /**
   * Updates the consumer with an updated value
   */
  override fun render(chartSupport: ChartSupport, frameTimestamp: Double, refreshDelta: Double) {
    if (disposeSupport.disposed) {
      unregister(chartSupport)
      return
    }

    val animationState = animated.animationFrame(frameTimestamp)

    if (animationState == AnimationState.Finished) {
      dispose()
      unregister(chartSupport)
      return
    }

    //@pct val interpolated = tween.interpolate(frameTimestamp)
    //setter(startValue + (targetValue - startValue) * interpolated)

    chartSupport.markAsDirty(DirtyReason.Animation)
  }

  /**
   * Unregisters this animation
   */
  private fun unregister(chartSupport: ChartSupport) {
    //Unregister this listener as soon as the animation has been finished
    chartSupport.removeOnRender(this)
    return
  }

  override fun dispose() {
    disposeSupport.dispose()
  }

  override fun onDispose(action: () -> Unit) {
    disposeSupport.onDispose(action)
  }

  val disposed: Boolean by disposeSupport::disposed
}

/**
 * Represents an object that is animated
 */
fun interface Animated {
  /**
   * Returns the state of the animation
   */
  fun animationFrame(frameTimestamp: @ms Double): AnimationState
}


/**
 * Represents the result of an animation
 */
enum class AnimationState {
  /**
   * The animation is active
   */
  Active,

  //TODO add some kind of dirty flag(?)

  /**
   * The animation has finished
   */
  Finished;

  companion object {
    fun finishedIf(finished: Boolean): AnimationState {
      return if (finished) Finished else Active
    }
  }
}


