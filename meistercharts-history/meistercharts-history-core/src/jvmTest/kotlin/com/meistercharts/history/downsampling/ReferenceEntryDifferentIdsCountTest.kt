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
