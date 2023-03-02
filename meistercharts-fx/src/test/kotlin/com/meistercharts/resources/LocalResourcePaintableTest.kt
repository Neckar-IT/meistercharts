package com.meistercharts.resources

import assertk.*
import assertk.assertions.*
import it.neckar.open.javafx.test.JavaFxTest
import com.meistercharts.fx.MeisterChartsPlatform
import kotlinx.coroutines.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@JavaFxTest
class LocalResourcePaintableTest {
  @BeforeEach
  fun setUp() {
    runBlocking(Dispatchers.Main) {
      MeisterChartsPlatform.init()
    }
  }

  @Test
  fun testLocalRes() {
    try {
      LocalResourcePaintable("invalid://asdf")
      fail("Where is the exception?")
    } catch (e: Exception) {
    }

    assertThat(LocalResourcePaintable("aTestResource.txt"))
      .isNotNull()
      .given {
        assertThat(it.delegate).isNotNull()
        assertThat(it.relativePath).isEqualTo("aTestResource.txt")
      }
  }
}
