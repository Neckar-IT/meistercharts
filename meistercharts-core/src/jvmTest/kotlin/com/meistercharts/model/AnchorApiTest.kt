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

import assertk.*
import assertk.assertions.*
import com.meistercharts.geometry.DirectionBasedBasePointProvider
import it.neckar.geometry.Direction
import it.neckar.geometry.Distance
import it.neckar.geometry.Rectangle
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
