package com.meistercharts.demo.descriptors

import com.meistercharts.algorithms.layers.AbstractLayer
import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.algorithms.layers.LayerType
import com.meistercharts.algorithms.layers.addClearBackground
import com.meistercharts.algorithms.layers.crosswire.CrossWireLayer.LabelIndex
import com.meistercharts.algorithms.painter.Color
import com.meistercharts.algorithms.painter.LabelPainter2
import com.meistercharts.algorithms.painter.LabelPlacement
import com.meistercharts.annotations.Window
import com.meistercharts.annotations.Zoomed
import com.meistercharts.canvas.fillRectCoordinates
import com.meistercharts.canvas.paintMark
import com.meistercharts.demo.ChartingDemo
import com.meistercharts.demo.ChartingDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.configurableDouble
import com.meistercharts.demo.configurableEnum
import com.meistercharts.demo.configurableRange
import com.meistercharts.design.Theme
import com.meistercharts.model.Direction
import com.meistercharts.provider.LabelsProvider
import it.neckar.open.provider.DoublesProvider
import it.neckar.open.provider.MultiProvider
import it.neckar.open.provider.asDoublesProvider1
import it.neckar.open.provider.fastForEach
import it.neckar.open.provider.mapped
import it.neckar.open.formatting.decimalFormat
import it.neckar.open.i18n.I18nConfiguration
import it.neckar.open.i18n.TextService
import com.meistercharts.style.BoxStyle

class LabelPainterDemoDescriptor : ChartingDemoDescriptor<Nothing> {

  override val name: String = "Label Painter"

  //language=HTML
  override val category: DemoCategory = DemoCategory.Calculations

  override fun createDemo(configuration: PredefinedConfiguration<Nothing>?): ChartingDemo {
    return ChartingDemo {
      meistercharts {

        configure {
          layers.addClearBackground()
          val layer = object : AbstractLayer() {
            override val type: LayerType = LayerType.Content

            var minMaxRange: @Window IntRange = IntRange(0, 500)

            val labelLocationsRaw: @Window DoublesProvider = DoublesProvider.forDoubles(
              0.1, 0.2, 0.3, 0.5, 0.6,
              10.0, 10.1,
              100.0, 100.1,
              200.0, 200.1, 200.2, 200.3, 200.4, 201.0,
              300.0, 300.1,
              400.0, 400.1,
              500.0, 500.1,
              600.0, 600.1,
              800.0, 800.1,
            )

            var offset: @Zoomed Double = 0.0

            val labelLocations: @Window DoublesProvider = labelLocationsRaw.mapped {
              it + offset
            }
            val labelLocations1 = labelLocations.asDoublesProvider1<LayerPaintingContext>()

            val labelBoxStyles: MultiProvider<LabelIndex, BoxStyle> = MultiProvider.invoke {
              //ATTENTION: These objects are recreated on every repaint. Which is ok for a demo - but not for production
              BoxStyle(fill = Theme.chartColors().valueAt(it))
            }

            val labelTexts = object : LabelsProvider<LabelIndex> {
              override fun valueAt(index: Int, param1: TextService, param2: I18nConfiguration): String {
                return "Value for $index (${decimalFormat.format(labelLocations.valueAt(index))})"
              }
            }

            val labelPainter: LabelPainter2 = LabelPainter2(true, true) {
            }

            override fun layout(paintingContext: LayerPaintingContext) {
              super.layout(paintingContext)

              labelPainter.layout(paintingContext, labelLocations1, labelBoxStyles, labelTexts, minMaxRange.first.toDouble(), minMaxRange.last.toDouble())
            }

            var labelPlacement = LabelPlacement.OnLeftSide

            override fun paint(paintingContext: LayerPaintingContext) {
              val gc = paintingContext.gc
              //Min Max area
              gc.fill(Color.silver.withAlpha(0.2))
              gc.fillRectCoordinates(0.0, minMaxRange.first.toDouble(), gc.width, minMaxRange.last.toDouble())

              gc.translateToCenterX()
              gc.paintMark()

              //Paint the values itself
              gc.fill(Color.orange)
              labelLocations.fastForEach { y ->
                gc.fillOvalCenter(0.0, y, 5.0)
                gc.fillText(decimalFormat.format(y), 0.0, y, Direction.CenterLeft, 5.0)
              }

              //Paint the labels
              labelPainter.paintLabels(
                paintingContext,
                labelBoxStyles = labelBoxStyles,
                labelTextColors = MultiProvider.always(Color.white), labelPlacement
              )
            }
          }
          layers.addLayer(layer)

          configurableEnum("Label Placement", layer::labelPlacement)

          configurableDouble("Label Spacing", layer.labelPainter.style::labelSpacing) {
            max = 20.0
          }
          configurableDouble("Gap", layer.labelPainter.style::gapToLabels) {
            max = 400.0
          }
          configurableDouble("Line Width", layer.labelPainter.style::lineWidth) {
            max = 10.0
          }

          configurableDouble("Location Offset", layer::offset) {
            min = -100.0
            max = 100.0
          }

          configurableRange("Min/Max", layer::minMaxRange) {
            min = -100
            max = 1500
          }
        }
      }
    }
  }
}
