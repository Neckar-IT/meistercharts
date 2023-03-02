package com.meistercharts.algorithms.model

import assertk.*
import assertk.assertions.*
import it.neckar.open.provider.DoublesProvider
import it.neckar.open.provider.fastForEach
import it.neckar.open.provider.fastForEachIndexed
import org.junit.jupiter.api.Test

/**
 *
 */
class ValuesProviderTest {
  @Test
  internal fun testForEach() {
    val valuesProvider = DoublesProvider.forDoubles(1.0, 2.0, 3.0)

    val received = mutableListOf<Double>()

    valuesProvider.fastForEach {
      received.add(it)
    }

    assertThat(received).hasSize(3)
    assertThat(received).containsExactly(1.0, 2.0, 3.0)
  }

  @Test
  internal fun testForEachIndexed() {
    val valuesProvider = DoublesProvider.forDoubles(1.0, 2.0, 3.0)

    val received = mutableListOf<Double>()

    valuesProvider.fastForEachIndexed { index, value ->
      assertThat(index).isEqualTo((value - 1).toInt())

      received.add(value)
    }

    assertThat(received).hasSize(3)
    assertThat(received).containsExactly(1.0, 2.0, 3.0)
  }
}
