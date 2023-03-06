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
import it.neckar.open.collections.fastForEach
import org.junit.jupiter.api.Test


class SizeClassificationTest {

  @Test
  fun testMinMax() {
    SizeClassification.values().fastForEach {
      assertThat(it.min < it.max)
    }
  }

  @Test
  fun testBreakpoints() {
    var previousSizeClassification: SizeClassification? = null
    SizeClassification.values().fastForEach { sizeClassification ->
      previousSizeClassification?.let {
        assertThat(sizeClassification.min).isEqualTo(it.max)
      }
      previousSizeClassification = sizeClassification
    }
  }
}
