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
import it.neckar.open.collections.IntArray2
import it.neckar.open.kotlin.lang.hex
import it.neckar.open.kotlin.lang.toBase64
import org.junit.jupiter.api.Test

/**
 */
class IntArray2SerializerTest {
  @Test
  fun testHexEncode() {
    val s = "000400030000000100000001000000010000000a0000000a0000000a000000640000006400000064000003e8000003e8000003e8"
    val encoded = s.encodeToByteArray().toBase64()

    assertThat(encoded).isEqualTo("MDAwNDAwMDMwMDAwMDAwMTAwMDAwMDAxMDAwMDAwMDEwMDAwMDAwYTAwMDAwMDBhMDAwMDAwMGEwMDAwMDA2NDAwMDAwMDY0MDAwMDAwNjQwMDAwMDNlODAwMDAwM2U4MDAwMDAzZTg=")
  }

  @Test
  fun testOverflow() {
    IntArray2(4, 1, intArrayOf(Int.MAX_VALUE, Int.MAX_VALUE, Int.MAX_VALUE, Int.MAX_VALUE)).let {
      val serialized = IntArray2SerializerOld.toByteArray(it)
      val deserialized = IntArray2SerializerOld.parse(serialized)
      assertThat(deserialized).isEqualTo(it)
    }

    IntArray2(4, 1, intArrayOf(Int.MIN_VALUE, Int.MIN_VALUE, Int.MIN_VALUE, Int.MIN_VALUE)).let {
      val serialized = IntArray2SerializerOld.toByteArray(it)
      val deserialized = IntArray2SerializerOld.parse(serialized)
      assertThat(deserialized).isEqualTo(it)
    }

    IntArray2(4, 1, intArrayOf(Int.MAX_VALUE, Int.MIN_VALUE, Int.MAX_VALUE, Int.MIN_VALUE)).let {
      val serialized = IntArray2SerializerOld.toByteArray(it)
      val deserialized = IntArray2SerializerOld.parse(serialized)
      assertThat(deserialized).isEqualTo(it)
    }

    IntArray2(4, 1, intArrayOf(Int.MIN_VALUE, Int.MAX_VALUE, Int.MIN_VALUE, Int.MAX_VALUE)).let {
      val serialized = IntArray2SerializerOld.toByteArray(it)
      val deserialized = IntArray2SerializerOld.parse(serialized)
      assertThat(deserialized).isEqualTo(it)
    }
  }
}
