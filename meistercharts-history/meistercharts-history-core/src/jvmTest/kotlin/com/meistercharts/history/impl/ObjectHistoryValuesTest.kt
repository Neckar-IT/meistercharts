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
import it.neckar.open.collections.IntArray2
import com.meistercharts.history.ReferenceEntriesDataMap
import com.meistercharts.history.ReferenceEntryDataSeriesIndex
import com.meistercharts.history.ReferenceEntryId
import com.meistercharts.history.TimestampIndex
import org.junit.jupiter.api.Test

class ObjectHistoryValuesTest {
  @Test
  fun testBasics() {
    val values = ReferenceEntryHistoryValues(
      values = IntArray2(3, 5) { it },
      differentIdsCount = null,
      dataMap = ReferenceEntriesDataMap.generated,
    )

    assertThat(values.timeStampsCount).isEqualTo(5)
    assertThat(values.dataSeriesCount).isEqualTo(3)

    assertThat(values.getReferenceEntryId(ReferenceEntryDataSeriesIndex(0), TimestampIndex(0))).isEqualTo(ReferenceEntryId((0)))
    assertThat(values.getReferenceEntryId(ReferenceEntryDataSeriesIndex(1), TimestampIndex(0))).isEqualTo(ReferenceEntryId((1)))
    assertThat(values.getReferenceEntryId(ReferenceEntryDataSeriesIndex(0), TimestampIndex(1))).isEqualTo(ReferenceEntryId((3)))
    assertThat(values.getReferenceEntryId(ReferenceEntryDataSeriesIndex(1), TimestampIndex(1))).isEqualTo(ReferenceEntryId((4)))
  }
}
