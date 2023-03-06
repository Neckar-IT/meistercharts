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

import it.neckar.open.collections.DoubleArray2
import it.neckar.open.serialization.roundTrip
import org.junit.jupiter.api.Test

class DecimalHistoryValuesTest {
  @Test
  fun testSerialization() {
    roundTrip(
      DecimalHistoryValues(
        values = DoubleArray2(2, 3) { it * 4.4 },
        minValues = DoubleArray2(2, 3) { it * 8.9 },
        maxValues = DoubleArray2(2, 3) { it * 9.9 },
      )
    ) {
      //language=JSON
      """
      {
        "values" : "AAIAAwAAAAAAAAAAQBGZmZmZmZpAIZmZmZmZmkAqZmZmZmZnQDGZmZmZmZpANgAAAAAAAA==",
        "minValues" : "AAIAAwAAAAAAAAAAQCHMzMzMzM1AMczMzMzMzUA6szMzMzM0QEHMzMzMzM1ARkAAAAAAAA==",
        "maxValues" : "AAIAAwAAAAAAAAAAQCPMzMzMzM1AM8zMzMzMzUA9szMzMzM0QEPMzMzMzM1ASMAAAAAAAA=="
      }
      """.trimIndent()
    }
  }

  @Test
  fun testSerializationNullValues() {
    roundTrip(
      DecimalHistoryValues(
        values = DoubleArray2(2, 3) { it * 4.4 },
        minValues = null,
        maxValues = null,
      )
    ) {
      """
      {
        "values" : "AAIAAwAAAAAAAAAAQBGZmZmZmZpAIZmZmZmZmkAqZmZmZmZnQDGZmZmZmZpANgAAAAAAAA==",
        "minValues" : null,
        "maxValues" : null
      }
      """.trimIndent()
    }
  }
}
