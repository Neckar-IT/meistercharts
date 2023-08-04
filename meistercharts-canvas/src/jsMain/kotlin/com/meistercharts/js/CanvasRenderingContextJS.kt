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
package com.meistercharts.js

import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.annotations.PhysicalPixel
import com.meistercharts.annotations.Window
import com.meistercharts.annotations.Zoomed
import com.meistercharts.canvas.AbstractCanvasRenderingContext
import com.meistercharts.canvas.ArcType
import com.meistercharts.canvas.Canvas
import com.meistercharts.canvas.text.CanvasStringShortener
import com.meistercharts.canvas.CanvasType
import com.meistercharts.canvas.DebugFeature
import com.meistercharts.font.FontDescriptor
import com.meistercharts.font.FontMetrics
import com.meistercharts.canvas.Image
import com.meistercharts.canvas.LineJoin
import com.meistercharts.canvas.calculateOffsetXForGap
import com.meistercharts.canvas.calculateOffsetYForGap
import com.meistercharts.canvas.saved
import com.meistercharts.color.CanvasLinearGradient
import com.meistercharts.color.CanvasPaint
import com.meistercharts.color.CanvasRadialGradient
import com.meistercharts.color.Color
import it.neckar.geometry.Distance
import it.neckar.geometry.Rectangle
import com.meistercharts.js.CanvasReadBackFrequency.Frequent
import it.neckar.geometry.Direction
import it.neckar.geometry.HorizontalAlignment
import it.neckar.geometry.Size
import it.neckar.geometry.VerticalAlignment
import com.meistercharts.model.Zoom
import it.neckar.logging.LoggerFactory
import it.neckar.open.kotlin.lang.isPositiveOrZero
import it.neckar.open.kotlin.lang.round
import it.neckar.open.unit.number.MayBeNegative
import it.neckar.open.unit.number.PositiveOrZero
import it.neckar.open.unit.other.px
import it.neckar.open.unit.si.rad
import org.khronos.webgl.WebGLRenderingContext
import org.w3c.dom.ALPHABETIC
import org.w3c.dom.BEVEL
import org.w3c.dom.BOTTOM
import org.w3c.dom.CENTER
import org.w3c.dom.CanvasGradient
import org.w3c.dom.CanvasImageSource
import org.w3c.dom.CanvasLineJoin
import org.w3c.dom.CanvasRenderingContext2D
import org.w3c.dom.CanvasTextAlign
import org.w3c.dom.CanvasTextBaseline
import org.w3c.dom.HTMLCanvasElement
import org.w3c.dom.LEFT
import org.w3c.dom.MIDDLE
import org.w3c.dom.MITER
import org.w3c.dom.RIGHT
import org.w3c.dom.ROUND
import org.w3c.dom.TOP
import kotlin.math.PI
import kotlin.math.min

/**
 * HTML 5 canvas graphics context.
 *
 * ## Browser zoom / High DPI support
 *
 * To be able to support high dpi devices and browser zoom the device pixel ratio is used.
 * The concept is described here:
 *
 * The browser uses the [com.meistercharts.algorithms.Environment.devicePixelRatio] to calculate logical pixels that are different from the physical pixels.
 *
 * To ensure the maximum quality we want the canvas to have the size of the physical pixels.
 * This can be achieved because an html canvas has two independent properties:
 * - [width](https://developer.mozilla.org/en-US/docs/Web/API/HTMLCanvasElement/width)/[height](https://developer.mozilla.org/en-US/docs/Web/API/HTMLCanvasElement/height) attributes: specify the amount of pixels the gc can paint to
 * - [BoundingClientRect](https://developer.mozilla.org/en-US/docs/Web/API/Element/getBoundingClientRect): the "real" size of the canvas in logical pixels; this size is computed by the browser from the CSS properties that hold for the canvas element
 *
 * To ensure that all painting operations use the correct (physical) size, a scale is applied to the graphics context in [CanvasRenderingContextJS.applyDefaults].
 *
 *
 * ### Example calculations
 *
 * #### Assumptions:
 *
 * * DevicePixelRatio: 1.4 (typical for UHD displays) which translates to a resolution of 134 dpi or 1.40 dppx
 * * UHD Display (3840 pixels wide)
 * * Browser in full screen
 *
 * #### Sizes:
 *
 * * Browser window width
 * ** Physical pixels: 3840
 * ** CSS (logical) pixels: 2742 pixels (3840/1.4)
 * * [org.w3c.dom.HTMLCanvasElement]
 * ** bounding client rect: 2742 pixels (CSS)
 * ** width: 3840 pixels (calculated when the [com.meistercharts.js.CanvasJS.sizeProperty] is updated)
 * * [CanvasJS]
 * ** width: 2742 pixels (used for calculations in the layer)
 * * [CanvasRenderingContextJS]
 * ** width: 2742 pixels (from the [CanvasJS])
 * ** scale set to 1.4 in [applyDefaults]
 *
 */
