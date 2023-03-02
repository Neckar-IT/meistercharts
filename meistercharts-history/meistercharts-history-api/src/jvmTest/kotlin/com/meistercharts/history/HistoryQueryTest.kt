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
