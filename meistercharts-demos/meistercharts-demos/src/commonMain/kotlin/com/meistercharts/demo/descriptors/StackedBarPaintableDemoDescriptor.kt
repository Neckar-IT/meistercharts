package com.meistercharts.demo.descriptors

import com.meistercharts.model.Orientation
import com.meistercharts.algorithms.ValueRange
import com.meistercharts.algorithms.layers.AbstractLayer
import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.algorithms.layers.LayerType
import com.meistercharts.algorithms.layers.addClearBackground
import com.meistercharts.algorithms.layers.barchart.StackedBarPaintable
import com.meistercharts.algorithms.layers.debug.WindowDebugLayer
import com.meistercharts.algorithms.painter.Color
import com.meistercharts.canvas.BorderRadius
import com.meistercharts.canvas.saved
import com.meistercharts.demo.ChartingDemo
import com.meistercharts.demo.ChartingDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.configurableBoolean
import com.meistercharts.demo.configurableColor
import com.meistercharts.demo.configurableColorPicker
import com.meistercharts.demo.configurableDouble
import com.meistercharts.demo.configurableEnum
import com.meistercharts.demo.configurableFont
import com.meistercharts.demo.createEnumConfigs
import com.meistercharts.demo.section
import com.meistercharts.model.Direction
import it.neckar.open.provider.DefaultDoublesProvider
import it.neckar.open.provider.MultiProvider
import com.meistercharts.style.Palette

/**
 */
class StackedBarPaintableDemoDescriptor : ChartingDemoDescriptor<Orientation> {
  override val name: String = "Stacked Bar paintable"
  override val description: String = "Demo that  demontrates the stacked bar paintable"

  override val category: DemoCategory = DemoCategory.Paintables

  override val predefinedConfigurations: List<PredefinedConfiguration<Orientation>> = createEnumConfigs()

