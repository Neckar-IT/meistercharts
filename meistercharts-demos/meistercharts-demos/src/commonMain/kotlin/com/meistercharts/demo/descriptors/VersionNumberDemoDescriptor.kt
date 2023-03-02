package com.meistercharts.demo.descriptors

import com.meistercharts.algorithms.layers.addClearBackground
import com.meistercharts.algorithms.layers.debug.addVersionNumber
import com.meistercharts.canvas.registerDirtyListener
import com.meistercharts.canvas.timerSupport
import com.meistercharts.demo.ChartingDemo
import com.meistercharts.demo.ChartingDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.PredefinedConfiguration
import it.neckar.open.observable.ObservableBoolean
import kotlin.time.Duration.Companion.milliseconds


class VersionNumberDemoDescriptor : ChartingDemoDescriptor<Nothing> {
  override val name: String = "Version Number"
  override val description: String = "## Show a version number layer"
  override val category: DemoCategory = DemoCategory.Layers

  override fun createDemo(configuration: PredefinedConfiguration<Nothing>?): ChartingDemo {
    return ChartingDemo {
      meistercharts {
        val versionNumberVisible = ObservableBoolean(true)

        configure {
          versionNumberVisible.registerDirtyListener(this)

          layers.addClearBackground()
          layers.addVersionNumber()

          chartSupport.timerSupport.repeat(500.0.milliseconds) {
            versionNumberVisible.toggle()
          }
        }

      }
    }
  }
}
