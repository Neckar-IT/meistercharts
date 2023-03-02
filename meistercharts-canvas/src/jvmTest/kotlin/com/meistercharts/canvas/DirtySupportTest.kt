package com.meistercharts.canvas

import assertk.*
import assertk.assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail

/**
 */
class DirtySupportTest {
  @Test
  fun testLogger() {
    val dirtySupport = DirtySupport()

    dirtySupport.ifDirty {
      fail("Must not be called")
    }

    dirtySupport.markAsDirty()

    var called = false
    dirtySupport.ifDirty {
      called = true
    }

    assertThat(called).isTrue()
  }
}
