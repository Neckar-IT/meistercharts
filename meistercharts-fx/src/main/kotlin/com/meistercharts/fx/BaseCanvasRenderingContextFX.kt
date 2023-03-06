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
package com.meistercharts.fx

import com.meistercharts.algorithms.painter.CanvasLinearGradient
import com.meistercharts.algorithms.painter.CanvasPaint
import com.meistercharts.algorithms.painter.CanvasRadialGradient
import com.meistercharts.annotations.PhysicalPixel
import com.meistercharts.annotations.Window
import com.meistercharts.annotations.Zoomed
import com.meistercharts.canvas.AbstractCanvasRenderingContext
import com.meistercharts.canvas.Canvas
import com.meistercharts.canvas.CanvasRenderingContext
import com.meistercharts.canvas.CanvasStringShortener
import com.meistercharts.canvas.DebugFeature
import com.meistercharts.canvas.FontDescriptor
import com.meistercharts.canvas.Image
import com.meistercharts.canvas.LineJoin
import com.meistercharts.canvas.calculateOffsetXForGap
import com.meistercharts.canvas.calculateOffsetYForGap
import com.meistercharts.canvas.saved
import com.meistercharts.fx.CanvasFX
import com.meistercharts.fx.CanvasRenderingContextFX
import com.meistercharts.fx.ColorCache
import com.meistercharts.fx.ShadowFxCache
import com.meistercharts.fx.font.FontMetricsCacheFX
import com.meistercharts.fx.font.NativeFontMetricsCache
import com.meistercharts.fx.font.TextBoundsFX
import com.meistercharts.fx.font.toFont
import com.meistercharts.fx.font.toFontDescriptor
import com.meistercharts.fx.screenshot
import com.meistercharts.fx.size
import com.meistercharts.model.Direction
import com.meistercharts.model.Distance
import com.meistercharts.model.HorizontalAlignment
import com.meistercharts.model.Rectangle
import com.meistercharts.model.Size
import com.meistercharts.model.VerticalAlignment
import com.meistercharts.model.Zoom
import it.neckar.open.kotlin.lang.isPositiveOrZero
import it.neckar.open.kotlin.lang.round
import it.neckar.open.unit.number.MayBeNegative
import it.neckar.open.unit.number.Positive
import it.neckar.open.unit.number.PositiveOrZero
import it.neckar.open.unit.other.px
import it.neckar.open.unit.si.rad
import com.sun.javafx.tk.FontMetrics
import javafx.geometry.VPos
import javafx.scene.canvas.GraphicsContext
import javafx.scene.paint.Color
import javafx.scene.paint.CycleMethod
import javafx.scene.paint.ImagePattern
import javafx.scene.paint.LinearGradient
import javafx.scene.paint.Paint
import javafx.scene.paint.RadialGradient
import javafx.scene.paint.Stop
import javafx.scene.shape.ArcType
import javafx.scene.shape.StrokeLineCap
import javafx.scene.shape.StrokeLineJoin
import javafx.scene.text.TextAlignment
import javafx.scene.transform.Affine
import kotlin.math.PI
import kotlin.math.min

private const val PLAUSIBILITY_CHECK_ENABLED: Boolean = false

/**
 * Base class for a fx canvas rendering context.
 * This class is required to be able to support a "simple" rendering context without an fx canvas
 *
 */
abstract class BaseCanvasRenderingContextFX : AbstractCanvasRenderingContext() {
  abstract val context: GraphicsContext

  var fill: Paint?
    get() = context.fill
    set(value) {
      context.fill = value
    }

  var stroke: Paint?
    get() = context.stroke
    set(value) {
      context.stroke = value
    }

  override var globalAlpha: Double
    get() = context.globalAlpha
    set(value) {
      context.globalAlpha = value
    }

  override var font: FontDescriptor
    set(value) {
      context.font = value.toFont()
    }
    get() {
      return context.font.toFontDescriptor()
    }

  override fun shadow(color: com.meistercharts.algorithms.painter.Color, blurRadius: Double, offsetX: Double, offsetY: Double) {
    val shadow = ShadowFxCache.shadow(blurRadius, offsetX, offsetY, color)
    context.setEffect(shadow)
  }

  override fun clearShadow() {
    context.setEffect(null)
  }

  override fun strokeStyle(color: CanvasPaint) {
    context.stroke = ColorCache.getPaint(color)
  }

  override fun fillStyle(color: CanvasPaint) {
    context.fill = ColorCache.getPaint(color)
  }

  override fun setLineDash(vararg dashes: Double) {
    context.setLineDashes(*dashes)
  }

