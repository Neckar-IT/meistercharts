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
package com.meistercharts.algorithms.layers.debug

import com.meistercharts.algorithms.layers.AbstractLayer
import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.algorithms.layers.LayerType
import com.meistercharts.canvas.paintTextBox
import com.meistercharts.canvas.saved
import com.meistercharts.geometry.Coordinates
import com.meistercharts.model.Direction
import it.neckar.open.formatting.decimalFormat2digits
import it.neckar.open.formatting.percentageFormat2digits
import com.meistercharts.style.BoxStyle

/**
 * Shows the current zoom and pan state
 */
class ZoomAndTranslationDebugLayer : AbstractLayer() {
  override val type: LayerType = LayerType.Notification

  override fun paint(paintingContext: LayerPaintingContext) {
    val gc = paintingContext.gc
    val chartCalculator = paintingContext.chartCalculator

    chartCalculator.window2domainRelative(Coordinates.origin).also {
      gc.paintTextBox("@DomainRelative: ${it.format(percentageFormat2digits)}", Direction.TopLeft, boxStyle = BoxStyle.gray)
    }


    gc.saved {
      gc.translateToCenter()
      gc.paintTextBox("Zoom: ${chartCalculator.chartState.zoom.format(decimalFormat2digits)}", Direction.Center, boxStyle = BoxStyle.gray)
    }


    chartCalculator.window2domainRelative(gc.width, gc.height).also {
      gc.translate(gc.width, gc.height)
      gc.paintTextBox("@DomainRelative: ${it.format(percentageFormat2digits)}", Direction.BottomRight, boxStyle = BoxStyle.gray)
    }

  }
}
