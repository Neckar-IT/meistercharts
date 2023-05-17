package com.meistercharts.canvas.layout.cache

import assertk.*
import assertk.assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class CoordinatesCacheTest {

  private lateinit var coordinatesCache: CoordinatesCache

  @BeforeEach
  fun setUp() {
    coordinatesCache = CoordinatesCache()
  }

  @Test
  fun `check size after initialization`() {
    assertThat(coordinatesCache.size).isEqualTo(0)
  }

  @Test
  fun `ensure size`() {
    coordinatesCache.prepare(5)
    assertThat(coordinatesCache.size).isEqualTo(5)
  }

  @Test
  fun `set and get x and y coordinates`() {
    coordinatesCache.prepare(1)
    assertThat(coordinatesCache.size).isEqualTo(1)
    coordinatesCache.set(0, 2.0, 3.0)
    assertThat(coordinatesCache.x(0)).isEqualTo(2.0)
    assertThat(coordinatesCache.y(0)).isEqualTo(3.0)
  }

  @Test
  fun `add coordinates and check size and values`() {
    assertThat(coordinatesCache.size).isEqualTo(0)
    coordinatesCache.add(5.0, 6.0)
    assertThat(coordinatesCache.size).isEqualTo(1)
    assertThat(coordinatesCache.size).isEqualTo(1)
    assertThat(coordinatesCache.x(0)).isEqualTo(5.0)
    assertThat(coordinatesCache.y(0)).isEqualTo(6.0)
  }

  @Test
  fun `coordinates provider returns correct values`() {
    coordinatesCache.add(7.0, 8.0)
    val provider = coordinatesCache.asCoordinatesProvider()
    assertThat(provider).isNotNull()
    assertThat(provider.size()).isEqualTo(1)
    assertThat(provider.xAt(0)).isEqualTo(7.0)
    assertThat(provider.yAt(0)).isEqualTo(8.0)
  }

  @Test
  fun testMoreAdd() {
    assertThat(coordinatesCache.size).isEqualTo(0)
    coordinatesCache.add(1.0, 2.0)
    assertThat(coordinatesCache.size).isEqualTo(1)

    assertThat(coordinatesCache.x(0)).isEqualTo(1.0)
    assertThat(coordinatesCache.y(0)).isEqualTo(2.0)

    coordinatesCache.add(3.0, 4.0)
    assertThat(coordinatesCache.size).isEqualTo(2)

    assertThat(coordinatesCache.x(0)).isEqualTo(1.0)
    assertThat(coordinatesCache.y(0)).isEqualTo(2.0)

    assertThat(coordinatesCache.x(1)).isEqualTo(3.0)
    assertThat(coordinatesCache.y(1)).isEqualTo(4.0)
  }
}