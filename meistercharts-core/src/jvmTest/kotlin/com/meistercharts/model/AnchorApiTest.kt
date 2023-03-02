package com.meistercharts.model

import assertk.*
import assertk.assertions.*
import org.junit.jupiter.api.Test

/**
 */
internal class AnchorApiTest {
  @Test
  internal fun `Message Layer - center, then above with a gap`() {
    val boundingBox = Rectangle(0.0, 0.0, 100.0, 150.0)

    val basePointProvider = DirectionBasedBasePointProvider(Direction.Center, Distance.zero)
    val basePoint = basePointProvider.calculateBasePoint(boundingBox)

    assertThat(boundingBox.left).isEqualTo(0.0)
    assertThat(boundingBox.right).isEqualTo(100.0)

    assertThat(basePoint.x).isEqualTo(50.0)
    assertThat(basePoint.y).isEqualTo(75.0)

    //zero point
    //starting point
    //origin point
    //initial point
    //point of origin
    //source
    //vantage point

    // place the label above
    val anchorDirection = Direction.BottomCenter
    val anchorGap = 15.0


    //gc.paintBox

  }

  @Test
  internal fun `Message Layer - bottom right with insets, then to the left with a gap`() {

  }
}
