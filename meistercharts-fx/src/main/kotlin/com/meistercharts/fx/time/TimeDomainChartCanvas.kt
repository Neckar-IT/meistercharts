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
package com.meistercharts.fx.time

import it.neckar.open.annotations.PaintContext
import com.meistercharts.algorithms.ValueRange
import com.meistercharts.algorithms.ZoomAndTranslationModifier
import com.meistercharts.algorithms.time.DataPoint
import com.meistercharts.annotations.Domain
import it.neckar.open.time.nowMillis
import javafx.scene.canvas.GraphicsContext

/**
 * Default implementation that provides a model
 *
 */
@Deprecated("use algorithms.module instead")
abstract class TimeDomainChartCanvas<T>
protected constructor(
  val model: TimeDiagramModel2<T>,
  @Domain domainValueRange: ValueRange,
  zoomAndPanModifier: ZoomAndTranslationModifier
) :
  BaseTimeDomainChartCanvas(
    model.createTimeRange(nowMillis()),
    domainValueRange,
    zoomAndPanModifier
  ) {

  init {
    model.addListener {
      markAsDirty()
      timeRange = model.createTimeRange(nowMillis())
    }
  }

  override fun paintDiagram(gc: GraphicsContext) {
    if (model.dataPoints.size < 2) {
      //we need at least two data points to paint
      return
    }

    paintCurves(gc, model.dataPoints)
  }

  /**
   * Paints the curves.
   * When this method is called the data points contains at least two elements
   */
  @PaintContext
  protected abstract fun paintCurves(gc: GraphicsContext, dataPoints: List<out @JvmWildcard DataPoint<T>>)
}
