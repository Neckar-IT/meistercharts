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

import com.meistercharts.history.ReferenceEntryData
import com.meistercharts.history.ReferenceEntryId
import com.meistercharts.history.UnparsedJson
import it.neckar.open.serialization.roundTrip
import it.neckar.open.i18n.TextKey
import org.junit.jupiter.api.Test

class ReferenceEntryDataTest {
  @Test
  fun testSerialization() {
    roundTrip(ReferenceEntryData(ReferenceEntryId(351583), TextKey.simple("The label"), UnparsedJson("{the unparsed json}"))) {
      //language=JSON
      """
        {
          "id" : 351583,
          "label" : {
            "key" : "The label",
            "fallbackText" : "The label"
          },
          "payload" : "{the unparsed json}"
        }
      """.trimIndent()
    }

    roundTrip(ReferenceEntryData(ReferenceEntryId(351583), TextKey.simple("The label"), null)) {
      //language=JSON
      """
        {
          "id" : 351583,
          "label" : {
            "key" : "The label",
            "fallbackText" : "The label"
          },
          "payload" : null
        }
      """.trimIndent()
    }
  }
}
