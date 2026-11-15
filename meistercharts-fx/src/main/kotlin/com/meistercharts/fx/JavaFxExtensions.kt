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
package com.meistercharts.fx

import com.meistercharts.canvas.CanvasRenderingContext
import com.meistercharts.canvas.FillRule
import com.meistercharts.canvas.LineJoin
import com.meistercharts.color.CanvasLinearGradient
import com.meistercharts.color.CanvasPaint
import com.meistercharts.color.CanvasRadialGradient
import it.neckar.geometry.HorizontalAlignment
import it.neckar.geometry.VerticalAlignment
import it.neckar.open.annotations.Allocates
import it.neckar.open.annotations.AllocationCost
import it.neckar.open.annotations.Hot
import javafx.geometry.VPos
import javafx.scene.canvas.GraphicsContext
import javafx.scene.paint.Color
import javafx.scene.paint.CycleMethod
import javafx.scene.paint.LinearGradient
import javafx.scene.paint.Paint
import javafx.scene.paint.RadialGradient
import javafx.scene.paint.Stop
import javafx.scene.shape.ArcType
import javafx.scene.shape.StrokeLineJoin
import javafx.scene.text.TextAlignment



/**
 * Converts the color descriptor to a JavaFX Paint
 */
@Allocates(AllocationCost.Constant)
fun CanvasPaint.toJavaFx(): Paint {
  return when (this) {
    is com.meistercharts.color.Color -> toJavaFx()
    is CanvasLinearGradient -> LinearGradient(
      x0, y0, x1, y1, false, CycleMethod.NO_CYCLE, listOf(
        Stop(0.0, color0.toJavaFx()),
        Stop(1.0, color1.toJavaFx())
      )
    )

    is CanvasRadialGradient -> RadialGradient(
      0.0,
      0.0,
      positionX,
      positionY,
      radius,
      false,
      CycleMethod.NO_CYCLE,
      Stop(0.0, color0.toJavaFx()),
      Stop(1.0, color1.toJavaFx())
    )

    else -> throw UnsupportedOperationException("Unsupported paint <$this>")
  }
}

/**
 * Converts a flat color to a JavaFX color
 */
fun com.meistercharts.color.Color.toJavaFx(): Color = Color.valueOf(this.web)

fun VPos.fromFx(): VerticalAlignment {
  return when (this) {
    VPos.TOP -> VerticalAlignment.Top
    VPos.CENTER -> VerticalAlignment.Center
    VPos.BASELINE -> VerticalAlignment.Baseline
    VPos.BOTTOM -> VerticalAlignment.Bottom
  }
}

fun VerticalAlignment.toFx(): VPos {
  return when (this) {
    VerticalAlignment.Center -> VPos.CENTER
    VerticalAlignment.Baseline -> VPos.BASELINE
    VerticalAlignment.Bottom -> VPos.BOTTOM
    VerticalAlignment.Top -> VPos.TOP
  }
}

fun TextAlignment.fromFx(): HorizontalAlignment {
  return when (this) {
    TextAlignment.LEFT -> HorizontalAlignment.Left
    TextAlignment.RIGHT -> HorizontalAlignment.Right
    TextAlignment.CENTER -> HorizontalAlignment.Center
    else -> throw IllegalArgumentException("Invalid text-alignment $this")
  }
}

fun HorizontalAlignment.toFx(): TextAlignment {
  return when (this) {
    HorizontalAlignment.Left -> TextAlignment.LEFT
    HorizontalAlignment.Right -> TextAlignment.RIGHT
    HorizontalAlignment.Center -> TextAlignment.CENTER
    else -> throw IllegalArgumentException("Invalid text-alignment $this")
  }
}

fun LineJoin.toFx(): StrokeLineJoin {
  return when (this) {
    LineJoin.Miter -> StrokeLineJoin.MITER
    LineJoin.Bevel -> StrokeLineJoin.BEVEL
    LineJoin.Round -> StrokeLineJoin.ROUND
  }
}

fun StrokeLineJoin.fromFx(): LineJoin {
  return when (this) {
    StrokeLineJoin.MITER -> LineJoin.Miter
    StrokeLineJoin.BEVEL -> LineJoin.Bevel
    StrokeLineJoin.ROUND -> LineJoin.Round
  }
}

fun com.meistercharts.canvas.ArcType.toFx(): ArcType? {
  return when (this) {
    com.meistercharts.canvas.ArcType.Open -> ArcType.OPEN
    com.meistercharts.canvas.ArcType.Chord -> ArcType.CHORD
    com.meistercharts.canvas.ArcType.Round -> ArcType.ROUND
  }
}

/**
 * Converts the color to a color descriptor
 */
fun Color.toColor(): com.meistercharts.color.Color {
  return com.meistercharts.color.Color.color(red, green, blue)
}

/**
 * Returns the JavaFX Graphics context
 */
fun CanvasRenderingContext.asFxContext(): GraphicsContext {
  return (this as CanvasRenderingContextFX).context
}

/**
 * Returns the JavaFX fill rule
 */
@Hot
fun FillRule.toFx(): javafx.scene.shape.FillRule {
  return when (this) {
    FillRule.NonZero -> javafx.scene.shape.FillRule.NON_ZERO
    FillRule.EvenOdd -> javafx.scene.shape.FillRule.EVEN_ODD
  }
}
