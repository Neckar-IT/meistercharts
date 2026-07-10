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
package com.meistercharts.algorithms.painter

import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.algorithms.layers.crosswire.CrossWireLayer.LabelIndex
import com.meistercharts.annotations.Window
import com.meistercharts.annotations.Zoomed
import com.meistercharts.canvas.CanvasRenderingContext
import com.meistercharts.canvas.StyleDsl
import com.meistercharts.canvas.DebugFeature
import com.meistercharts.canvas.i18nConfiguration
import com.meistercharts.canvas.layout.buffer.LayoutVariable
import com.meistercharts.canvas.layout.buffer.LayoutVariablesObjectBuffer
import com.meistercharts.canvas.layout.buffer.StringMultiBuffer.Companion.Uninitialized
import com.meistercharts.canvas.paintTextBox
import com.meistercharts.canvas.saved
import com.meistercharts.canvas.stroke
import com.meistercharts.canvas.textService
import com.meistercharts.color.Color
import com.meistercharts.color.ColorProvider
import com.meistercharts.color.get
import com.meistercharts.font.FontDescriptorFragment
import com.meistercharts.provider.LabelsProvider
import com.meistercharts.style.BoxStyle
import it.neckar.geometry.Direction
import it.neckar.open.kotlin.lang.asProvider
import it.neckar.open.provider.DoublesProvider1
import it.neckar.open.provider.MultiProvider
import it.neckar.open.provider.fastForEachIndexed
import it.neckar.open.unit.number.MayBeNaN
import it.neckar.open.unit.other.px

/**
 * Paints labels on the Y axis (above each other).
 *
 * This painter does *not* and will never support painting labels on the x-axis.
 * Layouting labels along the X axis requires a completely different approach and therefore an independent implementation.
 */
