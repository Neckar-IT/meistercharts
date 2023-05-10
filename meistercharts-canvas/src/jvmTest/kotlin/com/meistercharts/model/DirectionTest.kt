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
package com.meistercharts.model

import assertk.assertThat
import assertk.assertions.isSameAs
import org.junit.jupiter.api.Test

/**
 */
internal class DirectionTest {
  @Test
  internal fun testOpposite() {
    verifyOpposite(Direction.TopCenter, Direction.BottomCenter)
    verifyOpposite(Direction.TopLeft, Direction.BottomRight)
    verifyOpposite(Direction.TopRight, Direction.BottomLeft)

    verifyOpposite(Direction.CenterLeft, Direction.CenterRight)
    verifyOpposite(Direction.Center, Direction.Center)

    verifyOpposite(Direction.BaseLineLeft, Direction.BaseLineRight)
    verifyOpposite(Direction.BaseLineCenter, Direction.BaseLineCenter)
  }

  private fun verifyOpposite(direction: Direction, expectedOpposite: Direction) {
    assertThat(direction.opposite()).isSameAs(expectedOpposite)
    assertThat(expectedOpposite.opposite()).isSameAs(direction)
  }

  @Test
  internal fun testGetStability() {
    Direction.entries.forEach { anchorDirection ->
      assertThat(Direction.get(anchorDirection.verticalAlignment, anchorDirection.horizontalAlignment)).isSameAs(anchorDirection)
    }
  }
}
