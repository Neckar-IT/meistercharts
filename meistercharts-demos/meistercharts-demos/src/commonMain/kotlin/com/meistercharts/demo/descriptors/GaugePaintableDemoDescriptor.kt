package com.meistercharts.demo.descriptors

import com.meistercharts.algorithms.ValueRange
import com.meistercharts.algorithms.layers.AbstractLayer
import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.algorithms.layers.LayerType
import com.meistercharts.algorithms.layers.addClearBackground
import com.meistercharts.algorithms.layers.compass.ArrowValuePainter
import com.meistercharts.algorithms.layers.compass.CompassBasePainter
import com.meistercharts.algorithms.layers.compass.CompassTriangleValuePainter
import com.meistercharts.algorithms.layers.compass.GaugePaintable
import com.meistercharts.algorithms.layers.compass.LineValuePainter
import com.meistercharts.algorithms.layers.compass.ModernCompassPainter
import com.meistercharts.algorithms.painter.Color
import com.meistercharts.annotations.Domain
import com.meistercharts.canvas.paintLocation
import com.meistercharts.canvas.saved
import com.meistercharts.demo.ChartingDemo
import com.meistercharts.demo.ChartingDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.configurableColor
import com.meistercharts.demo.configurableDouble
import com.meistercharts.demo.configurableEnum
import com.meistercharts.demo.configurableInt
import com.meistercharts.demo.configurableList
import com.meistercharts.model.RotationDirection
import com.meistercharts.model.Size
import com.meistercharts.provider.ValueRangeProvider

class GaugePaintableDemoDescriptor : ChartingDemoDescriptor<Nothing> {
  override val name: String = "Compass"
  override val description: String = "## draws a compass"
  override val category: DemoCategory = DemoCategory.Paintables

  override fun createDemo(configuration: PredefinedConfiguration<Nothing>?): ChartingDemo {

    return ChartingDemo {
      meistercharts {

        configure {
          layers.addClearBackground()

          @Domain var startValueRange = 0.0
          @Domain var endValueRange = 200.0


          val valueRangeProviders = listOf<ValueRangeProvider>(
            { ValueRange.linear(startValueRange, endValueRange) },
            { ValueRange.degrees }
          )

          var currentValueRangeProvider = valueRangeProviders[0]

          @Domain var currentValue = 0.0


          val arrowValuePainter = ArrowValuePainter {
            pointerLineWidth = 3.0
          }
          val lineValuePainter = LineValuePainter()
          val compassTriangleValuePainter = CompassTriangleValuePainter()

          val valuePainters = listOf(arrowValuePainter, lineValuePainter, compassTriangleValuePainter)

          val compassBasePainter = CompassBasePainter() {
            backgroundColor = Color.color(0.9, 0.9, 1.0, 0.5)
            numberInnerCircles = 3
          }
          val modernCompassPainter = ModernCompassPainter()
          val basePainters = listOf(modernCompassPainter, compassBasePainter)

          val paintable = GaugePaintable(
            currentValueRangeProvider,
            { currentValue },
            Size(300.0, 300.0)
          ) {
            basePainter = modernCompassPainter
            valuePainter = arrowValuePainter

            startAt = 3.0 * kotlin.math.PI / 4.0
            extend = 3.0 * kotlin.math.PI / 2.0
            rotationDirection = RotationDirection.Clockwise
          }

          val style = paintable.style

          layers.addLayer(object : AbstractLayer() {
            override val type: LayerType
              get() = LayerType.Content

            override fun paint(paintingContext: LayerPaintingContext) {
              val gc = paintingContext.gc

              gc.saved {
                gc.translateToCenter()
                gc.paintLocation()
                paintable.paint(paintingContext, 0.0, 0.0)
              }
            }
          })


          configurableList("Base Painter", style.basePainter, basePainters) {
            converter = {
              it::class.simpleName ?: toString()
            }

            onChange {
              style.basePainter = it
              markAsDirty()
            }
          }

          configurableList("Value Painter", style.valuePainter, valuePainters) {
            converter = {
              it::class.simpleName ?: toString()
            }

            onChange {
              style.valuePainter = it
              markAsDirty()
            }
          }
          configurableList("ValueRange/Ticks", currentValueRangeProvider, valueRangeProviders) {
            converter = { valueRangeProvider ->
              when (valueRangeProviders.indexOf(valueRangeProvider)) {
                0 -> "Manual"
                1 -> "Degree"
                else -> valueRangeProvider.toString()
              }

            }
            onChange {
              currentValueRangeProvider = it
              markAsDirty()
            }
          }


          configurableDouble("currentValue", currentValue) {
            min = 0.0
            max = 200.0

            onChange {
              currentValue = it
              markAsDirty()
            }
          }

          configurableDouble("startValueRange", startValueRange) {
            min = 0.0
            max = 200.0

            onChange {
              startValueRange = it
              markAsDirty()
            }
          }

          configurableDouble("endValueRange", endValueRange) {
            min = 0.0
            max = 400.0

            onChange {
              endValueRange = it
              markAsDirty()
            }
          }

          configurableDouble("startAt", style::startAt) {
            max = 7.0
          }

          configurableDouble("extend", style::extend) {
            max = 7.0
          }

          configurableEnum("direction", style::rotationDirection, enumValues()) {
          }

          configurableDouble("width/height", paintable.size.height) {
            max = 800.0

            onChange {
              paintable.size = Size(it, it)
              markAsDirty()
            }
          }

          declare {
            section("Compass Base")
          }

          configurableColor("bg color", compassBasePainter.style::backgroundColor) {
          }

          configurableInt("numberInnerCircles", compassBasePainter.style::numberInnerCircles) {
            max = 20
          }

          configurableColor("compass color", compassBasePainter.style::compassColor) {
          }

          configurableColor("labels color", compassBasePainter.style::labelsColor) {
          }

          configurableColor("tick color", compassBasePainter.style::tickColor) {
          }

          declare {
            section("Arrow Pointer")
          }

          configurableDouble("pointerWidth", arrowValuePainter.style::pointerLineWidth) {
            max = 10.0
          }

          configurableColor("pointer color", arrowValuePainter.style::pointerColor) {
          }

          declare {
            section("Line Pointer")
          }

          configurableDouble("pointerWidth", lineValuePainter.style::pointerWidth) {
            max = 10.0
          }

          configurableColor("pointer color", lineValuePainter.style::pointerColor) {
          }

          configurableDouble("lineGap", lineValuePainter.style::lineGap) {
            max = 20.0
          }
        }
      }
    }
  }
}
