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
package com.meistercharts.design.neckarit

import com.meistercharts.color.Color
import com.meistercharts.animation.Easing
import com.meistercharts.annotations.DomainRelative
import com.meistercharts.canvas.animation.AnimationRepeatType
import com.meistercharts.canvas.animation.Tween
import com.meistercharts.geometry.BezierCurve
import com.meistercharts.geometry.BezierCurveRect
import it.neckar.geometry.Coordinates
import it.neckar.open.time.monotonicMillis
import it.neckar.open.time.monotonicTimeSource
import it.neckar.open.unit.si.ms
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.TimeSource

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
      end = Coordinates(1.00, 1.0 - 0.49 - halfWidthRight3),
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

  var tween0Control0X: Tween = Tween(monotonicMillis(), 5000.milliseconds, Easing.inOut, AnimationRepeatType.RepeatAutoReverse)
  var tween0Control0Y: Tween = Tween(monotonicMillis(), 3000.milliseconds, Easing.inOut, AnimationRepeatType.RepeatAutoReverse)
  var tween0Control1X: Tween = Tween(monotonicMillis(), 4500.milliseconds, Easing.inOut, AnimationRepeatType.RepeatAutoReverse)
  var tween0Control1Y: Tween = Tween(monotonicMillis(), 2800.milliseconds, Easing.inOut, AnimationRepeatType.RepeatAutoReverse)


  var tween1StartX: Tween = Tween.constant(0.0)
  var tween1StartY: Tween = Tween.constant(0.0)
  var tween1EndX: Tween = Tween.constant(0.0)
  var tween1EndY: Tween = Tween.constant(0.0)

  var tween1Control0X: Tween = Tween(monotonicMillis(), 5000.milliseconds, Easing.inOut, AnimationRepeatType.RepeatAutoReverse)
  var tween1Control0Y: Tween = Tween(monotonicMillis(), 3000.milliseconds, Easing.inOut, AnimationRepeatType.RepeatAutoReverse)
  var tween1Control1X: Tween = Tween(monotonicMillis(), 4500.milliseconds, Easing.inOut, AnimationRepeatType.RepeatAutoReverse)
  var tween1Control1Y: Tween = Tween(monotonicMillis(), 2800.milliseconds, Easing.inOut, AnimationRepeatType.RepeatAutoReverse)


  var tween2StartX: Tween = Tween.constant(0.0)
  var tween2StartY: Tween = Tween.constant(0.0)
  var tween2EndX: Tween = Tween.constant(0.0)
  var tween2EndY: Tween = Tween.constant(0.0)

  var tween2Control0X: Tween = Tween(monotonicMillis(), 5000.milliseconds, Easing.inOut, AnimationRepeatType.RepeatAutoReverse)
  var tween2Control0Y: Tween = Tween(monotonicMillis(), 3000.milliseconds, Easing.inOut, AnimationRepeatType.RepeatAutoReverse)
  var tween2Control1X: Tween = Tween(monotonicMillis(), 4500.milliseconds, Easing.inOut, AnimationRepeatType.RepeatAutoReverse)
  var tween2Control1Y: Tween = Tween(monotonicMillis(), 2800.milliseconds, Easing.inOut, AnimationRepeatType.RepeatAutoReverse)


  var tween3StartX: Tween = Tween.constant(0.0)
  var tween3StartY: Tween = Tween.constant(0.0)
  var tween3EndX: Tween = Tween.constant(0.0)
  var tween3EndY: Tween = Tween.constant(0.0)

  var tween3Control0X: Tween = Tween(monotonicMillis(), 5000.milliseconds, Easing.inOut, AnimationRepeatType.RepeatAutoReverse)
  var tween3Control0Y: Tween = Tween(monotonicMillis(), 3000.milliseconds, Easing.inOut, AnimationRepeatType.RepeatAutoReverse)
  var tween3Control1X: Tween = Tween(monotonicMillis(), 4500.milliseconds, Easing.inOut, AnimationRepeatType.RepeatAutoReverse)
  var tween3Control1Y: Tween = Tween(monotonicMillis(), 2800.milliseconds, Easing.inOut, AnimationRepeatType.RepeatAutoReverse)

  /**
   * Uniform movement
   */
  fun configureForUniformMovement() {
    tweenTimeSource = monotonicTimeSource
    lastConfiguration = ::configureForUniformMovement
    tween0Control0X = Tween(monotonicMillis(), 5000.milliseconds, Easing.inOut, AnimationRepeatType.RepeatAutoReverse)
    tween0Control0Y = Tween(monotonicMillis(), 3000.milliseconds, Easing.inOut, AnimationRepeatType.RepeatAutoReverse)
    tween0Control1X = Tween(monotonicMillis(), 4500.milliseconds, Easing.inOut, AnimationRepeatType.RepeatAutoReverse)
    tween0Control1Y = Tween(monotonicMillis(), 2800.milliseconds, Easing.inOut, AnimationRepeatType.RepeatAutoReverse)

    tween1Control0X = Tween(monotonicMillis(), 5000.milliseconds, Easing.inOut, AnimationRepeatType.RepeatAutoReverse)
    tween1Control0Y = Tween(monotonicMillis(), 3000.milliseconds, Easing.inOut, AnimationRepeatType.RepeatAutoReverse)
    tween1Control1X = Tween(monotonicMillis(), 4500.milliseconds, Easing.inOut, AnimationRepeatType.RepeatAutoReverse)
    tween1Control1Y = Tween(monotonicMillis(), 2800.milliseconds, Easing.inOut, AnimationRepeatType.RepeatAutoReverse)

    tween2Control0X = Tween(monotonicMillis(), 5000.milliseconds, Easing.inOut, AnimationRepeatType.RepeatAutoReverse)
    tween2Control0Y = Tween(monotonicMillis(), 3000.milliseconds, Easing.inOut, AnimationRepeatType.RepeatAutoReverse)
    tween2Control1X = Tween(monotonicMillis(), 4500.milliseconds, Easing.inOut, AnimationRepeatType.RepeatAutoReverse)
    tween2Control1Y = Tween(monotonicMillis(), 2800.milliseconds, Easing.inOut, AnimationRepeatType.RepeatAutoReverse)

    tween3Control0X = Tween(monotonicMillis(), 5000.milliseconds, Easing.inOut, AnimationRepeatType.RepeatAutoReverse)
    tween3Control0Y = Tween(monotonicMillis(), 3000.milliseconds, Easing.inOut, AnimationRepeatType.RepeatAutoReverse)
    tween3Control1X = Tween(monotonicMillis(), 4500.milliseconds, Easing.inOut, AnimationRepeatType.RepeatAutoReverse)
    tween3Control1Y = Tween(monotonicMillis(), 2800.milliseconds, Easing.inOut, AnimationRepeatType.RepeatAutoReverse)
  }

  /**
   * The time source the tweens have been started from; demos replace [monotonicTimeSource] on every start.
   */
  private var tweenTimeSource: TimeSource.WithComparableMarks = monotonicTimeSource

  /**
   * The configuration applied last; repeated when [monotonicTimeSource] has been replaced
   */
  private var lastConfiguration: () -> Unit = ::configureForRandomAlsoStartAndEnd

  init {
    configureForRandomAlsoStartAndEnd()
  }

  /**
   * Restarts the tweens when [monotonicTimeSource] has been replaced: [monotonicMillis] restarts at 0 for a new source.
   */
  private fun restartTweensOnNewTimeSource() {
    if (tweenTimeSource !== monotonicTimeSource) {
      lastConfiguration()
    }
  }

  fun configureForRandom() {
    tweenTimeSource = monotonicTimeSource
    lastConfiguration = ::configureForRandom
    tween0Control0X = Tween(monotonicMillis(), 5000.milliseconds, Easing.inOut, AnimationRepeatType.RepeatAutoReverse)
    tween0Control0Y = Tween(monotonicMillis(), 3000.milliseconds, Easing.inOut, AnimationRepeatType.RepeatAutoReverse)
    tween0Control1X = Tween(monotonicMillis(), 4500.milliseconds, Easing.inOut, AnimationRepeatType.RepeatAutoReverse)
    tween0Control1Y = Tween(monotonicMillis(), 2800.milliseconds, Easing.inOut, AnimationRepeatType.RepeatAutoReverse)

    tween1Control0X = Tween(monotonicMillis(), 4200.milliseconds, Easing.inOut, AnimationRepeatType.RepeatAutoReverse)
    tween1Control0Y = Tween(monotonicMillis(), 5300.milliseconds, Easing.inOut, AnimationRepeatType.RepeatAutoReverse)
    tween1Control1X = Tween(monotonicMillis(), 4700.milliseconds, Easing.inOut, AnimationRepeatType.RepeatAutoReverse)
    tween1Control1Y = Tween(monotonicMillis(), 3100.milliseconds, Easing.inOut, AnimationRepeatType.RepeatAutoReverse)

    tween2Control0X = Tween(monotonicMillis(), 2200.milliseconds, Easing.inOut, AnimationRepeatType.RepeatAutoReverse)
    tween2Control0Y = Tween(monotonicMillis(), 4900.milliseconds, Easing.inOut, AnimationRepeatType.RepeatAutoReverse)
    tween2Control1X = Tween(monotonicMillis(), 3500.milliseconds, Easing.inOut, AnimationRepeatType.RepeatAutoReverse)
    tween2Control1Y = Tween(monotonicMillis(), 3800.milliseconds, Easing.inOut, AnimationRepeatType.RepeatAutoReverse)

    tween3Control0X = Tween(monotonicMillis(), 4500.milliseconds, Easing.inOut, AnimationRepeatType.RepeatAutoReverse)
    tween3Control0Y = Tween(monotonicMillis(), 4700.milliseconds, Easing.inOut, AnimationRepeatType.RepeatAutoReverse)
    tween3Control1X = Tween(monotonicMillis(), 3700.milliseconds, Easing.inOut, AnimationRepeatType.RepeatAutoReverse)
    tween3Control1Y = Tween(monotonicMillis(), 4100.milliseconds, Easing.inOut, AnimationRepeatType.RepeatAutoReverse)
  }

  const val controlPointModificationFactor: Double = 0.1
  const val startEndPointModificationFactor: Double = 0.05

  fun configureForRandomAlsoStartAndEnd() {
    configureForRandom()
    lastConfiguration = ::configureForRandomAlsoStartAndEnd

    tween0StartY = Tween(monotonicMillis(), 3700.milliseconds, Easing.inOut, AnimationRepeatType.RepeatAutoReverse)
    tween0EndY = Tween(monotonicMillis(), 3200.milliseconds, Easing.inOut, AnimationRepeatType.RepeatAutoReverse)

    tween1StartY = Tween(monotonicMillis(), 2500.milliseconds, Easing.inOut, AnimationRepeatType.RepeatAutoReverse)
    tween1EndY = Tween(monotonicMillis(), 4200.milliseconds, Easing.inOut, AnimationRepeatType.RepeatAutoReverse)

    tween2StartY = Tween(monotonicMillis(), 3900.milliseconds, Easing.inOut, AnimationRepeatType.RepeatAutoReverse)
    tween2EndY = Tween(monotonicMillis(), 3700.milliseconds, Easing.inOut, AnimationRepeatType.RepeatAutoReverse)

    tween3StartY = Tween(monotonicMillis(), 5100.milliseconds, Easing.inOut, AnimationRepeatType.RepeatAutoReverse)
    tween3EndY = Tween(monotonicMillis(), 4800.milliseconds, Easing.inOut, AnimationRepeatType.RepeatAutoReverse)
  }

  /**
   * Returns the tweened shape 0 for the given frame time
   */
  fun shape0(frameMonotonicMillis: @ms Double): @DomainRelative BezierCurveRect {
    restartTweensOnNewTimeSource()
    val modifierCurve = BezierCurve(
      Coordinates.of(tween0StartX.interpolate(frameMonotonicMillis) * startEndPointModificationFactor, tween0StartY.interpolate(frameMonotonicMillis) * startEndPointModificationFactor),
      Coordinates.of(tween0Control0X.interpolate(frameMonotonicMillis) * controlPointModificationFactor, tween0Control0Y.interpolate(frameMonotonicMillis) * controlPointModificationFactor),
      Coordinates.of(tween0Control1X.interpolate(frameMonotonicMillis) * controlPointModificationFactor, tween0Control1Y.interpolate(frameMonotonicMillis) * controlPointModificationFactor),
      Coordinates.of(tween0EndX.interpolate(frameMonotonicMillis) * startEndPointModificationFactor, tween0EndY.interpolate(frameMonotonicMillis) * startEndPointModificationFactor)
    )

    return shape0 + BezierCurveRect(
      modifierCurve,
      modifierCurve
    )
  }

  /**
   * Returns the tweened shape for the given frame time
   */
  fun shape1(frameMonotonicMillis: @ms Double): @DomainRelative BezierCurveRect {
    restartTweensOnNewTimeSource()
    val modifierCurve = BezierCurve(
      Coordinates.of(tween1StartX.interpolate(frameMonotonicMillis) * startEndPointModificationFactor, tween1StartY.interpolate(frameMonotonicMillis) * startEndPointModificationFactor),
      Coordinates.of(tween1Control0X.interpolate(frameMonotonicMillis) * controlPointModificationFactor, tween1Control0Y.interpolate(frameMonotonicMillis) * controlPointModificationFactor),
      Coordinates.of(tween1Control1X.interpolate(frameMonotonicMillis) * controlPointModificationFactor, tween1Control1Y.interpolate(frameMonotonicMillis) * controlPointModificationFactor),
      Coordinates.of(tween1EndX.interpolate(frameMonotonicMillis) * startEndPointModificationFactor, tween1EndY.interpolate(frameMonotonicMillis) * startEndPointModificationFactor)
    )

    return shape1 + BezierCurveRect(
      modifierCurve,
      modifierCurve
    )
  }

  /**
   * Returns the tweened shape for the given frame time
   */
  fun shape2(frameMonotonicMillis: @ms Double): @DomainRelative BezierCurveRect {
    restartTweensOnNewTimeSource()
    val modifierCurve = BezierCurve(
      Coordinates.of(tween1StartX.interpolate(frameMonotonicMillis) * startEndPointModificationFactor, tween1StartY.interpolate(frameMonotonicMillis) * startEndPointModificationFactor),
      Coordinates.of(tween2Control0X.interpolate(frameMonotonicMillis) * controlPointModificationFactor, tween2Control0Y.interpolate(frameMonotonicMillis) * controlPointModificationFactor),
      Coordinates.of(tween2Control1X.interpolate(frameMonotonicMillis) * controlPointModificationFactor, tween2Control1Y.interpolate(frameMonotonicMillis) * controlPointModificationFactor),
      Coordinates.of(tween1EndX.interpolate(frameMonotonicMillis) * startEndPointModificationFactor, tween1EndY.interpolate(frameMonotonicMillis) * startEndPointModificationFactor)
    )

    return shape2 + BezierCurveRect(
      modifierCurve,
      modifierCurve
    )
  }

  /**
   * Returns the tweened shape for the given frame time
   */
  fun shape3(frameMonotonicMillis: @ms Double): @DomainRelative BezierCurveRect {
    restartTweensOnNewTimeSource()
    val modifierCurve = BezierCurve(
      Coordinates.of(tween1StartX.interpolate(frameMonotonicMillis) * startEndPointModificationFactor, tween1StartY.interpolate(frameMonotonicMillis) * startEndPointModificationFactor),
      Coordinates.of(tween3Control0X.interpolate(frameMonotonicMillis) * controlPointModificationFactor, tween3Control0Y.interpolate(frameMonotonicMillis) * controlPointModificationFactor),
      Coordinates.of(tween3Control1X.interpolate(frameMonotonicMillis) * controlPointModificationFactor, tween3Control1Y.interpolate(frameMonotonicMillis) * controlPointModificationFactor),
      Coordinates.of(tween1EndX.interpolate(frameMonotonicMillis) * startEndPointModificationFactor, tween1EndY.interpolate(frameMonotonicMillis) * startEndPointModificationFactor)
    )

    return shape3 + BezierCurveRect(
      modifierCurve,
      modifierCurve
    )
  }
}