class LabelPainter2(
  val snapXValues: Boolean,
  val snapYValues: Boolean,
  styleConfiguration: Style.() -> Unit = {},
) : AbstractPainter(snapXValues, snapYValues) {

  val style: Style = Style().also(styleConfiguration)

  fun paintingVariables(): LabelPainterPaintingVariables {
    return paintingVariables
  }

  /**
   * The labels that have been laid out by [layout] (sorted by preferred center y).
   * Internal access for tests.
   */
  internal val layoutedLabels: List<LayoutedLabel2>
    get() = paintingVariables.layoutedLabelsBuffer.values

  private val paintingVariables = object : LabelPainterPaintingVariables {
    /**
     * Contains the layouted labels
     */
    val layoutedLabelsBuffer: LayoutVariablesObjectBuffer<LayoutedLabel2> = LayoutVariablesObjectBuffer { LayoutedLabel2() }

    override fun isNotEmpty(): Boolean {
      return layoutedLabelsBuffer.isEmpty().not()
    }

    fun update(
      /**
       * Provides the label locations
       */
      labelLocations: @MayBeNaN @Window DoublesProvider1<LayerPaintingContext>,
      /**
       * The box styles
       */
      labelBoxStyles: MultiProvider<LabelIndex, BoxStyle>,
      /**
       * Provides the formatted value for the label
       */
      labelTexts: LabelsProvider<LabelIndex>,
      /**
       * The min coordinates for the labels. (top of the label)
       */
      min: @Window Double = -Double.MAX_VALUE,

      /**
       * The max coordinates the labels. (bottom of the label)
       */
      max: @Window Double = Double.MAX_VALUE,
      paintingContext: LayerPaintingContext,
    ) {
      val gc: CanvasRenderingContext = paintingContext.gc
      val textService = paintingContext.chartSupport.textService
      val i18nConfiguration = paintingContext.chartSupport.i18nConfiguration

      val preferredLabelsCount = labelLocations.size(paintingContext)

      //Ensure the cache is prepared - for the current size
      gc.font(style.font())
      @px val lineHeight = gc.getFontMetrics().totalHeight

      //The size will be later reduced - if not all labels are visible
      layoutedLabelsBuffer.prepare(preferredLabelsCount)

      //Fill the cache with the values - these are then used to calculate the layout
      labelLocations.fastForEachIndexed(paintingContext) { labelIndex: @LabelIndex Int, labelLocation: @MayBeNaN @Window Double ->
        layoutedLabelsBuffer.values[labelIndex].let { layoutedLabel ->
          layoutedLabel.index = labelIndex

          //Check if there is a valid label location - else skip
          if (labelLocation.isFinite().not()) {
            layoutedLabel.visible = false
            return@fastForEachIndexed
          }

          //Calculate the style and height
          val boxStyle = labelBoxStyles.valueAt(labelIndex)
          layoutedLabel.boxStyle = boxStyle
          layoutedLabel.height = lineHeight + boxStyle.padding.offsetHeight


          //Location
          @Window val snappedYLocation = snapYPosition(labelLocation)
          if ((snappedYLocation in min..max).not()) {
            //point not in visible area - not visible
            layoutedLabel.visible = false
            return@fastForEachIndexed
          }

          //Label is visible, update all other properties
          layoutedLabel.text = labelTexts.valueAt(labelIndex, textService, i18nConfiguration)
          layoutedLabel.visible = true

          layoutedLabel.preferredCenterY = snappedYLocation
          layoutedLabel.actualCenterY = snappedYLocation //also save the value to actual center
        }
      }

      //Remove all invisible elements
      layoutedLabelsBuffer.removeAll {
        it.visible.not()
      }

      calculateOptimalPositions(min, max, paintingContext)
    }

    /**
     * Drops labels until the remaining labels fit into the space between [min] and [max].
     *
     * Deterministic drop rules:
     * 1. Labels taller than the available space are dropped - they can never be painted and must not
     *    evict labels that would fit.
     * 2. While the remaining labels do not fit, the label with the highest [LayoutedLabel2.index]
     *    (= lowest priority) is dropped.
     *
     * The feasibility check calls [VerticalLabelPlacementSolver.requiredSpace] on the filled solver -
     * the exact computation [VerticalLabelPlacementSolver.solve] enforces, so the drop decision and the
     * solver can never disagree (not even by floating-point rounding).
     *
     * Side effect: [placementSolver] is filled with the remaining labels.
     */
    private fun dropLabelsExceedingAvailableSpace(labelSpacing: @Zoomed Double, min: @Window Double, max: @Window Double) {
      @px val availableSpace = max - min

      layoutedLabelsBuffer.removeAll {
        it.height > availableSpace
      }

      fillPlacementSolver()
      while (placementSolver.labelCount > 0 && placementSolver.requiredSpace(labelSpacing) > availableSpace) {
        removeLabelWithHighestIndex()
        fillPlacementSolver()
      }
    }

    /**
     * Fills [placementSolver] with the current buffer content.
     * The buffer must be sorted by [LayoutedLabel2.preferredCenterY].
     */
    private fun fillPlacementSolver() {
      placementSolver.clear()
      layoutedLabelsBuffer.fastForEach { label ->
        placementSolver.addLabel(label.preferredCenterY, label.height)
      }
    }

    /**
     * Removes the label with the highest [LayoutedLabel2.index] (= lowest priority) from the buffer
     */
    private fun removeLabelWithHighestIndex() {
      var highestIndex = -1
      layoutedLabelsBuffer.fastForEach { label ->
        if (label.index > highestIndex) {
          highestIndex = label.index
        }
      }
      layoutedLabelsBuffer.removeAll {
        it.index == highestIndex
      }
    }

    /**
     * Compares the layouted labels by the preferred center.
     * Explicit comparator to avoid boxing the double selector value on every comparison (as `compareBy` would).
     */
    private val layoutedLabelByPreferredYComparator: Comparator<LayoutedLabel2> = Comparator { a, b -> a.preferredCenterY.compareTo(b.preferredCenterY) }

    /**
     * The solver that computes the exact collision-free positions
     */
    private val placementSolver: VerticalLabelPlacementSolver = VerticalLabelPlacementSolver()

    /**
     * Calculates the optimal positions for all labels: overlap-free, within min/max, minimizing the
     * squared deviations from the preferred positions (exact solution, see [VerticalLabelPlacementSolver]).
     * Labels that do not fit into the available space are dropped deterministically first
     * (see [dropLabelsExceedingAvailableSpace]).
     */
    private fun calculateOptimalPositions(min: @Window Double, max: @Window Double, paintingContext: LayerPaintingContext) {
      //Sort the labels by Y location
      layoutedLabelsBuffer.sortWith(layoutedLabelByPreferredYComparator)

      //A negative configured spacing would break the solver's chain-constraint transformation - treat it as 0
      @Zoomed val labelSpacing = style.labelSpacing.coerceAtLeast(0.0)

      dropLabelsExceedingAvailableSpace(labelSpacing, min, max)

      //The stacked-at-top/bottom bounds are only consumed by the ShowMinMax debug overlay
      paintingContext.ifDebug(DebugFeature.ShowMinMax) {
        calculateAbsoluteMin(labelSpacing, min)
        calculateAbsoluteMax(labelSpacing, max)
      }

      placementSolver.solve(spacing = labelSpacing, min = min, max = max)

      layoutedLabelsBuffer.fastForEachWithIndex { index, label ->
        label.actualCenterY = placementSolver.placedCenterYAt(index)
      }
    }

    /**
     * Sets [LayoutedLabel2.centerYMin] for each label: the center when all labels are stacked at the top.
     * Only used for the [DebugFeature.ShowMinMax] overlay.
     */
    private fun calculateAbsoluteMin(labelSpacing: @Zoomed Double, min: @Window Double) {
      var lastMaxY = min - labelSpacing

      layoutedLabelsBuffer.fastForEach { label ->
        label.centerYMin = lastMaxY + labelSpacing + label.halfHeight
        lastMaxY = label.centerYMin + label.halfHeight
      }
    }

    /**
     * Sets [LayoutedLabel2.centerYMax] for each label: the center when all labels are stacked at the bottom.
     * Only used for the [DebugFeature.ShowMinMax] overlay.
     */
    private fun calculateAbsoluteMax(labelSpacing: @Zoomed Double, max: @Window Double) {
      var lastMinY = max + labelSpacing

      layoutedLabelsBuffer.fastForEachReversed { label ->
        label.centerYMax = lastMinY - labelSpacing - label.halfHeight
        lastMinY = label.centerYMax - label.halfHeight
      }
    }

  }

  /**
   * Calculates the layout
   */
  fun layout(
    paintingContext: LayerPaintingContext,

    /**
     * The label locations - in @[Window]
     * (based on [com.meistercharts.algorithms.layers.crosswire.CrossWireLayer.LabelIndex])
     */
    labelLocations: @Window DoublesProvider1<LayerPaintingContext>,

    /**
     * Provides the style for the label box
     */
    labelBoxStyles: MultiProvider<LabelIndex, BoxStyle>,

    /**
     * Provides the formatted value for the label
     */
    labelTexts: LabelsProvider<LabelIndex>,

    /**
     * The min value for the labels
     */
    min: @Window Double,
    /**
     * The max value for the labels
     */
    max: @Window Double,

    ) {
    paintingVariables.update(
      labelLocations,
      labelBoxStyles,
      labelTexts, min, max, paintingContext
    )
  }

  /**
   * Paints the labels.
   *
   * ATTENTION: It is required to call [layout] before!
   */
  fun paintLabels(
    paintingContext: LayerPaintingContext,

    /**
     * Provides the style for the label box
     */
    labelBoxStyles: MultiProvider<LabelIndex, BoxStyle>,

    /**
     * The text colors for the labels
     */
    labelTextColors: MultiProvider<LabelIndex, Color>,

    placement: LabelPlacement,

    ) {
    val gc: CanvasRenderingContext = paintingContext.gc

    gc.font(style.font())

    //Paint the labels
    paintingVariables.layoutedLabelsBuffer.fastForEach { label ->
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
      @px val innerX = style.gapToLabels * alignmentFactor

      gc.beginPath()
      gc.moveTo(0.0, preferredCenterY)
      gc.lineTo(innerX, actualCenterY)

      //Stroke the line to the box
      if (style.showLineToValueBox()) {
        gc.stroke(style.lineColor)
        gc.lineWidth = style.lineWidth
        gc.stroke()
      }

      gc.saved {
        val labelFillStyle = labelBoxStyles.valueAt(label.index)
        val labelTextColor = labelTextColors.valueAt(label.index)

        //Debug code
        paintingContext.ifDebug(DebugFeature.ShowMinMax) {
          gc.stroke(labelFillStyle.fill.get() ?: Color.darkgray())

          //Vertical line
          gc.strokeLine(0.0, label.centerYMin, 0.0, label.centerYMax)

          //Lines to the label itself
          gc.strokeLine(innerX, actualCenterY, 0.0, label.centerYMin)
          gc.strokeLine(innerX, actualCenterY, 0.0, label.centerYMax)
        }

        //Translate to the box location
        gc.translate(innerX, actualCenterY)

        val boxAnchor: Direction = when (placement) {
          LabelPlacement.OnRightSide -> Direction.CenterLeft
          LabelPlacement.OnLeftSide -> Direction.CenterRight
        }

        val textToDraw = label.text
        gc.paintTextBox(
          line = textToDraw,
          anchorDirection = boxAnchor,
          anchorGapHorizontal = 0.0,
          anchorGapVertical = 0.0,
          boxStyle = labelFillStyle,
          textColor = labelTextColor
        )

        //TODO: Required??? Save the label bounds (for mouse over)
      }
    }
  }

  @StyleDsl
  open class Style {
    /**
     * If set to true the connecting line to the value box is painted
     */
    var showLineToValueBox: () -> Boolean = { true }

    /**
     * The color of the line (from the label to the box)
     */
    var lineColor: ColorProvider = Color.gray

    /**
     * The width of the line (from the label to the box)
     */
    var lineWidth: @Zoomed Double = 1.0

    /**
     * The gap between the label and the center line
     * (direction depends on the alignment)
     */
    var gapToLabels: @Zoomed Double = 20.0

    /**
     * The space between the labels (y)
     */
    var labelSpacing: @Zoomed Double = 3.0

    /**
     * Font for labels
     */
    var font: () -> FontDescriptorFragment = FontDescriptorFragment.empty.asProvider()
  }
}


