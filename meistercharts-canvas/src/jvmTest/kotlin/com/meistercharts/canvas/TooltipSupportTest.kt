package com.meistercharts.canvas

import assertk.*
import assertk.assertions.*
import it.neckar.open.observable.ObservableObject
import org.junit.jupiter.api.Test

/**
 */
class TooltipSupportTest {
  @Test
  fun testTooltips() {
    val tooltip: ObservableObject<TooltipContent?> = ObservableObject(null)
    val tooltipSupport = TooltipSupport(tooltip)

    assertThat(tooltip.value).isNull()

    tooltipSupport.tooltipProperty("key").value = TooltipContent("dacontent")
    assertThat(tooltip.value).isEqualTo(TooltipContent("dacontent"))

    tooltipSupport.tooltipProperty("other").value = TooltipContent("dacontent2")
    assertThat(tooltip.value).isEqualTo(TooltipContent("dacontent"))

    tooltipSupport.tooltipProperty("key").value = null
    assertThat(tooltip.value).isEqualTo(TooltipContent("dacontent2"))
    tooltipSupport.tooltipProperty("other").value = null
    assertThat(tooltip.value).isNull()
  }
}
