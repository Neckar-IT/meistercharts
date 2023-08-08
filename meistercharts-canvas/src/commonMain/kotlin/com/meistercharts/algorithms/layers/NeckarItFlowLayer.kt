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
package com.meistercharts.algorithms.layers

import com.meistercharts.color.Color
import com.meistercharts.canvas.DebugFeature
import com.meistercharts.canvas.DirtyReason
import com.meistercharts.design.neckarit.NeckarItFlowPaintable
import it.neckar.geometry.Coordinates
import it.neckar.open.unit.other.pct
import it.neckar.open.unit.other.px

/**
 * Paints the Neckar IT 'flow'
 */
class NeckarItFlowLayer(
  configuration: Configuration.() -> Unit = {}
) : AbstractLayer() {
  override val type: LayerType = LayerType.Content

  val style: Configuration = Configuration().also(configuration)

  override
  fun paint(paintingContext: LayerPaintingContext) {
    val gc = paintingContext.gc

    val paintable = NeckarItFlowPaintable.forWidth(gc.width)

    //Translate to center Y
    gc.translate(0.0, gc.centerY + style.gapToCenterY)

    gc.globalAlpha = style.opacity

    paintable.paint(paintingContext, Coordinates.none)
    paintingContext.chartSupport.markAsDirty(DirtyReason.Animation)

    paintingContext.ifDebug(DebugFeature.ShowBounds) {
      gc.stroke(Color.red)
      gc.strokeRect(paintable.boundingBox(paintingContext))
    }
  }

  class Configuration {
    /**
     * The gap between the center of the canvas to the top side of the paintable
     */
    @px
    var gapToCenterY: Double = 25.0

    var opacity: @pct Double = 1.0
  }

}
