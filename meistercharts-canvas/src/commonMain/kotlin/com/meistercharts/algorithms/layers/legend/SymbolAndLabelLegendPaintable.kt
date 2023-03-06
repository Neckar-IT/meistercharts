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
import com.meistercharts.algorithms.painter.Color
import com.meistercharts.canvas.ChartSupport
import com.meistercharts.canvas.DebugFeature
import com.meistercharts.canvas.FontDescriptorFragment
import com.meistercharts.canvas.TextLineCalculations
import com.meistercharts.canvas.layout.cache.BoundsLayoutCache
import com.meistercharts.canvas.paintMark
import com.meistercharts.canvas.paintTextBox
import com.meistercharts.canvas.paintable.AbstractPaintable
import com.meistercharts.canvas.paintable.Paintable
import com.meistercharts.canvas.paintable.PaintablePaintingVariables
import com.meistercharts.canvas.paintable.PaintablePaintingVariablesImpl
import com.meistercharts.canvas.paintable.RectanglePaintable
import com.meistercharts.canvas.saved
import com.meistercharts.model.Coordinates
import com.meistercharts.model.Direction
import com.meistercharts.model.Rectangle
import com.meistercharts.model.Size
import it.neckar.open.kotlin.lang.asProvider
import it.neckar.open.kotlin.lang.fastFor
import it.neckar.open.provider.MultiProvider
import it.neckar.open.provider.SizedProvider1
import it.neckar.open.provider.asSizedProvider
import it.neckar.open.provider.fastForEachIndexed
import com.meistercharts.style.Palette
import it.neckar.open.unit.other.px

/**
 * A simple paintable that paints a vertical legend:
 * Consists of:
 * * a symbol ([Paintable])
 * * a label ([String]).
 */
