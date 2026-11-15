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
package com.meistercharts.demo.descriptors

import com.meistercharts.demo.configurable
import kotlin.uuid.Uuid
import com.meistercharts.algorithms.layers.AbstractLayer
import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.algorithms.layers.LayerType
import com.meistercharts.algorithms.layers.addClearBackground
import com.meistercharts.algorithms.layers.crosswire.CrossWireLayer.LabelIndex
import com.meistercharts.algorithms.painter.LabelPainter2
import com.meistercharts.algorithms.painter.LabelPlacement
import com.meistercharts.annotations.Window
import com.meistercharts.annotations.Zoomed
import com.meistercharts.canvas.fill
import com.meistercharts.canvas.fillRectCoordinates
import com.meistercharts.canvas.paintMark
import com.meistercharts.color.Color
import com.meistercharts.color.withAlpha
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.DemoQuality
import com.meistercharts.demo.MeisterchartsDemo
import com.meistercharts.demo.MeisterchartsDemoDescriptor
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.VariabilityType
import com.meistercharts.demo.configurableDouble
import com.meistercharts.demo.configurableEnum
import com.meistercharts.demo.configurableRange
import com.meistercharts.design.Theme
import com.meistercharts.design.valueAt
import com.meistercharts.provider.LabelsProvider
import com.meistercharts.style.BoxStyle
import it.neckar.geometry.Direction
import it.neckar.open.formatting.decimalFormat
import it.neckar.open.i18n.I18nConfiguration
import it.neckar.open.i18n.TextService
import it.neckar.open.provider.DoublesProvider
import it.neckar.open.provider.MultiProvider
import it.neckar.open.provider.asDoublesProvider1
import it.neckar.open.provider.fastForEach
import it.neckar.open.provider.mapped

class LabelPainterDemoDescriptor : MeisterchartsDemoDescriptor<Nothing> {
  override val uuid: Uuid = Uuid.parse("ca0cdea2-9c16-4751-aab7-38a9414056ad")

  override val name: String = "Label Painter"

  //language=HTML
  override val category: DemoCategory = DemoCategory.Calculations
  override val quality: DemoQuality = DemoQuality.High
  override val variabilityType: VariabilityType = VariabilityType.Stable

  override fun prepareDemo(configuration: PredefinedConfiguration<Nothing>?): MeisterchartsDemo {
    return MeisterchartsDemo(this) {
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
              BoxStyle(fill = Theme.chartColors.valueAt(it))
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
                labelTextColors = MultiProvider.always(Color.white()), labelPlacement
              )
            }
          }
          layers.addLayer(layer)

          configurable("Label Placement", layer::labelPlacement)

          configurable("Label Spacing", layer.labelPainter.style::labelSpacing) {
            max = 20.0
          }
          configurable("Gap", layer.labelPainter.style::gapToLabels) {
            max = 400.0
          }
          configurable("Line Width", layer.labelPainter.style::lineWidth) {
            max = 10.0
          }

          configurable("Location Offset", layer::offset) {
            min = -100.0
            max = 100.0
          }

          configurable("Min/Max", layer::minMaxRange) {
            min = -100
            max = 1500
          }
        }
      }
    }
  }
}