  override fun strokeLine(startX: Double, startY: Double, endX: Double, endY: Double) {
    if (PLAUSIBILITY_CHECK_ENABLED) {
      requirePlausiblePaintingValues(startX, startY, endX, endY)
    }

    context.lineCap = StrokeLineCap.BUTT
    context.strokeLine(startX, startY, endX, endY)
  }

  private fun requirePlausiblePaintingValues(value0: Double, value1: Double, value2: Double, value3: Double) {
    requirePlausiblePaintingValue(value0)
    requirePlausiblePaintingValue(value1)
    requirePlausiblePaintingValue(value2)
    requirePlausiblePaintingValue(value3)
  }

  private fun requirePlausiblePaintingValue(value: Double) {
    require(value in -10_000.0..10_000.0) {
      "Unplausible value: $value"
    }
  }

  override fun fillRectInternal(x: Double, y: Double, width: Double, height: Double) {
    context.fillRect(x, y, width, height)
  }

  /**
   * This object is used to avoid unnecessary object creations. Do *NOT* modify the object
   */
  private val emptyAffine = Affine()

  override fun resetTransform() {
    super.resetTransform()
    context.transform = emptyAffine
  }

  override fun strokeRectInternal(x: Double, y: Double, width: Double, height: Double) {
    context.strokeRect(x, y, width, height)
  }

  override fun strokeOvalOrigin(x: Double, y: Double, width: Double, height: Double) {
    context.strokeOval(x, y, width, height)
  }

  override fun fillOvalOrigin(x: Double, y: Double, width: Double, height: Double) {
    context.fillOval(x, y, width, height)
  }

  override fun ovalCenter(centerX: Double, centerY: Double, width: Double, height: Double) {
    context.moveTo(centerX + width / 2.0, centerY)
    context.arc(centerX, centerY, width / 2.0, height / 2.0, 0.0, -360.0)
  }

  override fun strokeOvalCenter(x: Double, y: Double, width: Double, height: Double) {
    context.strokeOval(x - width / 2.0, y - height / 2.0, width, height)
  }

  override fun fillOvalCenter(x: Double, y: Double, width: Double, height: Double) {
    context.fillOval(x - width / 2.0, y - height / 2.0, width, height)
  }

  override fun strokeArcCenter(@Window x: Double, @Window y: Double, @Zoomed radius: Double, @rad startAngle: Double, @rad arcExtent: Double, arcType: com.meistercharts.canvas.ArcType) {
    context.lineCap = StrokeLineCap.BUTT
    context.strokeArc(x - radius, y - radius, radius * 2, radius * 2, -Math.toDegrees(startAngle), -Math.toDegrees(arcExtent), arcType.toFx())
  }

  override fun fillArcCenter(@Window x: Double, @Window y: Double, @Zoomed radius: Double, @rad startAngle: Double, @rad arcExtent: Double, arcType: com.meistercharts.canvas.ArcType) {
    context.lineCap = StrokeLineCap.BUTT
    context.fillArc(x - radius, y - radius, radius * 2, radius * 2, -Math.toDegrees(startAngle), -Math.toDegrees(arcExtent), arcType.toFx())
  }

  override fun beginPath() {
    context.beginPath()
  }

  override fun closePath() {
    context.closePath()
  }

  override fun moveTo(x: Double, y: Double) {
    context.moveTo(x, y)
  }

  override fun lineTo(x: Double, y: Double) {
    context.lineTo(x, y)
  }

  override fun bezierCurveTo(controlX1: Double, controlY1: Double, controlX2: Double, controlY2: Double, x2: Double, y2: Double) {
    context.bezierCurveTo(controlX1, controlY1, controlX2, controlY2, x2, y2)
  }

  override fun quadraticCurveTo(controlX: Double, controlY: Double, x: Double, y: Double) {
    context.quadraticCurveTo(controlX, controlY, x, y)
  }

  override fun arcTo(controlX: Double, controlY: Double, x: Double, y: Double, radius: @PositiveOrZero Double) {
    require(radius.isPositiveOrZero()) { "Radius must be >= 0 but was <$radius>" }

    context.arcTo(controlX, controlY, x, y, radius)
  }

  override fun arcCenter(centerX: Double, centerY: Double, radius: Double, startAngle: @rad Double, extend: @rad Double) {
    context.arc(centerX, centerY, radius, radius, -Math.toDegrees(startAngle), -Math.toDegrees(extend))
  }

  override fun stroke() {
    context.stroke()
  }

  override fun fill() {
    context.fill()
  }

