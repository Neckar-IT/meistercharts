package com.meistercharts.algorithms.layout

import org.assertj.core.api.Java6Assertions.assertThat
import org.junit.jupiter.api.Test

class LayouterTest {
  @Test
  fun testCalculateCenter() {
    val layouter = Layouter()
    val sizes = doubleArrayOf(10.0, 20.0, 10.0)

    layouter.calculateCenters(sizes).let {
      assertThat(it).hasSize(3)
      assertThat(it).containsExactly(5.0, 20.0, 35.0)
    }

    layouter.calculateCenters(sizes, 2.0).let {
      assertThat(it).hasSize(3)
      assertThat(it).containsExactly(5.0, 20.0 + 2.0, 35.0 + 2.0 * 2)
    }
  }

  @Test
  fun testCalculateCenter2() {
    val layouter = Layouter()
    val sizes = doubleArrayOf(10.0, 20.0, 10.0, 12.0)

    layouter.calculateCenters(sizes).let {
      assertThat(it).hasSize(4)
      assertThat(it).containsExactly(5.0, 20.0, 35.0, 46.0)
    }

    layouter.calculateCenters(sizes, 2.0).let {
      assertThat(it).hasSize(4)
      assertThat(it).containsExactly(5.0, 20.0 + 2.0, 35.0 + 2.0 * 2, 46.0 + 2.0 * 3)
    }
  }

  @Test
  internal fun testfindMinLargestDistanceBetweenCenters() {
    assertThat(doubleArrayOf(10.0, 20.0, 10.0).findLargestDistanceBetweenCenters()).isEqualTo(15.0)
    assertThat(doubleArrayOf(10.0, 20.0, 10.0, 5.0, 20.0).findLargestDistanceBetweenCenters()).isEqualTo(15.0)
    assertThat(doubleArrayOf(10.0, 20.0, 10.0, 5.0, 28.0).findLargestDistanceBetweenCenters()).isEqualTo(16.5)
    assertThat(doubleArrayOf(10.0, 20.0, 20.0, 5.0, 30.0).findLargestDistanceBetweenCenters()).isEqualTo(20.0)
  }

  @Test
  fun testEquidistanceSimple() {
    val layouter = Layouter()
    val sizes = doubleArrayOf(10.0, 20.0, 10.0)

    layouter.calculateEquidistantCenters(sizes).let {
      assertThat(it).hasSize(3)
      assertThat(it).containsExactly(5.0, 20.0, 35.0)
    }
  }
}