class CanvasRenderingContextJS(
  override val canvas: CanvasJS,
  readBackFrequency: CanvasReadBackFrequency,
) : AbstractCanvasRenderingContext() {

  val context: CanvasRenderingContext2D = canvas.canvasElement.getCanvasRenderingContext2D(readBackFrequency)

  init {
    applyDefaults()
  }

  override fun clip(x: Double, y: Double, width: Double, height: Double) {
    context.beginPath()
    context.rect(x, y, width, height)
    context.clip()
  }

  override fun resetTransform() {
    super.resetTransform()
    // Beware that IE11 does not support resetTransform!
    context.resetTransform()
  }

  override fun save() {
    super.save()
    context.save()
  }

  override fun restore() {
    super.restore()
    context.restore()
  }

  /**
   * The size of the canvas in CSS (logical) pixels
   */
  @px
  override val canvasSize: Size
    get() = canvas.size

  /**
   * The width of the canvas in CSS (logical) pixels
   */
  @px
  override val width: Double
    get() = canvas.width

  /**
   * The height of the canvas in CSS (logical) pixels
   */
  @px
  override val height: Double
    get() = canvas.height

  override var globalAlpha: Double
    get() = context.globalAlpha
    set(value) {
      context.globalAlpha = value
    }

  override var lineWidth: Double
    get() = context.lineWidth
    set(value) {
      context.lineWidth = value
    }

  override var font: FontDescriptor
    set(value) {
      context.font = value.convertToHtmlFontString()
      FontConversionCacheJS.store(value, context.font)
    }
    get() {
      return try {
        FontConversionCacheJS.reverse(context.font)
      } catch (e: Exception) {
        logger.warn("Warning: Could not reverse <${context.font}> due to $e")
        FontDescriptor.Default
      }
    }

  override fun strokeLine(startX: Double, startY: Double, endX: Double, endY: Double) {
    context.beginPath()
    context.moveTo(startX, startY)
    context.lineTo(endX, endY)
    context.stroke()
  }

  override fun fillRectInternal(x: Double, y: Double, width: Double, height: Double) {
    context.fillRect(x, y, width, height)
  }

  override fun strokeRectInternal(x: Double, y: Double, width: Double, height: Double) {
    context.strokeRect(x, y, width, height)
  }

  /**
   * Adds an elliptical arc to the current sub-path.
   * @param x The x-axis (horizontal) coordinate of the ellipse's center.
   * @param y The y-axis (vertical) coordinate of the ellipse's center.
   * @param radiusX The ellipse's major-axis radius. Must be non-negative.
   * @param radiusY The ellipse's minor-axis radius. Must be non-negative.
   * @param rotation The rotation of the ellipse, expressed in radians.
   * @param startAngle The angle at which the ellipse starts, measured clockwise from the positive x-axis and expressed in radians.
   * @param endAngle The angle at which the ellipse ends, measured clockwise from the positive x-axis and expressed in radians.
   * @param anticlockwise An optional Boolean which, if true, draws the ellipse anticlockwise (counter-clockwise). The default value is false (clockwise).
   */
  private fun addEllipseSubPath(
    x: Double,
    y: Double,
    radiusX: Double,
    radiusY: Double,
    @rad rotation: Double,
    @rad startAngle: Double,
    @rad endAngle: Double,
    anticlockwise: Boolean = false,
  ) {
    //Note that IE11 does not support ellipse().
    context.ellipse(x, y, radiusX, radiusY, rotation, startAngle, endAngle, anticlockwise)
  }

  override fun strokeOvalCenter(x: Double, y: Double, width: Double, height: Double) {
    context.beginPath()
    addEllipseSubPath(x, y, width / 2.0, height / 2.0, 0.0, 0.0, 2 * PI, false)
    context.stroke()
  }

  override fun ovalCenter(centerX: Double, centerY: Double, width: Double, height: Double) {
    context.moveTo(centerX + width / 2.0, centerY)
    addEllipseSubPath(centerX, centerY, width / 2.0, height / 2.0, 0.0, 0.0, 2 * PI, false)
  }

  override fun strokeOvalOrigin(x: Double, y: Double, width: Double, height: Double) {
    context.beginPath()
    addEllipseSubPath(x + width / 2.0, y + height / 2.0, width / 2.0, height / 2.0, 0.0, 0.0, 2 * PI, false)
    context.stroke()
  }

  override fun fillOvalCenter(x: Double, y: Double, width: Double, height: Double) {
    context.beginPath()
    addEllipseSubPath(x, y, width / 2.0, height / 2.0, 0.0, 0.0, 2 * PI, false)
    context.fill()
  }

  override fun fillOvalOrigin(x: Double, y: Double, width: Double, height: Double) {
    context.beginPath()
    addEllipseSubPath(x + width / 2.0, y + height / 2.0, width / 2.0, height / 2.0, 0.0, 0.0, 2 * PI, false)
    context.fill()
  }

  override fun strokeArcCenter(@Window x: Double, @Window y: Double, @Zoomed radius: Double, @rad startAngle: Double, @rad arcExtent: Double, arcType: ArcType) {
    arcPath(x, y, radius, startAngle, arcExtent, arcType)
    context.stroke()
  }

  override fun fillArcCenter(@Window x: Double, @Window y: Double, @Zoomed radius: Double, @rad startAngle: Double, @rad arcExtent: Double, arcType: ArcType) {
    arcPath(x, y, radius, startAngle, arcExtent, arcType)
    context.fill()
  }

  fun arcPath(@Window x: Double, @Window y: Double, @Zoomed radius: Double, @rad startAngle: Double, @rad arcExtent: Double, arcType: ArcType) {
    context.beginPath()
    when (arcType) {
      ArcType.Open -> {
        context.arc(x, y, radius, startAngle, startAngle + arcExtent, arcExtent < 0)
      }

      ArcType.Chord -> TODO()
      ArcType.Round -> {
        context.moveTo(x, y)
        context.arc(x, y, radius, startAngle, startAngle + arcExtent, arcExtent < 0)
        context.closePath()
      }
    }
  }

  override fun arcCenter(centerX: Double, centerY: Double, radius: Double, startAngle: Double, extend: Double) {
    context.arc(centerX, centerY, radius, startAngle, startAngle + extend, extend < 0)
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

  override fun stroke() {
    context.stroke()
  }

  override fun fill() {
    context.fill()
  }

  override fun currentFillDebug(): String {
    return context.fillStyle.toString()
  }

  override fun currentStrokeDebug(): String {
    return context.strokeStyle.toString()
  }

  override fun translate(deltaX: Double, deltaY: Double) {
    super.translate(deltaX, deltaY)
    context.translate(deltaX, deltaY)
  }

  override fun translatePhysical(deltaX: Double, deltaY: Double) {
    super.translatePhysical(deltaX, deltaY)
    context.translate(deltaX / scaleX, deltaY / scaleY)
  }

  override fun shadow(color: Color, blurRadius: Double, offsetX: Double, offsetY: Double) {
    context.shadowColor = color.web
    context.shadowBlur = blurRadius
    context.shadowOffsetX = offsetX
    context.shadowOffsetY = offsetY
  }

  override fun clearShadow() {
    context.shadowColor = Color.transparent.web
  }

  /**
   * Adds a rotation to the transformation matrix.
   * The rotation center point is always the canvas origin. To change the center point, you will need to move the canvas by using the [translate] method.
   * @param angleInRadians The rotation angle, clockwise in radians.
   * @see [rotateDegrees]
   */
  override fun rotateRadians(@rad angleInRadians: Double) {
    super.rotateRadians(angleInRadians)
    context.rotate(angleInRadians)
  }

  override fun scale(x: Double, y: Double) {
    super.scale(x, y)
    context.scale(x, y)
  }

  var textBaseline: VerticalAlignment
    get() = context.textBaseline.fromHtml()
    set(value) {
      context.textBaseline = value.toHtml()
    }

  var horizontalAlignment: HorizontalAlignment
    get() = context.textAlign.fromHtml()
    set(value) {
      context.textAlign = value.toHtml()
    }

  override fun fillText(
    text: String, x: Double, y: Double, anchorDirection: Direction,
    gapHorizontal: Double, gapVertical: Double,
    maxWidth: Double?, maxHeight: Double?, stringShortener: CanvasStringShortener,
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
          val right = left + maxWidth

          stroke(Color.darkblue)
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
            fill(Color.silver)
            context.fillText(text, relevantX, relevantY)
            stroke(Color.darkgray)
            lineWidth = 1.0
            context.strokeText(text, relevantX, relevantY)
          }
        }
      }

      stringShortener.shorten(text, maxWidth, this)?.let {
        context.fillText(it, relevantX, relevantY)
      }
    } else {
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
    boxX: @Window Double,
    boxY: @Window Double,
    boxWidth: @Zoomed Double,
    boxHeight: @Zoomed Double,
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
    text: String, x: Double, y: Double, anchorDirection: Direction,
    gapHorizontal: Double, gapVertical: Double,
    maxWidth: Double?, maxHeight: Double?, stringShortener: CanvasStringShortener,
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
      if (DebugFeature.UnShortenedTexts.enabled(debug)) {
        saved {
          @px val width = calculateTextWidth(text)
          if (width > maxWidth) {
            //paint the complete text in silver
            stroke(Color.silver)
            context.strokeText(text, relevantX, relevantY)
          }
        }
      }

      stringShortener.shorten(text, maxWidth, this)?.let {
        context.strokeText(it, relevantX, relevantY)
      }
    } else {
      context.strokeText(text, relevantX, relevantY)
    }
  }

  override fun clearRect(x: Double, y: Double, width: Double, height: Double) {
    context.clearRect(x, y, width, height)
  }

  override fun rect(x: Double, y: Double, width: @MayBeNegative Double, height: @MayBeNegative Double) {
    context.rect(x, y, width, height)
  }

  override fun strokeStyle(color: CanvasPaint) {
    context.strokeStyle = color.toHtml()
  }

  override fun fillStyle(color: CanvasPaint) {
    context.fillStyle = color.toHtml()
  }

  /**
   * Converts the paint to an HTML color
   */
  private fun CanvasPaint.toHtml(): Any {
    return when (this) {
      is Color -> web
      is CanvasLinearGradient -> toHtmlGradient()
      is CanvasRadialGradient -> toHtmlGradient()
      else -> throw UnsupportedOperationException("Unsupported paint <$this>")
    }
  }

  /**
   * Converts a linear gradient to an html gradient that can then
   * be applied as fill or stroke style
   */
  fun CanvasLinearGradient.toHtmlGradient(): CanvasGradient {
    val gradient = context.createLinearGradient(this.x0, this.y0, this.x1, this.y1)
    gradient.addColorStop(0.0, this.color0.web)
    gradient.addColorStop(1.0, this.color1.web)
    return gradient
  }

  fun CanvasRadialGradient.toHtmlGradient(): CanvasGradient {
    val gradient = context.createRadialGradient(positionX, positionY, 0.0, positionX, positionY, radius)
    gradient.addColorStop(0.0, color0.web)
    gradient.addColorStop(1.0, color1.web)
    return gradient
  }

  override fun setLineDash(vararg dashes: Double) {
    context.setLineDash(dashes.toTypedArray())
  }

  override fun getFontMetrics(): FontMetrics {
    return FontMetricsCacheJS.get(font)
  }

  override fun calculateTextWidth(text: String): Double {
    return FontBoundsCacheJS.calculateWidth(text, font) {
      context.measureText(text).width
    }
  }

  override fun calculateTextSize(text: String): Size {
    return Size.of(calculateTextWidth(text), getFontMetrics().totalHeight)
  }

  override fun paintImage(image: Image, @Window x: Double, @Window y: Double, @Zoomed width: Double, @Zoomed height: Double) {
    context.drawImage(image.data as CanvasImageSource, x, y, width, height)
  }

  override fun paintImagePixelPerfect(image: Image, x: Double, y: Double) {
    saved {
      //Set the translation to the *rounded* value
      translationPhysical = translationPhysical.let { currentTranslation ->
        @PhysicalPixel val exactTargetTranslationX = currentTranslation.x + x * scaleX
        @PhysicalPixel val exactTargetTranslationY = currentTranslation.y + y * scaleY

        @PhysicalPixel val roundedTargetTranslationX = exactTargetTranslationX.round()
        @PhysicalPixel val roundedTargetTranslationY = exactTargetTranslationY.round()

        Distance(roundedTargetTranslationX, roundedTargetTranslationY)
      }

      scale = Zoom(1.0, 1.0)

      val imageJS = image.data as CanvasImageSource
      context.drawImage(imageJS, 0.0, 0.0)
    }
  }

  override var lineJoin: LineJoin
    get() = context.lineJoin.fromHtml()
    set(value) {
      context.lineJoin = value.toHtml()
    }

  @Deprecated("Only for debugging purposes")
  override val nativeTranslation: Distance
    get() {
      val transform = context.getTransform()
      return Distance(transform.e, transform.f)
    }

  override fun pattern(patternCanvas: Canvas) {
    val canvasElement: CanvasImageSource = (patternCanvas as CanvasJS).canvasElement
    val pattern = context.createPattern(canvasElement, "repeat")
    context.fillStyle = pattern
  }

  companion object {
    private val logger = LoggerFactory.getLogger("com.meistercharts.js.CanvasRenderingContextJS")
  }
}

