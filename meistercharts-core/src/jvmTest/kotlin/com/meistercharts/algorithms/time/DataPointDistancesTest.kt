package com.meistercharts.algorithms.time

import assertk.*
import assertk.assertions.*
import org.junit.jupiter.api.Test


 class DataPointDistancesTest {
  @Test
   fun testOrder() {
    val values = DataPointDistances.values()
    for (i in 1 until values.size) {
      assertThat(values[i - 1].distance).isLessThan(values[i].distance)
    }
  }

  @Test
   fun testNextGreaterDistance() {
    val values = DataPointDistances.values()
    assertThat(values.last().getNextGreaterDistance()).isNull()
    for (i in 0 until values.size - 1) {
      assertThat(values[i].getNextGreaterDistance()).isEqualTo(values[i + 1])
    }
  }

  @Test
  fun testPreviousSmallerDistance() {
    val values = DataPointDistances.values()
    assertThat(values.first().getPreviousSmallerDistance()).isNull()
    for (i in 1 until values.size) {
      assertThat(values[i].getPreviousSmallerDistance()).isEqualTo(values[i - 1])
    }
  }

  @Test
   fun testGreatestDistance() {
    assertThat(DataPointDistances.greatestDistance.getNextGreaterDistance()).isNull()
  }

  @Test
   fun testSmallestDistance() {
    assertThat(DataPointDistances.smallestDistance.getPreviousSmallerDistance()).isNull()
  }

   @Test
   fun testValuesAscending() {
     val values = DataPointDistances.valuesAscending
     for (i in 1 until values.size) {
       assertThat(values[i - 1].distance).isLessThan(values[i].distance)
     }
   }

   @Test
   fun testValuesDescending() {
     val values = DataPointDistances.valuesDescending
     for (i in 1 until values.size) {
       assertThat(values[i - 1].distance).isGreaterThan(values[i].distance)
     }
   }
 }
