/**
 * Copyright 2023 Neckar IT GmbH, Mössingen, Germany
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
