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

import com.meistercharts.algorithms.layers.AbstractLayer
import com.meistercharts.algorithms.layers.AxisPaintingVariables
import com.meistercharts.algorithms.layers.AxisStyle
import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.algorithms.layers.resolveTitle
import com.meistercharts.algorithms.painter.Color
import com.meistercharts.annotations.Window
import com.meistercharts.annotations.Zoomed
import com.meistercharts.canvas.CanvasRenderingContext
import com.meistercharts.canvas.DebugFeature
import com.meistercharts.canvas.paintLocation
import com.meistercharts.canvas.saved
import com.meistercharts.model.Direction
import com.meistercharts.model.Orientation
import com.meistercharts.model.Side
import com.meistercharts.model.Vicinity
import it.neckar.open.unit.other.px

/**
 * Base class for "normal" axis layers that contain
 * a title and an axis line.
 */
abstract class AbstractAxisLayer : AbstractLayer() {

  abstract override fun paintingVariables(): AxisPaintingVariables

  abstract val style: AxisStyle

  /**
   * Translates the graphics context to the axis origin - including the title (using the painting properties)
   */
  fun CanvasRenderingContext.translateToAxisTitleOrigin() {
    val gc = this.canvas.gc
    when (style.side) {
      Side.Right, Side.Left -> gc.translate(paintingVariables().axisTitleLocation, 0.0)
      Side.Top, Side.Bottom -> gc.translate(0.0, paintingVariables().axisTitleLocation)
    }
  }

  /**
   * Translates to the start at the *center* of the axis line.
   */
  fun CanvasRenderingContext.translateToAxisLineOrigin() {
    val gc = this.canvas.gc
    when (style.side) {
      Side.Right, Side.Left -> gc.translate(paintingVariables().axisLineLocation, 0.0)
      Side.Top, Side.Bottom -> gc.translate(0.0, paintingVariables().axisLineLocation)
    }
  }

  /**
   * Translates to the "inner" origin of the axis - without title
   */
  fun CanvasRenderingContext.translateToAxisInnerOrigin() {
    val gc = this.canvas.gc
    when (style.side) {
      Side.Right, Side.Left -> gc.translate(paintingVariables().axisContentLocation, 0.0)
      Side.Top, Side.Bottom -> gc.translate(0.0, paintingVariables().axisContentLocation)
    }
  }


  /**
   * Paints the title
   */
  fun paintTitle(paintingContext: LayerPaintingContext) {
    val titleText = style.resolveTitle(paintingContext) ?: return
    paintTitleAtCenter(titleText, paintingContext)
  }

  /**
   * Paints the title at the center of the axis
   */
  fun paintTitleAtCenter(titleText: String, paintingContext: LayerPaintingContext) {
    val gc = paintingContext.gc

    gc.translateToAxisTitleOrigin()
    paintingContext.ifDebug(DebugFeature.ShowAnchors) {
      gc.paintLocation(label = "axisTitleOrigin", color = Color.brown)
    }

    gc.font(style.titleFont)
    gc.fillStyle(style.titleColor())

    @Window val axisCenter = paintingVariables().axisCenter
    @Zoomed val maxTextWidth = paintingVariables().axisLength

    when (style.side) {
      Side.Left -> {
        gc.translate(0.0, axisCenter)
        debugAxisCenterLocation(paintingContext, gc)
        //to the center height of the canvas
        gc.rotateDegrees(-90.0)
        gc.fillText(titleText, 0.0, 0.0, Direction.TopCenter, maxWidth = maxTextWidth) //TopCenter because we rotated the view!
      }

      Side.Right -> {
        gc.translate(0.0, axisCenter)
        debugAxisCenterLocation(paintingContext, gc)
        //to the center height of the canvas
        gc.rotateDegrees(-90.0)
        gc.fillText(titleText, 0.0, 0.0, Direction.BottomCenter, maxWidth = maxTextWidth)
      }

      Side.Top -> {
        gc.translate(axisCenter, 0.0)
        debugAxisCenterLocation(paintingContext, gc)
        //To the center width of the canvas
        gc.fillText(titleText, 0.0, 0.0, Direction.TopCenter, maxWidth = maxTextWidth)
      }

      Side.Bottom -> {
        gc.translate(axisCenter, 0.0)
        debugAxisCenterLocation(paintingContext, gc)
        //To the center width of the canvas
        gc.fillText(titleText, 0.0, 0.0, Direction.BottomCenter, maxWidth = maxTextWidth)
      }
    }
  }

