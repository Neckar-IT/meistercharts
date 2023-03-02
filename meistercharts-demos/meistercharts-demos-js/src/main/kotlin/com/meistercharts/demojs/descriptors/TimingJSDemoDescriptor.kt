package com.meistercharts.demojs.descriptors

import com.meistercharts.algorithms.layers.addClearBackground
import com.meistercharts.algorithms.layers.text.addText
import com.meistercharts.canvas.FontDescriptorFragment
import com.meistercharts.demo.ChartingDemo
import com.meistercharts.demo.ChartingDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.PredefinedConfiguration
import it.neckar.open.unit.si.ms
import kotlinx.browser.window
import kotlin.reflect.KProperty0

/**
 */
class TimingJSDemoDescriptor : ChartingDemoDescriptor<Nothing> {
  override val name: String = "JS Timing"
  override val category: DemoCategory = DemoCategory.Platform

  override fun createDemo(configuration: PredefinedConfiguration<Nothing>?): ChartingDemo {
    return ChartingDemo {
      meistercharts {
        configure {
          layers.addClearBackground()

          layers.addText { _, _ ->
            val performance = window.performance
            val timing = performance.timing

            @ms val base = timing.navigationStart.toDouble()

            listOf(
              createLine(timing::navigationStart, base),
              createLine(timing::redirectStart, base),
              createLine(timing::redirectEnd, base),
              createLine(timing::unloadEventStart, base),
              createLine(timing::unloadEventEnd, base),
              createLine(timing::fetchStart, base),
              createLine(timing::domainLookupStart, base),
              createLine(timing::domainLookupEnd, base),
              createLine(timing::connectStart, base),
              createLine(timing::secureConnectionStart, base),
              createLine(timing::connectEnd, base),
              createLine(timing::requestStart, base),
              createLine(timing::responseStart, base),
              createLine(timing::responseEnd, base),
              createLine(timing::domLoading, base),
              createLine(timing::domInteractive, base),
              createLine(timing::domContentLoadedEventStart, base),
              createLine(timing::domContentLoadedEventEnd, base),
              createLine(timing::domComplete, base),
              createLine(timing::loadEventStart, base),
              createLine(timing::loadEventEnd, base)
            )
          }.also {
            it.style.font = FontDescriptorFragment.S
          }
        }
      }
    }
  }

  /**
   * Creates a loading line
   */
  private fun createLine(property: KProperty0<Number>, base: Double): String {
    val value = property.get().toDouble()
    val delta = value - base

    return "${property.name}: after $delta ms"
  }
}
