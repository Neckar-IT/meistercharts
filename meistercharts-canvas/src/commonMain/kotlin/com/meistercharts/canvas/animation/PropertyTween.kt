package com.meistercharts.canvas.animation

import it.neckar.open.unit.other.pct
import it.neckar.open.unit.si.ms
import kotlin.reflect.KMutableProperty0

/**
 * Tween for a property
 */
class PropertyTween(
  /**
   * The start value
   */
  val startValue: Double,
  /**
   * The target value
   */
  val targetValue: Double,

  /**
   * The tween that is used to move the property to the target value
   */
  val tween: Tween,

  /**
   * The setter for the interpolated value
   */
  val setter: (Double) -> Unit
) : Animated {

  constructor(
    property: KMutableProperty0<Double>,
    targetValue: Double,
    tween: Tween
  ) : this(property.get(), targetValue, tween, property::set)

  /**
   * Calculates and assigns the new property value
   */
  fun update(timestamp: @ms Double): AnimationState {
    if (tween.isFinished(timestamp)) {
      return AnimationState.Finished
    }

    @pct val interpolatedRatio = tween.interpolate(timestamp)

    val delta = targetValue - startValue
    val updatedValue = startValue + delta * interpolatedRatio

    setter(updatedValue)

    return AnimationState.Active
  }

  override fun animationFrame(frameTimestamp: Double): AnimationState {
    return update(frameTimestamp)
  }
}

/**
 * Animates the given property
 */
fun Tween.animate(property: KMutableProperty0<Double>, to: Double): PropertyTween {
  return PropertyTween(property, to, this)
}
