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
package com.meistercharts.history.impl

import assertk.*
import assertk.assertions.*
import com.meistercharts.history.HistoryEnumOrdinal
import com.meistercharts.history.HistoryEnumSet
import org.junit.jupiter.api.Test

/**
 *
 */
class HistoryEnumSetTest {

  @Test
  fun testJvmNanShift() {
    assertThat(7 shl 2).isEqualTo(28)
    assertThat(Double.NaN.toInt()).isEqualTo(0)
  }

  @Test
  fun testToEnumSet() {
    assertThat(HistoryEnumSet.forEnumOrdinal(HistoryEnumOrdinal.NoValue).firstSetOrdinal()).isEqualTo(HistoryEnumOrdinal.NoValue)

    assertThat(HistoryEnumSet.forEnumOrdinal(HistoryEnumOrdinal.BooleanTrue).firstSetOrdinal()).isEqualTo(HistoryEnumOrdinal.BooleanTrue)
    assertThat(HistoryEnumSet.forEnumOrdinal(HistoryEnumOrdinal.Pending).firstSetOrdinal()).isEqualTo(HistoryEnumOrdinal.Pending)
    assertThat(HistoryEnumSet.forEnumOrdinal(HistoryEnumOrdinal.Max).firstSetOrdinal()).isEqualTo(HistoryEnumOrdinal.Max)
    assertThat(HistoryEnumSet.forEnumOrdinal(HistoryEnumOrdinal.BooleanFalse).firstSetOrdinal()).isEqualTo(HistoryEnumOrdinal.BooleanFalse)
  }

  @Test
  fun testIterate1() {
    var called = false
    HistoryEnumSet.forEnumOrdinal(HistoryEnumOrdinal(7)).fastForSetBits {
      assertThat(called).isFalse()
      called = true

      assertThat(it).isEqualTo(HistoryEnumOrdinal(7))
    }

    assertThat(called).isTrue()
  }

  @Test
  fun testIterate2() {
    var calledCount = 0

    HistoryEnumSet(0b101).fastForSetBits {
      when (calledCount) {
        0 -> assertThat(it).isEqualTo(HistoryEnumOrdinal(0))
        1 -> assertThat(it).isEqualTo(HistoryEnumOrdinal(2))
      }

      calledCount++
    }

    assertThat(calledCount).isEqualTo(2)
  }

  @Test
  fun testNoValue() {
    assertThat(HistoryEnumSet.forEnumValue(0).firstSetOrdinal()).isEqualTo(HistoryEnumOrdinal(0))
    assertThat(HistoryEnumSet.forEnumValue(1).firstSetOrdinal()).isEqualTo(HistoryEnumOrdinal(1))
    assertThat(HistoryEnumSet.NoValue.firstSetOrdinal()).isEqualTo(HistoryEnumOrdinal.NoValue)
    assertThat(HistoryEnumSet.Pending.firstSetOrdinal()).isEqualTo(HistoryEnumOrdinal.Pending)
  }

  @Test
  fun testCreateFromEnumValue() {
    assertThat(HistoryEnumSet.forEnumValue(0)).isEqualTo(HistoryEnumSet.first)
    assertThat(HistoryEnumSet.forEnumValue(1)).isEqualTo(HistoryEnumSet.second)
    assertThat(HistoryEnumSet.forEnumValue(2)).isEqualTo(HistoryEnumSet.third)
    assertThat(HistoryEnumSet.forEnumValue(3)).isEqualTo(HistoryEnumSet.fourth)
  }

  @Test
  fun testBasics() {
    assertThat(HistoryEnumSet.first.bitset).isEqualTo(0b0000_0001)
    assertThat(HistoryEnumSet.second.bitset).isEqualTo(0b0000_0010)

    HistoryEnumSet.first.let {
      assertThat(it.isSet(HistoryEnumOrdinal(0))).isTrue()
      assertThat(it.isSet(HistoryEnumOrdinal(1))).isFalse()
      assertThat(it.isSet(HistoryEnumOrdinal(7))).isFalse()
      assertThat(it.isSet(HistoryEnumOrdinal(8))).isFalse()
    }

    HistoryEnumSet.second.let {
      assertThat(it.isSet(HistoryEnumOrdinal(0))).isFalse()
      assertThat(it.isSet(HistoryEnumOrdinal(1))).isTrue()
      assertThat(it.isSet(HistoryEnumOrdinal(2))).isFalse()
      assertThat(it.isSet(HistoryEnumOrdinal(7))).isFalse()
      assertThat(it.isSet(HistoryEnumOrdinal(8))).isFalse()
    }

    HistoryEnumSet(0b0101_0101).let {
      assertThat(it.isSet(HistoryEnumOrdinal(0))).isTrue()
      assertThat(it.isSet(HistoryEnumOrdinal(1))).isFalse()
      assertThat(it.isSet(HistoryEnumOrdinal(2))).isTrue()
      assertThat(it.isSet(HistoryEnumOrdinal(3))).isFalse()
      assertThat(it.isSet(HistoryEnumOrdinal(4))).isTrue()
      assertThat(it.isSet(HistoryEnumOrdinal(5))).isFalse()
      assertThat(it.isSet(HistoryEnumOrdinal(6))).isTrue()
      assertThat(it.isSet(HistoryEnumOrdinal(7))).isFalse()
      assertThat(it.isSet(HistoryEnumOrdinal(8))).isFalse()
    }
  }

  @Test
  fun testMax() {
    HistoryEnumSet.Max.let {
      assertThat(it.isPending()).isFalse()
      assertThat(it.isNoValue()).isFalse()

      assertThat(it.isSet(HistoryEnumOrdinal(0))).isTrue()
      assertThat(it.isSet(HistoryEnumOrdinal(1))).isTrue()
      assertThat(it.isSet(HistoryEnumOrdinal(2))).isTrue()
      assertThat(it.isSet(HistoryEnumOrdinal(3))).isTrue()
      assertThat(it.isSet(HistoryEnumOrdinal(4))).isTrue()
      assertThat(it.isSet(HistoryEnumOrdinal(5))).isTrue()
      assertThat(it.isSet(HistoryEnumOrdinal(6))).isTrue()
      assertThat(it.isSet(HistoryEnumOrdinal(7))).isTrue()
      assertThat(it.isSet(HistoryEnumOrdinal(8))).isTrue()
      assertThat(it.isSet(HistoryEnumOrdinal(10))).isTrue()
      assertThat(it.isSet(HistoryEnumOrdinal(15))).isTrue()
      assertThat(it.isSet(HistoryEnumOrdinal(16))).isTrue()
      assertThat(it.isSet(HistoryEnumOrdinal(17))).isTrue()
      assertThat(it.isSet(HistoryEnumOrdinal(24))).isTrue()
      assertThat(it.isSet(HistoryEnumOrdinal(25))).isTrue()
      assertThat(it.isSet(HistoryEnumOrdinal(26))).isTrue()
      assertThat(it.isSet(HistoryEnumOrdinal(27))).isTrue()
      assertThat(it.isSet(HistoryEnumOrdinal(28))).isTrue()
    }
  }
}
