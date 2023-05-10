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
package com.meistercharts.algorithms.layers.crosswire

import com.meistercharts.algorithms.layers.AbstractLayer
import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.algorithms.layers.LayerType
import com.meistercharts.algorithms.layers.PaintingVariables
import com.meistercharts.algorithms.layers.crosswire.CrossWireLayer.Style
import com.meistercharts.algorithms.painter.Color
import com.meistercharts.algorithms.painter.LabelPainter2
import com.meistercharts.algorithms.painter.LabelPlacement
import it.neckar.open.unit.number.MayBeNaN
import com.meistercharts.annotations.Window
import com.meistercharts.annotations.WindowRelative
import com.meistercharts.annotations.Zoomed
import com.meistercharts.canvas.FontDescriptorFragment
import com.meistercharts.canvas.StyleDsl
import com.meistercharts.canvas.paintTextBox
import com.meistercharts.canvas.saved
import com.meistercharts.design.Theme
import com.meistercharts.model.Direction
import com.meistercharts.model.Distance
import com.meistercharts.model.Insets
import com.meistercharts.provider.LabelsProvider
import it.neckar.open.provider.DoublesProvider1
import it.neckar.open.provider.HasSize
import it.neckar.open.provider.MultiProvider
import it.neckar.open.provider.MultiProviderIndexContextAnnotation
import it.neckar.open.i18n.I18nConfiguration
import it.neckar.open.i18n.TextService
import com.meistercharts.style.BoxStyle
import it.neckar.open.unit.other.px

/**
 * Renders a cross wire.
 *
 * If the cross wire line shall be painted in the background use this solution:
 * - two cross wire layers
 * - one in the background. Call [Style.applyShowOnlyCrossWireLine] for this one
 * - one in the content area. Set [Style.showCrossWireLine] to false
 */
