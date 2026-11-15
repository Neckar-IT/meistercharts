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
package com.meistercharts.demo.descriptors

import assertk.assertThat
import assertk.assertions.isLessThan
import org.junit.jupiter.api.Test
import kotlin.math.abs
import kotlin.math.sin

class FastSinTest {
  @Test
  fun `fastSin matches kotlin math sin within 1e-10`() {
    var maxError = 0.0
    var x = -1000.0
    while (x <= 1000.0) {
      val error = abs(fastSin(x) - sin(x))
      if (error > maxError) {
        maxError = error
      }
      x += 0.0137
    }
    assertThat(maxError).isLessThan(1e-10)
  }
}
