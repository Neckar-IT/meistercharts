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
import com.meistercharts.history.ReferenceEntryId
import com.meistercharts.history.isEqualToReferenceEntryId
import com.meistercharts.history.isEqualToReferenceEntryIdsCount
import org.junit.jupiter.api.Test

class ReferenceEntryCounterTest {
  @Test
  fun testSimpleBaseFromMeasured() {
    val counter = ReferenceEntryCounter()

    assertThat(counter.differentIdsCount()).isEqualTo(ReferenceEntryDifferentIdsCount.Pending)
    assertThat(counter.containsNoValue).isFalse()
    assertThat(counter.winnerMostOfTheTime()).isEqualTo(ReferenceEntryId.Pending)

    counter.addFromMeasured(ReferenceEntryId(17))

    assertThat(counter.differentIdsCount()).isEqualToReferenceEntryIdsCount(1)
    assertThat(counter.containsNoValue).isFalse()
    assertThat(counter.winnerMostOfTheTime()).isEqualTo(ReferenceEntryId(17))

    counter.addFromMeasured(ReferenceEntryId.NoValue)

    assertThat(counter.differentIdsCount()).isEqualToReferenceEntryIdsCount(1)
    assertThat(counter.containsNoValue).isTrue()
    assertThat(counter.winnerMostOfTheTime()).isEqualTo(ReferenceEntryId(17))

    counter.addFromMeasured(ReferenceEntryId(17))

    assertThat(counter.differentIdsCount()).isEqualToReferenceEntryIdsCount(1)
    assertThat(counter.containsNoValue).isTrue()
    assertThat(counter.winnerMostOfTheTime()).isEqualTo(ReferenceEntryId(17))

    counter.addFromMeasured(ReferenceEntryId(18))

    assertThat(counter.differentIdsCount()).isEqualToReferenceEntryIdsCount(2)
    assertThat(counter.containsNoValue).isTrue()
    assertThat(counter.winnerMostOfTheTime()).isEqualTo(ReferenceEntryId(17))
  }

  @Test
  fun testNoValueFromMeasured() {
    val counter = ReferenceEntryCounter()

    counter.addFromMeasured(ReferenceEntryId.NoValue)

    assertThat(counter.differentIdsCount()).isEqualTo(ReferenceEntryDifferentIdsCount.NoValue)
    assertThat(counter.containsNoValue).isTrue()
    assertThat(counter.winnerMostOfTheTime()).isEqualTo(ReferenceEntryId.NoValue)

    counter.addFromMeasured(ReferenceEntryId(17))

    assertThat(counter.differentIdsCount()).isEqualToReferenceEntryIdsCount(1)
    assertThat(counter.containsNoValue).isTrue()
    assertThat(counter.winnerMostOfTheTime()).isEqualTo(ReferenceEntryId(17))
  }

  @Test
  fun testFromCalculatedMinus1() {
    val counter = ReferenceEntryCounter()
    assertThat(counter.containsNoValue).isFalse()
    assertThat(counter.differentIdsCount()).isEqualTo(ReferenceEntryDifferentIdsCount.Pending)
    assertThat(counter.winnerMostOfTheTime()).isEqualTo(ReferenceEntryId.Pending)

    //Add one
    counter.addFromCalculated(ReferenceEntryId(37), ReferenceEntryDifferentIdsCount(1))
    assertThat(counter.winnerMostOfTheTime()).isEqualToReferenceEntryId(37)
    assertThat(counter.differentIdsCount()).isEqualToReferenceEntryIdsCount(1)

    //Second chunk: same ID - count does *not* increase
    counter.addFromCalculated(ReferenceEntryId(37), ReferenceEntryDifferentIdsCount(1))
    assertThat(counter.winnerMostOfTheTime()).isEqualToReferenceEntryId(37)
    assertThat(counter.differentIdsCount()).isEqualToReferenceEntryIdsCount(1)

    //Third Chunk:, same ID + 1 other ID - count increases - just by 1
    counter.addFromCalculated(ReferenceEntryId(37), ReferenceEntryDifferentIdsCount(2))
    assertThat(counter.winnerMostOfTheTime()).isEqualToReferenceEntryId(37)
    assertThat(counter.differentIdsCount()).isEqualToReferenceEntryIdsCount(2)

    //Fourth chunk: new id - by random chance the new ID
    counter.addFromCalculated(ReferenceEntryId(99), ReferenceEntryDifferentIdsCount(1))
    assertThat(counter.winnerMostOfTheTime()).isEqualToReferenceEntryId(37)
    assertThat(counter.differentIdsCount()).isEqualToReferenceEntryIdsCount(3)
  }

