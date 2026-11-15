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
import it.neckar.geometry.Orientation
import com.meistercharts.range.ValueRange
import com.meistercharts.algorithms.layers.AbstractLayer
import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.algorithms.layers.LayerType
import com.meistercharts.algorithms.layers.addClearBackground
import com.meistercharts.algorithms.layers.barchart.StackedBarPaintable
import com.meistercharts.algorithms.layers.debug.WindowDebugLayer
import com.meistercharts.color.Color
import com.meistercharts.model.BorderRadius
import com.meistercharts.canvas.saved
import com.meistercharts.canvas.stroke
import com.meistercharts.demo.MeisterchartsDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.DemoQuality
import com.meistercharts.demo.MeisterchartsDemo
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.VariabilityType
import com.meistercharts.demo.configurableBoolean
import com.meistercharts.demo.configurableColorProvider
import com.meistercharts.demo.configurableColorPickerProvider
import com.meistercharts.demo.configurableDouble
import com.meistercharts.demo.configurableEnum
import com.meistercharts.demo.configurableFont
import com.meistercharts.demo.createEnumConfigs
import com.meistercharts.demo.section
import it.neckar.geometry.Direction
import it.neckar.open.provider.DefaultDoublesProvider
import it.neckar.open.provider.MultiProvider
import com.meistercharts.style.Palette

/**
 */
class StackedBarPaintableDemoDescriptor : MeisterchartsDemoDescriptor<Orientation> {
  override val uuid: Uuid = Uuid.parse("b335603e-5300-41a5-9a63-76a3e460f025")
  override val name: String = "Stacked Bar paintable"
  override val description: String = "Shows the stacked bar paintable."

  override val category: DemoCategory = DemoCategory.Paintables
  override val quality: DemoQuality = DemoQuality.High
  override val variabilityType: VariabilityType = VariabilityType.Stable

  override val predefinedConfigurations: List<PredefinedConfiguration<Orientation>> = createEnumConfigs(uuid)

