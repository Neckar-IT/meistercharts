package com.meistercharts.demo.descriptors

import com.meistercharts.algorithms.ValueRange
import com.meistercharts.algorithms.axis.AxisEndConfiguration
import com.meistercharts.algorithms.layers.AxisStyle
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
import com.meistercharts.demo.configurableBoolean
import com.meistercharts.demo.configurableColor
import com.meistercharts.demo.configurableDouble
import com.meistercharts.demo.configurableEnum
import com.meistercharts.demo.configurableFont
import com.meistercharts.demo.configurableInsets
import com.meistercharts.demo.configurableInt
import com.meistercharts.model.Side
import com.meistercharts.model.Vicinity
import it.neckar.open.provider.BooleanProvider

/**
 * Very simple demo that shows how to work with a value axis layer
 */
class ValueAxisMaxTicksDemoDescriptor() : ChartingDemoDescriptor<Nothing> {
  override val name: String = "Value axis with max ticks"
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

          val valueRange = ValueRange.linear(0.0, 100.0)
          val valueAxisLayer = ValueAxisLayer(ValueAxisLayer.Data(valueRangeProvider = { valueRange })) {
            titleProvider = { _, _ -> "The Äxisq [m²/h]" }
          }

          layers.addLayer(valueAxisLayer)

          passpartoutLayer.style.bind(valueAxisLayer.style)

          declare {
            section("Layout")
          }

          var maxTickCount = 5

          configurableInt("Max Tick count") {
            max = 15
            value = maxTickCount

            onChange {
              maxTickCount = it
              valueAxisLayer.style.ticks = valueAxisLayer.style.ticks.withMaxNumberOfTicks(maxTickCount)
              markAsDirty()
            }
          }

          configurableEnum("Side", valueAxisLayer.style.side, Side.values()) {
            onChange {
              valueAxisLayer.style.side = it
              markAsDirty()
            }
          }

          configurableDouble("Width", valueAxisLayer.style::size) {
            max = 500.0
          }

          configurableInsets("Margin", valueAxisLayer.style::margin) {
          }

          declare {
            section("Title")
          }

          configurableBoolean("Show Title") {
            value = valueAxisLayer.style.titleVisible()
            onChange {
              valueAxisLayer.style.titleVisible = BooleanProvider(it)
              markAsDirty()
            }
          }

          configurableDouble("Title Gap", valueAxisLayer.style::titleGap) {
            max = 20.0
          }

          declare {
            section("Axis Config")
          }

          configurableEnum("Paint Range", valueAxisLayer.style.paintRange, AxisStyle.PaintRange.values()) {
            onChange {
              valueAxisLayer.style.paintRange = it
              markAsDirty()
            }
          }
          configurableEnum("Tick Orientation", valueAxisLayer.style.tickOrientation, Vicinity.values()) {
            onChange {
              valueAxisLayer.style.tickOrientation = it
              markAsDirty()
            }
          }
          configurableEnum("Axis End", valueAxisLayer.style.axisEndConfiguration, AxisEndConfiguration.values()) {
            onChange {
              valueAxisLayer.style.axisEndConfiguration = it
              markAsDirty()
            }
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

          configurableColor("Background Color", passpartoutLayer.style.color()) {
            onChange {
              passpartoutLayer.style.color = { it }
              markAsDirty()
            }
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
