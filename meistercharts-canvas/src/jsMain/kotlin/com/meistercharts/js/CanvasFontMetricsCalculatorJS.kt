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

import com.meistercharts.canvas.CanvasType
import com.meistercharts.canvas.FontDescriptor
import com.meistercharts.canvas.saved
import com.meistercharts.fonts.AbstractCanvasFontMetricsCalculator
import com.meistercharts.model.HorizontalAlignment
import com.meistercharts.model.Size
import com.meistercharts.model.VerticalAlignment
import it.neckar.open.unit.other.Scaled
import it.neckar.logging.Logger
import it.neckar.logging.LoggerFactory
import org.khronos.webgl.get
import org.w3c.dom.CanvasTextAlign
import org.w3c.dom.CanvasTextBaseline
import org.w3c.dom.ImageData
import org.w3c.dom.LEFT

/**
 * Calculates the font metrics
 */
class CanvasFontMetricsCalculatorJS(
  /**
   * The canvas element that is used to calculate the font metrics
   */
  canvas: CanvasJS,

  /**
   * The factor that is used to improve precision through scaling of the y-axis
   */
  scaleFactorY: Double = 4.0,
) : AbstractCanvasFontMetricsCalculator<CanvasJS, CanvasRenderingContextJS, ImageData>(canvas, scaleFactorY) {

  override val canvasRenderingContext: CanvasRenderingContextJS = CanvasRenderingContextJS(canvas)

  constructor(
    canvasFactor: CanvasFactoryJS = CanvasFactoryJS(),
    scaleFactorY: Double = 4.0,
    canvasSize: @Scaled Size = Size(200.0, 600 * scaleFactorY)
  ) : this(canvasFactor.createCanvas(CanvasType.OffScreen, canvasSize), scaleFactorY)

  override val ImageData.imageHeight: Int
    get() = height

  /**
   * Creates the image data for one char
   */
  override fun createImageData(font: FontDescriptor, verticalAlignment: VerticalAlignment, char: Char): ImageData {
    //Revert the transformation (scale for environment)
    canvasRenderingContext.resetTransform()

    canvasRenderingContext.clear()
    canvasRenderingContext.saved {
      it.translate(
        0.0,
        anchorY.toDouble()
      )
      it.scale(1.0, scaleFactorY)

      canvasRenderingContext.font = font

      //Set the text alignment stuff
      canvasRenderingContext.textBaseline = verticalAlignment
      canvasRenderingContext.horizontalAlignment = HorizontalAlignment.Left

      //Draw the letter
      canvasRenderingContext.context.fillText(char.toString(), 0.0, 0.0)
    }

    return canvasRenderingContext.context.getImageData(0.0, 0.0, width, height)
  }

  /**
   * Returns the image data for the given font and text
   */
  @Deprecated("use only one char")
  private fun createImageData(fontDescriptor: FontDescriptor, anchorY: Double, textBaseline: CanvasTextBaseline): ImageData {
    //Revert the transformation (scale for environment)
    canvasRenderingContext.resetTransform()

    canvasRenderingContext.clear()
    canvasRenderingContext.saved {
      it.translate(0.0, anchorY)
      it.scale(1.0, scaleFactorY)

      canvasRenderingContext.font = fontDescriptor

      //Set the text alignment stuff
      canvasRenderingContext.context.textBaseline = textBaseline
      canvasRenderingContext.context.textAlign = CanvasTextAlign.LEFT

      //Draw each letter on the same space to improve performance when evaluating
      heightCalculationText.forEach { char ->
        canvasRenderingContext.context.fillText(char.toString(), 0.0, 0.0)
      }
    }

    return canvasRenderingContext.context.getImageData(0.0, 0.0, width, height)
  }

  companion object {
    /**
     * The text that is used to calculate the "high" value (including umlauts)
     */
    @Deprecated("use only one char")
    private const val heightCalculationText = "BpÅÁqÜgÖfÄPqLT"

    val logger: Logger = LoggerFactory.getLogger("com.meistercharts.js.CanvasFontMetricsCalculatorJS")
  }

  /**
   * Returns true if this image data contains a pixel at the given row
   */
  override fun ImageData.containsPixel(y: Int): Boolean {
    for (x in 0 until width) {
      val baseOffset = ((width * y) + x) * 4
      val offsetRed = baseOffset + 0
      val offsetGreen = baseOffset + 1
      val offsetBlue = baseOffset + 2
      val offsetAlpha = baseOffset + 3

      val alpha = data[offsetAlpha]
      //val red = data[offsetRed]
      //val green = data[offsetGreen]
      //val blue = data[offsetBlue]

      if (alpha > 0) {
        //We found a pixel
        return true
      }
    }

    return false
  }
}