private fun CanvasTextBaseline.fromHtml(): VerticalAlignment {
  return when (this) {
    CanvasTextBaseline.TOP -> VerticalAlignment.Top
    CanvasTextBaseline.MIDDLE -> VerticalAlignment.Center
    CanvasTextBaseline.BOTTOM -> VerticalAlignment.Bottom
    CanvasTextBaseline.ALPHABETIC -> VerticalAlignment.Baseline
    else -> throw IllegalArgumentException("Invalid baseline $this")
  }
}

fun VerticalAlignment.toHtml(): CanvasTextBaseline {
  return when (this) {
    VerticalAlignment.Center -> CanvasTextBaseline.MIDDLE
    VerticalAlignment.Baseline -> CanvasTextBaseline.ALPHABETIC
    VerticalAlignment.Bottom -> CanvasTextBaseline.BOTTOM
    VerticalAlignment.Top -> CanvasTextBaseline.TOP
  }
}

private fun CanvasTextAlign.fromHtml(): HorizontalAlignment {
  return when (this) {
    CanvasTextAlign.LEFT -> HorizontalAlignment.Left
    CanvasTextAlign.RIGHT -> HorizontalAlignment.Right
    CanvasTextAlign.CENTER -> HorizontalAlignment.Center
    else -> throw IllegalArgumentException("Invalid text-alignment $this")
  }
}