class CrossWireLayer(
  val data: Data,
  /**
   * Can be configured to background (if necessary)
   */
  override val type: LayerType = LayerType.Content,
  styleConfiguration: Style .() -> Unit = {},
) : AbstractLayer() {

  val style: Style = Style().also(styleConfiguration)

  /**
   * The label painter that paints the value-labels (including layout)
   */
  val valueLabelPainter: LabelPainter2 = LabelPainter2(true, true) {
    font = { style.valueLabelFont }
    showLineToValueBox = { style.showLineToValueBox }
  }

  private val valueLabelLocations: @Window DoublesProvider1<LayerPaintingContext> = object : DoublesProvider1<LayerPaintingContext> {
    override fun valueAt(index: Int, param1: LayerPaintingContext): Double {
      return data.valueLabelsProvider.locationAt(index)
    }

    override fun size(param1: LayerPaintingContext): Int {
      return data.valueLabelsProvider.size()
    }
  }

  private val valueLabelTexts: LabelsProvider<LabelIndex> = object : LabelsProvider<LabelIndex> {
    override fun valueAt(index: Int, param1: TextService, param2: I18nConfiguration): String {
      return data.valueLabelsProvider.labelAt(index, param1, param2)
    }
  }

  override fun paintingVariables(): PaintingVariables {
    return paintingVariables
  }

  private val paintingVariables = object : PaintingVariables {
    /**
     * The exact location of the wire
     */
    var wireLocation: @Window Double = 0.0

    var valueLabelPlacement: LabelPlacement = LabelPlacement.OnRightSide

    var currentLocationLabelText: String = ""

    override fun calculate(paintingContext: LayerPaintingContext) {
      wireLocation = style.locationX(paintingContext)

      if (style.showValueLabels) {
        valueLabelPlacement = style.valueLabelPlacementStrategy(wireLocation, paintingContext)
      }

      currentLocationLabelText = if (style.showCurrentLocationLabel) {
        data.currentLocationLabelTextProvider(paintingContext, wireLocation)
      } else {
        ""
      }
    }
  }

  override fun layout(paintingContext: LayerPaintingContext) {
    super.layout(paintingContext)

    if (style.showValueLabels) {
      data.valueLabelsProvider.layout(paintingVariables.wireLocation, paintingContext)

      valueLabelPainter.layout(
        paintingContext,
        labelLocations = valueLabelLocations,
        labelTexts = valueLabelTexts,
        labelBoxStyles = style.valueLabelBoxStyle,
        min = style.valueLabelsStart(paintingContext),
        max = style.valueLabelsEnd(paintingContext),
      )
    }
  }

  override fun paint(paintingContext: LayerPaintingContext) {
    val chartCalculator = paintingContext.chartCalculator
    val gc = paintingContext.gc

    //The location of the wire itself
    gc.translate(paintingVariables.wireLocation, 0.0)

    //Paint the wire
    if (style.showCrossWireLine) {
      gc.stroke(style.wireColor)
      gc.lineWidth = style.wireWidth
      gc.strokeLine(0.0, chartCalculator.contentViewportMinY(), 0.0, chartCalculator.contentViewportMaxY())
    }

    if (style.showCurrentLocationLabel) {
      gc.saved {

        //Paint the location-label first!
        val currentLocationLabelText = paintingVariables.currentLocationLabelText

        gc.font(style.currentLocationLabelFont)

        style.currentLocationLabelAnchorPoint(paintingContext).let { anchorPointTranslation ->
          gc.translate(anchorPointTranslation.x, anchorPointTranslation.y)
        }

        gc.paintTextBox(
          currentLocationLabelText, style.currentLocationLabelAnchorDirection, 0.0, 0.0, style.currentLocationLabelBoxStyle, style.currentLocationLabelTextColor
        )
      }
    }

    //Paint the value-labels - if necessary
    if (style.showValueLabels) {
      valueLabelPainter.paintLabels(
        paintingContext = paintingContext,
        labelBoxStyles = style.valueLabelBoxStyle,
        labelTextColors = style.valueLabelTextColor,
        placement = paintingVariables.valueLabelPlacement
      )
    }
  }

  /**
   * Model for the cross wire
   */
  interface ValueLabelsProvider : HasSize {
    /**
     * Is called first - calculate the locations
     */
    fun layout(wireLocation: @Window Double, paintingContext: LayerPaintingContext)

    /**
     * Returns the location of the label for the given index
     */
    fun locationAt(index: Int): @Window @MayBeNaN Double

    /**
     * Returns the label for the given index
     */
    fun labelAt(index: Int, textService: TextService, i18nConfiguration: I18nConfiguration): String


    companion object {
      val Empty: ValueLabelsProvider = object : ValueLabelsProvider {
        override fun size(): Int {
          return 0
        }

        override fun layout(wireLocation: Double, paintingContext: LayerPaintingContext) {
        }

        override fun locationAt(index: Int): Double {
          throw UnsupportedOperationException("not implemented")
        }

        override fun labelAt(index: Int, textService: TextService, i18nConfiguration: I18nConfiguration): String {
          throw UnsupportedOperationException("not implemented")
        }
      }
    }
  }

  class Data(
    /**
     * Provides all information for the value-labels (provides [LabelIndex])
     */
    var valueLabelsProvider: ValueLabelsProvider,

    /**
     * Provides the label for the current location
     */
    var currentLocationLabelTextProvider: (paintingContext: LayerPaintingContext, crossWireLocation: @Window Double) -> String = { _, _ -> TODO("not implemented yet") },
  )

  /**
   * The style configuration for the cross wire layer
   */
  @StyleDsl
  open class Style {
    /**
     * The location of the cross wire itself
     */
    var locationX: (paintingContext: LayerPaintingContext) -> @Window Double = { it.chartCalculator.windowRelative2WindowX(0.75) }

    /**
     * Sets the [locationX] to provide a value relative to the window
     */
    fun locationXWindowRelative(location: @WindowRelative Double = 0.75) {
      locationX = { it.chartCalculator.windowRelative2WindowX(location) }
    }

    /**
     * Provides the box style for the given index
     */
    var valueLabelBoxStyle: MultiProvider<LabelIndex, BoxStyle> = MultiProvider.always(BoxStyle.modernGray)

    /**
     * Provides the color for the label text
     */
    var valueLabelTextColor: MultiProvider<LabelIndex, Color> = MultiProvider.always(Color.white)

    /**
     * Sets the given font for all labels of the cross wire
     */
    fun applyCrossWireFont(crossWireFont: FontDescriptorFragment) {
      valueLabelFont = crossWireFont
      currentLocationLabelFont = crossWireFont
    }

    /**
     * The font fragment for the value label
     */
    var valueLabelFont: FontDescriptorFragment = FontDescriptorFragment.empty

    /**
     * Whether to show the cross wire line itself
     */
    var showCrossWireLine: Boolean = true

    /**
     * If this method is called, only the cross wire line itself is shown.
     * This is useful if this layer instance shall be placed in the background.
     */
    fun applyShowOnlyCrossWireLine() {
      showCrossWireLine = true
      showValueLabels = false
      showCurrentLocationLabel = false
    }

    /**
     * Whether to show value-labels (default is visible)
     */
    var showValueLabels: Boolean = true

    /**
     * Whether to show the current location label (default is hidden)
     */
    var showCurrentLocationLabel: Boolean = false

    /**
     * If set to true the connecting line to the value box is painted
     */
    var showLineToValueBox: Boolean = true

    /**
     * The color of the cross wire
     */
    var wireColor: Color = Theme.crossWireLineColor()

    /**
     * The width of the cross wire
     */
    @px
    var wireWidth: Double = 2.0

    /**
     * Where to place the value-labels
     */
    var valueLabelPlacementStrategy: LabelPlacementStrategy = LabelPlacementStrategy.preferOnRightSide { 150.0 }

    /**
     * Provides the minimum y-value for the value-labels
     */
    var valueLabelsStart: (paintingContext: LayerPaintingContext) -> @Window Double = { it.chartCalculator.contentViewportMinY() }

    /**
     * Provides the maximum y-value for the cross wire
     */
    var valueLabelsEnd: (paintingContext: LayerPaintingContext) -> @Window Double = { it.chartCalculator.contentViewportMaxY() }

    /**
     * The box style for the location-label
     */
    var currentLocationLabelBoxStyle: BoxStyle = BoxStyle(fill = Color.silver, borderColor = Color.darkgray, padding = DefaultLabelBoxPadding)

    /**
     * The text color for the location-label
     */
    var currentLocationLabelTextColor: Color = Color.white

    /**
     * The font to be used for the location-label
     */
    var currentLocationLabelFont: FontDescriptorFragment = FontDescriptorFragment.empty

    /**
     * The anchor direction of the location-label
     */
    var currentLocationLabelAnchorDirection: Direction = Direction.TopCenter

    /**
     * Provides the translation to the anchor point.
     * Relative to the top of the cross-wire line.
     *
     * Usually the translation should only contain a y value
     */
    var currentLocationLabelAnchorPoint: (paintingContext: LayerPaintingContext) -> Distance = { Distance.none }

    companion object {
      /**
       * The default insets for the value labels
       */
      val DefaultLabelBoxPadding: Insets = Insets(7.0, 10.0, 7.0, 10.0)
    }
  }

  companion object {
    /**
     * Creates a cross wire layer that only paints the wire itself.
     * Can be used to place in the background
     */
    fun onlyWire(locationX: (paintingContext: LayerPaintingContext) -> @Window Double): CrossWireLayer {
      return CrossWireLayer(Data(ValueLabelsProvider.Empty)) {
        this.locationX = locationX
        this.applyShowOnlyCrossWireLine()
      }
    }
  }

  @Target(AnnotationTarget.TYPE)
  @MustBeDocumented
  @Retention(AnnotationRetention.SOURCE)
  @MultiProviderIndexContextAnnotation
  annotation class LabelIndex

}

