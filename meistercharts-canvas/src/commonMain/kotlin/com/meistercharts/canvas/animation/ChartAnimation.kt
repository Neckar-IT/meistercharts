package com.meistercharts.canvas.animation

import com.meistercharts.canvas.ChartSupport
import com.meistercharts.canvas.RefreshListener
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
) : Disposable, OnDispose, RefreshListener {

  private val disposeSupport: DisposeSupport = DisposeSupport()

  /**
   * Updates the consumer with an updated value
   */
  override fun refresh(chartSupport: ChartSupport, frameTimestamp: Double, refreshDelta: Double) {
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

    chartSupport.markAsDirty()
  }

  /**
   * Unregisters this animation
   */
  private fun unregister(chartSupport: ChartSupport) {
    //Unregister this listener as soon as the animation has been finished
    chartSupport.removeOnRefresh(this)
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


