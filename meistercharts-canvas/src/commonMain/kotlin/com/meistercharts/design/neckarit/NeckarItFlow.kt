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
package com.meistercharts.design.neckarit

import com.meistercharts.algorithms.painter.Color
import com.meistercharts.animation.Easing
import com.meistercharts.annotations.DomainRelative
import com.meistercharts.canvas.animation.AnimationRepeatType
import com.meistercharts.canvas.animation.Tween
import com.meistercharts.canvas.geometry.BezierCurve
import com.meistercharts.canvas.geometry.BezierCurveRect
import com.meistercharts.model.Coordinates
import it.neckar.open.time.nowMillis
import it.neckar.open.unit.si.ms

/**
 * Describes the Neckar IT flow shape
 */
object NeckarItFlow {
  val colorShape1: Color = Color.rgba(0, 161, 229, 0.7)
  val colorShape3: Color = Color.rgba(0, 161, 229, 0.7)
  val colorShape2: Color = Color.rgba(197, 229, 235, 0.7)
  val colorShape0: Color = Color.rgba(0, 46, 70, 1.0)


  const val halfWidthLeft0: Double = 0.23 / 2.0
  const val halfWidthRight0: Double = 0.26 / 2.0

  /**
   * The shape0 - without animation
   */
  @DomainRelative
  val shape0: BezierCurveRect = BezierCurveRect(
    BezierCurve(
      start = Coordinates(0.00, 1.0 - 0.44 - halfWidthLeft0),
      control1 = Coordinates(0.23, 1.0 - 0.63 - halfWidthLeft0),
      end = Coordinates(1.00, 1.0 - 0.97 - halfWidthRight0),
      control2 = Coordinates(0.78, 1.0 - 0.51 - halfWidthRight0),
    ),
    BezierCurve(
      start = Coordinates(0.00, 1.0 - 0.44 + halfWidthLeft0),
      control1 = Coordinates(0.23, 1.0 - 0.63 + halfWidthLeft0),
      end = Coordinates(1.00, 1.0 - 0.97 + halfWidthRight0),
      control2 = Coordinates(0.78, 1.0 - 0.51 + halfWidthRight0),
    )
  )

  const val halfWidthLeft1: Double = 0.39 / 2.0
  const val halfWidthRight1: Double = 0.52 / 2.0

  /**
   * The shape1 - without animation
   */
  @DomainRelative
  private val shape1: BezierCurveRect = BezierCurveRect(
    BezierCurve(
      start = Coordinates(0.00, 1.0 - 0.65 - halfWidthLeft1),
      control1 = Coordinates(0.17, 1.0 - 0.81 - halfWidthLeft1),
      end = Coordinates(1.00, 1.0 - 0.69 - halfWidthRight1),
      control2 = Coordinates(0.82, 1.0 - 0.12 - halfWidthRight1),
    ),
    BezierCurve(
      start = Coordinates(0.00, 1.0 - 0.65 + halfWidthLeft1),
      control1 = Coordinates(0.17, 1.0 - 0.81 + halfWidthLeft1),
      end = Coordinates(1.00, 1.0 - 0.69 + halfWidthRight1),
      control2 = Coordinates(0.82, 1.0 - 0.12 + halfWidthRight1),
    )
  )

  const val halfWidthLeft2: Double = 0.23 / 2.0
  const val halfWidthRight2: Double = 0.25 / 2.0

  /**
   * The shape2 - without animation
   */
  @DomainRelative
  private val shape2: BezierCurveRect = BezierCurveRect(
    BezierCurve(
      start = Coordinates(0.00, 1.0 - 0.49 - halfWidthLeft2),
      control1 = Coordinates(0.19, 1.0 - 0.69 - halfWidthLeft2),
      end = Coordinates(1.00, 1.0 - 0.89 - halfWidthRight2),
      control2 = Coordinates(0.76, 1.0 - 0.45 - halfWidthRight2),
    ),
    BezierCurve(
      start = Coordinates(0.00, 1.0 - 0.49 + halfWidthLeft2),
      control1 = Coordinates(0.19, 1.0 - 0.69 + halfWidthLeft2),
      end = Coordinates(1.00, 1.0 - 0.89 + halfWidthRight2),
      control2 = Coordinates(0.76, 1.0 - 0.45 + halfWidthRight2),
    )
  )

  const val halfWidthLeft3: Double = 0.16 / 2.0
  const val halfWidthRight3: Double = 0.19 / 2.0

