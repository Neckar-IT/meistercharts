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
package com.meistercharts.custom.rainsensor

import com.meistercharts.animation.Easing
import com.meistercharts.canvas.ChartSupport
import com.meistercharts.canvas.RefreshListener
import com.meistercharts.canvas.animation.ChartAnimation
import com.meistercharts.canvas.animation.PropertyTween
import com.meistercharts.canvas.animation.Tween
import com.meistercharts.canvas.currentFrameTimestamp
import it.neckar.open.kotlin.lang.isCloseTo
import it.neckar.open.unit.other.deg
import it.neckar.open.unit.si.ms

/**
 * Manages the animations for the rain sensor
 */
class RainSensorAnimationManager(val model: RainSensorModel) : RefreshListener {
  /**
   * The animation that opens/closes the window
   */
  var windowOpenCloseAnimation: ChartAnimation? = null


  override fun refresh(chartSupport: ChartSupport, frameTimestamp: Double, refreshDelta: Double) {
    //Animation still running, skip
    windowOpenCloseAnimation?.let { currentAnimation ->
      if (!currentAnimation.disposed) {
        return
      }
    }

    model.nextAction?.let { nextAction ->
      @deg val targetAngle = nextAction.targetAngle
      //Check if an animation is necessary
      if (targetAngle.isCloseTo(model.openAngle, 0.1)) {
        model.openAngle = targetAngle //enforce the exact value
        model.nextAction = null
        return
      }

      val close = Tween(currentFrameTimestamp, openCloseAnimationDuration, Easing.inOutQuad)
      val propertyTween = PropertyTween(model::openAngle, targetAngle, close)

      //Dispose the old tween if exists
      windowOpenCloseAnimation?.dispose()
      windowOpenCloseAnimation = ChartAnimation(propertyTween).also {
        chartSupport.onRefresh(it)
      }

      chartSupport.markAsDirty()
    }
  }

  companion object {
    const val openCloseAnimationDuration: @ms Double = 5000.0
  }
}