  override fun createDemo(configuration: PredefinedConfiguration<Orientation>?): ChartingDemo {
    require(configuration != null) { "Orientation required" }
    val orientation = configuration.payload

    return ChartingDemo {
      meistercharts {

        val valuesProvider0 = DefaultDoublesProvider(listOf(1.0, 2.0, 5.0, 20.0, 12.0))
        val valueRange0 = ValueRange.linear(0.0, 40.0)
        val colors0 = MultiProvider.Companion.forListModulo<StackedBarPaintable.StackedBarValueIndex, Color>(Palette.chartColors)

        val valuesProvider1 = DefaultDoublesProvider(listOf(1.0, 2.0, 5.0, 20.0, 12.0))
        val valueRange1 = ValueRange.linear(0.0, 70.0)
        val colors1 = MultiProvider.Companion.forListModulo<StackedBarPaintable.StackedBarValueIndex, Color>(Palette.stateColors)

        val valuesProvider2 = DefaultDoublesProvider(listOf(1.0, 2.0, -7.0, 20.0, -12.0))
        val valueRange2 = ValueRange.linear(-25.0, 30.0)
        val colors2 = MultiProvider.Companion.forListModulo<StackedBarPaintable.StackedBarValueIndex, Color>(Palette.primaryColors)

        val valuesProvider3 = DefaultDoublesProvider(listOf(-1.0, -2.0, -5.0, -20.0, -12.0))
        val valueRange3 = ValueRange.linear(-70.0, 0.0)
        val colors3 = MultiProvider.Companion.forListModulo<StackedBarPaintable.StackedBarValueIndex, Color>(Palette.chartColors)

        val valuesProvider4 = DefaultDoublesProvider(listOf(0.0, 2.0, 0.0, 1.0, 0.0))
        val valueRange4 = ValueRange.linear(0.0, 40.0)
        val colors4 = MultiProvider.Companion.forListModulo<StackedBarPaintable.StackedBarValueIndex, Color>(Palette.chartColors)


        val paintable = StackedBarPaintable(StackedBarPaintable.Data(valuesProvider0, valueRange0), 15.0, 200.0).also {
          it.style.showRemainderAsSegment = true
          it.style.applyOrientation(orientation)

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
              paintable.data.valueRange = valueRange0
              paintable.data.valuesProvider = valuesProvider0
              paintable.style.colorsProvider = colors0
              gc.saved {
                paintable.paint(paintingContext)
              }
              gc.fillText("${paintable.data.valueRange}, values: 0 - 40", gc.width / 2.0, 0.0, Direction.BottomCenter)
              if (strokeBoundingBox) {
                gc.stroke(Color.red)
                gc.strokeRect(paintable.boundingBox(paintingContext))
              }

              //2
              gc.translate(0.0, deltaY)
              paintable.data.valueRange = valueRange1
              paintable.data.valuesProvider = valuesProvider1
              paintable.style.colorsProvider = colors1
              gc.saved {
                paintable.paint(paintingContext)
              }
              gc.fillText("${paintable.data.valueRange}, values: 0 - 40", gc.width / 2.0, 0.0, Direction.BottomCenter)
              if (strokeBoundingBox) {
                gc.stroke(Color.red)
                gc.strokeRect(paintable.boundingBox(paintingContext))
              }

              //3
              gc.translate(0.0, deltaY)
              paintable.data.valueRange = valueRange2
              paintable.data.valuesProvider = valuesProvider2
              paintable.style.colorsProvider = colors2
              gc.saved {
                paintable.paint(paintingContext)
              }
              gc.fillText("${paintable.data.valueRange}, values: -19 - 21", gc.width / 2.0, 0.0, Direction.BottomCenter)
              if (strokeBoundingBox) {
                gc.stroke(Color.red)
                gc.strokeRect(paintable.boundingBox(paintingContext))
              }

              //4
              gc.translate(0.0, deltaY)
              paintable.data.valueRange = valueRange3
              paintable.data.valuesProvider = valuesProvider3
              paintable.style.colorsProvider = colors3
              gc.saved {
                paintable.paint(paintingContext)
              }
              gc.fillText("${paintable.data.valueRange}, values: -40 - 0", gc.width / 2.0, 0.0, Direction.BottomCenter)
              if (strokeBoundingBox) {
                gc.stroke(Color.red)
                gc.strokeRect(paintable.boundingBox(paintingContext))
              }

              //5
              gc.translate(0.0, deltaY)
              paintable.data.valueRange = valueRange4
              paintable.data.valuesProvider = valuesProvider4
              paintable.style.colorsProvider = colors4
              gc.saved {
                paintable.paint(paintingContext)
              }
              gc.fillText("${paintable.data.valueRange}, values: 0 - 40", gc.width / 2.0, 0.0, Direction.BottomCenter)
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
              paintable.data.valueRange = valueRange0
              paintable.data.valuesProvider = valuesProvider0
              paintable.style.colorsProvider = colors0
              gc.saved {
                paintable.paint(paintingContext)
              }
              gc.fillText("${paintable.data.valueRange}, values: 0 - 40", 0.0, gc.height / 2.0, Direction.BottomCenter)
              if (strokeBoundingBox) {
                gc.stroke(Color.red)
                gc.strokeRect(paintable.boundingBox(paintingContext))
              }

              //2
              gc.translate(deltaX, 0.0)
              paintable.data.valueRange = valueRange1
              paintable.data.valuesProvider = valuesProvider1
              paintable.style.colorsProvider = colors1
              gc.saved {
                paintable.paint(paintingContext)
              }
              gc.fillText("${paintable.data.valueRange}, values: 0 - 40", 0.0, gc.height / 2.0, Direction.BottomCenter)
              if (strokeBoundingBox) {
                gc.stroke(Color.red)
                gc.strokeRect(paintable.boundingBox(paintingContext))
              }

              //3
              gc.translate(deltaX, 0.0)
              paintable.data.valueRange = valueRange2
              paintable.data.valuesProvider = valuesProvider2
              paintable.style.colorsProvider = colors2
              gc.saved {
                paintable.paint(paintingContext)
              }
              gc.fillText("${paintable.data.valueRange}, values: -19 - 21", 0.0, gc.height / 2.0, Direction.BottomCenter)
              if (strokeBoundingBox) {
                gc.stroke(Color.red)
                gc.strokeRect(paintable.boundingBox(paintingContext))
              }

              //4
              gc.translate(deltaX, 0.0)
              paintable.data.valueRange = valueRange3
              paintable.data.valuesProvider = valuesProvider3
              paintable.style.colorsProvider = colors3
              gc.saved {
                paintable.paint(paintingContext)
              }
              gc.fillText("${paintable.data.valueRange}, values: -40 - 0", 0.0, gc.height / 2.0, Direction.BottomCenter)
              if (strokeBoundingBox) {
                gc.stroke(Color.red)
                gc.strokeRect(paintable.boundingBox(paintingContext))
              }

              //5
              gc.translate(deltaX, 0.0)
              paintable.data.valueRange = valueRange4
              paintable.data.valuesProvider = valuesProvider4
              paintable.style.colorsProvider = colors4
              gc.saved {
                paintable.paint(paintingContext)
              }
              gc.fillText("${paintable.data.valueRange}, values: 0 - 40", 0.0, gc.height / 2.0, Direction.BottomCenter)
              if (strokeBoundingBox) {
                gc.stroke(Color.red)
                gc.strokeRect(paintable.boundingBox(paintingContext))
              }
            }
          }
          layers.addLayer(layer)

          section("Debug")
          configurableBoolean("Show Bounding Box", layer::strokeBoundingBox)


          section("Paintable")

          configurableDouble("width", paintable::width) {
            max = 200.0
          }

          configurableDouble("height", paintable::height) {
            max = 600.0
            value = paintable.height
          }

          section("Segment")
          configurableDouble("Segments Gap", paintable.style::segmentsGap) {
            min = 0.0
            max = 10.0
          }

          configurableDouble("Segments Radii", paintable.style.segmentRadii.topLeft) {
            max = 10.0

            onChange {
              paintable.style.segmentRadii = BorderRadius.of(it)
              this@ChartingDemo.markAsDirty()
            }
          }

          section("Remainder")
          configurableBoolean("Show Remainder", paintable.style::showRemainderAsSegment)
          configurableDouble("Remainder Line Width", paintable.style::remainderSegmentBorderLineWidth) {
            max = 10.0
          }

          section("Value Label")

          configurableDouble("Value Label Gap Horizontal", paintable.style::valueLabelGapHorizontal) {
            min = 0.0
            max = 10.0
          }
          configurableDouble("Value Label Gap Vertical", paintable.style::valueLabelGapVertical) {
            min = 0.0
            max = 10.0
          }
          configurableBoolean("Visible", paintable.style::showValueLabels)
          configurableEnum("Anchor Direction", paintable.style::valueLabelAnchorDirection, enumValues())
          configurableFont("Font", paintable.style::valueLabelFont)


          section("Background")

          configurableDouble("bg radius", paintable.style::backgroundRadius) {
            max = 10.0
          }

          configurableBoolean("Paint background", paintable.style::paintBackground)
          configurableColor("BG", paintable.style::backgroundColor)
          configurableDouble("line width", paintable.style::backgroundLineWidth) {
            max = 10.0
          }
          configurableColorPicker("BG border", paintable.style::backgroundBorderColor)
        }
      }
    }
  }
}