/**
 * Contains the layout information for a label.
 * Does only support layouts in y direction.
 *
 */
class LayoutedLabel2 : LayoutVariable {
  /**
   * The box style for this label
   */
  var boxStyle: BoxStyle = BoxStyle.gray

  /**
   * The index of this label.
   * This property is necessary to be able to get the index later, after the cache has be sorted by y location
   */
  var index: Int = -1

  /**
   * Is set to true if the point for the label is visible.
   * A label is always/only visible, if the data point is visible
   */
  var visible: Boolean = false

  /**
   * The (translated) text of the label
   */
  var text: String = ""

  /**
   * The height of the label in pixels
   */
  var height: @Zoomed Double = 0.0

  /**
   * Half of the height
   */
  val halfHeight: @Zoomed Double
    get() = height / 2.0

  /**
   * The preferred center y locations for this label.
   * This is the location where the label *wants* to be painted.
   * This is *not* the location where the label is painted in the end - at least not in all cases.
   */
  var preferredCenterY: @Window Double = 0.0

  /**
   * The min value (center) for this label - the center when all labels are stacked at the top.
   * Only calculated when the [DebugFeature.ShowMinMax] overlay is enabled.
   *
   * The min value is independent form the [preferredCenterY]. It just depends on the min value and other labels.
   */
  var centerYMin: @Window Double = -Double.MAX_VALUE

