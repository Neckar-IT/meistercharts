package com.meistercharts.demo

import assertk.*
import assertk.assertions.*
import org.junit.jupiter.api.Test

class DemoMessagesTest {
  @Test
  fun testDemoMessages() {
    assertThat(DemoMessages).isNotNull()
  }
}
