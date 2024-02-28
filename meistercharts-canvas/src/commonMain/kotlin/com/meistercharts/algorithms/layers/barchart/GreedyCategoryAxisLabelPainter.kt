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

import it.neckar.geometry.Orientation
import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.algorithms.layout.EquisizedBoxLayout
import com.meistercharts.model.category.CategoryIndex
import com.meistercharts.model.category.valueAt
import com.meistercharts.color.Color
import com.meistercharts.annotations.Window
import com.meistercharts.annotations.Zoomed
import com.meistercharts.canvas.DebugFeature
import com.meistercharts.canvas.StrokeLocation
import com.meistercharts.canvas.ConfigurationDsl
import com.meistercharts.canvas.calculateOffsetXForGap
import com.meistercharts.canvas.calculateOffsetYForGap
import com.meistercharts.canvas.paintMark
import com.meistercharts.canvas.paintable.Paintable
import com.meistercharts.canvas.saved
import com.meistercharts.canvas.stroke
import com.meistercharts.canvas.strokeRect
import it.neckar.geometry.Direction
import it.neckar.geometry.Size
import com.meistercharts.provider.SizedLabelsProvider
import it.neckar.open.kotlin.lang.abs
import it.neckar.open.provider.MultiProvider
import it.neckar.open.unit.other.px
import kotlin.math.max
import kotlin.math.min

/**
 * A stateful [CategoryAxisLabelPainter] that greedily uses the available space to paint the labels.
 */
class GreedyCategoryAxisLabelPainter(styleConfiguration: Style.() -> Unit = {}) : CategoryAxisLabelPainter {

  val style: Style = Style().also(styleConfiguration)

  //for horizontal axes (usually painted from left to right)
  private var lastRight: @Window Double = 0.0

  //for vertical axes (usually painted from top to bottom)
  private var lastBottom: @Window Double = 0.0

  override fun layout(categoryLayout: EquisizedBoxLayout, labelsProvider: SizedLabelsProvider, labelVisibleCondition: LabelVisibleCondition) {
    //Reset all values
    lastRight = 0.0
    lastBottom = 0.0
  }

  override fun paint(
    paintingContext: LayerPaintingContext,
    x: @Window Double,
    y: @Window Double,
    width: @Zoomed Double,
    height: @Zoomed Double,
    tickDirection: Direction,
    label: String?,
    categoryIndex: CategoryIndex,
    categoryAxisOrientation: Orientation,
  ) {
    when (categoryAxisOrientation) {
      Orientation.Vertical -> paintVertical(paintingContext, x, y, width, tickDirection, label, categoryIndex)
      Orientation.Horizontal -> paintHorizontal(paintingContext, x, y, height, tickDirection, label, categoryIndex)
    }
  }

  /**
   * Paints labels of a horizontal category axis
   */
  private fun paintHorizontal(
    paintingContext: LayerPaintingContext,
    x: @Window Double,
    y: @Window Double,
    height: @Zoomed Double,
    direction: Direction,
    label: String?,
    categoryIndex: CategoryIndex,
  ) {
    val gc = paintingContext.gc

    val availableWidth: @Zoomed Double = min(x - lastRight - style.categoryLabelGap / 2.0, gc.width - x) * 2.0
    val availableHeight: @Zoomed Double = height

    if (availableWidth <= 0 || availableHeight <= 0) {
      return
    }

    if (DebugFeature.ShowBounds.enabled(paintingContext)) {
      gc.stroke(debugColors.valueAt(categoryIndex))
      gc.lineWidth = 1.0
      gc.strokeRect(x, y, availableWidth, availableHeight, direction, strokeLocation = StrokeLocation.Inside)
      gc.paintMark(x, y)
    }

    val categoryPaintable = style.imagesProvider.valueAt(categoryIndex)

    @px var imageLabelGapX = 0.0
    @px var imageLabelGapY = 0.0
    @px var neededWidth = 0.0
    if (categoryPaintable != null) {
      imageLabelGapX = direction.calculateOffsetXForGap(style.imageSize.width + style.imageLabelGap)
      imageLabelGapY = direction.calculateOffsetYForGap(style.imageSize.height + style.imageLabelGap)
      neededWidth = style.imageSize.width
      if (neededWidth > availableWidth) {
        //not enough space to display the symbol -> bail out
        return
      }
      gc.saved {
        categoryPaintable.paintInBoundingBox(paintingContext, x, y, direction, 0.0, 0.0, style.imageSize.width, style.imageSize.height)
      }
      lastRight = x + style.imageSize.width * 0.5
    }

    if (!label.isNullOrBlank()) {
      @px val textSize = gc.calculateTextSize(label)
      neededWidth = max(neededWidth, textSize.width)
      if (neededWidth > availableWidth) {
        //not enough space to display the text -> bail out
        return
      }
      gc.fillText(label, x + imageLabelGapX, y + imageLabelGapY, direction, 0.0, 0.0)
      lastRight = max(lastRight, x + imageLabelGapX + textSize.width * 0.5)
    }
  }

