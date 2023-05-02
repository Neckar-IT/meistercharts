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
package com.meistercharts.demo.elevator.gestalt

import com.meistercharts.animation.Easing
import it.neckar.open.unit.number.Abs
import com.meistercharts.canvas.ChartSupport
import com.meistercharts.canvas.RefreshListener
import com.meistercharts.canvas.animation.ChartAnimation
import com.meistercharts.canvas.animation.PropertyTween
import com.meistercharts.canvas.animation.SequentialAnimations
import com.meistercharts.canvas.animation.Tween
import com.meistercharts.canvas.currentFrameTimestamp
import it.neckar.open.kotlin.lang.abs
import it.neckar.open.kotlin.lang.isCloseTo
import it.neckar.open.kotlin.lang.random
import it.neckar.open.time.nowMillis
import it.neckar.open.unit.si.ms
import kotlin.math.sign

/**
 * Manages the elevator animations
 */
class ElevatorAnimationManager(
  val model: ElevatorModel
) : RefreshListener {
  /**
   * The elevator timeline that moves the elevator
   */
  var elevatorPropertyTween: ChartAnimation? = null

  /**
   * The last time when a floor had been scheduled
   */
  private var lastManuallyScheduledTime: @ms Double = 0.0
  private var lastAnimationFinishedTime: @ms Double = 0.0

  /**
   * Only for *manual* scheduling
   */
  fun pressedFloorButton(floor: @Floor Int) {
    model.addRequestedFloor(floor)
    lastManuallyScheduledTime = nowMillis()
  }

  override fun refresh(chartSupport: ChartSupport, frameTimestamp: Double, refreshDelta: Double) {
    //Animation still running, skip
    elevatorPropertyTween?.let { currentTween ->
      if (!currentTween.disposed) {
        return
      }
    }

    //No animation running, checked for scheduled floor
    model.nextScheduledFloor?.let { nextScheduledFloor ->
      //no animation active, activate next animation
      moveToFloor(nextScheduledFloor, chartSupport)
      return
    }

    //check for automatic animation, if there hasn't be any interaction for a given time
    if (frameTimestamp - lastManuallyScheduledTime > 7000.0) {
      if (frameTimestamp - lastAnimationFinishedTime > 800.0) {
        //Wait a little since the last stop
        val nextRandomFloor = random.nextInt(model.floorRange.numberOfFloors)
        model.addRequestedFloor(nextRandomFloor)
        moveToFloor(nextRandomFloor, chartSupport)
        return
      }
    }
  }

  private fun moveToFloor(targetFloor: @Floor Int, chartSupport: ChartSupport) {
    model.elevatorTarget = targetFloor

    if (model.elevatorLocation.isCloseTo(targetFloor.toDouble())) {
      //Already on correct floor
      return
    }

    //
    //Constants
    //

    //How long to pass one floor at full speed
    @ms val durationPerFloor = 1000.0

    //How long to accelerate
    @Floor val accelerationDistance = 0.25
    @ms val accelerationTime = durationPerFloor * accelerationDistance * 3 //Acceleration takes longer

    //How long to decelerate
    @Floor val decelerationDistance = 0.25
    @ms val decelerationTime = durationPerFloor * decelerationDistance * 3 //Deceleration takes longer

    //
    // Calculations
    //

    //The total distance that will be covered
    @Floor val distance = targetFloor - model.elevatorLocation //usually a multiple of 1
    val directionSign = distance.sign

    @Abs @Floor val distanceAbs = distance.abs()

    @Abs @Floor val fullSpeedDistanceAbs = distanceAbs.minus(accelerationDistance).minus(decelerationDistance).coerceAtLeast(0.0)
    @ms val fullSpeedTime = durationPerFloor * fullSpeedDistanceAbs


    //The percentage of the complete animation
    val accelerationTween = Tween(currentFrameTimestamp, accelerationTime, Easing.incoming)
    val fullSpeedTween = Tween(accelerationTween.endTime!!, fullSpeedTime, Easing.linear)
    val decelerationTween = Tween(fullSpeedTween.endTime!!, decelerationTime, Easing.out)


    @Floor val startLocation = model.elevatorLocation
    @Floor val afterAccelerationLocation = model.elevatorLocation + accelerationDistance * directionSign
    @Floor val beforeDecelerationLocation = model.elevatorLocation + (accelerationDistance + fullSpeedDistanceAbs) * directionSign
    @Floor val targetLocation = targetFloor.toDouble()

    val propertyTweenAcceleration = PropertyTween(startLocation, afterAccelerationLocation, accelerationTween) { model::elevatorLocation.set(it) }
    val propertyTweenFullSpeed = PropertyTween(afterAccelerationLocation, beforeDecelerationLocation, fullSpeedTween) { model::elevatorLocation.set(it) }
    val propertyTweenDeceleration = PropertyTween(beforeDecelerationLocation, targetLocation, decelerationTween) { model::elevatorLocation.set(it) }


    val sequence = SequentialAnimations(
      listOf(
        //acceleration
        propertyTweenAcceleration,

        //full speed
        propertyTweenFullSpeed,

        //deceleration
        propertyTweenDeceleration,
      )
    )

    elevatorPropertyTween?.dispose()

    elevatorPropertyTween = ChartAnimation(sequence).also {
      chartSupport.onRefresh(it)
      it.onDispose {
        //Reached the requested floor
        model.removeRequestedFloor(targetFloor)
        lastAnimationFinishedTime = currentFrameTimestamp
      }
    }

    chartSupport.markAsDirty()
  }
}