  override fun prepareDemo(configuration: PredefinedConfiguration<Orientation>?): MeisterchartsDemo {
    require(configuration != null) { "Orientation required" }
    val orientation = configuration.payload

    return MeisterchartsDemo(this) {
      meistercharts {

        val valuesProvider0 = DefaultDoublesProvider(listOf(1.0, 2.0, 5.0, 20.0, 12.0))
        val valueRange0 = ValueRange.linear(0.0, 40.0)
        val colors0 = MultiProvider.Companion.forListModuloProvider<StackedBarPaintable.StackedBarValueIndex, Color>(Palette.chartColors)

        val valuesProvider1 = DefaultDoublesProvider(listOf(1.0, 2.0, 5.0, 20.0, 12.0))
        val valueRange1 = ValueRange.linear(0.0, 70.0)
        val colors1 = MultiProvider.Companion.forListModuloProvider<StackedBarPaintable.StackedBarValueIndex, Color>(Palette.stateColors)

        val valuesProvider2 = DefaultDoublesProvider(listOf(1.0, 2.0, -7.0, 20.0, -12.0))
        val valueRange2 = ValueRange.linear(-25.0, 30.0)
        val colors2 = MultiProvider.Companion.forListModuloProvider<StackedBarPaintable.StackedBarValueIndex, Color>(Palette.primaryColors)

        val valuesProvider3 = DefaultDoublesProvider(listOf(-1.0, -2.0, -5.0, -20.0, -12.0))
        val valueRange3 = ValueRange.linear(-70.0, 0.0)
        val colors3 = MultiProvider.Companion.forListModuloProvider<StackedBarPaintable.StackedBarValueIndex, Color>(Palette.chartColors)

        val valuesProvider4 = DefaultDoublesProvider(listOf(0.0, 2.0, 0.0, 1.0, 0.0))
        val valueRange4 = ValueRange.linear(0.0, 40.0)
        val colors4 = MultiProvider.Companion.forListModuloProvider<StackedBarPaintable.StackedBarValueIndex, Color>(Palette.chartColors)


        val paintable = StackedBarPaintable(15.0, 200.0) {
          valuesProvider = valuesProvider0
          valueRange = valueRange0
        }.also {
          it.configuration.showRemainderAsSegment = true
          it.configuration.applyOrientation(orientation)

          when (orientation) {
            Orientation.Vertical -> {
              it.width = 15.0
              it.height = 200.0
            }

            Orientation.Horizontal -> {
              it.width = 200.0
              it.height = 15.0
            }
          }
        }

        configure {
          layers.addClearBackground()
          layers.addLayer(WindowDebugLayer(LayerType.Content))
          val layer = object : AbstractLayer() {
            override val type: LayerType = LayerType.Content

            var strokeBoundingBox = false

            override fun paint(paintingContext: LayerPaintingContext) {
              when (orientation) {
                Orientation.Vertical -> paintVertical(paintingContext)
                Orientation.Horizontal -> paintHorizontal(paintingContext)
              }
            }

            private fun paintHorizontal(paintingContext: LayerPaintingContext) {
              val gc = paintingContext.gc

              gc.translate(gc.width / 2.0, 0.0)

              val deltaY = gc.height / 6.0


              //1
              gc.translate(0.0, deltaY)
              paintable.configuration.valueRange = valueRange0
              paintable.configuration.valuesProvider = valuesProvider0
              paintable.configuration.colorsProvider = colors0
              gc.saved {
                paintable.paint(paintingContext)
              }
              gc.fillText("${paintable.configuration.valueRange}, values: 0 - 40", gc.width / 2.0, 0.0, Direction.BottomCenter)
              if (strokeBoundingBox) {
                gc.stroke(Color.red)
                gc.strokeRect(paintable.boundingBox(paintingContext))
              }

              //2
              gc.translate(0.0, deltaY)
              paintable.configuration.valueRange = valueRange1
              paintable.configuration.valuesProvider = valuesProvider1
              paintable.configuration.colorsProvider = colors1
              gc.saved {
                paintable.paint(paintingContext)
              }
              gc.fillText("${paintable.configuration.valueRange}, values: 0 - 40", gc.width / 2.0, 0.0, Direction.BottomCenter)
              if (strokeBoundingBox) {
                gc.stroke(Color.red)
                gc.strokeRect(paintable.boundingBox(paintingContext))
              }

              //3
              gc.translate(0.0, deltaY)
              paintable.configuration.valueRange = valueRange2
              paintable.configuration.valuesProvider = valuesProvider2
              paintable.configuration.colorsProvider = colors2
              gc.saved {
                paintable.paint(paintingContext)
              }
              gc.fillText("${paintable.configuration.valueRange}, values: -19 - 21", gc.width / 2.0, 0.0, Direction.BottomCenter)
              if (strokeBoundingBox) {
                gc.stroke(Color.red)
                gc.strokeRect(paintable.boundingBox(paintingContext))
              }

              //4
              gc.translate(0.0, deltaY)
              paintable.configuration.valueRange = valueRange3
              paintable.configuration.valuesProvider = valuesProvider3
              paintable.configuration.colorsProvider = colors3
              gc.saved {
                paintable.paint(paintingContext)
              }
              gc.fillText("${paintable.configuration.valueRange}, values: -40 - 0", gc.width / 2.0, 0.0, Direction.BottomCenter)
              if (strokeBoundingBox) {
                gc.stroke(Color.red)
                gc.strokeRect(paintable.boundingBox(paintingContext))
              }

              //5
              gc.translate(0.0, deltaY)
              paintable.configuration.valueRange = valueRange4
              paintable.configuration.valuesProvider = valuesProvider4
              paintable.configuration.colorsProvider = colors4
              gc.saved {
                paintable.paint(paintingContext)
              }
              gc.fillText("${paintable.configuration.valueRange}, values: 0 - 40", gc.width / 2.0, 0.0, Direction.BottomCenter)
              if (strokeBoundingBox) {
                gc.stroke(Color.red)
                gc.strokeRect(paintable.boundingBox(paintingContext))
              }
            }

            private fun paintVertical(paintingContext: LayerPaintingContext) {
              val gc = paintingContext.gc

              gc.translate(0.0, gc.height / 2.0)

              val deltaX = gc.width / 6.0


              //1
              gc.translate(deltaX, 0.0)
              paintable.configuration.valueRange = valueRange0
              paintable.configuration.valuesProvider = valuesProvider0
              paintable.configuration.colorsProvider = colors0
              gc.saved {
                paintable.paint(paintingContext)
              }
              gc.fillText("${paintable.configuration.valueRange}, values: 0 - 40", 0.0, gc.height / 2.0, Direction.BottomCenter)
              if (strokeBoundingBox) {
                gc.stroke(Color.red)
                gc.strokeRect(paintable.boundingBox(paintingContext))
              }

              //2
              gc.translate(deltaX, 0.0)
              paintable.configuration.valueRange = valueRange1
              paintable.configuration.valuesProvider = valuesProvider1
              paintable.configuration.colorsProvider = colors1
              gc.saved {
                paintable.paint(paintingContext)
              }
              gc.fillText("${paintable.configuration.valueRange}, values: 0 - 40", 0.0, gc.height / 2.0, Direction.BottomCenter)
              if (strokeBoundingBox) {
                gc.stroke(Color.red)
                gc.strokeRect(paintable.boundingBox(paintingContext))
              }

              //3
              gc.translate(deltaX, 0.0)
              paintable.configuration.valueRange = valueRange2
              paintable.configuration.valuesProvider = valuesProvider2
              paintable.configuration.colorsProvider = colors2
              gc.saved {
                paintable.paint(paintingContext)
              }
              gc.fillText("${paintable.configuration.valueRange}, values: -19 - 21", 0.0, gc.height / 2.0, Direction.BottomCenter)
              if (strokeBoundingBox) {
                gc.stroke(Color.red)
                gc.strokeRect(paintable.boundingBox(paintingContext))
              }

              //4
              gc.translate(deltaX, 0.0)
              paintable.configuration.valueRange = valueRange3
              paintable.configuration.valuesProvider = valuesProvider3
              paintable.configuration.colorsProvider = colors3
              gc.saved {
                paintable.paint(paintingContext)
              }
              gc.fillText("${paintable.configuration.valueRange}, values: -40 - 0", 0.0, gc.height / 2.0, Direction.BottomCenter)
              if (strokeBoundingBox) {
                gc.stroke(Color.red)
                gc.strokeRect(paintable.boundingBox(paintingContext))
              }

              //5
              gc.translate(deltaX, 0.0)
              paintable.configuration.valueRange = valueRange4
              paintable.configuration.valuesProvider = valuesProvider4
              paintable.configuration.colorsProvider = colors4
              gc.saved {
                paintable.paint(paintingContext)
              }
              gc.fillText("${paintable.configuration.valueRange}, values: 0 - 40", 0.0, gc.height / 2.0, Direction.BottomCenter)
              if (strokeBoundingBox) {
                gc.stroke(Color.red)
                gc.strokeRect(paintable.boundingBox(paintingContext))
              }
            }
          }
          layers.addLayer(layer)

          section("Debug")
          configurable("Show Bounding Box", layer::strokeBoundingBox)


          section("Paintable")

          configurable("width", paintable::width) {
            max = 200.0
          }

          configurable("height", paintable::height) {
            max = 600.0
            value = paintable.height
          }

          section("Segment")
          configurable("Segments Gap", paintable.style::segmentsGap) {
            min = 0.0
            max = 10.0
          }

          configurableDouble("Segments Radii", paintable.configuration.segmentRadii.topLeft) {
            max = 10.0

            onChange {
              paintable.configuration.segmentRadii = BorderRadius.of(it)
              this@MeisterchartsDemo.markAsDirty()
            }
          }

          section("Remainder")
          configurable("Show Remainder", paintable.style::showRemainderAsSegment)
          configurable("Remainder Line Width", paintable.style::remainderSegmentBorderLineWidth) {
            max = 10.0
          }

          section("Value Label")

          configurable("Value Label Gap Horizontal", paintable.style::valueLabelGapHorizontal) {
            min = 0.0
            max = 10.0
          }
          configurable("Value Label Gap Vertical", paintable.style::valueLabelGapVertical) {
            min = 0.0
            max = 10.0
          }
          configurable("Visible", paintable.style::showValueLabels)
          configurable("Anchor Direction", paintable.style::valueLabelAnchorDirection, kotlin.enums.enumEntries<Direction>().toList())
          configurable("Font", paintable.style::valueLabelFont)


          section("Background")

          configurable("bg radius", paintable.style::backgroundRadius) {
            max = 10.0
          }

          configurable("Paint background", paintable.style::paintBackground)
          configurableColorProvider("BG", paintable.style::backgroundColor)
          configurable("line width", paintable.style::backgroundLineWidth) {
            max = 10.0
          }
          configurable("BG border", paintable.style::backgroundBorderColor)
        }
      }
    }
  }
}
