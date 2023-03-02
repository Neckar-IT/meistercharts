package com.meistercharts.algorithms.painter

import assertk.assertThat
import assertk.assertions.hasSize
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import assertk.assertions.isNull
import assertk.assertions.isTrue
import org.junit.jupiter.api.Test

/**
 */
class PathTest {
  @Test
  internal fun testAdd() {
    val path = Path()
    assertThat(path.isEmpty()).isTrue()

    assertThat(path.currentPointOrNull).isNull()
    assertThat(path.firstPointOrNull).isNull()

    path.lineTo(1.0, 2.0)

    assertThat(path.currentPointOrNull).isNotNull()
    assertThat(path.actions).hasSize(1)

    assertThat(path.currentPointOrNull).isEqualTo(path.firstPointOrNull)
    assertThat(path.currentPoint).isEqualTo(path.firstPoint)
  }
}
