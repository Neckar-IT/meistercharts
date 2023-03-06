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
package com.meistercharts.algorithms.model

import assertk.*
import assertk.assertions.*
import it.neckar.open.provider.DoublesProvider1
import it.neckar.open.provider.fastForEach
import it.neckar.open.provider.fastForEachIndexed
import org.junit.jupiter.api.Test

/**
 *
 */
class ValuesProvider1Test {
  val doublesProvider: DoublesProvider1<String> = object : DoublesProvider1<String> {
    override fun size(param1: String): Int = 3

    override fun valueAt(index: Int, param1: String): Double {
      return (index + param1.length).toDouble()
    }
  }

  @Test
  internal fun testForEach() {
    val received = mutableListOf<Double>()

    doublesProvider.fastForEach("aa") {
      received.add(it)
    }

    assertThat(received).hasSize(3)
    assertThat(received).containsExactly(0 + 2.0, 1 + 2.0, 2 + 2.0)
  }

  @Test
  internal fun testForEachIndexed() {
    val received = mutableListOf<Double>()

    doublesProvider.fastForEachIndexed("aa") { index, value ->
      assertThat(index).isEqualTo((value - 2).toInt())

      received.add(value)
    }

    assertThat(received).hasSize(3)
    assertThat(received).containsExactly(0 + 2.0, 1 + 2.0, 2 + 2.0)
  }
}
