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
package com.meistercharts.history

import assertk.*
import assertk.assertions.*
import com.meistercharts.history.rest.HistoryQuery
import com.meistercharts.history.rest.QueryRange
import com.meistercharts.history.rest.toUrlQueryString
import org.junit.jupiter.api.Test

internal class HistoryQueryTest {
  @Test
  internal fun testConvertToUrl() {
    val query = HistoryQuery(QueryRange(1000.0, 2000.0, SamplingPeriod.EveryTenMillis))
    assertThat(query.toUrlQueryString()).isEqualTo("from=1000.0&to=2000.0&resolution=10.0")
  }
}
