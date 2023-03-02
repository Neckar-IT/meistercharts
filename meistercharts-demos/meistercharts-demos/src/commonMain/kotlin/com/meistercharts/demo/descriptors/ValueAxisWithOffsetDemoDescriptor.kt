package com.meistercharts.demo.descriptors

import com.meistercharts.algorithms.ValueRange
import com.meistercharts.algorithms.layers.AbstractLayer
import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.algorithms.layers.LayerType
import com.meistercharts.algorithms.layers.PasspartoutLayer
import com.meistercharts.algorithms.layers.ValueAxisWithOffsetLayer
import com.meistercharts.algorithms.layers.addClearBackground
import com.meistercharts.algorithms.layers.bind
import com.meistercharts.algorithms.painter.Color
import com.meistercharts.annotations.Domain
import com.meistercharts.canvas.layout.cache.DoubleCache
import com.meistercharts.canvas.paintTextBox
import com.meistercharts.demo.ChartingDemo
import com.meistercharts.demo.ChartingDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.configurableBooleanProvider
import com.meistercharts.demo.configurableColorPickerProvider
import com.meistercharts.demo.configurableDouble
import com.meistercharts.demo.configurableEnum
import com.meistercharts.demo.configurableFont
import com.meistercharts.demo.configurableInsets
import com.meistercharts.demo.configurableInt
import com.meistercharts.model.Direction
import com.meistercharts.model.Vicinity
import it.neckar.open.formatting.decimalFormat
import it.neckar.open.formatting.format

/**
 * Very simple demo that shows how to work with a value axis layer
 */
class ValueAxisWithOffsetDemoDescriptor : ChartingDemoDescriptor<Nothing> {
  override val name: String = "Value axis with offset"

  //language=HTML
  override val description: String = "<h3>Visualizes a value axis with offset</h3>"

  override val category: DemoCategory = DemoCategory.Axis

  override fun createDemo(configuration: PredefinedConfiguration<Nothing>?): ChartingDemo {
    return ChartingDemo {

      val valueAxisWithOffsetLayer = ValueAxisWithOffsetLayer("Title", ValueRange.linear(-200.0, 200.0))

      meistercharts {
        configure {
          layers.addClearBackground()

          val passpartoutLayer = PasspartoutLayer {
            color = { Color("rgba(69, 204, 112, 0.25)") } // use something different from white so the size of the axis can be better grasped
          }
          layers.addLayer(passpartoutLayer)
          layers.addLayer(valueAxisWithOffsetLayer)

          layers.addLayer(MyValueAxisWithOffsetDebugLayer(valueAxisWithOffsetLayer))

          passpartoutLayer.style.bind(valueAxisWithOffsetLayer.style)

          declare {
            section("Layout")
          }

          configurableInt("Space for Digits", valueAxisWithOffsetLayer.style::spaceForDigits) {
            min = 2
            onChange {
              valueAxisWithOffsetLayer.style.ticksFormat = decimalFormat(it)
            }
          }

          configurableDouble("Offset area size", valueAxisWithOffsetLayer.style::offsetAreaSize) {
            max = 500.0
          }

          configurableDouble("Axis size", valueAxisWithOffsetLayer.style::size) {
            value = 150.0
            max = 500.0
          }

          configurableInsets("Axis margin", valueAxisWithOffsetLayer.style::margin) {
          }

          declare {
            section("Title")
          }

          configurableBooleanProvider("Show Title", valueAxisWithOffsetLayer.style::titleVisible) {
          }

          configurableDouble("Title Gap", valueAxisWithOffsetLayer.style::titleGap) {
            max = 20.0
          }

          declare {
            section("Axis Config")
          }

          configurableEnum("Tick Orientation", valueAxisWithOffsetLayer.style::tickOrientation, enumValues()) {
            value = Vicinity.Outside
          }
          configurableEnum("Axis End", valueAxisWithOffsetLayer.style::axisEndConfiguration, enumValues()) {
          }

          declare {
            section("Axis")
          }

          configurableColorPickerProvider("Line color", valueAxisWithOffsetLayer.style::lineColor) {
          }

          configurableDouble("Axis line width", valueAxisWithOffsetLayer.style::axisLineWidth) {
            max = 20.0
          }
          configurableDouble("Tick length", valueAxisWithOffsetLayer.style::tickLength) {
            max = 20.0
          }
          configurableDouble("Tick width", valueAxisWithOffsetLayer.style::tickLineWidth) {
            max = 20.0
          }
          configurableDouble("Tick Label Gap", valueAxisWithOffsetLayer.style::tickLabelGap) {
            max = 20.0
          }

          configurableColorPickerProvider("Background Color", passpartoutLayer.style::color) {
          }

          configurableFont("Tick font", valueAxisWithOffsetLayer.style::tickFont) {
          }

          configurableFont("Title font", valueAxisWithOffsetLayer.style::titleFont) {
          }
        }
      }
    }
  }
}


private class MyValueAxisWithOffsetDebugLayer(val valueAxisWithOffsetLayer: ValueAxisWithOffsetLayer) : AbstractLayer() {
  override val type: LayerType = LayerType.Content

  override fun paint(paintingContext: LayerPaintingContext) {
    val lastTicks: @Domain DoubleCache = valueAxisWithOffsetLayer.paintingVariables().tickDomainValues

    val paintingVariables = valueAxisWithOffsetLayer.paintingVariables()

    val lines = mutableListOf<String>().also {
      it.add("Axis Start: ${paintingVariables.axisStart}")
      it.add("Axis End: ${paintingVariables.axisEnd}")
      it.add("Start Value: ${paintingVariables.startDomainValue}")
      it.add("End Value: ${paintingVariables.endDomainValue}")

      it.add("---")

      it.add("Last Ticks: ${lastTicks.size}")
      lastTicks.fastForEach { tick ->
        it.add(tick.format(10, 0))
      }

      it.add("---")

      it.add("Offset Ticks: ${paintingVariables.offsetTicks.size}")

      paintingVariables.offsetTicks.fastForEach { tick ->
        it.add(tick.format(10, 0))
      }
    }

    val gc = paintingContext.gc
    gc.translate(gc.width, 0.0)
    gc.paintTextBox(
      lines, Direction.TopRight,
      20.0,
    )
  }
}
