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
package com.meistercharts.fonts

import com.meistercharts.canvas.AlignmentCorrectionInformation
import com.meistercharts.canvas.Canvas
import com.meistercharts.canvas.CanvasRenderingContext
import com.meistercharts.canvas.FontDescriptor
import com.meistercharts.canvas.FontMetrics
import com.meistercharts.model.VerticalAlignment
import it.neckar.open.unit.other.Normalized
import it.neckar.open.unit.other.Scaled
import it.neckar.open.unit.other.px
import it.neckar.logging.LoggerFactory

/**
 * Abstract base class for canvas font metrics calculators
 */
abstract class AbstractCanvasFontMetricsCalculator<C : Canvas, GC : CanvasRenderingContext, ImageData>(
  /**
   * The canvas element that is used to calculate the font metrics
   */
  val canvas: C,

  /**
   * The factor that is used to improve precision through scaling of the y-axis
   */
  val scaleFactorY: Double = 4.0,
) {

  /**
   * The width of the canvas.
   * All characters are drawn above each other, therefore the width can be quite small
   */
  val width: @px Double
    get() {
      return canvas.physicalWidth
    }

  /**
   * The physical height of the canvas (scaled!)
   */
  val height: @Scaled Double
    get() {
      return canvas.physicalHeight
    }

  /**
   * The position that is used to draw the strings
   */
  val anchorY: Int
    get() = (height / 2).toInt()

  protected abstract val canvasRenderingContext: GC


  /**
   * Calculates the font metrics for the given font
   */
  fun calculateFontMetrics(font: FontDescriptor): FontMetrics {
    @Normalized @px val xLineAscent: Double = calculateXLineAscent(font)
    @Normalized @px val capitalHLineAscent: Double = calculateCapitalHAscent(font)
    @Normalized @px val pLineDescent: Double = calculatePLineDescent(font)

    @Normalized @px val accentLineAscent: Double = calculateAscentLineAscent(font)

    val alignmentCorrectionInformation = calculateCorrectionValue(font, accentLineAscent * scaleFactorY, pLineDescent * scaleFactorY)
    return FontMetrics(xLineAscent, capitalHLineAscent, accentLineAscent, pLineDescent, alignmentCorrectionInformation)
  }


  fun calculateAscentLineAscent(font: FontDescriptor): @Normalized Double {
    val imageData = createImageData(font, VerticalAlignment.Baseline, accentLineChar)
    @px val accentLineDescent = (findBottomLine(imageData, anchorY) - anchorY) / scaleFactorY
    return (anchorY - findTopLine(imageData, anchorY, font)) / scaleFactorY
  }

  fun calculatePLineDescent(font: FontDescriptor): @Normalized Double {
    val imageData = createImageData(font, VerticalAlignment.Baseline, pLineChar)
    @px val pLineAscent = (anchorY - findTopLine(imageData, anchorY, font)) / scaleFactorY
    return (findBottomLine(imageData, anchorY) - anchorY) / scaleFactorY
  }

  fun calculateCapitalHAscent(font: FontDescriptor): @Normalized Double {
    val imageData = createImageData(font, VerticalAlignment.Baseline, capitalHLineChar)
    @px val capitalHLineDescent = (findBottomLine(imageData, anchorY) - anchorY) / scaleFactorY
    return (anchorY - findTopLine(imageData, anchorY, font)) / scaleFactorY
  }

  fun calculateXLineAscent(font: FontDescriptor): @Normalized Double {
    run {
      val imageData = createImageData(font, VerticalAlignment.Baseline, xLineChar)
      //@px val xLineDescent = (findBottomLine(imageData, anchorY) - anchorY) / scaleFactorY
      return (anchorY - findTopLine(imageData, anchorY, font)) / scaleFactorY
    }
  }

  /**
   * Calculate the alignment correction information
   */
  protected fun calculateCorrectionValue(font: FontDescriptor, accentLineAscent: @Scaled Double, pLineDescent: @Scaled Double): AlignmentCorrectionInformation {
    @Normalized val correctionValueTop = calculateCorrectionValueTop(font, accentLineAscent)
    @Normalized val correctionValueCenter = calculateCorrectionValueCenter(font)
    @Normalized val correctionValueBottom = calculateCorrectionValueBottom(font, pLineDescent)

    return AlignmentCorrectionInformation(correctionValueTop, correctionValueCenter, correctionValueBottom)
  }


  /**
   * Calculates the correction value for text baseline is TOP
   */
  fun calculateCorrectionValueTop(font: FontDescriptor, ascent: @Scaled @px Double): @Normalized Double {
    val imageData = createImageData(font, VerticalAlignment.Top, accentLineChar)

    //Start looking from the assumed base line
    @Scaled val searchBase = (anchorY + ascent).toInt()
    @Scaled val topLine = findTopLine(imageData, searchBase, font)

    return (anchorY - topLine.toDouble() - 0.5) / scaleFactorY
  }

  /**
   * Calculates the correction value for bottom
   */
  fun calculateCorrectionValueBottom(font: FontDescriptor, descent: @px @Scaled Double): @Normalized Double {
    val imageData = createImageData(font, VerticalAlignment.Bottom, pLineChar)

    //Start looking from the assumed bottom
    @Scaled val searchBase = (anchorY - descent).toInt()
    @Scaled val bottomLine = findBottomLine(imageData, searchBase)

    return (anchorY - bottomLine.toDouble() - 0.5) / scaleFactorY
  }

  /**
   * Calculates the ascent for the given alignment.
   * Draw the text below the y value and count the pixels *above* the line
   */
  fun calculateCorrectionValueCenter(font: FontDescriptor): @Normalized Double {
    val imageData = createImageData(font, VerticalAlignment.Center, capitalHLineChar)

    @Normalized val topLine = findTopLine(imageData, anchorY, font) / scaleFactorY
    @Normalized val bottomLine = findBottomLine(imageData, anchorY) / scaleFactorY

    //Calculate the "real" center
    @Normalized val delta = (bottomLine - topLine)

    //the "optimal" center, where we expect the center to be aligned
    @Normalized val centerOptimal = topLine + delta / 2.0 + FontCenterAlignmentStrategy.calculateCenterOffset(delta)

    return anchorY / scaleFactorY - centerOptimal
  }

  /**
   * Returns the topmost line containing any pixel(s) above the base line.
   * Starts at the base line and searches upwards (to lower values)
   */
  protected fun findTopLine(imageData: ImageData, baseLineY: Int, font: FontDescriptor): @Scaled Int {
    //the minimum amount of empty lines that is required to detect the top line
    @Scaled val threshold = font.size.size / 2.0 * scaleFactorY

    //Count the empty lines. Only return if *enough* empty lines have been found
    var emptyLineCounter = 0

    //Search upwards
    for (y in baseLineY downTo 0) {
      if (!imageData.containsPixel(y)) {
        emptyLineCounter++

        if (emptyLineCounter > threshold) {
          return y + emptyLineCounter //returns the row below that must contain a value
        }
      } else {
        //Reset the empty line
        emptyLineCounter = 0
      }
    }

    //Fallback if there are empty lines at the top
    if (emptyLineCounter > 0) {
      return emptyLineCounter //returns the row below that must contain a value
    }

    logger.warn("WARNING: No top line found")

    return (height / 2.0).toInt()
  }

  /**
   *  Returns the bottommost line containing any pixel(s) below the base line.
   * Starts at the base line and searches downloads (to larger values)
   */
  protected fun findBottomLine(imageData: ImageData, baseLineY: Int): @Scaled Int {
    for (y in baseLineY..imageData.imageHeight) {
      if (!imageData.containsPixel(y)) {
        return y - 1 //returns the row below that must contain a value
      }
    }

    logger.warn("WARNING: No bottom line found")

    //Fallback - return half the height
    return (height / 2.0).toInt()
  }

  /**
   * Creates the image for the given character
   */
  abstract fun createImageData(font: FontDescriptor, verticalAlignment: VerticalAlignment, char: Char): ImageData

  /**
   * Returns the height for the image data
   */
  abstract val ImageData.imageHeight: Int

  /**
   * Returns true if the image contains any pixel in the given row
   */
  abstract fun ImageData.containsPixel(y: Int): Boolean

  companion object {
    private val logger = LoggerFactory.getLogger("com.meistercharts.fonts.AbstractCanvasFontMetricsCalculator")

    /**
     * The character for the accent line
     */
    const val accentLineChar: Char = 'Á'

    /**
     * The character for the "p" line
     */
    const val pLineChar: Char = 'p'

    /**
     * The character for the "H" line
     */
    const val capitalHLineChar: Char = 'H'

    /**
     * The character for the "x" line
     */
    const val xLineChar: Char = 'x'

    /**
     * All line chars - only used for debugging
     */
    const val lineChars: String = "$xLineChar$capitalHLineChar$accentLineChar$pLineChar"
  }
}
