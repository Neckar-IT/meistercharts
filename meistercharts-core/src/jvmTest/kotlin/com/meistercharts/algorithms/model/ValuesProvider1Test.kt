package com.meistercharts.algorithms.model

import assertk.*
import assertk.assertions.*
import it.neckar.open.provider.DoublesProvider1
import it.neckar.open.provider.fastForEach
import it.neckar.open.provider.fastForEachIndexed
import org.junit.jupiter.api.Test

/**
 *
 */
class ValuesProvider1Test {
  val doublesProvider: DoublesProvider1<String> = object : DoublesProvider1<String> {
    override fun size(param1: String): Int = 3

    override fun valueAt(index: Int, param1: String): Double {
      return (index + param1.length).toDouble()
    }
  }

  @Test
  internal fun testForEach() {
    val received = mutableListOf<Double>()

    doublesProvider.fastForEach("aa") {
      received.add(it)
    }

    assertThat(received).hasSize(3)
    assertThat(received).containsExactly(0 + 2.0, 1 + 2.0, 2 + 2.0)
  }

  @Test
  internal fun testForEachIndexed() {
    val received = mutableListOf<Double>()

    doublesProvider.fastForEachIndexed("aa") { index, value ->
      assertThat(index).isEqualTo((value - 2).toInt())

      received.add(value)
    }

    assertThat(received).hasSize(3)
    assertThat(received).containsExactly(0 + 2.0, 1 + 2.0, 2 + 2.0)
  }
}