fun HorizontalAlignment.toHtml(): CanvasTextAlign {
  return when (this) {
    HorizontalAlignment.Left -> CanvasTextAlign.LEFT
    HorizontalAlignment.Right -> CanvasTextAlign.RIGHT
    HorizontalAlignment.Center -> CanvasTextAlign.CENTER
    else -> throw IllegalArgumentException("Invalid text-alignment $this")
  }
}

private fun LineJoin.toHtml(): CanvasLineJoin {
  return when (this) {
    LineJoin.Miter -> CanvasLineJoin.MITER
    LineJoin.Bevel -> CanvasLineJoin.BEVEL
    LineJoin.Round -> CanvasLineJoin.ROUND
  }
}

private fun CanvasLineJoin.fromHtml(): LineJoin {
  return when (this) {
    CanvasLineJoin.MITER -> LineJoin.Miter
    CanvasLineJoin.BEVEL -> LineJoin.Bevel
    CanvasLineJoin.ROUND -> LineJoin.Round
    else -> throw IllegalArgumentException("Invalid lineJoin $this")
  }
}

/**
 * Returns the canvas rendering context 2D
 */
fun HTMLCanvasElement.getCanvasRenderingContext2D(readBackFrequency: CanvasReadBackFrequency): CanvasRenderingContext2D {
  val arguments: dynamic = when (readBackFrequency) {
    CanvasReadBackFrequency.Frequent -> js("{ willReadFrequently: true }")
    CanvasReadBackFrequency.Infrequent -> js("{}")
  }

  return getContext("2d", arguments) as? CanvasRenderingContext2D ?: throw IllegalStateException("context not found")
}

val HTMLCanvasElement.canvasRenderingContextWebGl: WebGLRenderingContext
  get() = getContext("webgl") as WebGLRenderingContext ?: throw IllegalStateException("context not found")


/**
 * Returns the graphics context cast to the platform implementation
 */
fun LayerPaintingContext.native(): CanvasRenderingContextJS = gc as CanvasRenderingContextJS

/**
 * Defines how often values from the canvas are read back.
 * This is used to optimize the rendering performance.
 *
 * When calling [CanvasRenderingContext2D.getImageData] (often), [Frequent] should be used.
 */
enum class CanvasReadBackFrequency {
  /**
   * The canvas is read back frequently
   */
  Frequent,

  /**
   * The canvas is read back infrequently
   */
  Infrequent,
  ;

  companion object {
    /**
     * Converts the canvas type to a read back frequency
     */
    fun CanvasType.readBackFrequency(): CanvasReadBackFrequency {
      return when (this) {
        CanvasType.Main, CanvasType.OffScreen -> Infrequent
        CanvasType.ReadBack -> Frequent
      }
    }
  }
}
