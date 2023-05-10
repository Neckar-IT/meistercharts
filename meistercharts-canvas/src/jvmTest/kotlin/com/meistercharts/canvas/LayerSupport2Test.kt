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
import org.junit.jupiter.api.Test

internal class LayerSupport2Test {
  @Test
  internal fun testSnapping() {
    val canvas = MockCanvas()
    val chartSupport = ChartSupport(canvas)
    val pixelSnapSupport = chartSupport.pixelSnapSupport

    assertThat(pixelSnapSupport.snapConfiguration.snapX).isFalse()
    assertThat(pixelSnapSupport.snapConfiguration.snapY).isFalse()

    assertThat(pixelSnapSupport.snapXValue(1.1)).isEqualTo(1.1)
    assertThat(pixelSnapSupport.snapYValue(1.1)).isEqualTo(1.1)

    pixelSnapSupport.snapConfiguration = SnapConfiguration.OnlyX
    assertThat(pixelSnapSupport.snapConfiguration.snapX).isTrue()
    assertThat(pixelSnapSupport.snapConfiguration.snapY).isFalse()

    assertThat(pixelSnapSupport.snapXValue(1.1)).isEqualTo(1.0)
    assertThat(pixelSnapSupport.snapYValue(1.1)).isEqualTo(1.1)

    pixelSnapSupport.snapConfiguration = SnapConfiguration.OnlyY
    assertThat(pixelSnapSupport.snapConfiguration.snapX).isFalse()
    assertThat(pixelSnapSupport.snapConfiguration.snapY).isTrue()

    pixelSnapSupport.snapConfiguration = SnapConfiguration.Both
    assertThat(pixelSnapSupport.snapXValue(1.1)).isEqualTo(1.0)
    assertThat(pixelSnapSupport.snapYValue(1.1)).isEqualTo(1.0)
  }
}
