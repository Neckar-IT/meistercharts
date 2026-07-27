/*
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
package com.meistercharts.algorithms.layers.barchart

import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.canvas.ConfigurationDsl
import com.meistercharts.canvas.paintTextBox
import com.meistercharts.canvas.paintable.Paintable
import com.meistercharts.canvas.saved
import com.meistercharts.color.Color
import com.meistercharts.color.ColorProvider
import com.meistercharts.font.FontDescriptorFragment
import com.meistercharts.model.Insets
import com.meistercharts.range.LinearValueRange
import com.meistercharts.range.ValueRange
import com.meistercharts.style.BoxStyle
import it.neckar.geometry.Coordinates
import it.neckar.geometry.Direction
import it.neckar.geometry.Rectangle
import it.neckar.geometry.Size
import it.neckar.open.kotlin.lang.asProvider
import it.neckar.open.provider.DefaultDoublesProvider
import it.neckar.open.provider.DoublesProvider
import it.neckar.open.provider.MultiProvider
import it.neckar.open.unit.other.px

/**
 * Paints a single bar with a label - with a configurable total height
 */
class StackedBarWithLabelPaintable(
  width: @px Double = 15.0,
  height: @px Double = 200.0,
  additionalConfiguration: Configuration.() -> Unit = {},
) : Paintable {

  constructor(
    name: String = "",
    valuesProvider: DoublesProvider = DefaultDoublesProvider(listOf(5.0, 6.0, 7.0)),
    valueRange: LinearValueRange = ValueRange.default,
    colors: List<ColorProvider>,
    width: @px Double = 15.0,
    height: @px Double = 200.0,
  ) : this(width, height, {
    this.name = name
    this.valuesProvider = valuesProvider
    this.valueRange = valueRange
    this.colorsProvider = MultiProvider.forListModuloProvider(values = colors, fallback = Color.gray())
  })

  val stackedBarPaintable: StackedBarPaintable = StackedBarPaintable(width, height)

  val configuration: Configuration = Configuration().also(additionalConfiguration)

  var width: Double by stackedBarPaintable::width
  var height: Double by stackedBarPaintable::height

  init {
    this.width = width
    this.height = height
  }

  override fun boundingBox(paintingContext: LayerPaintingContext): Rectangle = Rectangle(Coordinates.origin, Size(stackedBarPaintable.width, stackedBarPaintable.height))

  override fun paint(paintingContext: LayerPaintingContext, x: Double, y: Double) {
    val gc = paintingContext.gc

    gc.translate(x, y)

    gc.saved {
      stackedBarPaintable.paint(paintingContext, 0.0, 0.0)
    }

    gc.font(FontDescriptorFragment.L)
    gc.paintTextBox(line = configuration.name, anchorDirection = Direction.TopCenter, anchorGapHorizontal = 5.0, anchorGapVertical = 5.0, boxStyle = configuration.labelBoxStyle, textColor = configuration.labelColor)
  }

  @ConfigurationDsl
  inner class Configuration {
    /**
     * The label shown above the bar
     */
    var name: String = ""

    /**
     * Delegates to the inner stacked bar: the values shown in the bar.
     */
    var valuesProvider: DoublesProvider
      get() = stackedBarPaintable.configuration.valuesProvider
      set(value) {
        stackedBarPaintable.configuration.valuesProvider = value
      }

    /**
     * Delegates to the inner stacked bar: the value range of the bar.
     */
    var valueRange: LinearValueRange
      get() = stackedBarPaintable.configuration.valueRange
      set(value) {
        stackedBarPaintable.configuration.valueRange = value
      }

    /**
     * Delegates to the inner stacked bar: the colors of the bar segments.
     */
    var colorsProvider: MultiProvider<StackedBarPaintable.StackedBarValueIndex, Color>
      get() = stackedBarPaintable.configuration.colorsProvider
      set(value) {
        stackedBarPaintable.configuration.colorsProvider = value
      }

    /**
     * The color of the label text
     */
    var labelColor: Color = Color.web("#373e44")

    /**
     * The box style for the label
     */
    var labelBoxStyle: BoxStyle = BoxStyle(
      fill = Color.web("rgba(255, 255, 255, 0.55)").asProvider(),
      padding = Insets(3.0, 5.0, 3.0, 5.0)
    )
  }
}
