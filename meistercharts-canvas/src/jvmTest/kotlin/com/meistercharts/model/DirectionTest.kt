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
    Direction.values().forEach { anchorDirection ->
      assertThat(Direction.get(anchorDirection.verticalAlignment, anchorDirection.horizontalAlignment)).isSameAs(anchorDirection)
    }
  }
}
