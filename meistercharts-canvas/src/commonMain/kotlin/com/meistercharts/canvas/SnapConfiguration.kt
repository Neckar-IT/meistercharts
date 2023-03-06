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
package com.meistercharts.canvas

import com.meistercharts.model.Insets
import it.neckar.open.unit.other.px

/**
 * Whether to snap to full values
 */
enum class SnapConfiguration(
  /**
   * Snap on the x axis
   */
  val snapX: Boolean,
  /**
   * Snap on the y axis
   */
  val snapY: Boolean,
) {
  /**
   * Does not snap
   */
  None(false, false),
  OnlyX(true, false),
  OnlyY(false, true),
  Both(true, true);

  /**
   * Snaps the value to *physical* pixels
   */
  @px
  fun snapXValue(@px value: Double): Double {
    return PaintingUtils.snapPosition(value, snapX)
  }

  /**
   * Snaps the value to *physical* pixels
   */
  @px
  fun snapXSize(@px size: Double): Double {
    return PaintingUtils.snapSize(size, snapX)
  }

  /**
   * Snaps the value to *physical* pixels
   */
  @px
  fun snapYValue(@px value: Double): Double {
    return PaintingUtils.snapPosition(value, snapY)
  }

  /**
   * Snaps the value to *physical* pixels
   */
  @px
  fun snapYSize(@px size: Double): Double {
    return PaintingUtils.snapSize(size, snapY)
  }

  /**
   * Snaps the value to *physical* pixels
   */
  @px
  fun snapInsets(@px insets: Insets): Insets {
    return Insets(
      snapYValue(insets.top),
      snapXValue(insets.right),
      snapYValue(insets.bottom),
      snapXValue(insets.left)
    )
  }
}

/**
 * Snaps to the physical pixels - depending on the snap configuration
 */
fun CanvasRenderingContext.snapPhysicalTranslation(snapConfiguration: SnapConfiguration) {
  this.snapPhysicalTranslation(snapX = snapConfiguration.snapX, snapY = snapConfiguration.snapY)
}
