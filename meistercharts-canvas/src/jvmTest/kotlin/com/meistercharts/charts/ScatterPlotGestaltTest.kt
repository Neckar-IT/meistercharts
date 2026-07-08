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

import assertk.assertThat
import assertk.assertions.isEqualTo
import org.junit.jupiter.api.Test

class ScatterPlotGestaltTest {
  @Test
  fun testCreateDefaultDataPointCount() {
    val configuration = ScatterPlotGestalt.createDefaultData()

    //4 clouds with 100 points each - the providers must not contain capacity slack of the fill buffers
    assertThat(configuration.xValues.size()).isEqualTo(400)
    assertThat(configuration.yValues.size()).isEqualTo(400)
  }
}
