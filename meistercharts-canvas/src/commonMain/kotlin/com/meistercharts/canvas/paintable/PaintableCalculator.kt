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
package com.meistercharts.canvas.paintable

import com.meistercharts.calc.ChartCalculator
import com.meistercharts.annotations.ContentAreaRelative
import com.meistercharts.annotations.DomainRelative
import com.meistercharts.annotations.PaintableArea
import com.meistercharts.annotations.Zoomed
import it.neckar.geometry.Rectangle
import com.meistercharts.state.ChartState

/**
 *
 * Contains the calculation methods relevant for and in the context of a paintable
 *
 */
@Deprecated("Use an updated chart state instead")
class PaintableCalculator(
  val chartCalculator: ChartCalculator,
  val boundingBox: Rectangle
) {

  /**
   * Returns the chart state
   */
  val chartState: ChartState
    get() = chartCalculator.chartState

  /**
   * Returns the *delta* y value for a domain relative value.
   */
  fun domainRelative2heightDelta(valueY: @DomainRelative Double): @Zoomed Double {
    @ContentAreaRelative val contentArea0 = chartCalculator.domainRelative2contentAreaRelativeY(0.0)
    @ContentAreaRelative val contentAreaValue = chartCalculator.domainRelative2contentAreaRelativeY(valueY)

    return (contentAreaValue - contentArea0) * boundingBox.getHeight()
  }

  /**
   * Returns the *absolute* value (relative to the origin of the paintable)
   */
  fun domainRelative2y(valueY: @DomainRelative Double): @Zoomed Double {
    val factor = if (chartState.axisOrientationY.axisInverted) -1.0 else 1.0

    return valueY * boundingBox.getHeight() * factor + boundingBox.getY()
  }

  /**
   * Returns the *delta* x value for a domain relative value.
   */
  fun domainRelative2widthDelta(valueX: @DomainRelative Double): @PaintableArea Double {
    @ContentAreaRelative val contentArea0 = chartCalculator.domainRelative2contentAreaRelativeX(0.0)
    @ContentAreaRelative val contentAreaValue = chartCalculator.domainRelative2contentAreaRelativeX(valueX)

    return (contentAreaValue - contentArea0) * boundingBox.getWidth()
  }

  /**
   * Returns the *absolute* value (relative to the origin of the paintable)
   */
  fun domainRelative2x(valueX: @DomainRelative Double): @PaintableArea Double {
    val factor = if (chartState.axisOrientationX.axisInverted) -1.0 else 1.0
    return valueX * boundingBox.getWidth() * factor + boundingBox.getX()
  }
}
