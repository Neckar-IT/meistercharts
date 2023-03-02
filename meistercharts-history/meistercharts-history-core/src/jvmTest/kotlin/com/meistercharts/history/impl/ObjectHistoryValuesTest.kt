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
      dataMaps = List(3) { ReferenceEntriesDataMap.generated },
    )

    assertThat(values.timeStampsCount).isEqualTo(5)
    assertThat(values.dataSeriesCount).isEqualTo(3)

    assertThat(values.getReferenceEntryId(ReferenceEntryDataSeriesIndex(0), TimestampIndex(0))).isEqualTo(ReferenceEntryId((0)))
    assertThat(values.getReferenceEntryId(ReferenceEntryDataSeriesIndex(1), TimestampIndex(0))).isEqualTo(ReferenceEntryId((1)))
    assertThat(values.getReferenceEntryId(ReferenceEntryDataSeriesIndex(0), TimestampIndex(1))).isEqualTo(ReferenceEntryId((3)))
    assertThat(values.getReferenceEntryId(ReferenceEntryDataSeriesIndex(1), TimestampIndex(1))).isEqualTo(ReferenceEntryId((4)))
  }
}
