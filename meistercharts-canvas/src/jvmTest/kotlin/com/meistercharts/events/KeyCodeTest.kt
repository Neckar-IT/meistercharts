package com.meistercharts.events

import com.meistercharts.events.KeyCode
import org.assertj.core.api.Java6Assertions.assertThat
import org.junit.jupiter.api.Test

class KeyCodeTest {
  @Test
  fun testSimple() {
    assertThat(KeyCode('V')).isEqualTo(KeyCode(0x56))
  }
}