  private fun debugAxisCenterLocation(paintingContext: LayerPaintingContext, gc: CanvasRenderingContext) {
    paintingContext.ifDebug(DebugFeature.ShowAnchors) {
      gc.paintLocation(label = "axisCenter", color = Color.green)
    }
  }

  /**
   * Paints the axis - respects the paint range from the style
   */
  fun paintAxisLine(paintingContext: LayerPaintingContext) {
    if (style.axisLineWidth == 0.0) {
      return
    }

    val gc = paintingContext.gc

    gc.translateToAxisLineOrigin()
    paintingContext.ifDebug(DebugFeature.ShowAnchors) {
      gc.paintLocation(label = "axisLineOrigin", color = Color.cadetblue)
    }

    gc.strokeStyle(style.lineColor())
    gc.lineWidth = style.axisLineWidth

    when (style.orientation) {
      Orientation.Vertical ->
        gc.strokeLine(0.0, paintingVariables().axisStart, 0.0, paintingVariables().axisEnd)

      Orientation.Horizontal ->
        gc.strokeLine(paintingVariables().axisStart, 0.0, paintingVariables().axisEnd, 0.0)
    }
  }


  /**
   * Painting strategy for the layer:
   *
   * Paint from left to right. Always translate the gc after each segment
   */
  override fun paint(paintingContext: LayerPaintingContext) {
    when (style.side) {
      Side.Left -> paintLeft(paintingContext)
      Side.Right -> paintRight(paintingContext)
      Side.Top -> paintTop(paintingContext)
      Side.Bottom -> paintBottom(paintingContext)
    }
  }

  /**
   * Paints horizontally (at the bottom).
   */
  fun paintBottom(paintingContext: LayerPaintingContext) {
    val gc = paintingContext.gc

    fillBackground(paintingContext, Side.Bottom)

    //Paint the title (if there is one)
    gc.saved {
      paintTitle(paintingContext)
    }

    gc.saved {
      paintAxisLine(paintingContext)
    }

    //To the top of the title
    gc.translateToAxisInnerOrigin()
    paintingContext.ifDebug(DebugFeature.ShowAnchors) {
      gc.paintLocation(label = "axisInner", color = Color.fuchsia)
    }

    //Paint depending on the tick orientation
    when (style.tickOrientation) {
      Vicinity.Outside -> {
        gc.translate(0.0, -style.size + paintingVariables().spaceForTitleIncludingGap + style.axisLineWidth / 2.0)
        //to the center of the axis
        gc.translate(0.0, style.axisLineWidth / 2.0 + style.tickLength + style.tickLabelGap)
        //to text top
        paintTicksWithLabelsHorizontally(paintingContext, Direction.TopCenter)
      }

      Vicinity.Inside -> {
        gc.translate(0.0, -style.axisLineWidth / 2.0)
        //to the *center* of the axis
        gc.translate(0.0, -style.axisLineWidth / 2.0)
        //to the top side of the axis
        gc.translate(0.0, -style.tickLabelGap - style.tickLength)
        //to text bottom
        paintTicksWithLabelsHorizontally(paintingContext, Direction.BottomCenter)
      }
    }
  }


  fun paintTop(paintingContext: LayerPaintingContext) {
    val gc = paintingContext.gc

    fillBackground(paintingContext, Side.Top)

    gc.saved {
      paintTitle(paintingContext)
    }
    gc.saved {
      paintAxisLine(paintingContext)
    }
    gc.translateToAxisInnerOrigin()
    paintingContext.ifDebug(DebugFeature.ShowAnchors) {
      gc.paintLocation(label = "axisInner", color = Color.fuchsia)
    }

    //Paint depending on the tick orientation
    when (style.tickOrientation) {
      Vicinity.Outside -> {
        gc.translate(0.0, style.size - paintingVariables().spaceForTitleIncludingGap - style.axisLineWidth / 2.0)
        //to the center of the axis
        gc.translate(0.0, -style.axisLineWidth / 2.0 - style.tickLength - style.tickLabelGap)
        //to text bottom
        paintTicksWithLabelsHorizontally(paintingContext, Direction.BottomCenter)
      }

      Vicinity.Inside -> {
        gc.translate(0.0, style.axisLineWidth / 2.0)
        //to the *center* of the axis
        gc.translate(0.0, style.axisLineWidth / 2.0)
        //to the bottom side of the axis
        gc.translate(0.0, style.tickLabelGap + style.tickLength)
        //to text top
        paintTicksWithLabelsHorizontally(paintingContext, Direction.TopCenter)
      }
    }
  }