  override fun translate(deltaX: Double, deltaY: Double) {
    super.translate(deltaX, deltaY)
    context.translate(deltaX, deltaY)
  }

  override fun translatePhysical(deltaX: Double, deltaY: Double) {
    super.translatePhysical(deltaX, deltaY)
    context.translate(deltaX / scaleX, deltaY / scaleY)
  }

  override fun rotateRadians(@rad angleInRadians: Double) {
    super.rotateRadians(angleInRadians)
    context.rotate(angleInRadians * 180.0 / PI)
  }

  override fun scale(x: Double, y: Double) {
    super.scale(x, y)
    context.scale(x, y)
  }

  override var lineWidth: Double
    get() = context.lineWidth
    set(value) {
      context.lineWidth = value
    }

  var textBaseline: VerticalAlignment
    get() = context.textBaseline.fromFx()
    set(value) {
      context.textBaseline = value.toFx()
    }

  var horizontalAlignment: HorizontalAlignment
    get() = context.textAlign.fromFx()
    set(value) {
      context.textAlign = value.toFx()
    }

  override fun fillText(
    text: String,
    x: Double,
    y: Double,
    anchorDirection: Direction,
    gapHorizontal: Double,
    gapVertical: Double,
    maxWidth: Double?,
    maxHeight: Double?,
    stringShortener: CanvasStringShortener,
  ) {
    if (maxHeight != null && maxHeight <= 0.0) {
      //No max height provided, return immediately
      return
    }
    if (maxWidth != null && maxWidth <= 0.0) {
      //No max width provided, return immediately
      return
    }

    if (maxHeight != null && maxHeight < getFontMetrics().totalHeight) {
      // there is not enough space -> do not paint the text
      return
    }

    horizontalAlignment = anchorDirection.horizontalAlignment
    textBaseline = anchorDirection.verticalAlignment

    val relevantX = x + anchorDirection.calculateOffsetXForGap(gapHorizontal)
    val relevantY = y + anchorDirection.calculateOffsetYForGap(gapVertical) + getFontAlignmentCorrection(anchorDirection)

    if (maxWidth != null && maxWidth.isFinite() && maxWidth > 0.0) {
      if (DebugFeature.ShowMaxTextWidth.enabled(debug)) {
        saved {
          //Find the starting point
          val left = relevantX + when (horizontalAlignment) {
            HorizontalAlignment.Left -> 0.0
            HorizontalAlignment.Center -> -maxWidth / 2.0
            HorizontalAlignment.Right -> -maxWidth
          }
          //val left = relevantX + anchorDirection.horizontalAlignment.calculateOffsetXWithAnchor(maxWidth)
          val right = left + maxWidth

          stroke(com.meistercharts.algorithms.painter.Color.darkblue)
          lineWidth = 1.0
          strokeLine(left, relevantY - 5, left, relevantY + 5)
          strokeLine(left, relevantY, right, relevantY)
          strokeLine(right, relevantY - 5, right, relevantY + 5)
        }
      }

      if (DebugFeature.UnShortenedTexts.enabled(debug)) {
        saved {
          @px val width = calculateTextWidth(text)
          if (width > maxWidth) {
            fill(com.meistercharts.algorithms.painter.Color.silver)
            context.fillText(text, relevantX, relevantY)
            stroke(com.meistercharts.algorithms.painter.Color.darkgray)
            lineWidth = 1.0
            context.strokeText(text, relevantX, relevantY)
          }
        }
      }

      //Paint the shortened string
      stringShortener.shorten(text, maxWidth, this)?.let {
        context.fillText(it, relevantX, relevantY)
      }
    } else {
      //No max width is set, just paint the text
      context.fillText(text, relevantX, relevantY)
    }
  }

