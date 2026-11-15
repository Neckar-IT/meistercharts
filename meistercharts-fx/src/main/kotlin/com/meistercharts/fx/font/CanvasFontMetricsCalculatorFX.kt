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
package com.meistercharts.fx.font

import com.meistercharts.canvas.CanvasType
import com.meistercharts.font.AbstractCanvasFontMetricsCalculator
import com.meistercharts.font.FontDescriptor
import com.meistercharts.fx.CanvasFX
import com.meistercharts.fx.CanvasFactoryFX
import com.meistercharts.fx.CanvasRenderingContextFX
import it.neckar.geometry.HorizontalAlignment
import it.neckar.geometry.Size
import it.neckar.geometry.VerticalAlignment
import it.neckar.open.kotlin.lang.toIntFloor
import it.neckar.open.unit.other.Scaled
import javafx.scene.image.WritableImage

/**
 * Calculates the font metrics for JavaFX fonts
 */
class CanvasFontMetricsCalculatorFX(
  /**
   * The canvas element that is used to calculate the font metrics
   */
  canvas: CanvasFX,
  /**
   * The factor that is used to improve precision through scaling of the y axis
   */
  scaleFactorY: Double = 4.0
) : AbstractCanvasFontMetricsCalculator<CanvasFX, CanvasRenderingContextFX, WritableImage>(canvas, scaleFactorY) {

  override val canvasRenderingContext: CanvasRenderingContextFX = CanvasRenderingContextFX(canvas)

  constructor(
    canvasFactor: CanvasFactoryFX = CanvasFactoryFX(),
    scaleFactorY: Double = 4.0,
    canvasSize: @Scaled Size = Size(200.0, 600 * scaleFactorY)
  ) : this(canvasFactor.createCanvas(CanvasType.OffScreen, canvasSize), scaleFactorY)


  override val WritableImage.imageHeight: Int
    get() = height.toIntFloor()

  override fun createImageData(font: FontDescriptor, verticalAlignment: VerticalAlignment, char: Char): WritableImage {
    //Revert the transformation (scale for environment)
    canvasRenderingContext.resetTransform()

    canvasRenderingContext.clear()
    canvasRenderingContext.translate(
      0.0,
      anchorY.toDouble()
    )
    canvasRenderingContext.scale(1.0, scaleFactorY)

    canvasRenderingContext.font = font

    canvasRenderingContext.textBaseline = verticalAlignment
    canvasRenderingContext.horizontalAlignment = HorizontalAlignment.Left

    canvasRenderingContext.context.fillText(char.toString(), 0.0, 0.0)

    return canvas.snapshot()
  }

  companion object {
    const val AccentLineChar: Char = 'Á'
    const val PLineChar: Char = 'p'
    const val CapitalHLineChar: Char = 'H'
    const val XLineChar: Char = 'x'

    const val LineChars: String = "$XLineChar$CapitalHLineChar$AccentLineChar$PLineChar"
  }

  /**
   * Returns true if this image data contains a pixel at the given row
   */
  override fun WritableImage.containsPixel(y: Int): Boolean {
    val pixelReader = this.pixelReader

    for (x in 0 until width.toIntFloor()) {
      //alpha, red, green blue
      val argbValue = pixelReader.getArgb(x, y)

      if (argbValue != 0) {
        //We found a pixel
        return true
      }
    }

    return false
  }
}
