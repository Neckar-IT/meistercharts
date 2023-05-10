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

import com.meistercharts.annotations.Window
import com.meistercharts.annotations.Zoomed
import com.meistercharts.canvas.CanvasRenderingContext
import com.meistercharts.canvas.PaintingUtils
import it.neckar.open.unit.other.px
import kotlin.math.max
import kotlin.math.min

/**
 * Abstract base class for painter
 */
abstract class AbstractPainter(
  override val isSnapXValues: Boolean,
  override val isSnapYValues: Boolean
) : Painter {

  @px
  @Window
  override fun snapXPosition(@px @Window xValue: Double): Double {
    return PaintingUtils.snapPosition(xValue, isSnapXValues)
  }

  @px
  @Window
  override fun snapWidth(@px @Window xValue: Double): Double {
    return PaintingUtils.snapSize(xValue, isSnapXValues)
  }

  @px
  @Window
  override fun snapYPosition(@px @Window yValue: Double): Double {
    return PaintingUtils.snapPosition(yValue, isSnapYValues)
  }

  @px
  @Window
  override fun snapHeight(@px @Window yValue: Double): Double {
    return PaintingUtils.snapSize(yValue, isSnapYValues)
  }

  /**
   * Fills a rect - automatically detects which of the x/y values are larger/smaller
   */
  //TODO as extension method for gc
  fun fillRect(gc: CanvasRenderingContext, @px @Window x1: Double, @px @Window x2: Double, @px @Window y1: Double, @px @Window y2: Double) {
    @px @Window val largerY = max(y1, y2)
    @px @Window val smallerY = min(y1, y2)

    @px @Window val largerX = max(x1, x2)
    @px @Window val smallerX = min(x1, x2)

    @px @Zoomed val height = largerY - smallerY
    @px @Zoomed val width = largerX - smallerX

    gc.fillRect(snapXPosition(smallerX), snapYPosition(smallerY), snapWidth(width), snapHeight(height))
  }

}
