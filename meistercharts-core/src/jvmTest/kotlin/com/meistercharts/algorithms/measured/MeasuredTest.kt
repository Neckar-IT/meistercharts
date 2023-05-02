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
package com.meistercharts.algorithms.measured

import assertk.*
import assertk.assertions.*
import io.nacular.measured.units.Length.Companion.meters
import io.nacular.measured.units.Measure
import io.nacular.measured.units.Time
import io.nacular.measured.units.Time.Companion.milliseconds
import io.nacular.measured.units.Time.Companion.minutes
import io.nacular.measured.units.Time.Companion.seconds
import io.nacular.measured.units.div
import io.nacular.measured.units.times
import org.junit.jupiter.api.Test

/**
 *
 */
class MeasuredTest {
  @Test
  fun testIt() {
    val duration: Measure<Time> = 1_700.0 * milliseconds

    assertThat(duration.amount).isEqualTo(1700.0)
    assertThat(duration.units.suffix).isEqualTo("ms")
  }

  @Test
  fun testComplex() {
    val velocity = 5 * meters / seconds
    val acceleration = 9 * meters / (seconds * seconds)
    val time = 1 * minutes
  }
}