  /**
   * The shape3 - without animation
   */
  @DomainRelative
  private val shape3: BezierCurveRect = BezierCurveRect(
    BezierCurve(
      start = Coordinates(0.00, 1.0 - 0.85 - halfWidthLeft3),
      control1 = Coordinates(0.16, 1.0 - 1.00 - halfWidthLeft3),
      end = Coordinates(1.00, 01.0 - .49 - halfWidthRight3),
      control2 = Coordinates(0.78, 1.0 - 0.00 - halfWidthRight3)
    ),
    BezierCurve(
      start = Coordinates(0.00, 1.0 - 0.85 + halfWidthLeft3),
      control1 = Coordinates(0.16, 1.0 - 1.00 + halfWidthLeft3),
      end = Coordinates(1.00, 1.0 - 0.49 + halfWidthRight3),
      control2 = Coordinates(0.78, 1.0 - 0.00 + halfWidthRight3)
    )
  )

  var tween0StartX: Tween = Tween.constant(0.0)
  var tween0StartY: Tween = Tween.constant(0.0)
  var tween0EndX: Tween = Tween.constant(0.0)
  var tween0EndY: Tween = Tween.constant(0.0)

  var tween0Control0X: Tween = Tween(nowMillis(), 5000.0, Easing.inOut, AnimationRepeatType.RepeatAutoReverse)
  var tween0Control0Y: Tween = Tween(nowMillis(), 3000.0, Easing.inOut, AnimationRepeatType.RepeatAutoReverse)
  var tween0Control1X: Tween = Tween(nowMillis(), 4500.0, Easing.inOut, AnimationRepeatType.RepeatAutoReverse)
  var tween0Control1Y: Tween = Tween(nowMillis(), 2800.0, Easing.inOut, AnimationRepeatType.RepeatAutoReverse)


  var tween1StartX: Tween = Tween.constant(0.0)
  var tween1StartY: Tween = Tween.constant(0.0)
  var tween1EndX: Tween = Tween.constant(0.0)
  var tween1EndY: Tween = Tween.constant(0.0)

  var tween1Control0X: Tween = Tween(nowMillis(), 5000.0, Easing.inOut, AnimationRepeatType.RepeatAutoReverse)
  var tween1Control0Y: Tween = Tween(nowMillis(), 3000.0, Easing.inOut, AnimationRepeatType.RepeatAutoReverse)
  var tween1Control1X: Tween = Tween(nowMillis(), 4500.0, Easing.inOut, AnimationRepeatType.RepeatAutoReverse)
  var tween1Control1Y: Tween = Tween(nowMillis(), 2800.0, Easing.inOut, AnimationRepeatType.RepeatAutoReverse)


  var tween2StartX: Tween = Tween.constant(0.0)
  var tween2StartY: Tween = Tween.constant(0.0)
  var tween2EndX: Tween = Tween.constant(0.0)
  var tween2EndY: Tween = Tween.constant(0.0)

  var tween2Control0X: Tween = Tween(nowMillis(), 5000.0, Easing.inOut, AnimationRepeatType.RepeatAutoReverse)
  var tween2Control0Y: Tween = Tween(nowMillis(), 3000.0, Easing.inOut, AnimationRepeatType.RepeatAutoReverse)
  var tween2Control1X: Tween = Tween(nowMillis(), 4500.0, Easing.inOut, AnimationRepeatType.RepeatAutoReverse)
  var tween2Control1Y: Tween = Tween(nowMillis(), 2800.0, Easing.inOut, AnimationRepeatType.RepeatAutoReverse)


  var tween3StartX: Tween = Tween.constant(0.0)
  var tween3StartY: Tween = Tween.constant(0.0)
  var tween3EndX: Tween = Tween.constant(0.0)
  var tween3EndY: Tween = Tween.constant(0.0)

  var tween3Control0X: Tween = Tween(nowMillis(), 5000.0, Easing.inOut, AnimationRepeatType.RepeatAutoReverse)
  var tween3Control0Y: Tween = Tween(nowMillis(), 3000.0, Easing.inOut, AnimationRepeatType.RepeatAutoReverse)
  var tween3Control1X: Tween = Tween(nowMillis(), 4500.0, Easing.inOut, AnimationRepeatType.RepeatAutoReverse)
  var tween3Control1Y: Tween = Tween(nowMillis(), 2800.0, Easing.inOut, AnimationRepeatType.RepeatAutoReverse)

