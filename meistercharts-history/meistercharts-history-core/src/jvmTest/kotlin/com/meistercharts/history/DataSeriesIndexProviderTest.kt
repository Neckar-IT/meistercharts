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
package com.meistercharts.history

import assertk.*
import assertk.assertions.*
import org.junit.jupiter.api.Test

class DataSeriesIndexProviderTest {
  private val requestedVisibleIndices: DataSeriesIndexProvider<DecimalDataSeriesIndex> = DecimalDataSeriesIndexProvider.indices { 7 }

  @Test
  fun testAtMost() {
    assertThat(requestedVisibleIndices.size()).isEqualTo(7)
    val atMost = requestedVisibleIndices.atMost { 5 }

    assertThat(atMost.size()).isEqualTo(5)

    var calledWithIndex = -1
    atMost.fastForEachIndexed { index, value ->
      assertThat(index).isEqualTo(calledWithIndex + 1)
      assertThat(value.value).isEqualTo(index)

      calledWithIndex = index
    }

    assertThat(calledWithIndex).isEqualTo(4)
  }
}
