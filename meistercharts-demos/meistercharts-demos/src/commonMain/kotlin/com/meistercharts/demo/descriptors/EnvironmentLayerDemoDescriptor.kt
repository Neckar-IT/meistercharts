package com.meistercharts.demo.descriptors

import com.meistercharts.algorithms.environment
import com.meistercharts.algorithms.layers.addClearBackground
import com.meistercharts.algorithms.layers.text.addText
import com.meistercharts.canvas.i18nConfiguration
import com.meistercharts.demo.ChartingDemo
import com.meistercharts.demo.ChartingDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.PredefinedConfiguration
import it.neckar.open.formatting.decimalFormat
import it.neckar.open.i18n.DefaultSystemLocale

class EnvironmentLayerDemoDescriptor : ChartingDemoDescriptor<Nothing> {
  override val name: String = "Environment"

  //language=HTML
  override val description: String = "## Debug info about the environment"
  override val category: DemoCategory = DemoCategory.LowLevelTests

  override fun createDemo(configuration: PredefinedConfiguration<Nothing>?): ChartingDemo {
    return ChartingDemo {
      meistercharts {
        configure {
          layers.addClearBackground()

          layers.addText { _, _ ->
            val size = chartSupport.canvas.size
            listOf(
              "multiTouchSupported: ${environment.multiTouchSupported}",
              "devicePixelRatio: ${environment.devicePixelRatio}",
              "Canvas size: ${decimalFormat.format(size.width, chartSupport.i18nConfiguration)} / ${decimalFormat.format(size.height, chartSupport.i18nConfiguration)}",
              "Default System Locale $DefaultSystemLocale"
            )
          }
        }
      }
    }
  }
}
