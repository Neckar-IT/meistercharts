/*
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
package com.meistercharts.canvas.resize

import assertk.assertThat
import assertk.assertions.isEqualTo
import it.neckar.geometry.Direction
import org.junit.jupiter.api.Test

class HandleBoundsProviderTest {
  @Test
  fun `maxX and maxY use width and height respectively`() {
    //A non-square handle exposes the copy-paste bug: maxY must add height, not width.
    val provider = object : HandleBoundsProvider {
      override fun minX(direction: Direction): Double = 10.0
      override fun minY(direction: Direction): Double = 20.0
      override fun width(direction: Direction): Double = 3.0
      override fun height(direction: Direction): Double = 7.0
    }

    assertThat(provider.maxX(Direction.TopLeft)).isEqualTo(13.0) //10 + width 3
    assertThat(provider.maxY(Direction.TopLeft)).isEqualTo(27.0) //20 + height 7 (not width 3)
  }
}
