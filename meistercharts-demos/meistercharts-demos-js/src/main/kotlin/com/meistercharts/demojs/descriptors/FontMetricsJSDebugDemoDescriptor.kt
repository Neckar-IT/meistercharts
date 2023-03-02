package com.meistercharts.demojs.descriptors

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
import com.meistercharts.js.CanvasFontMetricsCalculatorJS
import it.neckar.open.unit.other.Normalized

/**
 */
class FontMetricsJSDebugDemoDescriptor : ChartingDemoDescriptor<FontMetricsDebugConfig> {
  override val name: String = "JS Font Metrics Debug"
  override val category: DemoCategory = DemoCategory.Platform

  override val predefinedConfigurations: List<PredefinedConfiguration<FontMetricsDebugConfig>> = createEnumConfigs()

  override fun createDemo(configuration: PredefinedConfiguration<FontMetricsDebugConfig>?): ChartingDemo {
    require(configuration != null) {
      "configuration must not be null"
    }

    return ChartingDemo {
      meistercharts {
        configure {
          layers.addClearBackground()

          val myLayer = object : AbstractLayer() {
            var fontFragment = FontDescriptorFragment.L

            override val type: LayerType = LayerType.Content

            override fun paint(paintingContext: LayerPaintingContext) {
              val gc = paintingContext.gc
              gc.translate(50.0, 20.0)

              val font = fontFragment.withDefaultValues()
              gc.font(fontFragment)
              val calculator = CanvasFontMetricsCalculatorJS(scaleFactorY = 4.0)

              when (configuration.payload) {
                FontMetricsDebugConfig.Ascents             -> paintingContext.drawAscents(calculator, font)
                FontMetricsDebugConfig.AlignmentCorrection -> paintingContext.drawAlignmentCorrections(calculator, font)
              }
            }
          }

          layers.addLayer(myLayer)
          configurableFont("font", myLayer::fontFragment)
        }
      }
    }
  }

  private fun LayerPaintingContext.drawAlignmentCorrections(calculator: CanvasFontMetricsCalculatorJS, font: FontDescriptor) {
    val fontMetrics = calculator.calculateFontMetrics(font)

    val correctionTopLine = calculator.calculateCorrectionValueTop(font, fontMetrics.accentLine * calculator.scaleFactorY)
    debugMeasuredLine(calculator, calculator.canvas.takeSnapshotPhysicalSize(), correctionTopLine)

    gc.translate(calculator.width, 0.0)
    val correctionBottomLine = calculator.calculateCorrectionValueBottom(font, fontMetrics.pLine * calculator.scaleFactorY)
    debugMeasuredLine(calculator, calculator.canvas.takeSnapshotPhysicalSize(), correctionBottomLine)

    gc.translate(calculator.width, 0.0)
    val correctionCenterLine = calculator.calculateCorrectionValueCenter(font)
    debugMeasuredLine(calculator, calculator.canvas.takeSnapshotPhysicalSize(), correctionCenterLine)
  }

  private fun LayerPaintingContext.drawAscents(calculator: CanvasFontMetricsCalculatorJS, font: FontDescriptor) {
    val xLineAscent = calculator.calculateXLineAscent(font)
    debugMeasuredLine(calculator, calculator.canvas.takeSnapshotPhysicalSize(), xLineAscent)

    gc.translate(calculator.width, 0.0)
    val capitalHLineAscent = calculator.calculateCapitalHAscent(font)
    debugMeasuredLine(calculator, calculator.canvas.takeSnapshotPhysicalSize(), capitalHLineAscent)

    gc.translate(calculator.width, 0.0)
    val ascentLineDescent = calculator.calculateAscentLineAscent(font)
    debugMeasuredLine(calculator, calculator.canvas.takeSnapshotPhysicalSize(), ascentLineDescent)

    gc.translate(calculator.width, 0.0)
    val pLineDescent = calculator.calculatePLineDescent(font)
    debugMeasuredLine(calculator, calculator.canvas.takeSnapshotPhysicalSize(), -pLineDescent)
  }

  private fun LayerPaintingContext.debugMeasuredLine(
    calculator: CanvasFontMetricsCalculatorJS,
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
    }
  }
}

enum class FontMetricsDebugConfig {
  Ascents,
  AlignmentCorrection
}
