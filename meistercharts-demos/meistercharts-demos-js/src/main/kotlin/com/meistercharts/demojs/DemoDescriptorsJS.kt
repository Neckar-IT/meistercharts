package com.meistercharts.demojs

import com.meistercharts.demo.ChartingDemoDescriptor
import com.meistercharts.demojs.descriptors.EventsJSDemoDescriptor
import com.meistercharts.demojs.descriptors.FontCacheJSDemoDescriptor
import com.meistercharts.demojs.descriptors.FontMetricsJSDemoDescriptor
import com.meistercharts.demojs.descriptors.FontMetricsJSDebugDemoDescriptor
import com.meistercharts.demojs.descriptors.TimingJSDemoDescriptor
import com.meistercharts.demojs.descriptors.LocalesJSDemoDescriptor
import com.meistercharts.demojs.descriptors.MemoryUsageDemoDescriptor
import com.meistercharts.demojs.descriptors.ZeroSizeDemoDescriptor

/**
 * Demo descriptors that are specific for JavaScript
 */
object DemoDescriptorsJS {
  val descriptors: List<ChartingDemoDescriptor<*>> = listOf(
    FontCacheJSDemoDescriptor(),
    FontMetricsJSDemoDescriptor(),
    FontMetricsJSDebugDemoDescriptor(),
    EventsJSDemoDescriptor(),
    ZeroSizeDemoDescriptor(),
    MemoryUsageDemoDescriptor(),
    LocalesJSDemoDescriptor(),
  )

  /**
   * Contains deprecated demo descriptor that are no longer useful
   */
  @Deprecated("no longer used")
  val deprecatedDescriptors: List<ChartingDemoDescriptor<*>> = listOf(
    TimingJSDemoDescriptor(),
  )
}
