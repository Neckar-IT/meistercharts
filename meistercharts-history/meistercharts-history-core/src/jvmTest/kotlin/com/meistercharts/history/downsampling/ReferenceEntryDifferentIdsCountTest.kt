package com.meistercharts.history.downsampling

import assertk.*
import assertk.assertions.*
import com.meistercharts.history.ReferenceEntryDifferentIdsCount
import com.meistercharts.history.isEqualToReferenceEntryIdsCount
import org.junit.jupiter.api.Test

class ReferenceEntryDifferentIdsCountTest {
  @Test
  fun testAdd() {
    assertThat(ReferenceEntryDifferentIdsCount(0) + ReferenceEntryDifferentIdsCount(0)).isEqualToReferenceEntryIdsCount(0)
    assertThat(ReferenceEntryDifferentIdsCount(1) + ReferenceEntryDifferentIdsCount(2)).isEqualToReferenceEntryIdsCount(3)

    assertThat(ReferenceEntryDifferentIdsCount.NoValue + ReferenceEntryDifferentIdsCount.NoValue).isEqualTo(ReferenceEntryDifferentIdsCount.NoValue)
    assertThat(ReferenceEntryDifferentIdsCount.Pending + ReferenceEntryDifferentIdsCount.NoValue).isEqualTo(ReferenceEntryDifferentIdsCount.NoValue)
    assertThat(ReferenceEntryDifferentIdsCount.NoValue + ReferenceEntryDifferentIdsCount.Pending).isEqualTo(ReferenceEntryDifferentIdsCount.NoValue)

    assertThat(ReferenceEntryDifferentIdsCount.Pending + ReferenceEntryDifferentIdsCount(2)).isEqualToReferenceEntryIdsCount(2)
    assertThat(ReferenceEntryDifferentIdsCount(2) + ReferenceEntryDifferentIdsCount.Pending).isEqualToReferenceEntryIdsCount(2)

    assertThat(ReferenceEntryDifferentIdsCount.NoValue + ReferenceEntryDifferentIdsCount(2)).isEqualToReferenceEntryIdsCount(2)
    assertThat(ReferenceEntryDifferentIdsCount(2) + ReferenceEntryDifferentIdsCount.NoValue).isEqualToReferenceEntryIdsCount(2)
  }
}
