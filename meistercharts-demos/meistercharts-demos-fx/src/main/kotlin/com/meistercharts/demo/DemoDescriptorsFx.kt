package com.meistercharts.demo

import com.meistercharts.demo.ChartingDemoDescriptor
import com.meistercharts.demo.descriptors.FontMetricsCalculatorFXDemoDescriptor
import com.meistercharts.demo.descriptors.FontMetricsFXDemoDescriptor
import com.meistercharts.demo.descriptors.LoadFontDemoDescriptor
import com.meistercharts.demo.descriptors.PvHorizonDemoDescriptor
import com.meistercharts.demo.descriptors.SvgGcDemoDescriptor
import com.meistercharts.demo.descriptors.ThemeKeysDemoDescriptor

/**
 *
 */
object DemoDescriptorsFx {
  /**
   * The JavaFX specific demo descriptors
   */
  val descriptors: List<ChartingDemoDescriptor<*>> = listOf(
    LoadFontDemoDescriptor(),
    SvgGcDemoDescriptor(),
    ThemeKeysDemoDescriptor(),
    PvHorizonDemoDescriptor(),
    FontMetricsFXDemoDescriptor(),
    FontMetricsCalculatorFXDemoDescriptor(),
  )
}
