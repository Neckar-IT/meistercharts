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
