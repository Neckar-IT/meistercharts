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
package com.meistercharts.algorithms.painter

import com.meistercharts.algorithms.layout.LabelLayouter
import com.meistercharts.annotations.Window
import com.meistercharts.annotations.Zoomed
import com.meistercharts.canvas.CanvasRenderingContext
import com.meistercharts.canvas.FontDescriptorFragment
import com.meistercharts.canvas.StyleDsl
import com.meistercharts.canvas.paintTextBox
import com.meistercharts.canvas.saved
import com.meistercharts.label.LayoutedLabel
import com.meistercharts.model.Direction
import com.meistercharts.model.Insets
import it.neckar.open.collections.fastForEach
import com.meistercharts.style.BoxStyle
import it.neckar.open.unit.other.px

/**
 * Paints the labels on the  y axis
 */
@Deprecated("do no use anymore")
class LabelPainter(
  @px @Window val startY: Double,
  @px @Window val endY: Double,
  val snapXValues: Boolean,
  val snapYValues: Boolean,
  styleConfiguration: Style.() -> Unit = {}
) : AbstractPainter(snapXValues, snapYValues) {

  val style: Style = Style().also(styleConfiguration)

  /**
   * Paints the labels
   */
  fun paintLabels(gc: CanvasRenderingContext, labels: List<@Window LayoutedLabel>, placement: LabelPlacement) {
    gc.font(style.font)
    @px @Zoomed val labelHeight = calculateForLabelHeight(gc)

    //Find the visible labels
    val visibleLabels = labels.asSequence()
      .filter {
        it.locationInWindow.isVisible()
      }.onEach {
        //Apply the label height for each visible label
        it.height = labelHeight
      }.toList()

    //Create label objects to calculate the optimal layout

    calculateLayoutY(visibleLabels)
    paintFilteredLabels(gc, visibleLabels, placement)
  }

  /**
   * Returns true if the given y value is visible within the window
   */
  private fun Double.isVisible(): Boolean {
    return this in startY..endY
  }

  /**
   * Calculate the layout for the given labels. Writes the new values into [LayoutedLabel]
   */
  private fun calculateLayoutY(
    /**
     * The labels that are layouted
     */
    labels: List<LayoutedLabel>
  ) {

    //Collect all labels that are visible
    labels
      .fastForEach {
        //
        it.preferredCenterY = snapYPosition(it.locationInWindow)
      }

    //Now calculate the layout on the y axis
    val labelLayouter = LabelLayouter(style.labelSpacing, startY, endY)
    labelLayouter.calculateOptimalPositions(labels)
  }

  /**
   * Paints the labels
   */
  private fun paintFilteredLabels(
    gc: CanvasRenderingContext,
    /**
     * The labels that have updated layout information
     */
    labels: List<LayoutedLabel>,

    /**
     * The alignment
     */
    placement: LabelPlacement
  ) {
    labels.fastForEach { label ->
      @Window @px val preferredCenterY = snapYPosition(label.preferredCenterY)
      @Window @px val actualCenterY = snapYPosition(label.actualCenterY)

      /**
       * All width / x values should be multiplied with this value to respect the alignment
       */
      val alignmentFactor = when (placement) {
        LabelPlacement.OnRightSide -> 1.0
        LabelPlacement.OnLeftSide -> -1.0
      }

      //The inner side of the x, where the line connects with the label
      @px val innerX = style.gap * alignmentFactor

      gc.beginPath()
      gc.moveTo(0.0, preferredCenterY)
      gc.lineTo(innerX, actualCenterY)

      //Stroke the line to the box
      gc.stroke(style.lineColor)
      gc.lineWidth = style.lineWidth
      gc.stroke()

      gc.saved {
        gc.translate(innerX, actualCenterY)

        val boxAnchor = when (placement) {
          LabelPlacement.OnRightSide -> Direction.CenterLeft
          LabelPlacement.OnLeftSide  -> Direction.CenterRight
        }

        val textToDraw = label.labelData.text

        val boxStyle = style.boxStyle.copy(fill = label.labelData.color)
        val bounds = gc.paintTextBox(line = textToDraw, anchorDirection = boxAnchor, anchorGapHorizontal = 0.0, anchorGapVertical = 0.0, boxStyle = boxStyle, textColor = style.textColor)

        //Save the label bounds (for mouse over)
        label.bounds = bounds.move(innerX, actualCenterY)
      }
    }
  }

  /**
   * Calculates the height for one label
   */
  private fun calculateForLabelHeight(gc: CanvasRenderingContext): @Zoomed Double {
    return gc.getFontMetrics().totalHeight + style.boxStyle.padding.offsetHeight
  }

  @StyleDsl
  open class Style {

    /**
     * The line color
     */
    var lineColor: Color = Color.gray

    /**
     * The width of the line
     */
    var lineWidth: Double = 1.0

    /**
     * The gap between the label and the center line
     * (direction depends on the alignment)
     */
    @px
    var gap: Double = 20.0

    /**
     * The space between the labels
     */
    @px
    var labelSpacing: Double = 3.0

    /**
     * The style of the box around the label.
     * The fill will be set from the [com.meistercharts.label.LabelData.color]
     */
    var boxStyle: BoxStyle = BoxStyle(
      padding = Insets.of(4.0)
    )

    /**
     * Font for labels
     */
    var font: FontDescriptorFragment = FontDescriptorFragment.empty

    /**
     * The color for the label text
     */
    var textColor: Color = Color.white
  }
}

/**
 * The alignment of the labels
 */
enum class LabelPlacement {
  /**
   * Aligned on the left side
   */
  OnRightSide,

  /**
   * Aligned on the right side
   */
  OnLeftSide
}