  /**
   * Uniform movement
   */
  fun configureForUniformMovement() {
    tween0Control0X = Tween(nowMillis(), 5000.0, Easing.inOut, AnimationRepeatType.RepeatAutoReverse)
    tween0Control0Y = Tween(nowMillis(), 3000.0, Easing.inOut, AnimationRepeatType.RepeatAutoReverse)
    tween0Control1X = Tween(nowMillis(), 4500.0, Easing.inOut, AnimationRepeatType.RepeatAutoReverse)
    tween0Control1Y = Tween(nowMillis(), 2800.0, Easing.inOut, AnimationRepeatType.RepeatAutoReverse)

    tween1Control0X = Tween(nowMillis(), 5000.0, Easing.inOut, AnimationRepeatType.RepeatAutoReverse)
    tween1Control0Y = Tween(nowMillis(), 3000.0, Easing.inOut, AnimationRepeatType.RepeatAutoReverse)
    tween1Control1X = Tween(nowMillis(), 4500.0, Easing.inOut, AnimationRepeatType.RepeatAutoReverse)
    tween1Control1Y = Tween(nowMillis(), 2800.0, Easing.inOut, AnimationRepeatType.RepeatAutoReverse)

    tween2Control0X = Tween(nowMillis(), 5000.0, Easing.inOut, AnimationRepeatType.RepeatAutoReverse)
    tween2Control0Y = Tween(nowMillis(), 3000.0, Easing.inOut, AnimationRepeatType.RepeatAutoReverse)
    tween2Control1X = Tween(nowMillis(), 4500.0, Easing.inOut, AnimationRepeatType.RepeatAutoReverse)
    tween2Control1Y = Tween(nowMillis(), 2800.0, Easing.inOut, AnimationRepeatType.RepeatAutoReverse)

    tween3Control0X = Tween(nowMillis(), 5000.0, Easing.inOut, AnimationRepeatType.RepeatAutoReverse)
    tween3Control0Y = Tween(nowMillis(), 3000.0, Easing.inOut, AnimationRepeatType.RepeatAutoReverse)
    tween3Control1X = Tween(nowMillis(), 4500.0, Easing.inOut, AnimationRepeatType.RepeatAutoReverse)
    tween3Control1Y = Tween(nowMillis(), 2800.0, Easing.inOut, AnimationRepeatType.RepeatAutoReverse)
  }

  init {
    configureForRandomAlsoStartAndEnd()
  }

  fun configureForRandom() {
    tween0Control0X = Tween(nowMillis(), 5000.0, Easing.inOut, AnimationRepeatType.RepeatAutoReverse)
    tween0Control0Y = Tween(nowMillis(), 3000.0, Easing.inOut, AnimationRepeatType.RepeatAutoReverse)
    tween0Control1X = Tween(nowMillis(), 4500.0, Easing.inOut, AnimationRepeatType.RepeatAutoReverse)
    tween0Control1Y = Tween(nowMillis(), 2800.0, Easing.inOut, AnimationRepeatType.RepeatAutoReverse)

    tween1Control0X = Tween(nowMillis(), 4200.0, Easing.inOut, AnimationRepeatType.RepeatAutoReverse)
    tween1Control0Y = Tween(nowMillis(), 5300.0, Easing.inOut, AnimationRepeatType.RepeatAutoReverse)
    tween1Control1X = Tween(nowMillis(), 4700.0, Easing.inOut, AnimationRepeatType.RepeatAutoReverse)
    tween1Control1Y = Tween(nowMillis(), 3100.0, Easing.inOut, AnimationRepeatType.RepeatAutoReverse)

    tween2Control0X = Tween(nowMillis(), 2200.0, Easing.inOut, AnimationRepeatType.RepeatAutoReverse)
    tween2Control0Y = Tween(nowMillis(), 4900.0, Easing.inOut, AnimationRepeatType.RepeatAutoReverse)
    tween2Control1X = Tween(nowMillis(), 3500.0, Easing.inOut, AnimationRepeatType.RepeatAutoReverse)
    tween2Control1Y = Tween(nowMillis(), 3800.0, Easing.inOut, AnimationRepeatType.RepeatAutoReverse)

    tween3Control0X = Tween(nowMillis(), 4500.0, Easing.inOut, AnimationRepeatType.RepeatAutoReverse)
    tween3Control0Y = Tween(nowMillis(), 4700.0, Easing.inOut, AnimationRepeatType.RepeatAutoReverse)
    tween3Control1X = Tween(nowMillis(), 3700.0, Easing.inOut, AnimationRepeatType.RepeatAutoReverse)
    tween3Control1Y = Tween(nowMillis(), 4100.0, Easing.inOut, AnimationRepeatType.RepeatAutoReverse)
  }

  const val controlPointModificationFactor: Double = 0.1
  const val startEndPointModificationFactor: Double = 0.05

