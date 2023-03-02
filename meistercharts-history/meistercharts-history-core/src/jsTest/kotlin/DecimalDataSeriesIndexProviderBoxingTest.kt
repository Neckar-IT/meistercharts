package com.meistercharts.history

import com.meistercharts.history.DecimalDataSeriesIndexProvider
import kotlin.test.Test

/**
 *
 */
class DecimalDataSeriesIndexProviderBoxingTest {
  @Test
  fun testBoxing() {
    val provider = DecimalDataSeriesIndexProvider.indices { 17 }
    println("CHECK: ###############")
    println("No boxing/unboxing!!!")
    println("CHECK: ###############")
    val result = provider.valueAt(4)
  }
}
