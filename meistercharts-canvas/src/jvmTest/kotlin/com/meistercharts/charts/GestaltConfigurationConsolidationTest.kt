/*
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
package com.meistercharts.charts

import assertk.*
import assertk.assertions.*
import com.meistercharts.range.ValueRange
import org.junit.jupiter.api.Test

/**
 * Verifies that gestalts expose a single [configuration] instance and that the
 * `additionalConfiguration` lambda mutates exactly that instance.
 *
 * These gestalts used to keep two separate configuration instances (`configuration` and `style`):
 * `additionalConfiguration` was applied to `style` only, while parts of the gestalt read from the
 * untouched `configuration`. Reading back a value set via `additionalConfiguration` silently
 * returned the default.
 */
class GestaltConfigurationConsolidationTest {
  @Test
  fun `BarChartGroupedGestalt additionalConfiguration mutates the exposed configuration`() {
    val customValueRange = ValueRange.linear(0.0, 999.0)

    val gestalt = BarChartGroupedGestalt {
      valueRange = customValueRange
    }

    assertThat(gestalt.configuration.valueRange).isSameInstanceAs(customValueRange)
  }

  @Test
  fun `PixelValuesGestalt additionalConfiguration mutates the exposed configuration`() {
    val gestalt = PixelValuesGestalt(additionalConfiguration = {
      showLines = false
    })

    assertThat(gestalt.configuration.showLines).isFalse()
  }
}
