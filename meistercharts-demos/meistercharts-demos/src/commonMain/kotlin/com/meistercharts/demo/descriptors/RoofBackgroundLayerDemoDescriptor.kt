package com.meistercharts.demo.descriptors

import com.meistercharts.algorithms.layers.addClearBackground
import com.meistercharts.demo.ChartingDemo
import com.meistercharts.demo.ChartingDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.configurableDouble
import com.meistercharts.model.Size
import com.meistercharts.charts.lizergy.planning.RoofBackgroundLayer
import it.neckar.open.observable.ObservableDouble
import kotlin.math.roundToInt

/**
 *
 */
class RoofBackgroundLayerDemoDescriptor : ChartingDemoDescriptor<Nothing> {
  override val name: String = "PV Roof background"
  override val category: DemoCategory = DemoCategory.Layers

  override fun createDemo(configuration: PredefinedConfiguration<Nothing>?): ChartingDemo {
    return ChartingDemo {
      meistercharts {
        configure {
          layers.addClearBackground()

          val roofWidth = ObservableDouble(15_000.0)
          val roofHeight = ObservableDouble(7_000.0)

          val layer = RoofBackgroundLayer(RoofBackgroundLayer.Data {
            Size(roofWidth.value.roundToInt(), roofHeight.value.roundToInt())
          })
          layers.addLayer(layer)

          configurableDouble("Width", roofWidth) {
            max = 20_000.0
          }
          configurableDouble("Height", roofHeight) {
            max = 20_000.0
          }

          declare {
            section("Tiles color")
            button("Red") {
              layer.style.useRedRoofTiles()
              this@ChartingDemo.markAsDirty()
            }
            button("Dark") {
              layer.style.useDarkRoofTiles()
              this@ChartingDemo.markAsDirty()
            }
          }
        }
      }
    }
  }
}
