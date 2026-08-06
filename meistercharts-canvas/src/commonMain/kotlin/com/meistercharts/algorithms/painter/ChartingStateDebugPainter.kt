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
package com.meistercharts.algorithms.painter

import com.meistercharts.annotations.ContentArea
import com.meistercharts.annotations.ContentAreaRelative
import com.meistercharts.annotations.Window
import com.meistercharts.annotations.Zoomed
import com.meistercharts.calc.ChartCalculator
import com.meistercharts.canvas.CanvasRenderingContext
import com.meistercharts.canvas.fillStyle
import com.meistercharts.canvas.saved
import com.meistercharts.color.Color
import it.neckar.geometry.AxisOrientationX
import it.neckar.geometry.AxisOrientationY
import it.neckar.geometry.Coordinates
import it.neckar.geometry.Direction
import it.neckar.geometry.Size
import it.neckar.open.formatting.decimalFormat
import it.neckar.open.i18n.CurrentI18nConfiguration
import it.neckar.open.i18n.I18nConfiguration
import it.neckar.open.kotlin.lang.WhitespaceConfig
import it.neckar.open.unit.other.px
import kotlin.math.min

/**
 * Painter that paints an image visualizing the current state of the canvas
 */
class ChartingStateDebugPainter {
  /**
   * Paints the state for the given calculator
   *
   * Attention: The given calculator usually does *NOT* correspond to the rendering context
   */
  fun paintState(
    /**
     * The rendering context to paint on
     */
    gc: CanvasRenderingContext,
    /**
     * The calculator that is used to calculate the state.
     * This calculator does *NOT* correspond to the provided rendering context
     */
    calculatorToVisualize: ChartCalculator,
    /**
     * The width of the canvas
     */
    @Zoomed @px canvasWidth: Double,
    /**
     * The height of the canvas
     */
    @Zoomed @px canvasHeight: Double,
  ) {

    //Calculate the min/max edges for the content area
    @ContentArea val contentAreaMin = calculatorToVisualize.contentAreaRelative2contentArea(Coordinates(0.0, 0.0))
    @ContentArea val contentAreaMax = calculatorToVisualize.contentAreaRelative2contentArea(Coordinates(1.0, 1.0))

    //Calculate the min/max edges for the window
    @ContentArea val windowMin = calculatorToVisualize.window2contentArea(Coordinates(0.0, 0.0))
    @ContentArea val windowMax = calculatorToVisualize.window2contentArea(Coordinates(calculatorToVisualize.chartState.windowWidth, calculatorToVisualize.chartState.windowHeight))

    //Calculate the max area that contains both the content and the window
    @ContentArea val minAll = Coordinates.minOf(contentAreaMin, windowMin)
    @ContentArea val maxAll = Coordinates.maxOf(contentAreaMax, windowMax)

    //Calculate the delta for all visible stuff
    @px val allDeltaX = maxAll.x - minAll.x
    @px val allDeltaY = maxAll.y - minAll.y


    //Calculate the relative start points for content area and window

    @ContentArea val offsetContent = contentAreaMin.delta(minAll)
    @ContentArea val contentSize = contentAreaMax.delta(contentAreaMin)

    @ContentArea val offsetWindow = windowMin.delta(minAll)
    @ContentArea val windowSize = windowMax.delta(windowMin)


    //The factor that is used to convert view values
    val factor = min(1 / (allDeltaX) * (canvasWidth - viewPadding * 2), 1 / (allDeltaY) * (canvasHeight - viewPadding * 2))

    //Draw the background
    gc.fillStyle(Color.white)
    gc.fillRect(0.0, 0.0, canvasWidth, canvasHeight)

    //Draw the content area
    run {
      @px val x = viewPadding + offsetContent.x * factor
      @px val y = viewPadding + offsetContent.y * factor
      @px val width = contentSize.x * factor
      @px val height = contentSize.y * factor


      @Window val contentAreaOriginInWindow = calculatorToVisualize.contentAreaRelative2window(0.0, 0.0)
      @Zoomed val contentAreaSizeZoomed = calculatorToVisualize.contentAreaRelative2zoomed(1.0, 1.0)

      gc.saved { gc ->
        gc.translate(x, y)
        paintArea(
          calculatorToVisualize = calculatorToVisualize,
          label = "ContentArea",
          coordinates = contentAreaOriginInWindow,
          size = contentAreaSizeZoomed,
          gc = gc,
          width = width,
          height = height,
          stroke = Color.red(),
          fill = contentAreaFill
        )

        //Paint the y axis orientation arrow
        if (width > 80 && height > 50) {
          gc.saved { gc ->
            gc.fillStyle(Color.red)
            gc.translate(8.0, 0.0)

            //Translate/rotate depending on the axis orientation
            when (calculatorToVisualize.chartState.axisOrientationY) {
              AxisOrientationY.OriginAtTop    -> {
                gc.translate(0.0, height)
                gc.rotateDegrees(180.0)
              }

              AxisOrientationY.OriginAtBottom -> {
                gc.translate(0.0, height - 100.0)
              }
            }

            gc.stroke(Arrows.toTop(100.0, 10.0))
          }

          //Paint the x axis orientation arrow
          gc.saved { gc ->
            gc.fillStyle(Color.red)
            gc.translate(0.0, height - 8.0)

            //Translate/rotate depending on the axis orientation
            when (calculatorToVisualize.chartState.axisOrientationX) {
              AxisOrientationX.OriginAtLeft  -> {
                gc.translate(100.0, 0.0)
                gc.rotateDegrees(90.0)
              }

              AxisOrientationX.OriginAtRight -> {
                gc.translate(15.0, 0.0)
                gc.rotateDegrees(270.0)
              }
            }

            gc.stroke(Arrows.toTop(100.0, 10.0))
          }
        }
      }
    }

    //Draw the window ("window")
    run {
      @px val x = viewPadding + offsetWindow.x * factor
      @px val y = viewPadding + offsetWindow.y * factor
      val width = windowSize.x * factor
      val height = windowSize.y * factor

      gc.saved {
        gc.translate(x, y)
        paintArea(
          calculatorToVisualize = calculatorToVisualize,
          label = "Window\nZF: ${calculatorToVisualize.chartState.zoomX.format(CurrentI18nConfiguration, 3)} / ${calculatorToVisualize.chartState.zoomY.format(CurrentI18nConfiguration, 3)}",
          coordinates = Coordinates.origin,
          size = calculatorToVisualize.chartState.contentAreaSize,
          gc = gc,
          width = width,
          height = height,
          stroke = Color.green(),
          fill = windowFill
        )
      }
    }
  }

