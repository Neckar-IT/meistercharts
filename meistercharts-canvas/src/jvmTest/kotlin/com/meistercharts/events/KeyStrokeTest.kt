package com.meistercharts.events

import assertk.*
import assertk.assertions.*
import com.meistercharts.events.KeyCode
import com.meistercharts.events.KeyStroke
import com.meistercharts.events.ModifierCombination
import org.junit.jupiter.api.Test

class KeyStrokeTest {
  @Test
  fun testKeyCodeChar() {
    KeyCode(68).let {
      assertThat(it.code).isEqualTo(68)
      assertThat(it).isEqualTo(KeyCode(68))
    }

    KeyCode('D').let {
      assertThat(it.code).isEqualTo(68)
      assertThat(it).isEqualTo(KeyCode('D'))
    }
  }

  @Test
  fun testEquals() {
    assertThat(ModifierCombination.CtrlShiftAlt).isEqualTo(
      ModifierCombination(
        shift = true,
        control = true,
        alt = true
      )
    )

    assertThat(
      KeyStroke(KeyCode('D'), ModifierCombination.CtrlShiftAlt)
    ).isEqualTo(
      KeyStroke(
        KeyCode('D'), ModifierCombination(
          shift = true,
          control = true,
          alt = true
        )
      )
    )
  }
}
