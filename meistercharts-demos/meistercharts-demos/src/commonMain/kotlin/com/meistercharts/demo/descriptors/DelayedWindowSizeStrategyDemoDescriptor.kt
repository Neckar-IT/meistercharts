package com.meistercharts.demo.descriptors

import com.meistercharts.algorithms.ResetToDefaultsOnWindowResize
import com.meistercharts.algorithms.axis.AxisSelection
import com.meistercharts.algorithms.impl.ZoomAndTranslationDefaults
import com.meistercharts.algorithms.layers.addClearBackground
import com.meistercharts.algorithms.layers.debug.ContentAreaDebugLayer
import com.meistercharts.canvas.DelayedWindowSizeBindingStrategy
import com.meistercharts.demo.ChartingDemo
import com.meistercharts.demo.ChartingDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.configurableDouble
import com.meistercharts.demo.configurableEnum
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.DurationUnit

/**
 */
class DelayedWindowSizeStrategyDemoDescriptor : ChartingDemoDescriptor<Nothing> {
  override val name: String = "Delayed Window Resize Strategy"
  override val category: DemoCategory = DemoCategory.Calculations

  override fun createDemo(configuration: PredefinedConfiguration<Nothing>?): ChartingDemo {
    return ChartingDemo {
      meistercharts {
        zoomAndTranslationDefaults(ZoomAndTranslationDefaults.tenPercentMargin)
        val delayedWindowSizeBindingStrategy = DelayedWindowSizeBindingStrategy(1000.0.milliseconds, AxisSelection.Both)
        windowSizeBindingStrategy = delayedWindowSizeBindingStrategy

        configure {
          chartSupport.windowResizeBehavior = ResetToDefaultsOnWindowResize

          layers.addClearBackground()
          layers.addLayer(ContentAreaDebugLayer())

          configurableDouble("delay", delayedWindowSizeBindingStrategy.delay.toDouble(DurationUnit.MILLISECONDS)) {
            min = 0.0
            max = 2000.0

            onChange {
              delayedWindowSizeBindingStrategy.delay = it.milliseconds
            }
          }

          configurableEnum("Delayed Axis", delayedWindowSizeBindingStrategy.axisSelection, enumValues()) {
            onChange {
              delayedWindowSizeBindingStrategy.axisSelection = it
            }
          }
        }
      }
    }
  }
}
