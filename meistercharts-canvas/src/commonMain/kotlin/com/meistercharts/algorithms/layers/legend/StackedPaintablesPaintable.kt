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

import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.algorithms.layout.PaintablesLayouter
import com.meistercharts.canvas.ConfigurationDsl
import com.meistercharts.color.Color
import com.meistercharts.canvas.DebugFeature
import com.meistercharts.canvas.paintable.AbstractPaintable
import com.meistercharts.canvas.paintable.Paintable
import com.meistercharts.canvas.paintable.PaintablePaintingVariables
import com.meistercharts.canvas.paintable.AbstractPaintablePaintingVariables
import it.neckar.geometry.HorizontalAlignment
import it.neckar.geometry.Orientation
import it.neckar.geometry.Rectangle
import it.neckar.geometry.VerticalAlignment
import it.neckar.open.provider.SizedProvider1
import it.neckar.open.provider.asMultiProvider
import it.neckar.open.provider.asSizedProvider
import it.neckar.open.unit.other.px

/**
 * Paints a list of vertically or horizontally stacked paintables
 * Places paintables from top to bottom - left aligned.
 *
 * ATTENTION: Call [layout] before painting
 */
class StackedPaintablesPaintable(
  /**
   * Provides the elements of the legend
   */
  elements: SizedProvider1<Paintable, LayerPaintingContext>,
  additionalConfiguration: Configuration.() -> Unit = {},
) : AbstractPaintable() {

  val configuration: Configuration = Configuration(elements = elements).also(additionalConfiguration)

  override fun paintingVariables(): PaintablePaintingVariables {
    return paintingVariables
  }

  /**
   * The painting variables
   */
  private val paintingVariables = object : AbstractPaintablePaintingVariables() {
    /**
     * Calculates the layout for the paintables
     */
    val layoutManager = PaintablesLayouter()

    override fun calculate(paintingContext: LayerPaintingContext) {
      super.calculate(paintingContext)

      layoutManager.configuration.also {
        it.layoutOrientation = configuration.layoutOrientation
        it.horizontalAlignment = configuration.horizontalAlignment
        it.verticalAlignment = configuration.verticalAlignment
        it.gap = configuration.entriesGap
      }

      layoutManager.calculate(paintingContext, configuration.elements.asSizedProvider(paintingContext))

      boundingBox = Rectangle.topLeft(layoutManager.totalSize())
    }
  }

  override fun paintAfterLayout(paintingContext: LayerPaintingContext, x: Double, y: Double) {
    val gc = paintingContext.gc

    gc.translate(x, y)
    paintingVariables.layoutManager.paintAllPaintables(paintingContext, configuration.elements.asMultiProvider(paintingContext))

    paintingContext.ifDebug(DebugFeature.ShowBounds) {
      gc.stroke(Color.blue)
      gc.strokeRect(paintingVariables.boundingBox)
    }
  }

  @ConfigurationDsl
  class Configuration(
    /**
     * Provides the elements of the legend
     */
    val elements: SizedProvider1<Paintable, LayerPaintingContext>,
  ) {

    /**
     * The (vertical) gap between the entries of the legend
     */
    var entriesGap: @px Double = 10.0

    /**
     * How the paintables are laid out.
     */
    var layoutOrientation: Orientation = Orientation.Vertical

    var horizontalAlignment: HorizontalAlignment = HorizontalAlignment.Left
    var verticalAlignment: VerticalAlignment = VerticalAlignment.Baseline
  }
}
