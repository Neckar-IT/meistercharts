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
package com.meistercharts.algorithms.layers.legend

import com.meistercharts.algorithms.layers.AbstractLayer
import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.algorithms.layers.LayerType
import com.meistercharts.canvas.DebugFeature
import com.meistercharts.canvas.ConfigurationDsl
import com.meistercharts.canvas.findXWithAnchor
import com.meistercharts.canvas.findYWithAnchor
import com.meistercharts.canvas.paintMark
import com.meistercharts.canvas.paintable.Paintable
import com.meistercharts.model.Direction
import com.meistercharts.model.Orientation
import it.neckar.open.provider.SizedProvider
import it.neckar.open.provider.delegate
import it.neckar.open.unit.other.px

/**
 * A layer that paints a legend
 *
 */
class LegendLayer constructor(
  /**
   * Provides the elements of the legend
   */
  elements: SizedProvider<Paintable>,
  layoutOrientation: Orientation = Orientation.Vertical,
  additionalConfiguration: Configuration.() -> Unit = {},
) : AbstractLayer() {

  val configuration: Configuration = Configuration(elements).also(additionalConfiguration)

  override val type: LayerType
    get() = LayerType.Content

  /**
   * The paintable that paints the legend
   */
  val stackedPaintablesPaintable: StackedPaintablesPaintable = StackedPaintablesPaintable(this.configuration::elements.delegate()) {
    this.layoutOrientation = layoutOrientation
  }

  override fun layout(paintingContext: LayerPaintingContext) {
    super.layout(paintingContext)
    stackedPaintablesPaintable.configuration.entriesGap = configuration.entriesGap
    stackedPaintablesPaintable.layout(paintingContext)
  }

  override fun paint(paintingContext: LayerPaintingContext) {
    val gc = paintingContext.gc

    val x = gc.findXWithAnchor(gc.width, configuration.horizontalGap, configuration.anchorDirection.horizontalAlignment)
    val y = gc.findYWithAnchor(gc.height, configuration.verticalGap, configuration.anchorDirection.verticalAlignment)

    if (gc.debug[DebugFeature.ShowAnchors]) {
      gc.paintMark(x, y)
    }

    stackedPaintablesPaintable.paintInBoundingBox(paintingContext, x, y, configuration.anchorDirection)
  }


  @ConfigurationDsl
  inner class Configuration(
    /**
     * Provides the elements of the legend
     */
    val elements: SizedProvider<Paintable>,
  ) {

    /**
     * The distance of the legend from the anchor
     */
    var horizontalGap: @px Double = 5.0

    /**
     * The distance of the legend from the anchor
     */
    var verticalGap: @px Double = 5.0

    /**
     * The gap between two entries
     */
    var entriesGap: @px Double = 8.0

    /**
     * The placement of the legend in relation to the window.
     */
    var anchorDirection: Direction = Direction.TopCenter
  }
}