  override fun fillTextWithin(
    text: String,
    x: @Window Double,
    y: @Window Double,
    anchorDirection: Direction,
    gapHorizontal: @Zoomed Double,
    gapVertical: @Zoomed Double,
    boxX: @Zoomed Double,
    boxY: @Zoomed Double,
    boxWidth: @Zoomed @Positive Double,
    boxHeight: @Zoomed @Positive Double,
    stringShortener: CanvasStringShortener,
  ) {
    val fontMetrics = getFontMetrics()
    if (boxHeight < fontMetrics.totalHeight) {
      // there is not enough space -> do not paint the text
      return
    }

    horizontalAlignment = anchorDirection.horizontalAlignment
    textBaseline = anchorDirection.verticalAlignment

    @Window val relevantX = x + anchorDirection.calculateOffsetXForGap(gapHorizontal)
    @Window val relevantY = y + anchorDirection.calculateOffsetYForGap(gapVertical) + getFontAlignmentCorrection(anchorDirection)

    //check if the anchor point is within the box. Else just return
    if (Rectangle.isPointWithin(relevantX, relevantY, boxX, boxY, boxWidth, boxHeight).not()) {
      return
    }

    //Ensure the top of the text is within the within box. Else just return
    when (anchorDirection.verticalAlignment) {
      VerticalAlignment.Top -> {
        relevantY
      }

      VerticalAlignment.Center -> {
        relevantY - fontMetrics.totalHeight / 2.0
      }

      VerticalAlignment.Baseline -> {
        relevantY - fontMetrics.accentLine
      }

      VerticalAlignment.Bottom -> {
        relevantY - fontMetrics.totalHeight
      }
    }.let { topOfText: @Window Double ->
      if (Rectangle.isPointWithin(relevantX, topOfText, boxX, boxY, boxWidth, boxHeight).not()) {
        return
      }
    }

    //Ensure the bottom of the text is within the within box. Else just return
    when (anchorDirection.verticalAlignment) {
      VerticalAlignment.Top -> {
        relevantY + fontMetrics.totalHeight
      }

      VerticalAlignment.Center -> {
        relevantY + fontMetrics.totalHeight / 2.0
      }

      VerticalAlignment.Baseline -> {
        relevantY + fontMetrics.pLine
      }

      VerticalAlignment.Bottom -> {
        relevantY
      }
    }.let { bottomOfText: @Window Double ->
      if (Rectangle.isPointWithin(relevantX, bottomOfText, boxX, boxY, boxWidth, boxHeight).not()) {
        return
      }
    }

    //Calculate the width that is available - depending on the anchor location and the "within" properties
    val availableWidth = when (anchorDirection.horizontalAlignment) {
      HorizontalAlignment.Left -> boxX + boxWidth - relevantX
      HorizontalAlignment.Right -> relevantX - boxX
      HorizontalAlignment.Center -> min(boxX + boxWidth - relevantX, relevantX - boxX) * 2.0
    }

    stringShortener.shorten(text, availableWidth, this)?.let {
      context.fillText(it, relevantX, relevantY)
    }
  }

  override fun strokeText(
    text: String,
    x: Double,
    y: Double,
    anchorDirection: Direction,
    gapHorizontal: Double,
    gapVertical: Double,
    maxWidth: Double?,
    maxHeight: Double?,
    stringShortener: CanvasStringShortener,
  ) {
    if (maxHeight != null && maxHeight <= 0.0) {
      //No max height provided, return immediately
      return
    }
    if (maxWidth != null && maxWidth <= 0.0) {
      //No max width provided, return immediately
      return
    }

    if (maxHeight != null && maxHeight < getFontMetrics().totalHeight) {
      // there is not enough space -> do not paint the text
      return
    }

    horizontalAlignment = anchorDirection.horizontalAlignment
    textBaseline = anchorDirection.verticalAlignment

    val relevantX = x + anchorDirection.calculateOffsetXForGap(gapHorizontal)
    val relevantY = y + anchorDirection.calculateOffsetYForGap(gapVertical) + getFontAlignmentCorrection(anchorDirection)

    if (maxWidth != null) {
      stringShortener.shorten(text, maxWidth, this).let {
        context.strokeText(it, relevantX, relevantY)
      }
    } else {
      context.strokeText(text, relevantX, relevantY)
    }
  }

  fun getFxFontMetrics(): FontMetrics {
    return NativeFontMetricsCache[context.font]
  }

  override fun getFontMetrics(): com.meistercharts.canvas.FontMetrics {
    return FontMetricsCacheFX.get(font)
  }

  override fun calculateTextSize(text: String): Size {
    return TextBoundsFX.calculateBounds(text, context.font).size()
  }

  override fun calculateTextWidth(text: String): Double {
    return TextBoundsFX.calculateWidth(text, context.font)
  }

  override fun save() {
    super.save()
    context.save()
  }

  override fun restore() {
    super.restore()
    context.restore()
  }

  override fun clearRect(x: Double, y: Double, width: Double, height: Double) {
    context.clearRect(x, y, width, height)
  }

  override fun rect(x: Double, y: Double, width: @MayBeNegative Double, height: @MayBeNegative Double) {
    context.rect(x, y, width, height)
  }

  override fun clip(x: Double, y: Double, width: Double, height: Double) {
    context.beginPath()
    context.rect(x, y, width, height)
    context.clip()
  }

