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
package com.meistercharts.history.generator

import assertk.*
import assertk.assertions.*
import com.meistercharts.history.ReferenceEntryId
import it.neckar.open.test.utils.RandomWithSeed
import org.junit.jupiter.api.Test
import kotlin.time.Duration.Companion.milliseconds

class ReferenceEntryGeneratorTest {
  @Test
  fun testAlways() {
    assertThat(ReferenceEntryGenerator.always(ReferenceEntryId(17)).generate(17.9)).isEqualTo(ReferenceEntryId(17))
    assertThat(ReferenceEntryGenerator.always(ReferenceEntryId(17)).generate(33.9)).isEqualTo(ReferenceEntryId(17))
  }

  @RandomWithSeed(seed = 99)
  @Test
  fun testRandom() {
    ReferenceEntryGenerator.random().let {
      assertThat(it.generate(17.9)).isEqualTo(ReferenceEntryId(79007))
      assertThat(it.generate(99.0)).isEqualTo(ReferenceEntryId(92338))
      assertThat(it.generate(99.0)).isEqualTo(ReferenceEntryId(34267))
    }
  }

  @Test
  fun testIncreasing() {
    ReferenceEntryGenerator.increasing(100.0.milliseconds).let {
      assertThat(it.generate(17.9)).isEqualTo(ReferenceEntryId(0))
      assertThat(it.generate(18.0)).isEqualTo(ReferenceEntryId(0))
      assertThat(it.generate(99.0)).isEqualTo(ReferenceEntryId(0))
      assertThat(it.generate(99.9)).isEqualTo(ReferenceEntryId(0))
      assertThat(it.generate(101.0)).isEqualTo(ReferenceEntryId(1))

      assertThat(it.generate(99_999_999_101.0)).isEqualTo(ReferenceEntryId(99991))
    }
  }
}
