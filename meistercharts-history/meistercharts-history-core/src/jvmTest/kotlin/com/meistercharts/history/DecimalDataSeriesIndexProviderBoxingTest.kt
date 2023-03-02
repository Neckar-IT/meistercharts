package com.meistercharts.history

import org.junit.jupiter.api.Test


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

