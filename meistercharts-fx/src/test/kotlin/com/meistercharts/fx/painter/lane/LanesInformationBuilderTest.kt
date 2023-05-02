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
package com.meistercharts.fx.painter.lane

import assertk.*
import assertk.assertions.*
import com.meistercharts.algorithms.ValueRange
import com.meistercharts.algorithms.painter.Color
import com.google.common.collect.ImmutableList
import org.junit.jupiter.api.Test

/**
 */
internal class LanesInformationBuilderTest {

  @Test
  fun testIt() {
    val valueRange = ValueRange.linear(10.0, 20.0)

    val builder = LanesInformationBuilder(valueRange)
    builder.addEdge(11.0, 0.5, 0.7, "asdf", EDGE_STROKE)

    val lanesInformation = builder.build()

    assertThat(lanesInformation.edges).isEqualTo(ImmutableList.of(LanesInformation.Edge(11.0, 0.5, 0.7, "asdf", EDGE_STROKE)))

    assertThat(lanesInformation.lanes).hasSize(2)
    assertThat(lanesInformation.lanes).isEqualTo(
      ImmutableList.of<LanesInformation.Lane>(
        LanesInformation.Lane(0.5, 10.0, 11.0),
        LanesInformation.Lane(0.7, 11.0, 20.0)
      )
    )
  }

  companion object {
    val EDGE_STROKE = Color.rgb(0, 127, 195)
  }
}