  fun configureForRandomAlsoStartAndEnd() {
    configureForRandom()

    tween0StartY = Tween(nowMillis(), 3700.0, Easing.inOut, AnimationRepeatType.RepeatAutoReverse)
    tween0EndY = Tween(nowMillis(), 3200.0, Easing.inOut, AnimationRepeatType.RepeatAutoReverse)

    tween1StartY = Tween(nowMillis(), 2500.0, Easing.inOut, AnimationRepeatType.RepeatAutoReverse)
    tween1EndY = Tween(nowMillis(), 4200.0, Easing.inOut, AnimationRepeatType.RepeatAutoReverse)

    tween2StartY = Tween(nowMillis(), 3900.0, Easing.inOut, AnimationRepeatType.RepeatAutoReverse)
    tween2EndY = Tween(nowMillis(), 3700.0, Easing.inOut, AnimationRepeatType.RepeatAutoReverse)

    tween3StartY = Tween(nowMillis(), 5100.0, Easing.inOut, AnimationRepeatType.RepeatAutoReverse)
    tween3EndY = Tween(nowMillis(), 4800.0, Easing.inOut, AnimationRepeatType.RepeatAutoReverse)
  }

  /**
   * Returns the tweened shape 0 for the given point in time
   */
  fun shape0(now: @ms Double): @DomainRelative BezierCurveRect {
    val modifierCurve = BezierCurve(
      Coordinates.of(tween0StartX.interpolate(now) * startEndPointModificationFactor, tween0StartY.interpolate(now) * startEndPointModificationFactor),
      Coordinates.of(tween0Control0X.interpolate(now) * controlPointModificationFactor, tween0Control0Y.interpolate(now) * controlPointModificationFactor),
      Coordinates.of(tween0Control1X.interpolate(now) * controlPointModificationFactor, tween0Control1Y.interpolate(now) * controlPointModificationFactor),
      Coordinates.of(tween0EndX.interpolate(now) * startEndPointModificationFactor, tween0EndY.interpolate(now) * startEndPointModificationFactor)
    )

    return shape0 + BezierCurveRect(
      modifierCurve,
      modifierCurve
    )
  }

  /**
   * Returns the tweened shape for the given point in time
   */
  fun shape1(now: @ms Double): @DomainRelative BezierCurveRect {
    val modifierCurve = BezierCurve(
      Coordinates.of(tween1StartX.interpolate(now) * startEndPointModificationFactor, tween1StartY.interpolate(now) * startEndPointModificationFactor),
      Coordinates.of(tween1Control0X.interpolate(now) * controlPointModificationFactor, tween1Control0Y.interpolate(now) * controlPointModificationFactor),
      Coordinates.of(tween1Control1X.interpolate(now) * controlPointModificationFactor, tween1Control1Y.interpolate(now) * controlPointModificationFactor),
      Coordinates.of(tween1EndX.interpolate(now) * startEndPointModificationFactor, tween1EndY.interpolate(now) * startEndPointModificationFactor)
    )

    return shape1 + BezierCurveRect(
      modifierCurve,
      modifierCurve
    )
  }

  /**
   * Returns the tweened shape for the given point in time
   */
  fun shape2(now: @ms Double): @DomainRelative BezierCurveRect {
    val modifierCurve = BezierCurve(
      Coordinates.of(tween1StartX.interpolate(now) * startEndPointModificationFactor, tween1StartY.interpolate(now) * startEndPointModificationFactor),
      Coordinates.of(tween2Control0X.interpolate(now) * controlPointModificationFactor, tween2Control0Y.interpolate(now) * controlPointModificationFactor),
      Coordinates.of(tween2Control1X.interpolate(now) * controlPointModificationFactor, tween2Control1Y.interpolate(now) * controlPointModificationFactor),
      Coordinates.of(tween1EndX.interpolate(now) * startEndPointModificationFactor, tween1EndY.interpolate(now) * startEndPointModificationFactor)
    )

    return shape2 + BezierCurveRect(
      modifierCurve,
      modifierCurve
    )
  }

  /**
   * Returns the tweened shape for the given point in time
   */
  fun shape3(now: @ms Double): @DomainRelative BezierCurveRect {
    val modifierCurve = BezierCurve(
      Coordinates.of(tween1StartX.interpolate(now) * startEndPointModificationFactor, tween1StartY.interpolate(now) * startEndPointModificationFactor),
      Coordinates.of(tween3Control0X.interpolate(now) * controlPointModificationFactor, tween3Control0Y.interpolate(now) * controlPointModificationFactor),
      Coordinates.of(tween3Control1X.interpolate(now) * controlPointModificationFactor, tween3Control1Y.interpolate(now) * controlPointModificationFactor),
      Coordinates.of(tween1EndX.interpolate(now) * startEndPointModificationFactor, tween1EndY.interpolate(now) * startEndPointModificationFactor)
    )

    return shape3 + BezierCurveRect(
      modifierCurve,
      modifierCurve
    )
  }
}
