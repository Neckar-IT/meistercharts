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
import com.meistercharts.color.Color
import com.meistercharts.canvas.ChartSupport
import com.meistercharts.canvas.ConfigurationDsl
import com.meistercharts.canvas.fill
import com.meistercharts.font.FontDescriptorFragment
import com.meistercharts.canvas.text.TextLineCalculations
import com.meistercharts.canvas.paintTextBox
import com.meistercharts.canvas.paintable.AbstractPaintable
import com.meistercharts.canvas.paintable.PaintablePaintingVariables
import com.meistercharts.canvas.paintable.AbstractPaintablePaintingVariables
import com.meistercharts.color.ColorProvider
import it.neckar.geometry.Coordinates
import it.neckar.geometry.Direction
import it.neckar.geometry.Rectangle
import it.neckar.geometry.Size
import it.neckar.open.kotlin.lang.asProvider
import it.neckar.open.provider.SizedProvider1
import it.neckar.open.provider.asSizedProvider
import it.neckar.open.provider.fastForEachIndexed
import com.meistercharts.style.Palette
import it.neckar.open.unit.other.px

/**
 * A super simple paintable that paints a vertical legend - without a symbol
 */
class VerticallyStackedLabelsPaintable(
  /**
   * The labels that are shown on the right side of the legend.
   * The labels are shown in the provided order (from top to bottom)
   */
  labels: @LegendEntryIndex SizedProvider1<String, ChartSupport>,

  additionalConfiguration: Configuration.() -> Unit = {},
) : AbstractPaintable() {

  val configuration: Configuration = Configuration(labels).also(additionalConfiguration)

  override fun paintingVariables(): PaintablePaintingVariables {
    return paintingVariables
  }

  private val paintingVariables = object : AbstractPaintablePaintingVariables() {
    @px
    var rowHeightWithoutGap: Double = Double.NaN

    override fun calculate(paintingContext: LayerPaintingContext) {
      super.calculate(paintingContext)

      val chartSupport = paintingContext.chartSupport

      val gc = chartSupport.canvas.gc
      gc.font(configuration.textFont())
      val fontMetrics = gc.getFontMetrics()
      rowHeightWithoutGap = fontMetrics.totalHeight


      val labels = configuration.labels

      @px val textBlockHeight = TextLineCalculations.calculateTextBlockHeight(
        chartSupport.canvas.gc.getFontMetrics(),
        linesCount = labels.size(chartSupport),
        spaceBetweenLines = configuration.entriesGap,
      )

      @px val textBlockWidth = TextLineCalculations.calculateMultilineTextWidth(gc, labels.asSizedProvider(chartSupport), configuration.maxLabelWidth)

      val size = Size(
        width = textBlockWidth, height = textBlockHeight
      )

      boundingBox = Rectangle(Coordinates.origin, size)
    }
  }

  override fun paintAfterLayout(paintingContext: LayerPaintingContext, x: Double, y: Double) {
    val gc = paintingContext.gc
    gc.translate(x, y)

    gc.font(configuration.textFont())

    //Translate to the *center* of the first row
    gc.translate(0.0, paintingVariables.rowHeightWithoutGap / 2.0)

    configuration.labels.fastForEachIndexed(paintingContext.chartSupport) { index, label ->
      gc.fill(Color.black)
      gc.paintTextBox(
        line = label,
        anchorDirection = Direction.CenterLeft,
        anchorGapHorizontal = 0.0,
        anchorGapVertical = 0.0,
        textColor = configuration.labelColor(),
        maxStringWidth = configuration.maxLabelWidth
      )
      gc.translate(0.0, configuration.entriesGap + paintingVariables.rowHeightWithoutGap)
    }
  }

  @ConfigurationDsl
  class Configuration(
    /**
     * The labels that are shown on the right side of the legend.
     * The labels are shown in the provided order (from top to bottom)
     */
    val labels: @LegendEntryIndex SizedProvider1<String, ChartSupport>,
  ) {

    /**
     * The (optional) max length of the labels
     */
    var maxLabelWidth: @px Double = Double.MAX_VALUE

    /**
     * The spacing between two entries (vertical)
     */
    var entriesGap: @px Double = 5.0

    /**
     * The color the text is painted with
     */
    var labelColor: ColorProvider = Palette.defaultGray

    /**
     * The font the text is painted with
     */
    var textFont: () -> FontDescriptorFragment = FontDescriptorFragment.empty.asProvider()
  }
}
