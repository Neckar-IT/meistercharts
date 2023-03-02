package com.meistercharts.events

import assertk.*
import assertk.assertions.*
import com.meistercharts.events.KeyCode
import com.meistercharts.events.KeyStroke
import com.meistercharts.events.ModifierCombination
import org.junit.jupiter.api.Test

/**
 */
class KeyCombinationTest {
  @Test
  fun testBasics() {
    val keyCombination = KeyStroke(KeyCode('V'), ModifierCombination.Control)

    assertThat(keyCombination.modifierCombination.alt).isEqualTo(false)
    assertThat(keyCombination.modifierCombination.control).isEqualTo(true)
  }
}
