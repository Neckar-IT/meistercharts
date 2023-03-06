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
package com.meistercharts.demo.descriptors

import com.meistercharts.algorithms.layers.AbstractLayer
import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.algorithms.layers.LayerType
import com.meistercharts.algorithms.layers.addClearBackground
import com.meistercharts.algorithms.painter.Color
import com.meistercharts.canvas.FontDescriptor
import com.meistercharts.canvas.FontDescriptorFragment
import com.meistercharts.canvas.Image
import com.meistercharts.canvas.saved
import com.meistercharts.demo.ChartingDemo
import com.meistercharts.demo.ChartingDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.configurableFont
import com.meistercharts.demo.createEnumConfigs
import com.meistercharts.fx.font.CanvasFontMetricsCalculatorFX
import com.meistercharts.fx.native
import com.meistercharts.model.Direction
import it.neckar.open.unit.other.Normalized

/**
 *
 */
class FontMetricsCalculatorFXDemoDescriptor : ChartingDemoDescriptor<FontMetricsDebugConfig> {
  override val name: String = "Fx Font Metrics Calculator"
  override val category: DemoCategory = DemoCategory.Platform
  override val predefinedConfigurations: List<PredefinedConfiguration<FontMetricsDebugConfig>> = createEnumConfigs()

  override fun createDemo(configuration: PredefinedConfiguration<FontMetricsDebugConfig>?): ChartingDemo {
    requireNotNull(configuration) {
      "configuration must not be null"
    }

    return ChartingDemo {
      meistercharts {
        configure {
          layers.addClearBackground()


          val myLayer = object : AbstractLayer() {
            var fontFragment: FontDescriptorFragment = FontDescriptorFragment.L

            override val type: LayerType = LayerType.Content

            override fun paint(paintingContext: LayerPaintingContext) {
              val gc = paintingContext.gc
              gc.translate(50.0, 20.0)

              val font = fontFragment.withDefaultValues()
              gc.font(fontFragment)
              val calculator = CanvasFontMetricsCalculatorFX(scaleFactorY = 4.0)

              when (configuration.payload) {
                FontMetricsDebugConfig.Ascents -> paintingContext.drawAscents(calculator, font)
                FontMetricsDebugConfig.AlignmentCorrection -> paintingContext.drawAlignmentCorrections(calculator, font)
                FontMetricsDebugConfig.FxNativeFont -> paintingContext.drawJavaFxNative(calculator, font)
              }
            }
          }

          layers.addLayer(myLayer)
          configurableFont("font", myLayer::fontFragment)
        }
      }
    }
  }

  private fun LayerPaintingContext.drawAlignmentCorrections(calculator: CanvasFontMetricsCalculatorFX, font: FontDescriptor) {
    val fontMetrics = calculator.calculateFontMetrics(font)

    val correctionTopLine = calculator.calculateCorrectionValueTop(font, fontMetrics.accentLine * calculator.scaleFactorY)
    debugMeasuredLine(calculator, calculator.canvas.takeSnapshot(), correctionTopLine)

    gc.translate(calculator.width, 0.0)
    val correctionBottomLine = calculator.calculateCorrectionValueBottom(font, fontMetrics.pLine * calculator.scaleFactorY)
    debugMeasuredLine(calculator, calculator.canvas.takeSnapshot(), correctionBottomLine)

    gc.translate(calculator.width, 0.0)
    val correctionCenterLine = calculator.calculateCorrectionValueCenter(font)
    debugMeasuredLine(calculator, calculator.canvas.takeSnapshot(), correctionCenterLine)
  }

  private fun LayerPaintingContext.drawJavaFxNative(calculator: CanvasFontMetricsCalculatorFX, font: FontDescriptor) {
    val gc = this.gc.native()

    val fxFontMetrics = gc.getFxFontMetrics()

    calculator.calculateXLineAscent(font) //necessary to fill the canvas
    debugMeasuredLine(calculator, calculator.canvas.takeSnapshot(), fxFontMetrics.xheight.toDouble())

    gc.translate(calculator.width, 0.0)
    calculator.calculateAscentLineAscent(font) //necessary to fill the canvas
    debugMeasuredLine(calculator, calculator.canvas.takeSnapshot(), fxFontMetrics.ascent.toDouble())
  }

  private fun LayerPaintingContext.drawAscents(calculator: CanvasFontMetricsCalculatorFX, font: FontDescriptor) {
    val xLineAscent = calculator.calculateXLineAscent(font)
    debugMeasuredLine(calculator, calculator.canvas.takeSnapshot(), xLineAscent)

    gc.translate(calculator.width, 0.0)
    val capitalHLineAscent = calculator.calculateCapitalHAscent(font)
    debugMeasuredLine(calculator, calculator.canvas.takeSnapshot(), capitalHLineAscent)

    gc.translate(calculator.width, 0.0)
    val ascentLineDescent = calculator.calculateAscentLineAscent(font)
    debugMeasuredLine(calculator, calculator.canvas.takeSnapshot(), ascentLineDescent)

    gc.translate(calculator.width, 0.0)
    val pLineDescent = calculator.calculatePLineDescent(font)
    debugMeasuredLine(calculator, calculator.canvas.takeSnapshot(), -pLineDescent)
  }

  private fun LayerPaintingContext.debugMeasuredLine(
    calculator: CanvasFontMetricsCalculatorFX,
    snapshot: Image,
    measuredLine: @Normalized Double
  ) {
    val gc = gc

    gc.saved {
      gc.saved {
        //The canvas itself has been scaled up. To show the representation human "readable", we invert the scaling before painting
        gc.scale(1.0, 1.0 / calculator.scaleFactorY)
        snapshot.paint(this)

        //Stroke the bounds
        gc.stroke(Color.silver)
        gc.strokeRect(0.0, 0.0, calculator.width, calculator.height)
      }

      //The baseline is calculated using the scaling factor. Revert here because the canvas is painted with inverted scaling
      val baseLineY = calculator.anchorY.toDouble() / calculator.scaleFactorY

      gc.stroke(Color.red)
      gc.strokeLine(0.0, baseLineY, calculator.canvas.width, baseLineY)

      gc.stroke(Color.blue)
      gc.strokeLine(0.0, baseLineY - measuredLine, calculator.canvas.width, baseLineY - measuredLine)

      gc.font = FontDescriptor.Default
      gc.fillText(measuredLine.toString(), 0.0, 0.0, Direction.CenterLeft)
    }
  }
}

enum class FontMetricsDebugConfig {
  Ascents,
  AlignmentCorrection,
  FxNativeFont
}
