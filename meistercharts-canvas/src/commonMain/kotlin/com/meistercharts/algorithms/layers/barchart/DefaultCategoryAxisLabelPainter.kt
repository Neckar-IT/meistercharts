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
import com.meistercharts.algorithms.layout.EquisizedBoxLayout
import com.meistercharts.model.category.CategoryIndex
import com.meistercharts.model.category.valueAt
import com.meistercharts.color.Color
import com.meistercharts.annotations.Window
import com.meistercharts.annotations.Zoomed
import com.meistercharts.canvas.CanvasRenderingContext
import com.meistercharts.canvas.DebugFeature
import com.meistercharts.canvas.ConfigurationDsl
import com.meistercharts.canvas.calculateOffsetXForGap
import com.meistercharts.canvas.calculateOffsetYForGap
import com.meistercharts.canvas.paintMark
import com.meistercharts.canvas.paintable.Paintable
import com.meistercharts.canvas.saved
import com.meistercharts.canvas.stroke
import com.meistercharts.canvas.strokeRect
import it.neckar.geometry.Direction
import it.neckar.geometry.Orientation
import it.neckar.geometry.Size
import com.meistercharts.provider.SizedLabelsProvider
import it.neckar.open.kotlin.lang.abs
import it.neckar.open.kotlin.lang.centerIndexOf
import it.neckar.open.kotlin.lang.substr
import it.neckar.open.provider.MultiProvider
import it.neckar.open.unit.other.px

/**
 * A default implementation of the [CategoryAxisLabelPainter]
 */
class DefaultCategoryAxisLabelPainter(styleConfiguration: Style.() -> Unit = {}) : CategoryAxisLabelPainter {

  val style: Style = Style().also(styleConfiguration)

  override fun layout(categoryLayout: EquisizedBoxLayout, labelsProvider: SizedLabelsProvider, labelVisibleCondition: LabelVisibleCondition) {
    //nothing to layout
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
    val gc = paintingContext.gc
    val categoryPaintable = style.imagesProvider.valueAt(categoryIndex)

    if (DebugFeature.ShowBounds.enabled(paintingContext)) {
      gc.apply {
        stroke(Color.lightblue)
        lineWidth = 1.0
        strokeRect(x, y, width, height, tickDirection)
        paintMark(x, y, color = Color.orange())
      }
    }

    @px var gapX = 0.0
    @px var gapY = 0.0

    if (categoryPaintable != null) {
      gc.saved {
        categoryPaintable.paintInBoundingBox(paintingContext, x, y, tickDirection, 0.0, 0.0, style.imageSize.width, style.imageSize.height)
      }
      gapX += tickDirection.calculateOffsetXForGap(style.imageSize.width + style.imageLabelGap)
      gapY += tickDirection.calculateOffsetYForGap(style.imageSize.height + style.imageLabelGap)
    }

    if (label.isNullOrBlank()) return


    //The max width of the label
    var maxWidth: @px Double? = null
    var maxHeight: @px Double? = null

    if (style.labelWithinCategory) {
      when (categoryAxisOrientation) {
        Orientation.Vertical -> {
          maxHeight = height
          maxWidth = width - gapX.abs()
        }

        Orientation.Horizontal -> {
          maxWidth = width - gapX.abs() - style.labelWithinCategoryGap
          if (maxWidth < 4.0) {
            //if there are less than 4 pixels available we skip the expansive fillText with maxWidth altogether
            return
          }
        }
      }
    }

    if (DebugFeature.ShowBounds.enabled(paintingContext)) {
      gc.paintMark(x + gapX, y + gapY, color = Color.orangered())
    }

    when (style.wrapMode) {
      LabelWrapMode.NoWrap -> {
        paintSingleLineLabel(gc, label, x, gapX, y, gapY, tickDirection, maxWidth, maxHeight)
      }

      LabelWrapMode.IfNecessary -> {
        if (maxWidth == null) {
          //no max width found
          paintSingleLineLabel(gc, label, x, gapX, y, gapY, tickDirection, maxWidth, maxHeight)
          return
        }

        @px val textWidth = gc.calculateTextWidth(label)
        if (textWidth <= maxWidth) {
          //enough space for the label in one line
          paintSingleLineLabel(gc, label, x, gapX, y, gapY, tickDirection, maxWidth, maxHeight)
          return
        }

        //We do not have enough space, try to split it
        val spaceIndex = label.centerIndexOf(' ')
        if (spaceIndex > 0) {
          val firstLine = label.substring(0, spaceIndex)
          val lastLine = label.substr(spaceIndex + 1)

          val y1 = y + gapY

          val offset = gc.getFontMetrics().totalHeight / 2.0 + style.twoLinesGap / 2.0

          gc.fillText(firstLine, x + gapX, y1 - offset, tickDirection, maxWidth = maxWidth)
          paintSingleLineLabel(gc, lastLine, x, gapX, y1, offset, tickDirection, maxWidth, maxHeight)
        } else {
          //No space found - paint a single line
          paintSingleLineLabel(gc, label, x, gapX, y, gapY, tickDirection, maxWidth, maxHeight)
        }
      }
    }
  }

  private fun paintSingleLineLabel(gc: CanvasRenderingContext, label: String, x: Double, gapX: Double, y: Double, gapY: Double, direction: Direction, maxWidth: Double?, maxHeight: Double?) {
    gc.fillText(label, x + gapX, y + gapY, direction, maxWidth = maxWidth, maxHeight = maxHeight)
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
     * If set to true, the label is:
     * * horizontal axis: shortened to fit into the category
     * * vertical axis: not shown if the height is too large
     */
    var labelWithinCategory: @px Boolean = true

    /**
     * The gap that is applied if [labelWithinCategory] is set to true.
     * Half of the gap will be applied on each side.
     *
     * The labels of two categories have exactly that gap between them.
     */
    var labelWithinCategoryGap: @px Double = 0.0

    /**
     * The split lines mode
     */
    var wrapMode: LabelWrapMode = LabelWrapMode.NoWrap

    /**
     * The gap between two lines - if two lines are painted
     */
    var twoLinesGap: @px Double = 0.0
  }
}

