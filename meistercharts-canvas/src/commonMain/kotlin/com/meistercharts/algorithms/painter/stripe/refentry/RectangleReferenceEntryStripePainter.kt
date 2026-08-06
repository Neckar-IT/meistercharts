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
package com.meistercharts.algorithms.painter.stripe.refentry

import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.algorithms.layers.resolve
import com.meistercharts.annotations.Window
import com.meistercharts.annotations.Zoomed
import com.meistercharts.canvas.CanvasRenderingContext
import com.meistercharts.canvas.ConfigurationDsl
import com.meistercharts.canvas.DebugFeature
import com.meistercharts.canvas.SnapConfiguration
import com.meistercharts.canvas.fill
import com.meistercharts.canvas.snapPhysicalTranslation
import com.meistercharts.color.Color
import com.meistercharts.color.ColorProvider
import com.meistercharts.color.ColorProviderNullable
import com.meistercharts.color.get
import com.meistercharts.font.FontDescriptor
import com.meistercharts.font.FontDescriptorFragment
import com.meistercharts.font.combineWith
import com.meistercharts.history.HistoryConfiguration
import com.meistercharts.history.HistoryEnumSet
import com.meistercharts.history.MayBeNoValueOrPending
import com.meistercharts.history.ReferenceEntryData
import com.meistercharts.history.ReferenceEntryDataSeriesIndex
import com.meistercharts.history.ReferenceEntryDifferentIdsCount
import com.meistercharts.history.ReferenceEntryId
import it.neckar.geometry.Direction
import it.neckar.open.annotations.Hot
import it.neckar.open.annotations.HotAllocation
import it.neckar.open.formatting.intFormat
import it.neckar.open.unit.number.MayBeNaN
import it.neckar.open.unit.si.ms

/**
 * Paints stripes using colored (filled) rectangles
 */