  @Test
  fun testNoValuesFromCalculated() {
    val counter = ReferenceEntryCounter()
    assertThat(counter.containsNoValue).isFalse()
    assertThat(counter.differentIdsCount()).isEqualTo(ReferenceEntryDifferentIdsCount.Pending)
    assertThat(counter.winnerMostOfTheTime()).isEqualTo(ReferenceEntryId.Pending)

    counter.addFromCalculated(ReferenceEntryId(17), ReferenceEntryDifferentIdsCount(123))

    assertThat(counter.differentIdsCount()).isEqualToReferenceEntryIdsCount(123)
    assertThat(counter.containsNoValue).isFalse()
    assertThat(counter.winnerMostOfTheTime()).isEqualTo(ReferenceEntryId(17))

    counter.addFromCalculated(ReferenceEntryId(17), ReferenceEntryDifferentIdsCount(12))

    assertThat(counter.differentIdsCount()).isEqualToReferenceEntryIdsCount(134)
    assertThat(counter.containsNoValue).isFalse()
    assertThat(counter.winnerMostOfTheTime()).isEqualTo(ReferenceEntryId(17))

    counter.addFromCalculated(ReferenceEntryId(18), ReferenceEntryDifferentIdsCount(3))
    assertThat(counter.differentIdsCount()).isEqualToReferenceEntryIdsCount(137)
    assertThat(counter.containsNoValue).isFalse()
    assertThat(counter.winnerMostOfTheTime()).isEqualTo(ReferenceEntryId(17)) //still 17

    counter.addFromCalculated(ReferenceEntryId(18), ReferenceEntryDifferentIdsCount(2))
    assertThat(counter.differentIdsCount()).isEqualToReferenceEntryIdsCount(138)
    assertThat(counter.winnerMostOfTheTime()).isEqualTo(ReferenceEntryId(17)) //still 17

    counter.addFromCalculated(ReferenceEntryId(18), ReferenceEntryDifferentIdsCount(9))
    assertThat(counter.differentIdsCount()).isEqualToReferenceEntryIdsCount(146)
    assertThat(counter.winnerMostOfTheTime()).isEqualTo(ReferenceEntryId(18)) //now 18 wins

    counter.reset()
    assertThat(counter.containsNoValue).isFalse()
    assertThat(counter.winnerMostOfTheTime()).isEqualTo(ReferenceEntryId.Pending)
    assertThat(counter.differentIdsCount()).isEqualTo(ReferenceEntryDifferentIdsCount.Pending)
  }

  @Test
  fun testNoValuesFromCalculatedNoValue() {
    val counter = ReferenceEntryCounter()
    assertThat(counter.containsNoValue).isFalse()
    assertThat(counter.differentIdsCount()).isEqualTo(ReferenceEntryDifferentIdsCount.Pending)
    assertThat(counter.winnerMostOfTheTime()).isEqualTo(ReferenceEntryId.Pending)

    counter.addFromCalculated(ReferenceEntryId.NoValue, ReferenceEntryDifferentIdsCount.NoValue)

    assertThat(counter.differentIdsCount()).isEqualTo(ReferenceEntryDifferentIdsCount.NoValue)
    assertThat(counter.containsNoValue).isTrue()
    assertThat(counter.winnerMostOfTheTime()).isEqualTo(ReferenceEntryId.NoValue)

    counter.addFromCalculated(ReferenceEntryId(17), ReferenceEntryDifferentIdsCount(12))

    assertThat(counter.differentIdsCount()).isEqualToReferenceEntryIdsCount(12)
    assertThat(counter.containsNoValue).isTrue()
    assertThat(counter.winnerMostOfTheTime()).isEqualTo(ReferenceEntryId(17))
  }
}
