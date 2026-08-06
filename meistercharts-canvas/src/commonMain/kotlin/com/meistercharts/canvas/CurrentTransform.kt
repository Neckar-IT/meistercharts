/*
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

import com.meistercharts.annotations.PhysicalPixel
import com.meistercharts.geometry.Matrix
import it.neckar.geometry.Distance
import com.meistercharts.model.Zoom
import it.neckar.open.annotations.Allocates
import it.neckar.open.annotations.AllocationCost
import it.neckar.open.annotations.Hot
import it.neckar.open.annotations.HotAllocation
import it.neckar.open.unit.si.rad

/**
 * Holds the transformation of the canvas rendering context
 */
class CurrentTransform {
  /**
   * Describes which value in the stack is used
   */
  var stackDepth: Int = 0
    private set

  val matrices: Array<Matrix> = Array(SaveRestoreStackSize) { Matrix() }

  /**
   * The current matrix
   */
  val matrix: Matrix
    get() = matrices[stackDepth]

  /**
   * Returns the current translation - does *not* include the scale
   */
  @Allocates(AllocationCost.Constant)
  var translation: @PhysicalPixel Distance
    get() = Distance.of(matrix.tx, matrix.ty)
    set(value) {
      matrix.tx = value.x
      matrix.ty = value.y
    }

  /**
   * Returns the current translation x - does *not* include the scale
   */
  var translationX: @PhysicalPixel Double
    get() = matrix.tx
    set(value) {
      matrix.tx = value
    }

  /**
   * Returns the current translation y - does *not* include the scale
   */
  var translationY: @PhysicalPixel Double
    get() = matrix.ty
    set(value) {
      matrix.ty = value
    }

  @Allocates(AllocationCost.Constant)
  var scale: Zoom
    get() = Zoom.of(matrix.a, matrix.d)
    set(value) {
      //TODO does *not* work with rotation!!!
      matrix.a = value.scaleX
      matrix.d = value.scaleY
    }

  var scaleX: Double
    get() = matrix.a
    set(value) {
      //TODO does *not* work with rotation!!!
      matrix.a = value
    }

  var scaleY: Double
    get() = matrix.d
    set(value) {
      //TODO does *not* work with rotation!!!
      matrix.d = value
    }

  @Hot
  fun rotate(theta: @rad Double) {
    @HotAllocation("Only on gc.rotate calls - one temporary Matrix per rotation; axis titles rotate twice per frame per vertical axis. Fix candidate: prerotate in place without the temp Matrix")
    matrix.prerotate(theta)
  }

  /**
   * Resets the current translation, scale and rotation.
   */
  fun reset() {
    matrix.reset()
  }

  /**
   * Saves the current translation, scale and rotation.
   *
   * They can be restored with [restore]
   */
  fun save() {
    stackDepth++
    check(stackDepth < SaveRestoreStackSize) {
      "Saved too deep:: $stackDepth"
    }

    //copy the values from the stack depth below
    matrix.copyFrom(matrices[stackDepth - 1])
  }

  /**
   * Restores a previously saved translation, scale and rotation.
   *
   * You must have called [save] before calling this function.
   */
  fun restore() {
    stackDepth--
    check(stackDepth >= 0) {
      "Invalid stack depth: $stackDepth"
    }
  }

  /**
   * Translate by the given amount
   *
   * ATTENTION: Scale is respected!
   */
  @Hot
  fun translateScaled(deltaX: Double, deltaY: Double) {
    if (deltaX != 0.0) {
      translationX += deltaX * scaleX
    }
    if (deltaY != 0.0) {
      translationY += deltaY * scaleY
    }
  }

  /**
   * Translates by the given amount - does *not* respect the current scale
   */
  @Hot
  fun translatePhysical(deltaX: @PhysicalPixel Double, deltaY: @PhysicalPixel Double) {
    translationX += deltaX
    translationY += deltaY
  }

  /**
   * Sets the translation in physical pixels!.
   *
   * ATTENTION: Does *not* apply the current scale!!!
   */
  fun setTranslatePhysical(newTranslationX: @PhysicalPixel Double, newTranslationY: @PhysicalPixel Double) {
    translationX = newTranslationX
    translationY = newTranslationY
  }

  fun scale(x: Double, y: Double) {
    scaleX *= x
    scaleY *= y
  }

  companion object {
    /**
     * The size of the save/restore stack
     */
    const val SaveRestoreStackSize: Int = 10
  }
}
