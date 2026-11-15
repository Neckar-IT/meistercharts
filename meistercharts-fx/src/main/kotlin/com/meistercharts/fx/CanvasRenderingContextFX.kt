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

import com.meistercharts.annotations.PhysicalPixel
import com.meistercharts.annotations.Window
import com.meistercharts.annotations.Zoomed
import com.meistercharts.canvas.AbstractCanvasRenderingContext
import com.meistercharts.canvas.Canvas
import com.meistercharts.canvas.CanvasRenderingContext
import com.meistercharts.canvas.DebugFeature
import com.meistercharts.canvas.FillRule
import com.meistercharts.canvas.Image
import com.meistercharts.canvas.LineJoin
import com.meistercharts.canvas.calculateOffsetXForGap
import com.meistercharts.canvas.calculateOffsetYForGap
import com.meistercharts.canvas.fill
import com.meistercharts.canvas.saved
import com.meistercharts.canvas.stroke
import com.meistercharts.canvas.text.CanvasStringShortener
import com.meistercharts.color.CanvasPaint
import com.meistercharts.font.FontDescriptor
import com.meistercharts.fx.font.FontMetricsCacheFX
import com.meistercharts.fx.font.NativeFontMetricsCache
import com.meistercharts.fx.font.TextBoundsFX
import com.meistercharts.fx.font.toFont
import com.meistercharts.fx.font.toFontDescriptor
import com.meistercharts.model.Zoom
import com.sun.javafx.tk.FontMetrics
import it.neckar.geometry.Direction
import it.neckar.geometry.Distance
import it.neckar.geometry.HorizontalAlignment
import it.neckar.geometry.Rectangle
import it.neckar.geometry.Size
import it.neckar.geometry.VerticalAlignment
import it.neckar.open.annotations.Hot
import it.neckar.open.annotations.HotAllocation
import it.neckar.open.kotlin.lang.isPositiveOrZero
import it.neckar.open.kotlin.lang.round
import it.neckar.open.unit.number.MayBeNegative
import it.neckar.open.unit.number.Positive
import it.neckar.open.unit.number.PositiveOrZero
import it.neckar.open.unit.other.px
import it.neckar.open.unit.si.rad
import javafx.scene.canvas.GraphicsContext
import javafx.scene.paint.ImagePattern
import javafx.scene.paint.Paint
import javafx.scene.shape.StrokeLineCap
import javafx.scene.transform.Affine
import kotlin.math.PI
import kotlin.math.min


private const val PlausibilityCheckEnabled: Boolean = false

/**
 * Implementation for [CanvasRenderingContext] that uses a [CanvasFX]
 */