class RectangleReferenceEntryStripePainter(
  additionalConfiguration: Configuration.() -> Unit = {},
) : AbstractReferenceEntryStripePainter() {

  override val configuration: Configuration = Configuration().also(additionalConfiguration)

  /**
   * 1-element cache for the label font: [CanvasRenderingContext.font] combines the current canvas font
   * with [Configuration.labelFont] and allocates a new [FontDescriptor] on *every* call - one per labeled
   * segment per frame. The combined descriptor only changes when the base font or the fragment changes,
   * so it is cached here and re-applied via the (allocation-free) [CanvasRenderingContext.font] setter.
   */
  private var combinedLabelFontBase: FontDescriptor? = null
  private var combinedLabelFontFragment: FontDescriptorFragment? = null
  private var combinedLabelFont: FontDescriptor? = null

  /**
   * Applies [Configuration.labelFont] to the canvas - using [combinedLabelFont] to avoid the
   * per-call allocation of [CanvasRenderingContext.font].
   */
  @Hot
  private fun applyLabelFont(gc: CanvasRenderingContext) {
    val fragment = configuration.labelFont
    val currentFont = gc.font
    val cached = combinedLabelFont

    if (cached != null && fragment === combinedLabelFontFragment) {
      if (currentFont === combinedLabelFontBase || currentFont === cached) {
        gc.font = cached
        return
      }
    }

    @HotAllocation("cache miss - the combined font descriptor is cached until the base font or the label font changes")
    val combined = currentFont.combineWith(fragment)
    combinedLabelFontBase = currentFont
    combinedLabelFontFragment = fragment
    combinedLabelFont = combined
    gc.font = combined
  }

  @Hot
  override fun paintSegment(
    paintingContext: LayerPaintingContext,
    dataSeriesIndex: ReferenceEntryDataSeriesIndex,
    segment: ReferenceEntryStripePainterPaintingVariablesForOneDataSeries.Segment,
  ) {
    @Window val startX = segment.startX //might be out of the screen
    @Window val endX = segment.endX //might be out of the screen
    @MayBeNoValueOrPending val idToPaint: ReferenceEntryId = ReferenceEntryId(segment.id)
    @MayBeNoValueOrPending val count: ReferenceEntryDifferentIdsCount = ReferenceEntryDifferentIdsCount(segment.count)
    val statusEnumSet: HistoryEnumSet = HistoryEnumSet(segment.status)
    val entryData: ReferenceEntryData? = segment.data

    val gc = paintingContext.gc
    val chartCalculator = paintingContext.chartCalculator
    val chartSupport = paintingContext.chartSupport

    if (idToPaint == ReferenceEntryId.NoValue) {
      //the value is NoValue, do *not* paint anything
      return
    }

    //value has changed, paint the rect
    require(startX.isFinite()) {
      "Start value is missing $startX"
    }
    require(endX.isFinite()) {
      "End value is missing $endX"
    }

    val paintingVariables = paintingVariables()

    val historyConfiguration = paintingVariables.historyConfiguration

    @Window val startXinViewport = chartCalculator.coerceInViewportX(startX)
    @Window val endXinViewport = chartCalculator.coerceInViewportX(endX)

    @Zoomed val rectangleHeight = forDataSeriesIndex(dataSeriesIndex).height
    @Zoomed val rectangleWidth = endXinViewport - startXinViewport

    if (idToPaint.isNoValue()) {
      if (gc.debug[DebugFeature.HistoryGaps]) {
        gc.fill(Color.red)
        gc.fillRect(startXinViewport, 0.0, rectangleWidth, rectangleHeight)
        gc.fill(Color.white)
        @HotAllocation("DebugFeature.HistoryGaps only - text rendering allocates")
        gc.fillText("-", startXinViewport + rectangleWidth / 2.0, rectangleHeight / 2.0, Direction.Center, maxWidth = rectangleWidth, maxHeight = rectangleHeight)
      }
      return
    }

    if (idToPaint.isPending()) {
      if (gc.debug[DebugFeature.HistoryGaps]) {
        gc.fill(Color.orange)
        gc.fillRect(startXinViewport, 0.0, rectangleWidth, rectangleHeight)
        gc.fill(Color.white)
        @HotAllocation("DebugFeature.HistoryGaps only - text rendering allocates")
        gc.fillText("?", startXinViewport + rectangleWidth / 2.0, rectangleHeight / 2.0, Direction.Center, maxWidth = rectangleWidth, maxHeight = rectangleHeight)
      }
      return
    }

    when {
      count.value == 0 -> {
        //Do not paint anything
      }

      count.value == 1 -> {
        gc.fill(configuration.fillProvider.color(dataSeriesIndex, idToPaint, statusEnumSet, historyConfiguration))

        val snapConfiguration = configuration.snapConfiguration()
        gc.snapPhysicalTranslation(snapConfiguration)
        @Zoomed val rectangleWidth = snapConfiguration.snapXSize(rectangleWidth)
        gc.fillRect(startXinViewport, 0.0, rectangleWidth, snapConfiguration.snapYSize(rectangleHeight))

        //Stroke the left + right separators
        configuration.separatorStroke.get()?.let {
          if (configuration.separatorSize > 0) {
            gc.lineWidth = configuration.separatorSize
            gc.stroke(it)
            gc.strokeLine(startXinViewport, 0.0, startXinViewport, rectangleHeight)
            gc.strokeLine(startXinViewport + rectangleWidth, 0.0, startXinViewport + rectangleWidth, rectangleHeight)
          }
        }

        //Paint the label
        entryData?.label?.resolve(paintingContext)?.let { label ->
          gc.fill(configuration.labelColorProvider(idToPaint, statusEnumSet, historyConfiguration))
          applyLabelFont(gc)
          @HotAllocation("Label painting - text rendering allocates")
          gc.fillText(label, startXinViewport + rectangleWidth / 2.0, rectangleHeight / 2.0, Direction.Center, maxWidth = rectangleWidth, maxHeight = rectangleHeight)
        }
      }

      count.value > 1 -> {
        //Count is > 1, we show the number of entries
        gc.fill(configuration.countFill)

        val snapConfiguration = configuration.snapConfiguration()
        gc.snapPhysicalTranslation(snapConfiguration)
        gc.fillRect(startXinViewport, 0.0, snapConfiguration.snapXSize(rectangleWidth - 2.0), snapConfiguration.snapYSize(rectangleHeight))

        gc.fill(configuration.countLabelColor)
        applyLabelFont(gc)
        @HotAllocation("Count-label painting - number formatting (cached, allocates on miss) and text rendering allocate")
        gc.fillText(intFormat.format(count.value.toDouble()), startXinViewport + rectangleWidth / 2.0, rectangleHeight / 2.0, Direction.Center, maxWidth = rectangleWidth, maxHeight = rectangleHeight)
      }
    }
  }

  @ConfigurationDsl
  class Configuration : AbstractReferenceEntryStripePainter.Configuration() {
    /**
     * The snap configuration for the stripes
     */
    var snapConfiguration: () -> SnapConfiguration = { SnapConfiguration.OnlyX }

    /**
     * Provides the fill color for the given value
     */
    var fillProvider: ReferenceEntryStatusColorProvider = ReferenceEntryStatusColorProvider.default()

    /**
     * Provides the color of the label for the given value
     */
    var labelColorProvider: (value: ReferenceEntryId, statusEnumSet: HistoryEnumSet, historyConfiguration: HistoryConfiguration) -> Color = { _, _, _ -> Color.white() }

    /**
     * The font of the label
     */
    var labelFont: FontDescriptorFragment = FontDescriptorFragment.DefaultSize

    /**
     * The color when the count is shown
     */
    var countLabelColor: ColorProvider = Color.gray

    /**
     * The fill that is used when the count is displayed
     */
    var countFill: ColorProvider = Color.silver

    /**
     * The size of the separator.
     * If set to 0.0 the separators are not visible
     */
    var separatorSize: Double = 1.0

    /**
     * The color of the separator between two lines.
     * The separator is not displayed if set to null
     */
    var separatorStroke: ColorProviderNullable = Color.white
  }
}
