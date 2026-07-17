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
package com.meistercharts.canvas.allocation

import assertk.*
import assertk.assertions.*
import org.junit.jupiter.api.Test

class AllocationReportFormatTest {
  @Test
  fun `renders layers, types and the top stacktraces`() {
    val report = AllocationReport(
      listOf(
        LayerAllocations(
          layerName = "AbstractHistoryStripeLayer",
          allocationsByType = listOf(
            TypeAllocation(
              typeName = "com.meistercharts.history.EnumDataSeriesIndex",
              samples = 42,
              estimatedBytes = 672,
              stacktraces = mapOf(
                "com.meistercharts.algorithms.layers.AbstractHistoryStripeLayer.layout(182)" to 40,
                "com.meistercharts.algorithms.layers.AbstractHistoryStripeLayer.paint(276)" to 2,
              ),
            ),
          ),
        ),
      ),
    )

    val text = report.format()

    assertThat(text).contains("[AbstractHistoryStripeLayer]")
    assertThat(text).contains("42x com.meistercharts.history.EnumDataSeriesIndex")
    //most frequent stacktrace first
    assertThat(text).contains("40x @")
    assertThat(text).contains("AbstractHistoryStripeLayer.layout(182)")
  }

  @Test
  fun `empty report is rendered as a hint`() {
    assertThat(AllocationReport.empty.format()).contains("empty")
  }
}
