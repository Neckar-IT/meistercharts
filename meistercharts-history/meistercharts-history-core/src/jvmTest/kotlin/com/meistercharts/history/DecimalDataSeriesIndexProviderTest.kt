package com.meistercharts.history

import org.junit.jupiter.api.Test

/**
 *
 */
class DecimalDataSeriesIndexProviderTest {
  @Test
  fun testBoxing() {
    val provider = DecimalDataSeriesIndexProvider.indices { 7 }

    val result = provider.valueAt(7)

  }
}