  /**
   * Paints labels of a vertical category axis
   */
  private fun paintVertical(
    paintingContext: LayerPaintingContext,
    x: @Window Double,
    y: @Window Double,
    width: @Zoomed Double,
    direction: Direction,
    label: String?,
    categoryIndex: CategoryIndex,
  ) {
    val gc = paintingContext.gc

    val availableWidth: @Zoomed Double = width
    val availableHeight: @Zoomed Double = min(y - lastBottom - style.categoryLabelGap / 2.0, gc.height - y) * 2.0

    if (availableWidth <= 0 || availableHeight <= 0) {
      return
    }

    if (DebugFeature.ShowBounds.enabled(paintingContext)) {
      gc.stroke(debugColors.valueAt(categoryIndex))
      gc.lineWidth = 1.0
      gc.strokeRect(x, y, availableWidth, availableHeight, direction, strokeLocation = StrokeLocation.Inside)
      gc.paintMark(x, y)
    }

    val categoryPaintable = style.imagesProvider.valueAt(categoryIndex)

    @px var imageLabelGapX = 0.0
    @px var imageLabelGapY = 0.0
    @px var neededHeight = 0.0
    if (categoryPaintable != null) {
      imageLabelGapX = direction.calculateOffsetXForGap(style.imageSize.width + style.imageLabelGap)
      imageLabelGapY = direction.calculateOffsetYForGap(style.imageSize.height + style.imageLabelGap)
      neededHeight = style.imageSize.height
      if (neededHeight > availableHeight) {
        //not enough space to display the symbol -> bail out
        return
      }
      gc.saved {
        categoryPaintable.paintInBoundingBox(paintingContext, x, y, direction, 0.0, 0.0, style.imageSize.width, style.imageSize.height)
      }
      lastBottom = y + style.imageSize.height * 0.5
    }

    if (!label.isNullOrBlank()) {
      @px val textHeight = gc.getFontMetrics().totalHeight
      neededHeight = max(neededHeight, textHeight)
      if (neededHeight > availableHeight) {
        //not enough space to display the symbol -> bail out
        return
      }
      //enable the current string-shortener to do its job by passing the maximum width
      gc.fillText(label, x + imageLabelGapX, y + imageLabelGapY, direction, 0.0, 0.0, availableWidth - imageLabelGapX.abs())
      lastBottom = max(lastBottom, y + imageLabelGapY + textHeight * 0.5)
    }
  }

  @ConfigurationDsl
  open class Style {
    /**
     * Provides the images for the axis
     */
    var imagesProvider: MultiProvider<CategoryIndex, Paintable?> = MultiProvider.alwaysNull()

    /**
     * The gap between the label of the category and a potential image provided by [imagesProvider]
     */
    var imageLabelGap: @px Double = 5.0

    /**
     * The size of the image provided by [imagesProvider]
     */
    var imageSize: @px Size = Size.PX_30

    /**
     * The minimal space that needs to be empty between two consecutive category labels or icons.
     */
    var categoryLabelGap: @px Double = 5.0
  }

  companion object {
    private val debugColors = MultiProvider.forListModulo<CategoryIndex, Color>(
      listOf(
        Color.web("#E3403F"),
        Color.web("#4887E7"),
        Color.web("#5CC761"),
        Color.web("#F7B433"),
      )
    )
  }
}
