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