class SymbolAndLabelLegendPaintable(
  /**
   * The labels that are shown on the right side of the legend.
   * The labels are shown in the provided order (from top to bottom)
   */
  labels: @LegendEntryIndex SizedProvider1<String, ChartSupport>,

  /**
   * Provides the paintable that is used to paint the symbol
   */
  symbols: MultiProvider<LegendEntryIndex, Paintable>,

  additionalConfiguration: Configuration.() -> Unit = {},
) : AbstractPaintable() {

  val configuration: Configuration = Configuration(labels, symbols).also(additionalConfiguration)

  override fun paintingVariables(): PaintablePaintingVariables {
    return paintingVariables
  }

  private var paintingVariables = object : PaintablePaintingVariablesImpl() {
    var labelsCount: Int = 0

    /**
     * The max height over all symbols
     */
    var maxSymbolHeight: @px Double = Double.NaN

    /**
     * The max width over all symbols
     */
    var maxSymbolWidth: @px Double = Double.NaN

    /**
     * The height of one row - without gaps.
     * This height is calculated using the label height and symbol height
     */
    var rowHeightWithoutGap: @px Double = Double.NaN

    /**
     * Contains the symbol bounding boxes
     */
    val symbolBoundingBoxes = BoundsLayoutCache()

    /**
     * The height of the text block
     */
    var textBlockHeight: @px Double = Double.NaN

    /**
     * The width of the text block
     */
    var textBlockWidth: @px Double = Double.NaN

    override fun reset() {
      super.reset()
      labelsCount = 0
      maxSymbolHeight = Double.NaN
      maxSymbolWidth = Double.NaN
      textBlockHeight = Double.NaN
      textBlockWidth = Double.NaN
      boundingBox = Rectangle.zero
    }

    override fun calculate(paintingContext: LayerPaintingContext) {
      reset()
      super.calculate(paintingContext)

      val chartSupport = paintingContext.chartSupport

      val gc = chartSupport.canvas.gc
      gc.font(configuration.textFont())

      val labels = configuration.labels
      labelsCount = labels.size(chartSupport)
      symbolBoundingBoxes.ensureSize(labelsCount)

      if (labelsCount == 0) {
        //Nothing to paint, just exit
        return
      }

      maxSymbolHeight = 0.0
      maxSymbolWidth = 0.0
      labelsCount.fastFor { index ->
        val symbol = configuration.symbols.valueAt(index)

        val boundingBox = symbol.boundingBox(paintingContext)
        symbolBoundingBoxes[index] = boundingBox

        maxSymbolHeight = maxSymbolHeight.coerceAtLeast(
          boundingBox.getHeight()
        )
        maxSymbolWidth = maxSymbolWidth.coerceAtLeast(
          boundingBox.getWidth()
        )
      }

      rowHeightWithoutGap = gc.getFontMetrics().totalHeight.coerceAtLeast(maxSymbolHeight)


      textBlockHeight = TextLineCalculations.calculateTextBlockHeight(
        chartSupport.canvas.gc.getFontMetrics(),
        linesCount = labelsCount,
        spaceBetweenLines = configuration.entriesGap,
        minLineHeight = maxSymbolHeight
      )

      textBlockWidth = TextLineCalculations.calculateMultilineTextWidth(gc, labels.asSizedProvider(chartSupport), configuration.maxLabelWidth)


      //Calculate the bounding box
      val size = Size(
        width = maxSymbolWidth + configuration.symbolLabelGap + textBlockWidth, height = textBlockHeight
      )
      boundingBox = Rectangle(Coordinates(-maxSymbolWidth - configuration.symbolLabelGap / 2.0, 0.0), size)
    }
  }

  override fun paintAfterLayout(paintingContext: LayerPaintingContext, x: Double, y: Double) {
    if (paintingVariables.labelsCount == 0) {
      //Nothing to paint, just return
      return
    }

    val gc = paintingContext.gc
    gc.translate(x, y)

    //Apply the text font
    gc.font(configuration.textFont())

    val labels = configuration.labels

    //Translate to the *center* of the first row
    gc.translate(0.0, paintingVariables.rowHeightWithoutGap / 2.0)

    labels.fastForEachIndexed(paintingContext.chartSupport) { index, label ->
      gc.fill(Color.black)

      if (gc.debug[DebugFeature.ShowAnchors]) {
        gc.paintMark()
      }

      gc.saved {
        val symbol = configuration.symbols.valueAt(index)
        symbol.paintInBoundingBox(paintingContext, -configuration.symbolLabelGap / 2.0, 0.0, Direction.CenterRight)
      }
      gc.paintTextBox(
        line = label,
        anchorDirection = Direction.CenterLeft,
        anchorGapHorizontal = configuration.symbolLabelGap / 2.0,
        anchorGapVertical = 0.0,
        textColor = configuration.labelColors.valueAt(index),
        maxStringWidth = configuration.maxLabelWidth
      )
      gc.translate(0.0, configuration.entriesGap + paintingVariables.rowHeightWithoutGap)
    }
  }

  class Configuration(
    /**
     * The labels that are shown on the right side of the legend.
     * The labels are shown in the provided order (from top to bottom)
     */
    val labels: @LegendEntryIndex SizedProvider1<String, ChartSupport>,

    /**
     * Provides the paintable that is used to paint the symbol
     */
    var symbols: MultiProvider<LegendEntryIndex, Paintable>,
  ) {

    /**
     * The color the text is painted with
     */
    var labelColors: MultiProvider<LegendEntryIndex, Color> = MultiProvider.always(Palette.defaultGray)

    /**
     * The (optional) max length of the labels
     */
    var maxLabelWidth: @px Double = Double.MAX_VALUE

    /**
     * The gap between the symbol and the text
     */
    var symbolLabelGap: @px Double = 5.0

    /**
     * The spacing between two entries (vertical)
     */
    var entriesGap: @px Double = 5.0

    /**
     * The font the text is painted with
     */
    var textFont: () -> FontDescriptorFragment = FontDescriptorFragment.empty.asProvider()
  }

  companion object {
    /**
     * Creates a default symbols painter
     */
    fun defaultSymbols(
      symbolSize: @px Size,
      /**
       * Provides the colors for the legend entries
       */
      symbolColors: MultiProvider<LegendEntryIndex, Color>,
    ): MultiProvider<LegendEntryIndex, Paintable> {
      val rectanglePaintable = RectanglePaintable(symbolSize, Color.pink) //the color is applied before returning the paintable

      return MultiProvider { index ->
        rectanglePaintable.color = {
          //Apply the correct color
          symbolColors.valueAt(index)
        }
        rectanglePaintable
      }
    }

    /**
     * Creates a new simple legend paintable that uses rectangles of the given size
     */
    fun rectangles(
      /**
       * The labels that are shown on the right side of the legend.
       * The labels are shown in the provided order (from top to bottom)
       */
      labels: @LegendEntryIndex SizedProvider1<String, ChartSupport>,
      symbolColors: MultiProvider<LegendEntryIndex, Color>,
      symbolSize: @px Size = Size.PX_16,
      additionalConfiguration: Configuration.() -> Unit = {},
    ): SymbolAndLabelLegendPaintable {
      return SymbolAndLabelLegendPaintable(
        labels,
        defaultSymbols(symbolSize, symbolColors),
        additionalConfiguration,
      )
    }
  }
}
