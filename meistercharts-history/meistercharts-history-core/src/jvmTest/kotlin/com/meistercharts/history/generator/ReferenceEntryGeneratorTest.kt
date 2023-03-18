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
import com.meistercharts.history.downsampling.nowForTests
import com.meistercharts.history.isEqualToReferenceEntryId
import it.neckar.open.test.utils.RandomWithSeed
import org.junit.jupiter.api.Test
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

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

  @Test
  fun testIncreasing2() {
    ReferenceEntryGenerator.increasing(step = 1000.0.milliseconds).let {
      assertThat(it.generate(17.9)).isEqualTo(ReferenceEntryId(0))
      assertThat(it.generate(18.0)).isEqualTo(ReferenceEntryId(0))
      assertThat(it.generate(99.0)).isEqualTo(ReferenceEntryId(0))
      assertThat(it.generate(99.9)).isEqualTo(ReferenceEntryId(0))
      assertThat(it.generate(101.0)).isEqualTo(ReferenceEntryId(0))
      assertThat(it.generate(999.0)).isEqualTo(ReferenceEntryId(0))
      assertThat(it.generate(1000.0)).isEqualTo(ReferenceEntryId(1))
      assertThat(it.generate(1001.0)).isEqualTo(ReferenceEntryId(1))
      assertThat(it.generate(99_999_999_101.0)).isEqualTo(ReferenceEntryId(99999))
    }
  }

  @Test
  fun testIncreasingBug() {
    ReferenceEntryGenerator.increasing(step = 65.seconds).let {
      assertThat(it.generate(nowForTests)).isEqualTo(ReferenceEntryId(62665))
      assertThat(it.generate(nowForTests + 1_000)).isEqualTo(ReferenceEntryId(62665))
      assertThat(it.generate(nowForTests + 10_000)).isEqualTo(ReferenceEntryId(62665))
    }
  }

  @Test
  fun testRealValues() {
    val generator100 = ReferenceEntryGenerator.increasing(100.0.milliseconds)
    val generator1000 = ReferenceEntryGenerator.increasing(1000.0.milliseconds)

    assertThat(generator100.generate(timestamp = nowForTests)).isEqualToReferenceEntryId(32415)
    assertThat(generator1000.generate(timestamp = nowForTests)).isEqualToReferenceEntryId(73241)

    assertThat(generator100.generate(nowForTests + 1)).isEqualToReferenceEntryId(32415)
    assertThat(generator1000.generate(nowForTests + 1)).isEqualToReferenceEntryId(73241)

    assertThat(generator100.generate(nowForTests + 100)).isEqualToReferenceEntryId(32416)
    assertThat(generator1000.generate(nowForTests + 100)).isEqualToReferenceEntryId(73241)

    assertThat(generator100.generate(nowForTests + 250)).isEqualToReferenceEntryId(32417)
    assertThat(generator1000.generate(nowForTests + 250)).isEqualToReferenceEntryId(73241)

    assertThat(generator100.generate(nowForTests + 500)).isEqualToReferenceEntryId(32420)
    assertThat(generator1000.generate(nowForTests + 500)).isEqualToReferenceEntryId(73242)

    assertThat(generator100.generate(nowForTests + 750)).isEqualToReferenceEntryId(32422)
    assertThat(generator1000.generate(nowForTests + 750)).isEqualToReferenceEntryId(73242)

    assertThat(generator100.generate(nowForTests + 999)).isEqualToReferenceEntryId(32424)
    assertThat(generator1000.generate(nowForTests + 999)).isEqualToReferenceEntryId(73242)
  }
}
