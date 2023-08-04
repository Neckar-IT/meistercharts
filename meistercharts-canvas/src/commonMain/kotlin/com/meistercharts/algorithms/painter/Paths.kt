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
package com.meistercharts.algorithms.painter

import it.neckar.geometry.Direction
import it.neckar.open.unit.other.px

/**
 * Contains several predefined paths and methods related to paths
 */


/**
 * Helper class that creates arrows
 */
object Arrows {
  /**
   * Creates an arrow that points upwards (to the top)
   * 0/0 represents the tip of the arrow
   */
  fun toTop(
    /**
     * The total length of the arrow - including the head
     */
    arrowLength: @px Double,
    /**
     * The height of the head
     */
    arrowHeadHeight: @px Double = 5.0,
    /**
     * The width of the head
     */
    arrowHeadWidth: @px Double = 5.0
  ): Path {
    return to(Direction.TopCenter, arrowLength, arrowHeadHeight, arrowHeadWidth)
  }

  /**
   * Creates an arrow that points towards the given direction
   * 0/0 represents the tip of the arrow
   */
  fun to(
    /**
     * The direction the arrow points towards to
     */
    direction: Direction,
    /**
     * The total length of the arrow - including the head
     */
    arrowLength: @px Double,
    /**
     * The height of the head
     */
    arrowHeadHeight: @px Double = 5.0,
    /**
     * The width of the head
     */
    arrowHeadWidth: @px Double = 5.0
  ): Path {
    return ArrowHead.forOrientation(direction, arrowHeadHeight, arrowHeadWidth).also {
      //the arrow itself

      //Move to the middle bottom
      var centerX = 0.0
      var centerY = 0.0

      var bottomX = 0.0
      var bottomY = 0.0

      when (direction) {
        Direction.TopCenter -> {
          centerX = 0.0
          centerY = arrowHeadHeight

          bottomX = 0.0
          bottomY = arrowLength + arrowHeadHeight
        }

        Direction.BottomCenter -> {
          centerX = 0.0
          centerY = -arrowHeadHeight

          bottomX = 0.0
          bottomY = -arrowLength - arrowHeadHeight
        }

        Direction.CenterLeft -> {
          centerY = 0.0
          centerX = arrowHeadHeight

          bottomY = 0.0
          bottomX = arrowLength + arrowHeadHeight
        }

        Direction.CenterRight -> {
          centerY = 0.0
          centerX = -arrowHeadHeight

          bottomY = 0.0
          bottomX = -arrowLength - arrowHeadHeight
        }

        else -> TODO()
      }


      it.moveTo(centerX, centerY) //middle bottom

      //move to the middle bottom
      it.lineTo(bottomX, bottomY) //middle bottom
    }
  }
}

/**
 * Creates arrow heads
 */
object ArrowHead {
  /**
   * Creates an arrow head that points towards the top.
   * 0/0 represents the tip of the arrow
   */
  fun toTop(@px arrowHeadLength: Double = 5.0, @px arrowHeadWidth: Double = 5.0): Path {
    return Path().also {
      it.moveTo(0.0, 0.0) //start @ top of arrow
      it.lineTo(arrowHeadWidth / 2.0, arrowHeadLength) //bottom right
      it.lineTo(-arrowHeadWidth / 2.0, arrowHeadLength) //bottom left
      it.lineTo(0.0, 0.0) //back to top of arrow
    }
  }

  /**
   * Creates an arrow head that points towards the given direction.
   * 0/0 represents the tip of the arrow
   */
  fun forOrientation(direction: Direction, @px arrowHeadLength: Double = 5.0, @px arrowHeadWidth: Double = 5.0): Path {
    return Path().also { path ->
      forOrientation(path, direction, arrowHeadLength, arrowHeadWidth) //back to top of arrow
    }
  }

  /**
   * adds the arrow path
   */
  fun forOrientation(
    path: SupportsPathActions,
    direction: Direction,
    /**
     * Distance between tip of the arrow to its base
     */
    arrowHeadLength: @px Double = 5.0,
    arrowHeadWidth: @px Double = 5.0,
  ) {
    path.moveTo(0.0, 0.0) //start @ top of arrow

    var bottomRightX = 0.0
    var bottomRightY = 0.0
    var bottomLeftX = 0.0
    var bottomLeftY = 0.0

    when (direction) {
      Direction.TopCenter -> {
        bottomRightX = arrowHeadWidth / 2.0
        bottomRightY = arrowHeadLength
        bottomLeftX = -arrowHeadWidth / 2.0
        bottomLeftY = arrowHeadLength
      }

      Direction.BottomCenter -> {
        bottomRightX = -arrowHeadWidth / 2.0
        bottomRightY = -arrowHeadLength
        bottomLeftX = arrowHeadWidth / 2.0
        bottomLeftY = -arrowHeadLength
      }

      Direction.CenterLeft -> {
        bottomRightY = -arrowHeadWidth / 2.0
        bottomRightX = arrowHeadLength
        bottomLeftY = arrowHeadWidth / 2.0
        bottomLeftX = arrowHeadLength
      }

      Direction.CenterRight -> {
        bottomRightY = -arrowHeadWidth / 2.0
        bottomRightX = -arrowHeadLength
        bottomLeftY = arrowHeadWidth / 2.0
        bottomLeftX = -arrowHeadLength
      }

      else -> TODO()
    }

    path.lineTo(bottomRightX, bottomRightY) //bottom right
    path.lineTo(bottomLeftX, bottomLeftY) //bottom left
    path.closePath()
  }
}
