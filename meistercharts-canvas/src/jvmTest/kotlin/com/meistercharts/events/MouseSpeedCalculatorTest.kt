package com.meistercharts.events

import assertk.*
import assertk.assertions.*
import com.meistercharts.events.gesture.MouseSpeedCalculator
import org.junit.jupiter.api.Test


/**
 */
class MouseSpeedCalculatorTest {
  @Test
  fun testBasics() {
    val mouseSpeedCalculator = MouseSpeedCalculator()

    mouseSpeedCalculator.add(10.0, 20.0, 30.0)
    assertThat(mouseSpeedCalculator.calculateSpeedX()).isBetween(1.0, 2.0)

    mouseSpeedCalculator.add(10.0, 30.0, 30.0)
    assertThat(mouseSpeedCalculator.calculateSpeedX()).isBetween(1.0, 2.0)
  }
}
