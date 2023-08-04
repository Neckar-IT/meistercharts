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

import com.meistercharts.annotations.Zoomed
import it.neckar.geometry.Coordinates
import it.neckar.geometry.Direction
import it.neckar.open.unit.other.px

/**
 * An anchor point (e.g. when painting texts)
 */
data class Anchoring(
  /**
   * The location of the anchoring
   */
  val anchorX: @px Double,
  val anchorY: @px Double,

  /**
   * The gap between anchor and anchored object
   */
  val gapHorizontal: @Zoomed Double = 0.0,
  val gapVertical: @Zoomed Double = gapHorizontal,
  /**
   * The direction to the *anchor*. The anchored object (e.g. a text) is located to the opposite direction
   */
  val anchorDirection: Direction
) {
  constructor(
    anchor: Coordinates,
    gapHorizontal: @Zoomed Double = 0.0,
    gapVertical: @Zoomed Double = gapHorizontal,
    anchorDirection: Direction

  ) : this(anchor.x, anchor.y, gapHorizontal, gapVertical, anchorDirection)

  /**
   * The location of the anchoring
   *
   * ATTENTION: Creates an object when called
   */
  val anchor: Coordinates
    get() {
      return Coordinates(anchorX, anchorY)
    }
}
