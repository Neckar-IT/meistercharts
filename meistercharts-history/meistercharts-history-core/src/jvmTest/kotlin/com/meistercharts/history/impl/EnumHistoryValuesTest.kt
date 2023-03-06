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

import it.neckar.open.collections.IntArray2
import it.neckar.open.serialization.roundTrip
import org.junit.jupiter.api.Test

class EnumHistoryValuesTest {
  @Test
  fun testIt() {
    val enumHistoryValues = EnumHistoryValues(IntArray2(3, 4) { 17 * it }, null)

    roundTrip(enumHistoryValues) {
      //language=JSON
      """
        {
          "values" : "AAMABAAAAAAAAAARAAAAIgAAADMAAABEAAAAVQAAAGYAAAB3AAAAiAAAAJkAAACqAAAAuw==",
          "mostOfTheTimeValues" : null
        }
      """.trimIndent()
    }
  }
}
