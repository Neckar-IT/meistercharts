package com.meistercharts.demo.descriptors

import com.meistercharts.algorithms.layers.addClearBackground
import com.meistercharts.algorithms.layers.hudLayer
import com.meistercharts.algorithms.layers.withMaxNumberOfTicks
import com.meistercharts.annotations.Domain
import com.meistercharts.demo.ChartingDemo
import com.meistercharts.demo.ChartingDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.configurableBooleanProvider
import com.meistercharts.demo.configurableColorPickerProvider
import com.meistercharts.demo.configurableDouble
import com.meistercharts.demo.configurableEnum
import com.meistercharts.demo.configurableInsets
import com.meistercharts.demo.configurableInt
import com.meistercharts.model.Side
import it.neckar.open.provider.DoublesProvider

/**
 * Very simple demo that shows how to work with a value axis layer
 */
class ValueAxisWithHudDemoDescriptor : ChartingDemoDescriptor<ValueAxisDemoConfig> {
  override val name: String = "Value axis - with HUD"

  //language=HTML
  override val description: String = "<h3>Visualizes a value axis</h3>"

  override val category: DemoCategory = DemoCategory.Axis

  override val predefinedConfigurations: List<PredefinedConfiguration<ValueAxisDemoConfig>> = ValueAxisDemoConfig.createConfigs()

  override fun createDemo(configuration: PredefinedConfiguration<ValueAxisDemoConfig>?): ChartingDemo {
    require(configuration != null)

    return ChartingDemo {

      val valueAxisLayer = configuration.payload.createValueAxis()

      meistercharts {
        configure {
          layers.addClearBackground()

          val values = object {
            var domainValue0: @Domain Double = 85.0
            var domainValue1: @Domain Double = 15.0
            var domainValue2: @Domain Double = 105.0
          }

          valueAxisLayer.style.apply {
            ticks = ticks.withMaxNumberOfTicks(10)
          }
          layers.addLayer(valueAxisLayer)
          val hudLayer = valueAxisLayer.hudLayer(
            domainValues = object : DoublesProvider {
              override fun valueAt(index: Int): Double {
                return when (index) {
                  0 -> values.domainValue0
                  1 -> values.domainValue1
                  2 -> values.domainValue2
                  else -> throw IllegalArgumentException("invalid index $index")
                }
              }

              override fun size(): Int {
                return 3
              }
            }
          )
          layers.addLayer(hudLayer)

          configurableDouble("@DomainRelative Value 0", values::domainValue0) {
            max = 150.0
          }
          configurableDouble("@DomainRelative Value 1", values::domainValue1) {
            max = 150.0
          }
          configurableDouble("@DomainRelative Value 2", values::domainValue2) {
            max = 150.0
          }

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
          }

          configurableInsets("Axis margin", valueAxisLayer.style::margin) {
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
            section("Axis")
          }

          configurableColorPickerProvider("Line color", valueAxisLayer.style::lineColor) {
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
        }
      }
    }
  }
}
