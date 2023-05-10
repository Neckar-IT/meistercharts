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
package com.meistercharts.algorithms.layout

import assertk.*
import assertk.assertions.*
import com.meistercharts.model.Insets
import it.neckar.open.serialization.roundTrip
import org.junit.jupiter.api.Test


class InsetsTest {
  @Test
  fun testSerializer() {
    roundTrip(Insets(1.0, 2.0, 3.0, 4.0), Insets.serializer()) {
      //language=JSON
      """
        {
          "top" : 1.0,
          "right" : 2.0,
          "bottom" : 3.0,
          "left" : 4.0
        }
      """.trimIndent()
    }
  }

  @Test
  fun testBasics() {
    val insets = Insets(1.0, 2.0, 3.0, 4.0)
    assertThat(insets.top).isEqualTo(1.0)
    assertThat(insets.right).isEqualTo(2.0)
    assertThat(insets.bottom).isEqualTo(3.0)
    assertThat(insets.left).isEqualTo(4.0)

    assertThat(insets.offsetWidth).isEqualTo(insets.left + insets.right)
    assertThat(insets.offsetHeight).isEqualTo(insets.top + insets.bottom)
  }

  @Test
  fun testCopyFunctions() {
    val insets = Insets(1.0, 2.0, 3.0, 4.0)

    assertThat(insets.withTop(5.0)).isEqualTo(Insets(5.0, 2.0, 3.0, 4.0))
    assertThat(insets.withRight(5.0)).isEqualTo(Insets(1.0, 5.0, 3.0, 4.0))
    assertThat(insets.withBottom(5.0)).isEqualTo(Insets(1.0, 2.0, 5.0, 4.0))
    assertThat(insets.withLeft(5.0)).isEqualTo(Insets(1.0, 2.0, 3.0, 5.0))
  }

}
