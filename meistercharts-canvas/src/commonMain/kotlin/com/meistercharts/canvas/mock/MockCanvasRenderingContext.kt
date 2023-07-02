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
package com.meistercharts.canvas.mock

import com.meistercharts.color.CanvasPaint
import com.meistercharts.color.Color
import com.meistercharts.annotations.Window
import com.meistercharts.annotations.Zoomed
import com.meistercharts.canvas.AbstractCanvasRenderingContext
import com.meistercharts.canvas.ArcType
import com.meistercharts.canvas.Canvas
import com.meistercharts.canvas.text.CanvasStringShortener
import com.meistercharts.font.FontMetrics
import com.meistercharts.canvas.Image
import com.meistercharts.canvas.LineJoin
import com.meistercharts.font.FontDescriptor
import com.meistercharts.model.Direction
import com.meistercharts.geometry.Distance
import com.meistercharts.model.Size
import it.neckar.open.unit.other.px
import it.neckar.open.unit.si.rad

/**
 *
 */
class MockCanvasRenderingContext : AbstractCanvasRenderingContext() {
  override val canvas: Canvas
    get() = throw UnsupportedOperationException()

  override var canvasSize: Size = Size.of(800.0, 600.0)

  override val width: Double
    get() = canvasSize.width

  override val height: Double
    get() = canvasSize.height

  override var globalAlpha: Double = 1.0

  override var font: FontDescriptor = FontDescriptor.Default

  override fun currentFillDebug(): String {
    return "not implemented"
  }

  override fun currentStrokeDebug(): String {
    return "not implemented"
  }

  override fun paintImage(image: Image, x: Double, y: Double, width: Double, height: Double) {
  }

  override fun paintImagePixelPerfect(image: Image, x: Double, y: Double) {
  }

  override fun strokeLine(startX: Double, startY: Double, endX: Double, endY: Double) {
  }

  override fun fillRectInternal(x: Double, y: Double, width: Double, height: Double) {
  }

  override fun ovalCenter(centerX: Double, centerY: Double, width: Double, height: Double) {
  }

  override fun strokeRectInternal(x: Double, y: Double, width: Double, height: Double) {
  }

  override fun strokeOvalCenter(x: Double, y: Double, width: Double, height: Double) {
  }

  override fun fillOvalCenter(x: Double, y: Double, width: Double, height: Double) {
  }

  override fun strokeOvalOrigin(x: Double, y: Double, width: Double, height: Double) {
  }

  override fun fillOvalOrigin(x: Double, y: Double, width: Double, height: Double) {
  }

  override fun strokeArcCenter(@Window x: Double, @Window y: Double, @Zoomed radius: Double, @rad startAngle: Double, @rad arcExtent: Double, arcType: ArcType) {
  }

  override fun fillArcCenter(x: Double, y: Double, radius: Double, startAngle: Double, arcExtent: Double, arcType: ArcType) {
  }

  override fun beginPath() {
  }

  override fun arcCenter(centerX: Double, centerY: Double, radius: Double, startAngle: Double, extend: Double) {
  }

  override fun closePath() {
  }

  override fun moveTo(x: Double, y: Double) {
  }

  override fun lineTo(x: Double, y: Double) {
  }

  override fun bezierCurveTo(controlX1: Double, controlY1: Double, controlX2: Double, controlY2: Double, x2: Double, y2: Double) {
  }

  override fun quadraticCurveTo(controlX: Double, controlY: Double, x: Double, y: Double) {
  }

  override fun arcTo(controlX: Double, controlY: Double, x: Double, y: Double, radius: Double) {
  }

  override fun stroke() {
  }

  override fun fill() {
  }

  override fun shadow(color: Color, blurRadius: Double, offsetX: Double, offsetY: Double) {
  }

  override fun clearShadow() {
  }

  override fun pattern(patternCanvas: Canvas) {
  }

  override fun calculateTextSize(text: String): Size {
    return Size.zero
  }

  override fun fillText(text: String, x: Double, y: Double, anchorDirection: Direction, gapHorizontal: Double, gapVertical: Double, maxWidth: Double?, maxHeight: Double?, stringShortener: CanvasStringShortener) {
  }

  override fun fillTextWithin(text: String, x: Double, y: Double, anchorDirection: Direction, gapHorizontal: Double, gapVertical: Double, boxX: Double, boxY: Double, boxWidth: Double, boxHeight: Double, stringShortener: CanvasStringShortener) {
  }

  override fun strokeText(text: String, x: Double, y: Double, anchorDirection: Direction, gapHorizontal: Double, gapVertical: Double, maxWidth: Double?, maxHeight: Double?, stringShortener: CanvasStringShortener) {
  }

  override fun getFontMetrics(): FontMetrics {
    return FontMetrics(10.0, 2.0, 15.0, 4.0)
  }

  override fun clip(x: Double, y: Double, width: Double, height: Double) {
  }

  @px
  var mockTextWidth: Double = 20.0

  override fun calculateTextWidth(text: String): Double {
    return mockTextWidth
  }

  override fun rect(x: Double, y: Double, width: Double, height: Double) {
  }

  override fun clearRect(x: Double, y: Double, width: Double, height: Double) {
  }

  override fun strokeStyle(color: CanvasPaint) {
  }

  override fun fillStyle(color: CanvasPaint) {
  }

  override fun setLineDash(vararg dashes: Double) {
  }

  @px
  override var lineWidth: Double = 1.0

  override var lineJoin: LineJoin = LineJoin.Round

  override val nativeTranslation: Distance?
    get() = null
}