/**
 * The strategy where to place the value-labels in relation to the cross-wire
 */
fun interface LabelPlacementStrategy {
  /**
   * Returns the label placement
   */
  operator fun invoke(wireLocation: @Window Double, paintingContext: LayerPaintingContext): LabelPlacement


  companion object {
    /**
     * Labels are always painted on the right side of the cross-wire
     */
    val AlwaysOnRightSide: LabelPlacementStrategy = LabelPlacementStrategy { _, _ -> LabelPlacement.OnRightSide }

    /**
     * Labels are always painted on the left side of the cross-wire
     */
    val AlwaysOnLeftSide: LabelPlacementStrategy = LabelPlacementStrategy { _, _ -> LabelPlacement.OnLeftSide }

    /**
     * Prefer painting the labels on the right side of the cross wire but paint
     * them on the left side if there is not enough space on the right side.
     */
    fun preferOnRightSide(minWidthForValueLabelOnSideRight: () -> @Zoomed Double = { 150.0 }): LabelPlacementStrategy {
      return LabelPlacementStrategy { wireLocation, paintingContext ->
        val gc = paintingContext.gc

        @Zoomed val availableWidthOnRightSide = gc.width - wireLocation

        if (availableWidthOnRightSide >= minWidthForValueLabelOnSideRight()) {
          LabelPlacement.OnRightSide
        } else {
          LabelPlacement.OnLeftSide
        }
      }
    }
  }
}
