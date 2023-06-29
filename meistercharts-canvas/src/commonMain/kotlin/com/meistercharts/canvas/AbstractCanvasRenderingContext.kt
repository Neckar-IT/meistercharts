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
package com.meistercharts.canvas

import com.meistercharts.environment
import com.meistercharts.annotations.PhysicalPixel
import com.meistercharts.geometry.Distance
import com.meistercharts.model.Zoom
import it.neckar.open.collections.fastForEach
import it.neckar.open.unit.si.rad

/**
 */
abstract class AbstractCanvasRenderingContext : CanvasRenderingContext {
  override fun applyDefaults() {
    super.applyDefaults()

    resetTransform()
    //Set the scale depending on the device pixel ratio
    scale(environment.devicePixelRatio, environment.devicePixelRatio)
  }

  /**
   * Holds the transformation.
   * It is necessary to hold the transformation because not all browsers (e.g. IE11) provide access to the current matrix
   */
  val currentTransform: CurrentTransform = CurrentTransform()

  /**
   * ATTENTION: This method must be overridden by all implementing classes
   */
  override fun resetTransform() {
    currentTransform.reset()
  }

  /**
   * ATTENTION: This method must be overridden by all implementing classes
   */
  override fun save() {
    currentTransform.save()
  }

  /**
   * ATTENTION: This method must be overridden by all implementing classes
   */
  override fun restore() {
    currentTransform.restore()
  }

  override val debug: DebugConfiguration = DebugConfiguration()

  final override var translation: Distance
    get() = currentTransform.translation.divide(scaleX, scaleY)
    set(value) {
      val deltaX = value.x - currentTransform.translationX
      val deltaY = value.y - currentTransform.translationY

      translate(deltaX, deltaY)
    }

  final override var translationX: Double
    get() = currentTransform.translationX / scaleX
    set(value) {
      val deltaX = value - currentTransform.translationX
      translate(deltaX, 0.0)
    }

  final override var translationY: Double
    get() = currentTransform.translationY / scaleY
    set(value) {
      val deltaY = value - currentTransform.translationY
      translate(0.0, deltaY)
    }

  final override var translationPhysical: Distance
    get() = currentTransform.translation
    set(value) {
      val deltaX = value.x - currentTransform.translationX
      val deltaY = value.y - currentTransform.translationY

      translatePhysical(deltaX, deltaY)
    }

  override val translationPhysicalX: @PhysicalPixel Double
    get() = currentTransform.translationX

  override val translationPhysicalY: @PhysicalPixel Double
    get() = currentTransform.translationY

  override fun calculatePhysicalSnapCorrectionX(translationX: Double, snapX: Boolean): Double {
    if (snapX.not()) {
      return 0.0
    }

    @PhysicalPixel val remainderX = (translationPhysicalX + translationX * scaleX) % 1

    return if (remainderX < 0.5) {
      -remainderX
    } else {
      1 - remainderX
    }
  }

  override fun calculatePhysicalSnapCorrectionY(translationY: Double, snapY: Boolean): Double {
    if (snapY.not()) {
      return 0.0
    }

    @PhysicalPixel val remainderY = (translationPhysicalY + translationY * scaleY) % 1

    return if (remainderY < 0.5) {
      -remainderY
    } else {
      1 - remainderY
    }
  }

  override fun snapPhysicalTranslation(additionalValueX: Double, additionalValueY: Double, snapX: Boolean, snapY: Boolean) {
    if (snapX.not() && snapY.not()) {
      //Do nothing - no snapping at all
      return
    }

    @PhysicalPixel val toTranslateX: Double = if (snapX) {
      additionalValueX + calculatePhysicalSnapCorrectionX(0.0, snapX)
    } else {
      0.0
    }

    @PhysicalPixel val toTranslateY: Double = if (snapY) {
      additionalValueY + calculatePhysicalSnapCorrectionY(0.0, snapY)
    } else {
      0.0
    }

    translatePhysical(toTranslateX, toTranslateY)
  }

  /**
   * ATTENTION: This method must be overridden by all implementing classes
   */
  override fun translate(deltaX: Double, deltaY: Double) {
    currentTransform.translateScaled(deltaX, deltaY)
  }

  /**
   * ATTENTION: This method must be overridden by all implementing classes
   */
  override fun translatePhysical(deltaX: Double, deltaY: Double) {
    currentTransform.translatePhysical(deltaX, deltaY)
  }

  final override var scale: Zoom
    get() = currentTransform.scale
    set(value) {
      val deltaX = value.scaleX / currentTransform.scaleX
      val deltaY = value.scaleY / currentTransform.scaleY

      scale(deltaX, deltaY)
    }

  final override var scaleX: Double
    get() = currentTransform.scaleX
    set(value) {
      val deltaX = value / currentTransform.scaleX
      scale(deltaX, 1.0)
    }

  final override var scaleY: Double
    get() = currentTransform.scaleY
    set(value) {
      val deltaY = value / currentTransform.scaleY
      scale(1.0, deltaY)
    }

  /**
   * ATTENTION: This method must be overridden by all implementing classes
   */
  override fun scale(x: Double, y: Double) {
    currentTransform.scale(x, y)
  }

  /**
   * ATTENTION: This method must be overridden by all implementing classes
   */
  override fun rotateRadians(angleInRadians: @rad Double) {
    currentTransform.rotate(angleInRadians)
  }

  /**
   * The delayed actions that can be executed later
   */
  protected val delayedActions: MutableList<(gc: CanvasRenderingContext) -> Unit> = mutableListOf()

  override fun delayed(delayedAction: (gc: CanvasRenderingContext) -> Unit) {
    delayedActions.add(delayedAction)
  }

  override fun paintDelayed() {
    delayedActions.fastForEach {
      it(this)
    }

    delayedActions.clear()
  }

  override fun cleanDelayed() {
    delayedActions.clear()
  }
}
