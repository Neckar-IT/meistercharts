package com.meistercharts.demo.descriptors

import com.meistercharts.algorithms.ValueRange
import com.meistercharts.algorithms.layers.PasspartoutLayer
import com.meistercharts.algorithms.layers.ValueAxisLayer
import com.meistercharts.algorithms.layers.addClearBackground
import com.meistercharts.algorithms.layers.bind
import com.meistercharts.algorithms.layers.withMaxNumberOfTicks
import com.meistercharts.algorithms.painter.Color
import com.meistercharts.demo.ChartingDemo
import com.meistercharts.demo.ChartingDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.configurableBooleanProvider
import com.meistercharts.demo.configurableColorPickerProvider
import com.meistercharts.demo.configurableDouble
import com.meistercharts.demo.configurableEnum
import com.meistercharts.demo.configurableFont
import com.meistercharts.demo.configurableInt
import com.meistercharts.model.Side

/**
 * Very simple demo that shows how to work with a value axis layer
 */
class ExtremeValueAxisDemoDescriptor(
) : ChartingDemoDescriptor<Nothing> {
  override val name: String = "Extreme Value axis"
  override val category: DemoCategory = DemoCategory.Axis

  override fun createDemo(configuration: PredefinedConfiguration<Nothing>?): ChartingDemo {
    return ChartingDemo {

      meistercharts {
        configure {
          layers.addClearBackground()

          val passpartoutLayer = PasspartoutLayer {
            color = { Color("rgba(69, 204, 112, 0.25)") } // use something different from white so the size of the axis can be better grasped
          }
          layers.addLayer(passpartoutLayer)

          val valueRange = ValueRange.linear(1.594979395468E12, 1.594979395468E12 + 1000 * 60 * 60)
          val valueAxisLayer = ValueAxisLayer(ValueAxisLayer.Data(valueRangeProvider = { valueRange })) {
            titleProvider = { _, _ -> "The Äxisq [m²/h]" }
            ticks = ticks.withMaxNumberOfTicks(10)
          }.also {
            layers.addLayer(it)
          }

          passpartoutLayer.style.bind(valueAxisLayer.style)

          declare {
            section("Layout")
          }

          configurableEnum("Side", valueAxisLayer.style.side, Side.values()) {
            onChange {
              valueAxisLayer.style.side = it
              markAsDirty()
            }
          }

          configurableInt("Max tick count") {
            min = 0
            max = 50
            value = 10
            onChange {
              valueAxisLayer.style.ticks = valueAxisLayer.style.ticks.withMaxNumberOfTicks(it)
              markAsDirty()
            }
          }

          configurableDouble("Axis size", valueAxisLayer.style::size) {
            max = 500.0

            onChange {
              valueAxisLayer.style.size = it
              markAsDirty()
            }
          }

          declare {
            section("Title")
          }

          configurableBooleanProvider("Show Title", valueAxisLayer.style::titleVisible) {
          }

          configurableDouble("Title Gap", valueAxisLayer.style::titleGap) {
            max = 20.0
          }

          declare {
            section("Axis Config")
          }

          configurableEnum("Paint Range", valueAxisLayer.style::paintRange, enumValues()) {
          }
          configurableEnum("Tick Orientation", valueAxisLayer.style::tickOrientation, enumValues()) {
          }
          configurableEnum("Axis End", valueAxisLayer.style::axisEndConfiguration, enumValues()) {
          }

          declare {
            section("Widths")
          }

          configurableDouble("Axis line width", valueAxisLayer.style::axisLineWidth) {
            max = 20.0
          }
          configurableDouble("Tick length", valueAxisLayer.style::tickLength) {
            max = 20.0
          }
          configurableDouble("Tick width", valueAxisLayer.style::tickLineWidth) {
            max = 20.0
          }
          configurableDouble("Tick Label Gap", valueAxisLayer.style::tickLabelGap) {
            max = 20.0
          }

          configurableColorPickerProvider("Background Color", passpartoutLayer.style::color) {
          }

          configurableFont("Tick font", valueAxisLayer.style::tickFont) {
          }

          configurableFont("Title font", valueAxisLayer.style::titleFont) {
          }
        }
      }
    }
  }
}
