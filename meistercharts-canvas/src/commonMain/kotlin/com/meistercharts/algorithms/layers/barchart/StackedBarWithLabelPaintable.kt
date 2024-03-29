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
package com.meistercharts.algorithms.layers.barchart

import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.canvas.ConfigurationDsl
import com.meistercharts.font.FontDescriptorFragment
import com.meistercharts.canvas.paintTextBox
import com.meistercharts.canvas.paintable.Paintable
import com.meistercharts.canvas.saved
import com.meistercharts.color.Color
import it.neckar.geometry.Coordinates
import it.neckar.geometry.Rectangle
import it.neckar.geometry.Direction
import com.meistercharts.model.Insets
import it.neckar.geometry.Size
import com.meistercharts.range.LinearValueRange
import com.meistercharts.range.ValueRange
import com.meistercharts.style.BoxStyle
import it.neckar.open.provider.DefaultDoublesProvider
import it.neckar.open.provider.DoublesProvider
import it.neckar.open.provider.MultiProvider
import it.neckar.open.unit.other.px

/**
 * Paints a single bar with a label - with a configurable total height
 */
class StackedBarWithLabelPaintable(
  val data: Data = Data(),
  width: @px Double = 15.0,
  height: @px Double = 200.0
) : Paintable {

  constructor(
    name: String = "",
    valuesProvider: DoublesProvider = DefaultDoublesProvider(listOf(5.0, 6.0, 7.0)),
    valueRange: LinearValueRange = ValueRange.default,
    colors: List<Color>,
    width: @px Double = 15.0,
    height: @px Double = 200.0
  ) : this(Data(name, valuesProvider, valueRange), width, height) {
    stackedBarPaintable.style.colorsProvider = MultiProvider.forListModulo(colors, Color.gray)
  }

  val style: Style = Style()

  val stackedBarPaintable: StackedBarPaintable = StackedBarPaintable(StackedBarPaintable.Data(data.valuesProvider, data.valueRange), width, height)

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
    gc.paintTextBox(line = data.name, anchorDirection = Direction.TopCenter, anchorGapHorizontal = 5.0, anchorGapVertical = 5.0, boxStyle = style.labelBoxStyle, textColor = style.labelColor)
  }

  class Data(
    var name: String = "",
    var valuesProvider: DoublesProvider = DefaultDoublesProvider(listOf(5.0, 6.0, 7.0)),
    var valueRange: LinearValueRange = ValueRange.default
  )

  @ConfigurationDsl
  class Style {
    /**
     * The color of the label text
     */
    var labelColor: Color = Color.web("#373e44")

    /**
     * The box style for the label
     */
    var labelBoxStyle: BoxStyle = BoxStyle(
      fill = Color("rgba(255, 255, 255, 0.55)"),
      padding = Insets(3.0, 5.0, 3.0, 5.0)
    )
  }
}