  private fun paintArea(calculatorToVisualize: ChartCalculator, label: String, @Window coordinates: Coordinates, @Zoomed size: Size, gc: CanvasRenderingContext, @px width: Double, @px height: Double, stroke: Color, fill: Color) {
    val x = 0.0
    val y = 0.0

    gc.fillStyle(fill)
    gc.fillRect(x, y, width, height)

    gc.strokeStyle(stroke)
    gc.strokeRect(x, y, width, height)

    if (width < 80 || height < 50) {
      return

    }

    gc.fillStyle(stroke)
    gc.fillText(label, x + width / 2.0, y + height / 2.0, Direction.Center)

    @ContentAreaRelative val sizeInView = calculatorToVisualize.zoomed2contentAreaRelative(size)

    //Add x axis labels
    gc.strokeStyle(stroke)

    gc.fillText("Width (WP): ${size.width.format()}\nView: ${sizeInView.width.formatPct()}", width / 2.0, 10.0, Direction.TopCenter)

    //Add y axis label
    gc.strokeStyle(stroke)
    gc.fillText("Height\nWP: ${size.height.format()}\nView: ${sizeInView.height.formatPct()}", 10.0, height / 2.0, Direction.TopLeft)

    //Add coordinates
    gc.strokeStyle(stroke)
    gc.fillText("WP: ${coordinates.x.format()}/${coordinates.y.format()}", 5.0, 10.0, Direction.TopLeft)

    val bottomRight = coordinates.plus(size)

    gc.fillText("WP: ${bottomRight.x.format()}/${bottomRight.y.format()}", width - 5.0, height - 5.0, Direction.BottomRight)
  }

  companion object {
    /**
     * Padding around the graphics context
     */
    @px
    private const val viewPadding: Double = 20.0

    /**
     * Fill for the content area
     */
    private val contentAreaFill = Color.color(1.0, 0.0, 0.0, 0.3)

    /**
     * Fill for the window
     */
    private val windowFill = Color.color(0.0, 1.0, 0.0, 0.2)

    private fun Double.format(i18nConfiguration: I18nConfiguration = CurrentI18nConfiguration, maxFractionDigits: Int = 1, whitespaceConfig: WhitespaceConfig = WhitespaceConfig.NonBreaking): String {
      return decimalFormat(maxFractionDigits).format(this, i18nConfiguration, whitespaceConfig)
    }

    private fun Double.formatPct(i18nConfiguration: I18nConfiguration = CurrentI18nConfiguration, maxFractionDigits: Int = 1, whitespaceConfig: WhitespaceConfig = WhitespaceConfig.NonBreaking): String {
      return "${decimalFormat(maxFractionDigits).format(this, i18nConfiguration, whitespaceConfig)}%"
    }
  }
}
