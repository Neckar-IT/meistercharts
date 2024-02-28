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
package com.meistercharts.canvas.slider

import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.canvas.fill
import com.meistercharts.canvas.paintable.AbstractPaintable
import com.meistercharts.canvas.paintable.AbstractPaintablePaintingVariables
import com.meistercharts.canvas.paintable.PaintablePaintingVariables
import com.meistercharts.color.Color
import com.meistercharts.color.ColorProvider
import com.meistercharts.color.ColorProviderNullable
import com.meistercharts.color.get
import com.meistercharts.style.Shadow
import it.neckar.geometry.Rectangle
import it.neckar.open.unit.other.px

/**
 * Paints a round handle for a slider.
 */
class RoundHandlePaintable(
  additionalConfiguration: Configuration.() -> Unit = {},
) : AbstractPaintable(), Slider.HandlePaintable {

  val configuration: Configuration = Configuration().also(additionalConfiguration)

  private var paintingVariables = object : AbstractPaintablePaintingVariables() {
    override fun calculate(paintingContext: LayerPaintingContext) {
      super.calculate(paintingContext)
      boundingBox = Rectangle.centered(configuration.diameter, configuration.diameter)
    }
  }

  override fun paintingVariables(): PaintablePaintingVariables {
    return paintingVariables
  }

  override fun updateState(state: Slider.State) {
    this.configuration.state = state
  }

  override fun paintAfterLayout(paintingContext: LayerPaintingContext, x: Double, y: Double) {
    val gc = paintingContext.gc


    val relevantStroke: Color?

    when (configuration.state) {
      Slider.State.Default -> {
        gc.fill(configuration.fill)
        gc.shadow(configuration.shadow)
        relevantStroke = configuration.stroke.get()
      }

      Slider.State.MouseOverHandle -> {
        gc.fill(configuration.fillMouseOver)
        gc.shadow(configuration.shadowMouseOver)
        relevantStroke = configuration.strokeMouseOver.get()
      }

      Slider.State.Dragging -> {
        gc.fill(configuration.fillDragging)
        gc.shadow(configuration.shadowDragging)
        relevantStroke = configuration.strokeDragging.get()
      }
    }

    gc.translate(x, y)
    gc.fillOvalCenter(0.0, 0.0, configuration.diameter, configuration.diameter)

    if (relevantStroke != null) {
      gc.clearShadow()
      gc.stroke(relevantStroke)
      gc.strokeOvalCenter(0.0, 0.0, configuration.diameter, configuration.diameter)
    }
  }

  class Configuration {
    /**
     * The diameter of the slider handle
     */
    var diameter: @px Double = 15.0

    var fill: ColorProvider = Color.white
    var fillMouseOver: ColorProvider = Color.whitesmoke
    var fillDragging: ColorProvider = Color.whitesmoke

    var stroke: ColorProviderNullable = Color.silver
    var strokeMouseOver: ColorProviderNullable = Color.silver
    var strokeDragging: ColorProviderNullable = Color.silver

    var shadow: Shadow? = Shadow.Drop
    var shadowMouseOver: Shadow? = Shadow.Drop
    var shadowDragging: Shadow? = Shadow.DropSmall

    var state: Slider.State = Slider.State.Default
  }
}
