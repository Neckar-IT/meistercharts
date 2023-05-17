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

import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.algorithms.layers.crosswire.CrossWireLayer.LabelIndex
import it.neckar.open.unit.number.MayBeNaN
import com.meistercharts.annotations.Window
import com.meistercharts.annotations.Zoomed
import com.meistercharts.canvas.CanvasRenderingContext
import com.meistercharts.canvas.DebugFeature
import com.meistercharts.canvas.FontDescriptorFragment
import com.meistercharts.canvas.StyleDsl
import com.meistercharts.canvas.i18nConfiguration
import com.meistercharts.canvas.layout.cache.LayoutVariable
import com.meistercharts.canvas.layout.cache.LayoutVariablesObjectCache
import com.meistercharts.canvas.layout.cache.StringsCache.Companion.Uninitialized
import com.meistercharts.canvas.paintTextBox
import com.meistercharts.canvas.saved
import com.meistercharts.canvas.textService
import com.meistercharts.model.Direction
import com.meistercharts.provider.LabelsProvider
import it.neckar.open.kotlin.lang.asProvider
import it.neckar.open.provider.DoublesProvider1
import it.neckar.open.provider.MultiProvider
import it.neckar.open.provider.fastForEachIndexed
import com.meistercharts.style.BoxStyle
import it.neckar.open.unit.other.pct
import it.neckar.open.unit.other.px
import kotlin.math.max
import kotlin.math.min

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

  private val paintingVariables = object : LabelPainterPaintingVariables {
    /**
     * Contains the layouted labels
     */
    val layoutedLabelsCache: LayoutVariablesObjectCache<LayoutedLabel2> = LayoutVariablesObjectCache { LayoutedLabel2() }

    override fun isNotEmpty(): Boolean {
      return layoutedLabelsCache.isEmpty().not()
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
      layoutedLabelsCache.prepare(preferredLabelsCount)

      //Fill the cache with the values - these are then used to calculate the layout
      @px var availableSpace = max - min

      labelLocations.fastForEachIndexed(paintingContext) { labelIndex: @LabelIndex Int, labelLocation: @MayBeNaN @Window Double ->
        layoutedLabelsCache.values[labelIndex].let { layoutedLabel ->
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

          //Is there enough space for this label remaining
          if (availableSpace < layoutedLabel.height) {
            //Not enough space!
            layoutedLabel.visible = false
            return@fastForEachIndexed
          }
          //subtract the height and gap
          availableSpace = availableSpace - layoutedLabel.height - style.labelSpacing


          //Label is visible, update all other properties
          layoutedLabel.text = labelTexts.valueAt(labelIndex, textService, i18nConfiguration)
          layoutedLabel.visible = true

          layoutedLabel.preferredCenterY = snappedYLocation
          layoutedLabel.setActualCenterY(layoutedLabel.preferredCenterY) //also save the value to actual center
        }
      }

      //Remove all invisible elements
      layoutedLabelsCache.removeAll {
        it.visible.not()
      }

      calculateOptimalPositions(min, max)
    }

    /**
     * Compares the layouted labels by the preferred center
     */
    private val layoutedLabelByPreferredYComparator: Comparator<LayoutedLabel2> = compareBy { it.preferredCenterY }

    /**
     * Calculates the optimal positions for all labels
     */
    private fun calculateOptimalPositions(min: @Window Double, max: @Window Double) {
      //Sort the labels by Y location
      layoutedLabelsCache.sortWith(layoutedLabelByPreferredYComparator)

      // Step 1: Calculate absolute min/max values for each label
      // These are the absolute min/max values - when the labels are stacked at the top/bottom
      // min/max values do *not* consider the preferred location
      calculateAbsoluteMin(min)
      calculateAbsoluteMax(max)

      //Layout from min to max  (simple stack)
      minToMaxLayout(min)

      //(re)overlap labels for 50%
      revertPercent(0.5)

      //stack from current position from max to min
      maxToMinLayout(max)

      //Optimize the layout locally (each label within the bounds of its neighbors)
      optimizeLayoutYLocally()
    }

    /**
     * Sets the min value for each label
     */
    private fun calculateAbsoluteMin(min: @Window Double) {
      var lastMaxY = min - style.labelSpacing

      layoutedLabelsCache.fastForEach { label ->
        label.setCenterYMin(lastMaxY + style.labelSpacing + label.halfHeight)
        lastMaxY = label.centerYMin + label.halfHeight
      }
    }

    /**
     * Sets the max values for each label
     */
    private fun calculateAbsoluteMax(max: @Window Double) {
      var lastMinY = max + style.labelSpacing

      layoutedLabelsCache.fastForEachReverse { label ->
        label.setCenterYMax(lastMinY - style.labelSpacing - label.halfHeight)
        lastMinY = label.centerYMax - label.halfHeight
      }
    }


    /**
     * Stack from low y values to high y values.
     *
     * Initially the actual values are set to the preferred values!
     */
    private fun minToMaxLayout(min: @Window Double) {
      @px var lastMaxY = min - style.labelSpacing

      layoutedLabelsCache.fastForEach { label ->
        //Check the min y with the last stored top y
        if (label.actualMinY < lastMaxY + style.labelSpacing) {
          //We are too low - move up
          label.setActualCenterY(lastMaxY + style.labelSpacing + label.halfHeight)
        }

        lastMaxY = label.actualMaxY
      }
    }

    /**
     * Move 50% towards the natural position
     */
    private fun revertPercent(@pct correctionFactor: Double) {
      layoutedLabelsCache.fastForEachFiltered({ it.hasModifiedActualY() }) { label ->
        //The delta to the preferred center
        @px val delta = label.actualCenterY - label.preferredCenterY
        label.setActualCenterY(label.actualCenterY - delta * correctionFactor)
      }
    }

    /**
     * Layout from high y values to low y values
     */
    private fun maxToMinLayout(max: @Window Double) {
      @px var lastMinY = max + style.labelSpacing

      layoutedLabelsCache.fastForEachReverse { label ->
        //Check the max y with the last stored bottom y
        if (label.actualMaxY > lastMinY - style.labelSpacing) {
          //We are too high - move down
          label.setActualCenterY(lastMinY - style.labelSpacing - label.halfHeight)
        }

        lastMinY = label.actualMinY
      }
    }

    /**
     * Optimize the layout locally
     */
    private fun optimizeLayoutYLocally() {
      if (layoutedLabelsCache.isEmpty()) {
        return
      }

      layoutedLabelsCache.fastForEachWithIndex { index, middleLabel ->
        val topLabel: LayoutedLabel2? = if (index == 0) {
          null
        } else {
          layoutedLabelsCache[index - 1]
        }

        val bottomLabel: LayoutedLabel2? = if (index == layoutedLabelsCache.size - 1) {
          null
        } else {
          layoutedLabelsCache[index + 1]
        }

        avoidOverlap(topLabel, middleLabel, bottomLabel)
      }
    }

    /**
     * Avoids overlaps between the labels by moving [label]
     */
    fun avoidOverlap(topLabel: LayoutedLabel2?, label: LayoutedLabel2, bottomLabel: LayoutedLabel2?) {
      if (topLabel != null && label.overlapsActualY(topLabel)) {
        //label is overlapping with the top label

        //The smallest y value that does not overlap with the lower label
        @px val minY = topLabel.actualMaxY + style.labelSpacing + label.height / 2.0
        label.setActualCenterY(max(label.preferredCenterY, minY))
        return
      }

      if (bottomLabel != null && label.overlapsActualY(bottomLabel)) {
        //Label is overlapping with the bottom label:

        @px val maxY = bottomLabel.actualMinY - style.labelSpacing - label.height / 2.0
        label.setActualCenterY(min(label.preferredCenterY, maxY))
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
    paintingVariables.layoutedLabelsCache.fastForEach { label ->
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
          gc.stroke(labelFillStyle.fill ?: Color.darkgray)

          //Vertical line
          if (label.centerYMin > label.centerYMax) {
            //Reversed order, max is smaller than min (not enough space!)
            gc.setLineDash(1.0, 2.0)
          }

          gc.strokeLine(0.0, label.centerYMin, 0.0, label.centerYMax)

          //Lines to the label itself
          gc.strokeLine(innerX, actualCenterY, 0.0, label.centerYMin)
          gc.strokeLine(innerX, actualCenterY, 0.0, label.centerYMax)
          //"normal" order
        }

        //Translate to the box location
        gc.translate(innerX, actualCenterY)

        val boxAnchor = when (placement) {
          LabelPlacement.OnRightSide -> Direction.CenterLeft
          LabelPlacement.OnLeftSide -> Direction.CenterRight
        }

        val textToDraw = label.text
        val bounds = gc.paintTextBox(
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
    var lineColor: Color = Color.gray

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
   * The min value (center) for this label.
   * *Might* be larger then [centerYMax] if there is not enough space for all labels
   *
   * The min value is independent form the [preferredCenterY]. It just depends on the min value and other labels.
   */
  var centerYMin: @Window Double = -Double.MAX_VALUE
    private set

  fun setCenterYMin(newValue: @Window Double) {
    this.centerYMin = newValue
    this.actualCenterY = actualCenterY.coerceAtLeast(centerYMin)
  }

  /**
   * The max value (center) for this label
   * Might* be smaller then [centerYMin] if there is not enough space for all labels
   * The max value is independent form the [preferredCenterY]. It just depends on the max value and other labels.
   */
  var centerYMax: @Window Double = Double.MAX_VALUE
    private set

  fun setCenterYMax(newValue: @Window Double) {
    this.centerYMax = newValue
    this.actualCenterY = actualCenterY.coerceAtMost(centerYMax)
  }

  /**
   * The actual position where the label is painted.
   * Is calculated and depends on the amount and location of other labels
   */
  var actualCenterY: @Window Double = 0.0
    private set

  fun setActualCenterY(value: @Window Double) {
    this.actualCenterY = value.coerceIn(centerYMin, centerYMax)
  }

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
   * Returns whether the given y value is within the actual range of this label info
   */
  fun containsActual(@px @Window y: Double): Boolean {
    return y in actualMinY..actualMaxY
  }

  /**
   * Returns true if the actual y is different than the natural y
   */
  fun hasModifiedActualY(): Boolean {
    return preferredCenterY != actualCenterY
  }

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
