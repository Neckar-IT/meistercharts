/**
 * Copyright 2023 Neckar IT GmbH, Mössingen, Germany
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
