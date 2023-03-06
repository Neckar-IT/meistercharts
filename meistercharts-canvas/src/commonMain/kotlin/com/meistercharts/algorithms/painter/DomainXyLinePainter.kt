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

import com.meistercharts.algorithms.ChartCalculator
import com.meistercharts.annotations.DomainRelative
import com.meistercharts.annotations.TimeRelative
import com.meistercharts.canvas.CanvasRenderingContext

/**
 * Paints a xy line
 *
 */
@Deprecated("convert manually")
class DomainXyLinePainter(
  calculator: ChartCalculator,
  private val delegate: XYPainter
) : AbstractDomainRelativePainter(calculator, delegate.isSnapXValues, delegate.isSnapYValues) {

  constructor(
    calculator: ChartCalculator,
    gc: CanvasRenderingContext,
    snapXValues: Boolean,
    snapYValues: Boolean
  ) : this(calculator, FancyXyLinePainter(snapXValues, snapYValues))

  fun addPoint(gc: CanvasRenderingContext, @TimeRelative x: Double, @DomainRelative y: Double) {
    delegate.addCoordinate(
      gc,
      calculator.domainRelative2windowX(x),
      calculator.domainRelative2windowY(y)
    )
  }

  /**
   * Is called at the end of the path
   */
  fun finish(gc: CanvasRenderingContext) {
    delegate.finish(gc)
  }
}