  override fun paintImage(image: Image, @Window x: Double, @Window y: Double, @Zoomed width: Double, @Zoomed height: Double) {
    val fxImage = image.data as javafx.scene.image.Image
    context.drawImage(fxImage, x, y, width, height)
  }

  override fun pattern(patternCanvas: Canvas) {
    val primitiveCanvas = (patternCanvas as CanvasFX).canvas

    //TODO maybe cache the image
    val image = primitiveCanvas.screenshot()
    //context.fill = ImagePattern(image, 0.0, 0.0, 1.0, 1.0, true)
    //TODO device pixel ratio!
    context.fill = ImagePattern(image, 0.0, 0.0, image.width, image.height, false)
  }

  override fun paintImagePixelPerfect(image: Image, x: Double, y: Double) {
    saved {
      //Set the *physical* translation to the *rounded* value
      translationPhysical = translationPhysical.let { currentTranslation ->
        @PhysicalPixel val exactTargetTranslationX = currentTranslation.x + x * scaleX
        @PhysicalPixel val exactTargetTranslationY = currentTranslation.y + y * scaleY

        @PhysicalPixel val roundedTargetTranslationX = exactTargetTranslationX.round()
        @PhysicalPixel val roundedTargetTranslationY = exactTargetTranslationY.round()

        Distance(roundedTargetTranslationX, roundedTargetTranslationY)
      }

      scale = Zoom(1.0, 1.0)

      val imageFX = image.data as javafx.scene.image.Image
      context.drawImage(imageFX, 0.0, 0.0, imageFX.width, imageFX.height)
    }
  }

  override var lineJoin: LineJoin
    get() = context.lineJoin.fromFx()
    set(value) {
      context.lineJoin = value.toFx()
    }

  /**
   * The temporary affine object that is used to set the transformation matrix without creating new objects
   */
  private val tmpAffine = Affine()

  override val nativeTranslation: Distance
    get() {
      val transform = context.getTransform(tmpAffine)
      val tx = transform.tx
      val ty = transform.ty

      return Distance(tx, ty)
    }
}

/**
 * Converts the color descriptor to a JavaFX Paint
 */
fun CanvasPaint.toJavaFx(): Paint {
  return when (this) {
    is com.meistercharts.algorithms.painter.Color -> toJavaFx()
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
fun com.meistercharts.algorithms.painter.Color.toJavaFx(): Color = Color.valueOf(this.web)

private fun VPos.fromFx(): VerticalAlignment {
  return when (this) {
    VPos.TOP -> VerticalAlignment.Top
    VPos.CENTER -> VerticalAlignment.Center
    VPos.BASELINE -> VerticalAlignment.Baseline
    VPos.BOTTOM -> VerticalAlignment.Bottom
  }
}

private fun VerticalAlignment.toFx(): VPos {
  return when (this) {
    VerticalAlignment.Center -> VPos.CENTER
    VerticalAlignment.Baseline -> VPos.BASELINE
    VerticalAlignment.Bottom -> VPos.BOTTOM
    VerticalAlignment.Top -> VPos.TOP
  }
}

private fun TextAlignment.fromFx(): HorizontalAlignment {
  return when (this) {
    TextAlignment.LEFT -> HorizontalAlignment.Left
    TextAlignment.RIGHT -> HorizontalAlignment.Right
    TextAlignment.CENTER -> HorizontalAlignment.Center
    else -> throw IllegalArgumentException("Invalid text-alignment $this")
  }
}

private fun HorizontalAlignment.toFx(): TextAlignment {
  return when (this) {
    HorizontalAlignment.Left -> TextAlignment.LEFT
    HorizontalAlignment.Right -> TextAlignment.RIGHT
    HorizontalAlignment.Center -> TextAlignment.CENTER
    else -> throw IllegalArgumentException("Invalid text-alignment $this")
  }
}

private fun LineJoin.toFx(): StrokeLineJoin {
  return when (this) {
    LineJoin.Miter -> StrokeLineJoin.MITER
    LineJoin.Bevel -> StrokeLineJoin.BEVEL
    LineJoin.Round -> StrokeLineJoin.ROUND
  }
}

private fun StrokeLineJoin.fromFx(): LineJoin {
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
fun Color.toColor(): com.meistercharts.algorithms.painter.Color {
  return com.meistercharts.algorithms.painter.Color.color(red, green, blue)
}

/**
 * Returns the JavaFX Graphics context
 */
fun CanvasRenderingContext.asFxContext(): GraphicsContext {
  return (this as CanvasRenderingContextFX).context
}