  /**
   * Paints vertically (at the left)
   */
  fun paintLeft(paintingContext: LayerPaintingContext) {
    val gc = paintingContext.gc

    fillBackground(paintingContext, Side.Left)

    gc.saved {
      paintTitle(paintingContext)
    }
    gc.saved {
      paintAxisLine(paintingContext)
    }
    gc.translateToAxisInnerOrigin()
    paintingContext.ifDebug(DebugFeature.ShowAnchors) {
      gc.paintLocation(label = "axisInner", color = Color.fuchsia)
    }

    //Paint depending on the tick orientation
    when (style.tickOrientation) {
      Vicinity.Outside -> {
        gc.translate(paintingVariables().tickValueLabelMaxWidth, 0.0)
        //to the right side of the tick value labels
        paintTicksWithLabelsVertically(paintingContext, Direction.CenterRight)
      }

      Vicinity.Inside -> {
        gc.translate(style.axisLineWidth + style.tickLabelGap + style.tickLength, 0.0)
        paintTicksWithLabelsVertically(paintingContext, Direction.CenterLeft)
      }
    }
  }

  /**
   * Paints vertically (at the right)
   */
  fun paintRight(paintingContext: LayerPaintingContext) {
    val gc = paintingContext.gc

    fillBackground(paintingContext, Side.Right)

    gc.saved {
      paintTitle(paintingContext)
    }
    gc.saved {
      paintAxisLine(paintingContext)
    }
    gc.translateToAxisInnerOrigin()
    paintingContext.ifDebug(DebugFeature.ShowAnchors) {
      gc.paintLocation(label = "axisInner", color = Color.fuchsia)
    }


    //Paint depending on the tick orientation
    when (style.tickOrientation) {
      Vicinity.Outside -> {
        @px val maxTickValueWidth = paintingVariables().tickValueLabelMaxWidth
        gc.translate(-maxTickValueWidth, 0.0)
        //to the left side of the tick value labels
        paintTicksWithLabelsVertically(paintingContext, Direction.CenterLeft)
        gc.translate(-style.tickLabelGap - style.tickLength - style.axisLineWidth / 2.0, 0.0)
      }

      Vicinity.Inside -> {
        gc.translate(-style.axisLineWidth / 2.0, 0.0)
        //to the *center* of the axis
        gc.translate(-style.axisLineWidth / 2.0, 0.0)
        //to the left side of the axis
        gc.translate(-style.tickLabelGap - style.tickLength, 0.0)
        //to the right side of the label
        paintTicksWithLabelsVertically(paintingContext, Direction.CenterRight)
      }
    }
  }

  private fun fillBackground(paintingContext: LayerPaintingContext, side: Side) {
    style.background.invoke()?.let { background ->
      val gc = paintingContext.gc

      gc.fill(background)
      val paintingProperties = paintingVariables()

      when (side) {
        Side.Left -> gc.fillRect(paintingProperties.axisTitleLocation, paintingProperties.axisStart, style.size, paintingProperties.axisLength)
        Side.Right -> gc.fillRect(paintingProperties.axisTitleLocation, paintingProperties.axisStart, -style.size, paintingProperties.axisLength)
        Side.Top -> gc.fillRect(paintingProperties.axisStart, paintingProperties.axisTitleLocation, paintingProperties.axisLength, style.size)
        Side.Bottom -> gc.fillRect(paintingProperties.axisStart, paintingProperties.axisTitleLocation, paintingProperties.axisLength, -style.size)
      }
    }
  }

  /**
   * Paint (only) the ticks and labels - not the axis!
   * Must be called with the graphics context translated to the *edge* of the tick value labels
   */
  abstract fun paintTicksWithLabelsVertically(paintingContext: LayerPaintingContext, direction: Direction)

  /**
   * Paint (only) the ticks and labels - not the axis!
   * Must be called with the graphics context translated to the *edge* of the tick value labels
   */
  abstract fun paintTicksWithLabelsHorizontally(paintingContext: LayerPaintingContext, direction: Direction)
}