class CanvasRenderingContextFX(
  override val canvas: CanvasFX,
) : AbstractCanvasRenderingContext() {

  val context: GraphicsContext = canvas.canvas.graphicsContext2D

  init {
    applyDefaults()
  }

  override fun defaultFont(): FontDescriptor {
    //TODO somehow extract fhe font?
    return FontDescriptor.Default
  }

  @px
  override val canvasSize: Size
    get() = canvas.size

  @px
  override val width: Double
    get() = canvas.width

  @px
  override
  val height: Double
    get() = canvas.height

  var fill: Paint?
    get() = context.fill
    set(value) {
      context.fill = value
    }

  override fun currentFillDebug(): String {
    return fill.toString()
  }

  var stroke: Paint?
    get() = context.stroke
    set(value) {
      context.stroke = value
    }

  override fun currentStrokeDebug(): String {
    return stroke.toString()
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

  @Hot
  override fun shadow(color: com.meistercharts.color.Color, blurRadius: Double, offsetX: Double, offsetY: Double) {
    @HotAllocation("warm shadow cache - one capturing lambda per call, DropShadow only on miss")
    val shadow = ShadowFxCache.shadow(blurRadius, offsetX, offsetY, color)
    context.setEffect(shadow)
  }

  @Hot
  override fun clearShadow() {
    context.setEffect(null)
  }

  @Hot
  override fun strokeStyle(color: CanvasPaint) {
    context.stroke = ColorCache.getPaint(color)
  }

  @Hot
  override fun fillStyle(color: CanvasPaint) {
    context.fill = ColorCache.getPaint(color)
  }

  @Hot
  override fun setLineDash(vararg dashes: Double) {
    context.setLineDashes(*dashes)
  }

  @Hot
  override fun setLineDashes(dashes: DoubleArray) {
    //Via Java interop: a Kotlin spread (*dashes) would copy the array on every call.
    //GraphicsContext.setLineDashes copies the values into its own state internally.
    GraphicsContextInterop.setLineDashes(context, dashes)
  }

  @Hot
  override fun strokeLine(startX: Double, startY: Double, endX: Double, endY: Double) {
    if (PlausibilityCheckEnabled) {
      requirePlausiblePaintingValues(startX, startY, endX, endY)
    }

    context.lineCap = StrokeLineCap.BUTT
    context.strokeLine(startX, startY, endX, endY)
  }

  @Hot
  private fun requirePlausiblePaintingValues(value0: Double, value1: Double, value2: Double, value3: Double) {
    requirePlausiblePaintingValue(value0)
    requirePlausiblePaintingValue(value1)
    requirePlausiblePaintingValue(value2)
    requirePlausiblePaintingValue(value3)
  }

  @Hot
  private fun requirePlausiblePaintingValue(value: Double) {
    require(value in -10_000.0..10_000.0) {
      "Unplausible value: $value"
    }
  }

  @Hot
  override fun fillRectInternal(x: Double, y: Double, width: Double, height: Double) {
    context.fillRect(x, y, width, height)
  }

  override fun resetTransform() {
    super.resetTransform()
    context.transform = emptyAffine
  }

  @Hot
  override fun strokeRectInternal(x: Double, y: Double, width: Double, height: Double) {
    context.strokeRect(x, y, width, height)
  }

  override fun strokeOvalOrigin(x: Double, y: Double, width: Double, height: Double) {
    context.strokeOval(x, y, width, height)
  }

  override fun fillOvalOrigin(x: Double, y: Double, width: Double, height: Double) {
    context.fillOval(x, y, width, height)
  }

  @Hot
  override fun ovalCenter(centerX: Double, centerY: Double, width: Double, height: Double) {
    context.moveTo(centerX + width / 2.0, centerY)
    context.arc(centerX, centerY, width / 2.0, height / 2.0, 0.0, -360.0)
  }

  @Hot
  override fun strokeOvalCenter(x: Double, y: Double, width: Double, height: Double) {
    context.strokeOval(x - width / 2.0, y - height / 2.0, width, height)
  }

  @Hot
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

  @Hot
  override fun beginPath() {
    context.beginPath()
  }

  @Hot
  override fun closePath() {
    context.closePath()
  }

  @Hot
  override fun moveTo(x: Double, y: Double) {
    context.moveTo(x, y)
  }

  @Hot
  override fun lineTo(x: Double, y: Double) {
    context.lineTo(x, y)
  }

  @Hot
  override fun bezierCurveTo(controlX1: Double, controlY1: Double, controlX2: Double, controlY2: Double, x2: Double, y2: Double) {
    context.bezierCurveTo(controlX1, controlY1, controlX2, controlY2, x2, y2)
  }

  @Hot
  override fun quadraticCurveTo(controlX: Double, controlY: Double, x: Double, y: Double) {
    context.quadraticCurveTo(controlX, controlY, x, y)
  }

  @Hot
  override fun arcTo(controlX: Double, controlY: Double, x: Double, y: Double, radius: @PositiveOrZero Double) {
    require(radius.isPositiveOrZero()) { "Radius must be >= 0 but was <$radius>" }

    context.arcTo(controlX, controlY, x, y, radius)
  }

  override fun arcCenter(centerX: Double, centerY: Double, radius: Double, startAngle: @rad Double, extend: @rad Double) {
    context.arc(centerX, centerY, radius, radius, -Math.toDegrees(startAngle), -Math.toDegrees(extend))
  }

  @Hot
  override fun stroke() {
    context.stroke()
  }

  @Hot
  override fun fill() {
    context.fill()
  }

  @Hot
  override fun translate(deltaX: Double, deltaY: Double) {
    super.translate(deltaX, deltaY)
    context.translate(deltaX, deltaY)
  }

  @Hot
  override fun translatePhysical(deltaX: Double, deltaY: Double) {
    super.translatePhysical(deltaX, deltaY)
    context.translate(deltaX / scaleX, deltaY / scaleY)
  }

  @Hot
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

  @Hot
  override fun fillRule(fillRule: FillRule) {
    context.fillRule = fillRule.toFx()
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
          val left = relevantX + when (horizontalAlignment) {
            HorizontalAlignment.Left -> 0.0
            HorizontalAlignment.Center -> -maxWidth / 2.0
            HorizontalAlignment.Right -> -maxWidth
          }
          val right = left + maxWidth

          stroke(com.meistercharts.color.Color.darkblue)
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
            fill(com.meistercharts.color.Color.silver)
            context.fillText(text, relevantX, relevantY)
            stroke(com.meistercharts.color.Color.darkgray)
            lineWidth = 1.0
            context.strokeText(text, relevantX, relevantY)
          }
        }
      }

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

  @Deprecated("Does not work in JavaFX 20+")
  fun getFxFontMetrics(): FontMetrics {
    return NativeFontMetricsCache[context.font]
  }

  @Hot
  override fun getFontMetrics(): com.meistercharts.font.FontMetrics {
    @HotAllocation("warm font-metrics cache - one capturing lambda per call, computation only on miss")
    val metrics = FontMetricsCacheFX.get(font)
    return metrics
  }

  override fun calculateTextSize(text: String): Size {
    return TextBoundsFX.calculateBounds(text, context.font).size()
  }

  @Hot
  override fun calculateTextWidth(text: String): Double {
    @HotAllocation("warm text-width cache - one capturing lambda per call, layout only on miss")
    val width = TextBoundsFX.calculateWidth(text, context.font)
    return width
  }

  override fun save() {
    super.save()
    context.save()
  }

  override fun restore() {
    super.restore()
    context.restore()
  }

  @Hot
  override fun clearRect(x: Double, y: Double, width: Double, height: Double) {
    context.clearRect(x, y, width, height)
  }

  @Hot
  override fun rect(x: Double, y: Double, width: @MayBeNegative Double, height: @MayBeNegative Double) {
    context.rect(x, y, width, height)
  }

  @Hot
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

  companion object {
    /**
     * This object is used to avoid unnecessary object creations. Do *NOT* modify the object
     */
    private val emptyAffine: Affine = Affine()
      get() {
        require(field.isEmpty()) { "emptyAffine must remain the identity transform \u2014 was modified to [$field]" }
        return field
      }
  }
}

private fun Affine.isEmpty(): Boolean {
  return this.mxx == 1.0 && this.mxy == 0.0 && this.mxz == 0.0 &&
    this.myx == 0.0 && this.myy == 1.0 && this.myz == 0.0 &&
    this.mzx == 0.0 && this.mzy == 0.0 && this.mzz == 1.0
}

/**
 * Returns the graphics context cast to the platform implementation
 */
fun CanvasRenderingContext.native(): CanvasRenderingContextFX = this as CanvasRenderingContextFX
