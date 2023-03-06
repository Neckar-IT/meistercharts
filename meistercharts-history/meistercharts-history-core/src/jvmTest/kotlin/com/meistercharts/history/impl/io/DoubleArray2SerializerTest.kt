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
package com.meistercharts.history.impl.io

import assertk.*
import assertk.assertions.*
import com.meistercharts.history.impl.HistoryValues
import it.neckar.open.collections.DoubleArray2
import it.neckar.open.kotlin.lang.hex
import it.neckar.open.kotlin.lang.toBase64
import org.junit.jupiter.api.Test

/**
 */
class DoubleArray2SerializerTest {
  @Test
  fun testIt() {
    val values = HistoryValues(
      arrayOf(
        doubleArrayOf(1.0, 2.0, 3.0),
        doubleArrayOf(10.0, 20.0, 30.0),
        doubleArrayOf(100.0, 200.0, 300.0),
        doubleArrayOf(1000.0, 2000.0, 3000.0)
      ),
      arrayOf(
        intArrayOf(1, 2, 3),
        intArrayOf(10, 20, 30),
        intArrayOf(100, 200, 300),
        intArrayOf(1000, 2000, 3000)
      ),
      arrayOf(
        intArrayOf(7, 8, 9),
        intArrayOf(70, 80, 90),
        intArrayOf(700, 800, 900),
        intArrayOf(7000, 8000, 9000)
      ),
    ).makeRelative()

    val serialized = DoubleArray2SerializerOld.toByteArray(values.decimalValues)
    assertThat(serialized.size).isEqualTo((4 * 3 * 8) + 4)
    assertThat(serialized.hex).isEqualTo("000400033ff000000000000040240000000000004059000000000000408f4000000000003ff000000000000040240000000000004059000000000000408f4000000000003ff000000000000040240000000000004059000000000000408f400000000000")

    val deserialized = DoubleArray2SerializerOld.parse(serialized)
    assertThat(deserialized).isEqualTo(values.decimalValues)
    assertThat(deserialized.data).hasSize(12)
    assertThat(deserialized[0, 0]).isEqualTo(values.decimalValues[0, 0])
    assertThat(deserialized[0, 1]).isEqualTo(values.decimalValues[0, 1])
    assertThat(deserialized[0, 2]).isEqualTo(values.decimalValues[0, 2])
    assertThat(deserialized[1, 0]).isEqualTo(values.decimalValues[1, 0])
  }

  @Test
  fun testHexEncode() {
    val s = "000400030000000100000001000000010000000a0000000a0000000a000000640000006400000064000003e8000003e8000003e8"
    val encoded = s.encodeToByteArray().toBase64()

    assertThat(encoded).isEqualTo("MDAwNDAwMDMwMDAwMDAwMTAwMDAwMDAxMDAwMDAwMDEwMDAwMDAwYTAwMDAwMDBhMDAwMDAwMGEwMDAwMDA2NDAwMDAwMDY0MDAwMDAwNjQwMDAwMDNlODAwMDAwM2U4MDAwMDAzZTg=")
  }

  @Test
  fun testOverflow() {
    DoubleArray2(4, 1, doubleArrayOf(Double.MAX_VALUE, Double.MAX_VALUE, Double.MAX_VALUE, Double.MAX_VALUE)).let {
      val serialized = DoubleArray2SerializerOld.toByteArray(it)
      val deserialized = DoubleArray2SerializerOld.parse(serialized)
      assertThat(deserialized).isEqualTo(it)
    }

    DoubleArray2(4, 1, doubleArrayOf(Double.MIN_VALUE, Double.MIN_VALUE, Double.MIN_VALUE, Double.MIN_VALUE)).let {
      val serialized = DoubleArray2SerializerOld.toByteArray(it)
      val deserialized = DoubleArray2SerializerOld.parse(serialized)
      assertThat(deserialized).isEqualTo(it)
    }

    DoubleArray2(4, 1, doubleArrayOf(Double.MAX_VALUE, Double.MIN_VALUE, Double.MAX_VALUE, Double.MIN_VALUE)).let {
      val serialized = DoubleArray2SerializerOld.toByteArray(it)
      val deserialized = DoubleArray2SerializerOld.parse(serialized)
      assertThat(deserialized).isEqualTo(it)
    }

    DoubleArray2(4, 1, doubleArrayOf(Double.MIN_VALUE, Double.MAX_VALUE, Double.MIN_VALUE, Double.MAX_VALUE)).let {
      val serialized = DoubleArray2SerializerOld.toByteArray(it)
      val deserialized = DoubleArray2SerializerOld.parse(serialized)
      assertThat(deserialized).isEqualTo(it)
    }
  }
}