  /**
   * The max value (center) for this label - the center when all labels are stacked at the bottom.
   * Only calculated when the [DebugFeature.ShowMinMax] overlay is enabled.
   *
   * The max value is independent form the [preferredCenterY]. It just depends on the max value and other labels.
   */
  var centerYMax: @Window Double = Double.MAX_VALUE

  /**
   * The actual position where the label is painted.
   * Is calculated and depends on the amount and location of other labels
   */
  var actualCenterY: @Window Double = 0.0

  /**
   * The minimum y value (top side of the label)
   */
  @Window
  val actualMinY: Double
    get() = actualCenterY - height / 2.0

  /**
   * The maximum y value (bottom side of the label)
   */
  @Window
  val actualMaxY: Double
    get() = actualCenterY + height / 2.0

  /**
   * Returns true if the actual y values overlaps with the actual bounds of the other label
   */
  fun overlapsActualY(other: LayoutedLabel2): Boolean {
    //this is above other
    if (actualMinY > other.actualMaxY) {
      return false
    }

    //this is below other
    return actualMaxY >= other.actualMinY
  }

  override fun reset() {
    index = -1
    text = Uninitialized
    height = 0.0
    preferredCenterY = 0.0
    centerYMin = -Double.MAX_VALUE
    centerYMax = Double.MAX_VALUE
    actualCenterY = 0.0
    visible = false
  }

  override fun toString(): String {
    return "LayoutedLabel2(index=$index, visible=$visible, text='$text', height=$height, preferredCenterY=$preferredCenterY, centerYMin=$centerYMin, centerYMax=$centerYMax, actualCenterY=$actualCenterY)"
  }
}

interface LabelPainterPaintingVariables {
  /**
   * Returns true if at least one label is visible
   */
  @Deprecated("No longer required - at the moment")
  fun isNotEmpty(): Boolean
}
