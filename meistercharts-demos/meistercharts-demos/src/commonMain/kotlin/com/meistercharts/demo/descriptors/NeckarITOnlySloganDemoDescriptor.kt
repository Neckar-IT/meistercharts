package com.meistercharts.demo.descriptors

import com.meistercharts.algorithms.axis.AxisOrientationY
import com.meistercharts.algorithms.layers.FillBackgroundLayer
import com.meistercharts.algorithms.layers.SloganLayer
import com.meistercharts.demo.ChartingDemo
import com.meistercharts.demo.ChartingDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.configurableDouble

/**
 *
 */
class NeckarITOnlySloganDemoDescriptor : ChartingDemoDescriptor<String?> {
  override val name: String = "Neckar IT only Slogan"
  override val category: DemoCategory = DemoCategory.NeckarIT

  override val predefinedConfigurations: List<PredefinedConfiguration<String?>> = listOf(
    PredefinedConfiguration(payload = null),
    PredefinedConfiguration("Sparkling Charts!", "Sparkling Charts!"),
    PredefinedConfiguration("Ferner gibt es auch niemanden, der den Schmerz um seiner selbst willen liebt, der ihn sucht und haben will, einfach, weil es Schmerz ist", "Long Text"),
  )

  override fun createDemo(configuration: PredefinedConfiguration<String?>?): ChartingDemo {
    return ChartingDemo {
      meistercharts {
        configure {
          chartSupport.rootChartState.axisOrientationY = AxisOrientationY.OriginAtBottom

          layers.addLayer(FillBackgroundLayer() {
            dark()
          })

          val data: SloganLayer.Data = configuration?.payload?.let {
            SloganLayer.Data { _, _ -> it }
          } ?: SloganLayer.Data()

          val layer = SloganLayer(data)
          layers.addLayer(layer)

          configurableDouble("Max Percentage", layer.style::maxPercentage)
        }
      }
    }
  }
}
