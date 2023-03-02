package com.meistercharts.history.downsampling

import assertk.*
import assertk.assertions.*
import com.meistercharts.history.HistoryEnumOrdinal
import com.meistercharts.history.HistoryEnumSet
import org.junit.jupiter.api.Test

/**
 *
 */
class HistoryEnumOrdinalCounterTest {
  @Test
  fun testPending() {
    val counter = HistoryEnumOrdinalCounter()
    assertThat(counter.winner()).isEqualTo(HistoryEnumOrdinal.Pending)
  }

  @Test
  fun testSimple() {
    val counter = HistoryEnumOrdinalCounter()
    assertThat(counter.winner()).isEqualTo(HistoryEnumOrdinal.Pending)

    assertThat(counter.count(HistoryEnumOrdinal(0))).isEqualTo(0)
    assertThat(counter.count(HistoryEnumOrdinal(7))).isEqualTo(0)

    counter.add(HistoryEnumOrdinal(7))
    assertThat(counter.count(HistoryEnumOrdinal(0))).isEqualTo(0)
    assertThat(counter.count(HistoryEnumOrdinal(7))).isEqualTo(1)

    assertThat(counter.winner()).isEqualTo(HistoryEnumOrdinal(7))

    counter.add(HistoryEnumOrdinal(8))
    assertThat(counter.count(HistoryEnumOrdinal(0))).isEqualTo(0)
    assertThat(counter.count(HistoryEnumOrdinal(7))).isEqualTo(1)
    assertThat(counter.count(HistoryEnumOrdinal(8))).isEqualTo(1)

    //Tie
    assertThat(counter.winner()).isEqualTo(HistoryEnumOrdinal(7))

    counter.add(HistoryEnumOrdinal(8))
    assertThat(counter.count(HistoryEnumOrdinal(0))).isEqualTo(0)
    assertThat(counter.count(HistoryEnumOrdinal(7))).isEqualTo(1)
    assertThat(counter.count(HistoryEnumOrdinal(8))).isEqualTo(2)

    //Tie
    assertThat(counter.winner()).isEqualTo(HistoryEnumOrdinal(8))
  }

  @Test
  fun testAddSet() {
    val counter = HistoryEnumOrdinalCounter()
    assertThat(counter.winner()).isEqualTo(HistoryEnumOrdinal.Pending)

    counter.addAll(HistoryEnumSet(0b1010))
    assertThat(counter.count(HistoryEnumOrdinal(0))).isEqualTo(0)
    assertThat(counter.count(HistoryEnumOrdinal(1))).isEqualTo(1)
    assertThat(counter.count(HistoryEnumOrdinal(2))).isEqualTo(0)
    assertThat(counter.count(HistoryEnumOrdinal(3))).isEqualTo(1)
    assertThat(counter.count(HistoryEnumOrdinal(7))).isEqualTo(0)

    assertThat(counter.winner()).isEqualTo(HistoryEnumOrdinal(1))

    counter.addAll(HistoryEnumSet(0b1011))
    assertThat(counter.count(HistoryEnumOrdinal(0))).isEqualTo(1)
    assertThat(counter.count(HistoryEnumOrdinal(1))).isEqualTo(2)
    assertThat(counter.count(HistoryEnumOrdinal(2))).isEqualTo(0)
    assertThat(counter.count(HistoryEnumOrdinal(3))).isEqualTo(2)
    assertThat(counter.count(HistoryEnumOrdinal(7))).isEqualTo(0)

    //Tie
    assertThat(counter.winner()).isEqualTo(HistoryEnumOrdinal(1))

    counter.addAll(HistoryEnumSet(0b1001))
    assertThat(counter.count(HistoryEnumOrdinal(0))).isEqualTo(2)
    assertThat(counter.count(HistoryEnumOrdinal(1))).isEqualTo(2)
    assertThat(counter.count(HistoryEnumOrdinal(2))).isEqualTo(0)
    assertThat(counter.count(HistoryEnumOrdinal(3))).isEqualTo(3)
    assertThat(counter.count(HistoryEnumOrdinal(7))).isEqualTo(0)

    //Tie
    assertThat(counter.winner()).isEqualTo(HistoryEnumOrdinal(3))
  }
}
