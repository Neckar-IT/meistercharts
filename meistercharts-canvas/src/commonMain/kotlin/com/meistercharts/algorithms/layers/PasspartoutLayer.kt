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

import com.meistercharts.algorithms.painter.Color
import com.meistercharts.algorithms.painter.NonOverlappingPasspartoutPaintingStrategy
import com.meistercharts.algorithms.painter.OverlappingPasspartoutPaintingStrategy
import com.meistercharts.algorithms.painter.PasspartoutPainter
import com.meistercharts.algorithms.painter.PasspartoutPaintingStrategy
import com.meistercharts.annotations.Zoomed
import com.meistercharts.canvas.StyleDsl
import com.meistercharts.model.Insets
import com.meistercharts.model.Side
import com.meistercharts.provider.ColorProvider
import com.meistercharts.provider.InsetsProvider
import it.neckar.open.unit.other.px

/**
 * Paints a passpartout around a given area.
 *
 * ATTENTION: Often it is better to use the [ClippingLayer] to make sure a layer does not paint in a given region.
 * A passpartout paints *over* an area. Therefore, it does not work with transparent areas
 */
class PasspartoutLayer(
  styleConfiguration: Style.() -> Unit = {}
) : AbstractLayer() {

  val painter: PasspartoutPainter = PasspartoutPainter()

  val style: Style = Style().also(styleConfiguration)

  override val type: LayerType
    get() = LayerType.Content

  override fun paint(paintingContext: LayerPaintingContext) {
    painter.paintPasspartout(
      paintingContext,
      style.color(),
      style.margin(),
      style.insets(),
      style.strategy,
    )
  }

  @StyleDsl
  class Style {
    var strategy: PasspartoutPaintingStrategy = OverlappingPasspartoutPaintingStrategy

    /**
     * The insets of the [PasspartoutLayer]; this area is filled with [color]
     */
    var insets: @Zoomed InsetsProvider = { Insets.empty }

    /**
     * A margin around the passpartout (which is *not* painted)
     */
    var margin: @Zoomed InsetsProvider = { Insets.empty }

    /**
     * The color used for the passpartout (the area defined by [insets])
     */
    var color: ColorProvider = { Color.white }
  }

  companion object {
    val availableStrategies: List<PasspartoutPaintingStrategy> = listOf(OverlappingPasspartoutPaintingStrategy, NonOverlappingPasspartoutPaintingStrategy)
  }
}

/**
 * Adds a [PasspartoutLayer] with the given [insets] and of the given [color]
 */
fun Layers.addPasspartout(@px @Zoomed insets: Insets, color: Color): PasspartoutLayer {
  PasspartoutLayer {
    this.insets = { insets }
    this.color = { color }
  }.let {
    addLayer(it)
    return it
  }
}

/**
 * Binds this style's insets to the size stored by [axisStyle].
 * Also binds this style's margin to the margin stored by [axisStyle].
 */
fun PasspartoutLayer.Style.bind(axisStyle: AxisStyle) {
  insets = {
    when (axisStyle.side) {
      Side.Left   -> Insets.onlyLeft(axisStyle.size)
      Side.Right  -> Insets.onlyRight(axisStyle.size)
      Side.Top    -> Insets.onlyTop(axisStyle.size)
      Side.Bottom -> Insets.onlyBottom(axisStyle.size)
    }
  }
  margin = {
    when (axisStyle.side) {
      Side.Left   -> Insets.onlyLeft(axisStyle.margin.left)
      Side.Right  -> Insets.onlyRight(axisStyle.margin.right)
      Side.Top    -> Insets.onlyTop(axisStyle.margin.top)
      Side.Bottom -> Insets.onlyBottom(axisStyle.margin.bottom)
    }
  }
}
