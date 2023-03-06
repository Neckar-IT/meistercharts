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
import assertk.assertions.support.*
import it.neckar.open.formatting.formatUtc
import it.neckar.open.unit.si.ms

/**
 * Compares two time stamps
 */
fun Assert<@ms Double>.isEqualToTimeStamp(utcFormatted: @ms String): Unit = given {
  if (it.formatUtc() == utcFormatted) {
    return
  }

  expected("<${utcFormatted}> but was <${it.formatUtc()}>")
}

fun Assert<@ms Double>.isEqualToTimeStamp(expected: @ms Double): Unit = given {
  if (it == expected) {
    return
  }

  expected("<${expected.formatUtc()}> but was <${it.formatUtc()}>")
}


fun Assert<ReferenceEntryId>.isEqualToReferenceEntryId(expectedId: Int): Unit = given {
  if (it.id == expectedId) {
    return
  }

  assertThat(it.id).isEqualTo(expectedId)
}

fun Assert<ReferenceEntryDifferentIdsCount>.isEqualToReferenceEntryIdsCount(expectedCount: Int): Unit = given {
  if (it.value == expectedCount) {
    return
  }

  assertThat(it.value).isEqualTo(expectedCount)
}
