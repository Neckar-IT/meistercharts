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
package com.meistercharts.charts.timeline

import com.meistercharts.algorithms.ChartState
import com.meistercharts.algorithms.TimeRange
import com.meistercharts.algorithms.ValueRange
import com.meistercharts.algorithms.axis.AxisEndConfiguration
import com.meistercharts.algorithms.impl.DelegatingZoomAndTranslationDefaults
import com.meistercharts.algorithms.impl.FittingWithMargin
import com.meistercharts.algorithms.impl.MoveDomainValueToLocation
import com.meistercharts.algorithms.layers.AbstractLayer
import com.meistercharts.algorithms.layers.AxisStyle
import com.meistercharts.algorithms.layers.AxisTitleLocation
import com.meistercharts.algorithms.layers.AxisTopTopTitleLayer
import com.meistercharts.algorithms.layers.DirectionalLinesLayer
import com.meistercharts.algorithms.layers.EmptyLayer.disposeSupport
import com.meistercharts.algorithms.layers.HistoryEnumLayer
import com.meistercharts.algorithms.layers.HudElementIndex
import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.algorithms.layers.LayerType
import com.meistercharts.algorithms.layers.Layers.PaintingOrder
import com.meistercharts.algorithms.layers.MultiValueAxisLayer
import com.meistercharts.algorithms.layers.MultipleLayersDelegatingLayer
import com.meistercharts.algorithms.layers.PaintingPropertyKey
import com.meistercharts.algorithms.layers.TilesLayer
import com.meistercharts.algorithms.layers.TimeAxisLayer
import com.meistercharts.algorithms.layers.TransformingChartStateLayer
import com.meistercharts.algorithms.layers.ValueAxisHudLayer
import com.meistercharts.algorithms.layers.ValueAxisLayer
import com.meistercharts.algorithms.layers.addClearBackground
import com.meistercharts.algorithms.layers.addTilesDebugLayer
import com.meistercharts.algorithms.layers.barchart.CategoryAxisLayer
import com.meistercharts.algorithms.layers.barchart.DefaultCategoryAxisLabelPainter
import com.meistercharts.algorithms.layers.barchart.LabelWrapMode
import com.meistercharts.algorithms.layers.clipped
import com.meistercharts.algorithms.layers.crosswire.CrossWireLayer
import com.meistercharts.algorithms.layers.crosswire.CrossWireLayer.LabelIndex
import com.meistercharts.algorithms.layers.crosswire.LabelPlacementStrategy
import com.meistercharts.algorithms.layers.debug.addVersionNumberHidden
import com.meistercharts.algorithms.layers.linechart.LineStyle
import com.meistercharts.algorithms.layers.timeChartCalculator
import com.meistercharts.algorithms.layers.visibleIf
import com.meistercharts.algorithms.layout.BoxIndex
import com.meistercharts.algorithms.layout.LayoutDirection
import com.meistercharts.algorithms.painter.Color
import com.meistercharts.algorithms.painter.DirectLinePainter
import com.meistercharts.algorithms.painter.stripe.enums.RectangleEnumStripePainter
import com.meistercharts.algorithms.tile.AverageHistoryCanvasTilePainter
import com.meistercharts.algorithms.tile.CachedTileProvider
import com.meistercharts.algorithms.tile.CandleHistoryCanvasTilePainter
import com.meistercharts.algorithms.tile.CanvasTilePainter
import com.meistercharts.algorithms.tile.CanvasTileProvider
import com.meistercharts.algorithms.tile.DefaultHistoryGapCalculator
import com.meistercharts.algorithms.tile.DefaultHistoryTileInvalidator
import com.meistercharts.algorithms.tile.HistoryCanvasTilePainter
import com.meistercharts.algorithms.tile.HistoryGapCalculator
import com.meistercharts.algorithms.tile.HistoryRenderPropertiesCalculatorLayer
import com.meistercharts.algorithms.tile.HistoryTileInvalidator
import com.meistercharts.algorithms.tile.HistoryTilesInvalidationResult
import com.meistercharts.algorithms.tile.MinDistanceSamplingPeriodCalculator
import com.meistercharts.algorithms.tile.cached
import com.meistercharts.algorithms.tile.canvasTiles
import com.meistercharts.algorithms.tile.delegate
import com.meistercharts.algorithms.tile.withMinimum
import com.meistercharts.algorithms.withContentAreaSize
import com.meistercharts.algorithms.withContentViewportMargin
import com.meistercharts.algorithms.withTranslation
import com.meistercharts.algorithms.withZoom
import com.meistercharts.annotations.Domain
import com.meistercharts.annotations.DomainRelative
import com.meistercharts.annotations.PhysicalPixel
import com.meistercharts.annotations.Window
import com.meistercharts.annotations.WindowRelative
import com.meistercharts.annotations.Zoomed
import com.meistercharts.canvas.BorderRadius
import com.meistercharts.canvas.ChartSupport
import com.meistercharts.canvas.StyleDsl
import com.meistercharts.canvas.debug
import com.meistercharts.canvas.devicePixelRatioSupport
import com.meistercharts.canvas.i18nConfiguration
import com.meistercharts.canvas.layout.cache.DoubleCache
import com.meistercharts.canvas.layout.cache.IntCache
import com.meistercharts.canvas.layout.cache.ObjectCache
import com.meistercharts.canvas.layout.cache.StringCache
import com.meistercharts.canvas.paintingProperties
import com.meistercharts.canvas.textService
import com.meistercharts.canvas.translateOverTime
import com.meistercharts.charts.AbstractChartGestalt
import com.meistercharts.charts.ChartGestalt
import com.meistercharts.charts.ChartId
import com.meistercharts.charts.ChartRefreshGestalt
import com.meistercharts.charts.ContentViewportGestalt
import com.meistercharts.charts.support.ThresholdsSupport
import com.meistercharts.charts.support.ValueAxisSupport
import com.meistercharts.charts.support.thresholdsSupport
import com.meistercharts.design.Theme
import com.meistercharts.history.AndBefore
import com.meistercharts.history.DecimalDataSeriesIndex
import com.meistercharts.history.DecimalDataSeriesIndexProvider
import com.meistercharts.history.EnumDataSeriesIndex
import com.meistercharts.history.EnumDataSeriesIndexProvider
import com.meistercharts.history.HistoryConfiguration
import com.meistercharts.history.HistoryEnum
import com.meistercharts.history.HistoryEnumOrdinal
import com.meistercharts.history.HistoryEnumSet
import com.meistercharts.history.HistoryEnumSetInt
import com.meistercharts.history.HistoryStorage
import com.meistercharts.history.InMemoryHistoryStorage
import com.meistercharts.history.MayBeNoValueOrPending
import com.meistercharts.history.ObservableHistoryStorage
import com.meistercharts.history.SamplingPeriod
import com.meistercharts.history.atMost
import com.meistercharts.history.delegate
import com.meistercharts.history.fastForEachIndexed
import com.meistercharts.history.search
import com.meistercharts.history.valueAt
import com.meistercharts.model.Insets
import com.meistercharts.model.Side
import com.meistercharts.model.Size
import com.meistercharts.model.Vicinity
import com.meistercharts.provider.SizedLabelsProvider
import it.neckar.open.kotlin.lang.isPositive
import it.neckar.open.time.nowMillis
import it.neckar.open.provider.DoublesProvider1
import it.neckar.open.provider.MultiProvider
import it.neckar.open.provider.MultiProvider2
import it.neckar.open.provider.SizedProvider
import it.neckar.open.provider.delegate
import it.neckar.open.dispose.DisposeSupport
import it.neckar.open.dispose.OnDispose
import it.neckar.open.formatting.CachedNumberFormat
import it.neckar.open.formatting.DateTimeFormat
import it.neckar.open.formatting.TimeFormatWithMillis
import it.neckar.open.formatting.decimalFormat
import it.neckar.open.formatting.decimalFormat2digits
import it.neckar.open.i18n.I18nConfiguration
import it.neckar.open.i18n.TextKey
import it.neckar.open.i18n.TextService
import it.neckar.open.i18n.resolve
import it.neckar.open.observable.ObservableBoolean
import it.neckar.open.observable.ObservableObject
import it.neckar.open.observable.ReadOnlyObservableObject
import com.meistercharts.style.BoxStyle
import com.meistercharts.style.Shadow
import com.meistercharts.style.withFillIfNull
import it.neckar.open.dispose.Disposable
import it.neckar.open.time.TimeConstants
import it.neckar.open.unit.number.MayBeNaN
import it.neckar.open.unit.number.MayBeZero
import it.neckar.open.unit.number.Positive
import it.neckar.open.unit.other.px
import it.neckar.open.unit.quantity.Time
import it.neckar.open.unit.si.ms
import kotlin.jvm.JvmOverloads

/**
 * Callback for value axis styles
 */
typealias ValueAxisStyleConfiguration = (style: ValueAxisLayer.Style, dataSeriesIndex: DecimalDataSeriesIndex) -> Unit
typealias ValueAxisTopTitleStyleConfiguration = (style: AxisTopTopTitleLayer.Configuration, dataSeriesIndex: DecimalDataSeriesIndex) -> Unit

/**
 * Represents an interactive line chart with a time axis.
 *
 * Supports at most 10 visible [ValueAxisLayer]s at once.
 *
 * Shows the enum values at the bottom as strips.
 * The "default" content area is used for decimal values.
 * The enum values are painted using a [com.meistercharts.algorithms.layers.TransformingChartStateLayer].
 */
class TimeLineChartGestalt
@JvmOverloads constructor(
  val chartId: ChartId,
  /**
   * The data
   */
  val data: Data = Data(),
  styleConfiguration: Style.() -> Unit = {},
) : AbstractChartGestalt(), ChartGestalt {

  val style: Style = Style().also(styleConfiguration)

  /**
   * Configures the refresh rate of the chart
   */
  val chartRefreshGestalt: ChartRefreshGestalt = ChartRefreshGestalt()

  /**
   * THe physical tile size that is used by the tiles for this gestalt
   */
  private val physicalTileSize: @PhysicalPixel Size = Size.of(400.0, 400.0)

  /**
   * Is used to calculate the history render properties
   */
  val historyRenderPropertiesCalculatorLayer: HistoryRenderPropertiesCalculatorLayer = HistoryRenderPropertiesCalculatorLayer(
    samplingPeriodCalculator = MinDistanceSamplingPeriodCalculator(1.0).withMinimum { data.minimumSamplingPeriod },
    historyGapCalculator = { renderedSamplingPeriod ->
      data.historyGapCalculator.calculateMinGapDistance(renderedSamplingPeriod)
    },
    contentAreaTimeRange = { style.contentAreaTimeRange }
  )

  /**
   * The tile painter that is used
   */
  var tilePainter: CanvasTilePainter = createAverageHistoryCanvasTilePainter()

  /**
   * Creates a new instance of [HistoryCanvasTilePainter]
   */
  private fun createAverageHistoryCanvasTilePainter(): HistoryCanvasTilePainter = AverageHistoryCanvasTilePainter(
    AverageHistoryCanvasTilePainter.Configuration(
      historyStorage = data.historyStorage,
      contentAreaTimeRange = { style.contentAreaTimeRange },
      valueRanges = style::lineValueRanges.delegate(),
      visibleDecimalSeriesIndices = { style.actualVisibleDecimalSeriesIndices },
      lineStyles = style::lineStyles.delegate(),
      linePainters = MultiProvider.always(DirectLinePainter(snapXValues = false, snapYValues = false))
    )
  )

  /**
   * Creates a new instance of [HistoryCanvasTilePainter]
   */
  private fun createCandleHistoryCanvasTilePainter(): HistoryCanvasTilePainter = CandleHistoryCanvasTilePainter(
    CandleHistoryCanvasTilePainter.Configuration(
      historyStorage = data.historyStorage,
      contentAreaTimeRange = { style.contentAreaTimeRange },
      valueRanges = style::lineValueRanges.delegate(),
      visibleDecimalSeriesIndices = { style.actualVisibleDecimalSeriesIndices },
      lineStyles = style::lineStyles.delegate(),
      linePainters = MultiProvider.always(DirectLinePainter(snapXValues = false, snapYValues = false))
    )
  )

  /**
   * Configures this gestalt to show candles
   */
  fun configureForCandle() {
    tilePainter = createCandleHistoryCanvasTilePainter()
    historyRenderPropertiesCalculatorLayer.samplingPeriodCalculator = MinDistanceSamplingPeriodCalculator(3.0).withMinimum { data.minimumSamplingPeriod }
  }

  /**
   * The tile provider that is used to get the tiles
   */
  val tileProvider: CachedTileProvider = CanvasTileProvider(physicalTileSize, ::tilePainter.delegate()).cached(chartId)

  /**
   * The tiles layer that paints the tiles (for decimal values)
   */
  val tilesLayer: TilesLayer = TilesLayer(tileProvider)

  val tilesLayerTransformed: TransformingChartStateLayer<TilesLayer> = TransformingChartStateLayer(tilesLayer) { chartState ->
    viewportSupport.calculateDecimalsAreaChartState(chartState)
  }

  val valueAxisSupport: ValueAxisSupport<DecimalDataSeriesIndex> = ValueAxisSupport(valueRangeProvider = { dataSeriesIndex -> style.lineValueRanges.valueAt(dataSeriesIndex) }) {
    this.valueAxisConfiguration = { dataSeriesIndex, _, valueAxisTitleLocation ->
      side = Side.Left
      size = when (valueAxisTitleLocation) {
        AxisTitleLocation.AtValueAxis -> 100.0
        AxisTitleLocation.AtTop -> 80.0
      }
      tickOrientation = Vicinity.Inside
      paintRange = AxisStyle.PaintRange.Continuous

      ticksFormat = decimalFormat2digits //Apply the default
      titleProvider = { textService, i18nConfiguration -> data.historyConfiguration.decimalConfiguration.getDisplayName(dataSeriesIndex).resolve(textService, i18nConfiguration) }

      val colorProvider = { style.lineStyles.valueAt(dataSeriesIndex.value).color }
      lineColor = colorProvider
      tickLabelColor = colorProvider
      titleColor = colorProvider

      //Apply the callback from the style
      style.valueAxisStyleConfiguration.invoke(this, dataSeriesIndex)
    }

    this.topTitleLayerConfiguration = { dataSeriesIndex: DecimalDataSeriesIndex, layer: AxisTopTopTitleLayer ->
      //Apply the callback from the style
      style.valueAxisTopTitleStyleConfiguration.invoke(this, dataSeriesIndex)
    }
  }

  /**
   * Returns the value axis layer - creates a new instance if necessary
   */
  fun getValueAxisLayer(dataSeriesIndex: DecimalDataSeriesIndex): ValueAxisLayer {
    return valueAxisSupport.getAxisLayer(dataSeriesIndex)
  }

  /**
   * Returns the top title layer for the value axis
   */
  fun getValueAxisTopTitleLayer(dataSeriesIndex: DecimalDataSeriesIndex): AxisTopTopTitleLayer {
    return valueAxisSupport.getTopTitleLayer(dataSeriesIndex)
  }

  /**
   * Configures the thresholds
   */
  val thresholdsSupport: ThresholdsSupport<DecimalDataSeriesIndex> = valueAxisSupport.thresholdsSupport(
    thresholdValueProvider = data::thresholdValueProvider.delegate(),
    thresholdLabelProvider = data::thresholdLabelProvider.delegate(),
  ) {
    hudLayerConfiguration = { decimalDataSeriesIndex: DecimalDataSeriesIndex, axis: ValueAxisHudLayer ->
      val color = style.lineStyles.valueAt(decimalDataSeriesIndex).color
      axis.configuration.boxStyles = MultiProvider.always(BoxStyle(fill = Color.white, borderColor = color, radii = BorderRadius.all5))
      axis.configuration.textColors = MultiProvider.always(color)
    }
  }

  /**
   * Returns the hud layer for the given data series index
   */
  fun getHudLayer(dataSeriesIndex: DecimalDataSeriesIndex): ValueAxisHudLayer {
    return thresholdsSupport.getHudLayer(dataSeriesIndex)
  }

  /**
   * Delegates for the hud layers
   */
  private val hudLayersDelegate: MultipleLayersDelegatingLayer<ValueAxisHudLayer> = MultipleLayersDelegatingLayer(object : SizedProvider<ValueAxisHudLayer> {
    override fun size(): Int {
      return multiValueAxisLayer.paintingVariables().visibleAxisCount
    }

    /**
     * The index represents the nth visible data series
     */
    override fun valueAt(index: Int): ValueAxisHudLayer {
      val decimalDataSeriesIndex = style.actualVisibleValueAxesIndices.valueAt(index)
      return getHudLayer(decimalDataSeriesIndex)
    }
  })


  /**
   * Returns the threshold layer for the given data series index
   */
  fun getThresholdLinesLayer(dataSeriesIndex: DecimalDataSeriesIndex): DirectionalLinesLayer {
    return thresholdsSupport.getThresholdLinesLayer(dataSeriesIndex)
  }

  /**
   * Delegates for the threshold layers
   */
  private val thresholdLinesLayersDelegate: MultipleLayersDelegatingLayer<DirectionalLinesLayer> = MultipleLayersDelegatingLayer(object : SizedProvider<DirectionalLinesLayer> {
    override fun size(): Int {
      return multiValueAxisLayer.paintingVariables().visibleAxisCount
    }

    /**
     * The index represents the nth visible data series
     */
    override fun valueAt(index: Int): DirectionalLinesLayer {
      val decimalDataSeriesIndex = style.actualVisibleValueAxesIndices.valueAt(index)
      return getThresholdLinesLayer(decimalDataSeriesIndex)
    }
  })

  private fun visibleValueAxisCount(): Int {
    return style.actualVisibleValueAxesIndices.size()
  }

  /**
   * The layer that lays out and paints the value axis layers
   */
  val multiValueAxisLayer: MultiValueAxisLayer = MultiValueAxisLayer(
    valueAxesProvider = object : SizedProvider<ValueAxisLayer> {
      override fun size(): Int {
        return visibleValueAxisCount()
      }

      override fun valueAt(index: Int): ValueAxisLayer {
        val decimalDataSeriesIndex = style.actualVisibleValueAxesIndices.valueAt(index)
        return getValueAxisLayer(decimalDataSeriesIndex)
      }
    },
  ) {
    valueAxesGap = 10.0
    valueAxesMaxWidthPercentage = 1.0
    background = { style.valueAxesBackground }
  }

  /**
   * Contains the
   */
  val multiValueAxisTopTitleLayer: MultipleLayersDelegatingLayer<AxisTopTopTitleLayer> = MultipleLayersDelegatingLayer(object : SizedProvider<AxisTopTopTitleLayer> {
    override fun size(): Int {
      return visibleValueAxisCount()
    }

    override fun valueAt(index: Int): AxisTopTopTitleLayer {
      val decimalDataSeriesIndex = style.actualVisibleValueAxesIndices.valueAt(index)
      return valueAxisSupport.getTopTitleLayer(decimalDataSeriesIndex)
    }
  })

  /**
   * Shows the strips for the enum values
   */
  val historyEnumLayer: HistoryEnumLayer = HistoryEnumLayer(
    HistoryEnumLayer.Configuration(
      historyStorage = data.historyStorage,
      historyConfiguration = { data.historyConfiguration },
      visibleIndices = style::actualVisibleEnumSeriesIndices.delegate(),
      contentAreaTimeRange = {
        style.contentAreaTimeRange
      },
    )
  ) {
    layoutDirection = LayoutDirection.CenterVertical
  }

  /**
   * Compartment layer that moves the content area of this layer to the bottom
   */
  val historyEnumLayerTransformed: TransformingChartStateLayer<HistoryEnumLayer> = TransformingChartStateLayer(historyEnumLayer) { originalChartState ->
    viewportSupport.calculateEnumsAreaChartState(originalChartState)
  }

  /**
   * Paints the axis for the enum category.
   *
   * ATTENTION: This layer only works with *visible* sizes/providers.
   */
  val enumCategoryAxisLayer: CategoryAxisLayer = CategoryAxisLayer(
    CategoryAxisLayer.Data(
      labelsProvider = object : SizedLabelsProvider {
        override fun size(param1: TextService, param2: I18nConfiguration): Int {
          return historyEnumLayer.configuration.visibleIndices.size()
            .coerceAtMost(data.historyConfiguration.enumDataSeriesCount)
        }

        override fun valueAt(index: Int, textService: TextService, i18nConfiguration: I18nConfiguration): String {
          val dataSeriesIndex: EnumDataSeriesIndex = this@TimeLineChartGestalt.style.actualVisibleEnumSeriesIndices.valueAt(index)
          val labelTextKey = this@TimeLineChartGestalt.style.enumCategoryAxisLabelProvider.valueAt(dataSeriesIndex)
          return labelTextKey.resolve(textService, i18nConfiguration)
        }
      },
      layoutProvider = {
        historyEnumLayer.paintingVariables().stripesLayout
      }
    ),
  ) {
    side = Side.Left
    //titleProvider = "Enums".asProvider2() //title is necessary to align the layout with the value axis
    tickOrientation = Vicinity.Outside
    axisEndConfiguration = AxisEndConfiguration.Default
    paintRange = AxisStyle.PaintRange.ContentArea
    background = {
      style.valueAxesBackground
    }
    axisLabelPainter = DefaultCategoryAxisLabelPainter {
      wrapMode = LabelWrapMode.IfNecessary
    }
    axisLabelPainter = DefaultCategoryAxisLabelPainter {
      wrapMode = LabelWrapMode.IfNecessary
    }

    hideTicks()
    tickLength = 5.0
    //hideAxis()
  }

  /**
   * Compartment layer that moves the content area of this layer to the bottom
   */
  val enumCategoryAxisLayerTransformed: TransformingChartStateLayer<CategoryAxisLayer> = TransformingChartStateLayer(
    enumCategoryAxisLayer
  ) { originalChartState ->
    viewportSupport.calculateEnumsAreaChartState(originalChartState)
  }

  /**
   * Paints a horizontal line between the tiles layer (with decimal values) and the enums layer
   */
  val horizontalLineBetweenEnumAndDecimalsLayer: AbstractLayer = object : AbstractLayer() {
    override val type: LayerType = LayerType.Content

    override fun paint(paintingContext: LayerPaintingContext) {
      //Paint horizontal line
      val gc = paintingContext.gc
      gc.stroke(enumCategoryAxisLayer.style.lineColor())
      val y = gc.height - viewportSupport.decimalsAreaViewportMarginBottom()
      gc.strokeLine(0.0, y, gc.width, y)
    }
  }

  /**
   * The time axis layer
   */
  val timeAxisLayer: TimeAxisLayer = TimeAxisLayer {
    side = Side.Bottom
  }

  /**
   * Calculates the view ports for the different areas
   */
  val viewportSupport: TimelineChartViewportSupport = TimelineChartViewportSupport()

  /**
   * Calculates the different viewport (margins) related to timeline chart
   */
  inner class TimelineChartViewportSupport {
    /**
     * Creates the chart state that is used for the enum layers.
     *
     * This method is both used for [historyEnumLayer] ([historyEnumLayerTransformed]) and [enumCategoryAxisLayer] (enumCategoryAxisCompartmentLayer)
     */
    fun calculateEnumsAreaChartState(originalChartState: ChartState): ChartState {
      @Zoomed val windowSize = originalChartState.windowSize

      @Zoomed val enumsLayerHeight = totalHeightRequiredForEnumsLayer()

      return originalChartState.withContentAreaSize(originalChartState.contentAreaSize.withHeight(enumsLayerHeight)).withTranslation(originalChartState.windowTranslation.withY(viewportSupport.enumAreaViewportMarginTop(windowSize.height)))
        .withZoom(originalChartState.zoom.withY(1.0)) //horizontal zoom is kept, vertical zoom hard coded to 1.0

      //TODO add content view port!
    }

    /**
     * Creates a new chart state for the decimals area
     */
    fun calculateDecimalsAreaChartState(originalChartState: ChartState): ChartState {
      val updatedViewportMargin = originalChartState.contentViewportMargin.withBottom(decimalsAreaViewportMarginBottom())
      return originalChartState.withContentViewportMargin(updatedViewportMargin)
    }

    /**
     * Calculates the enums area bottom insets
     */
    fun enumsAreaViewportMarginBottom(): @Zoomed Double {
      return if (style.showTimeAxis) {
        style.timeAxisSize + timeAxisLayer.style.margin.bottom
      } else {
        0.0
      }
    }

    /**
     * Calculates the top margin of the viewport of the enum area (the y value at the top of the enum area)
     */
    fun enumAreaViewportMarginTop(
      windowHeight: @MayBeZero @Zoomed Double,
    ): @Zoomed Double {
      return windowHeight - enumsAreaViewportMarginBottom() - totalHeightRequiredForEnumsLayer()
    }


    /**
     * The margin for the viewport of the decimals area
     */
    fun enumsAreaViewportMargin(windowHeight: @MayBeZero @Zoomed Double): @Zoomed Insets {
      return Insets.of(enumAreaViewportMarginTop(windowHeight), 0.0, enumsAreaViewportMarginBottom(), 0.0)
    }

    /**
     * Margin for the viewport of the content area for the decimal values (and value axis)
     *
     * This insets contain:
     * - time axis
     * - the additional "luft" for the enum layer (if there is any)
     */
    fun decimalsAreaViewportMarginBottom(): @Zoomed Double {
      return enumsAreaViewportMarginBottom() + totalHeightRequiredForEnumsLayer()
    }

    /**
     * The margin for the decimals area viewport
     */
    fun decimalsAreaViewportMargin(): @Zoomed Insets {
      return contentViewportMargin.withBottom(decimalsAreaViewportMarginBottom())
    }

    /**
     * Only clips below - to avoid painting into the other layers
     */
    fun decimalsAreaViewportClipMargin(): @Zoomed Insets {
      return Insets.onlyBottom(decimalsAreaViewportMarginBottom())
    }
  }

  /**
   * Calculates the total height required for the enums layer.
   * Also contains the insets ([Style.enumLayerInsets])
   *
   * The returned value depends on the available and visible enum series
   */
  private fun totalHeightRequiredForEnumsLayer(): @Zoomed Double {
    val visibleEnumsCount = style.actualVisibleEnumSeriesIndices.size()
    @Zoomed val netEnumLayerHeight = historyEnumLayer.configuration.calculateTotalHeight(visibleEnumsCount)
    if (netEnumLayerHeight == 0.0) {
      //Do not add the gap if the enum layer is not visible
      return 0.0
    }

    return netEnumLayerHeight + style.enumLayerInsets.offsetHeight
  }

  /**
   * Provides the cross wire labels - for the decimal values
   */
  private val crossWireDecimalValuesLabelsProvider = object : CrossWireLayer.ValueLabelsProvider {
    /**
     * Contains the y locations for each label.
     *
     * This size of this cache is used to get the number of labels
     */
    val locationsYCache = @Window DoubleCache()

    /**
     * Contains the domain values (that are later used for formatting)
     */
    @Deprecated("Not used???")
    val domainValuesCache = @Domain DoubleCache()

    /**
     * The label texts
     */
    val labelsCache = StringCache()

    /**
     * Contains the box style for the label
     */
    val boxStylesCache = ObjectCache(BoxStyle.modernBlue)

    /**
     * Contains the label text colors
     */
    val labelTextColorCache = ObjectCache<Color>(Color.pink)

    override fun layout(wireLocation: @Window Double, paintingContext: LayerPaintingContext) {
      val visibleLinesCount = style.actualVisibleDecimalSeriesIndices.size()
      prepare(visibleLinesCount)

      //The labels should show the value of the visible lines at the cross wire position x.
      //If there are no visible line we do not show any labels.
      if (visibleLinesCount == 0) {
        return
      }

      val chartSupport = paintingContext.chartSupport
      val chartCalculator = chartSupport.chartCalculator
      val timeChartCalculator = chartSupport.timeChartCalculator(style.contentAreaTimeRange)


      //retrieve the history buckets for the timestamp at the cross wire position
      @ms val timeStampUnderCrossWire = timeChartCalculator.window2timeX(wireLocation)

      //The max time until a value is interpreted as gap
      //TODO different gap for cross wire and lines
      @ms val minGapSize = chartSupport.paintingProperties.retrieve(PaintingPropertyKey.MinGapDistance)

      //We look a little to the left to ensure there are some values visible
      @ms val start = timeStampUnderCrossWire - minGapSize
      //TODO add some values to end, too?
      @ms val end = timeStampUnderCrossWire

      //The sampling period that is used for the currently visible tiles
      @ms val samplingPeriod = chartSupport.paintingProperties.retrieve(PaintingPropertyKey.SamplingPeriod)

      //TODO add some kind of caching(?)
      //Use the same sampling period as the tiles visualize, to ensure the cross wire labels have the same values as the painted lines
      val historyBuckets = data.historyStorage.query(start, end, samplingPeriod)
      if (historyBuckets.isEmpty()) {
        return clearLabels()
      }

      //We have to find the best time stamp / value for end.
      val searchResult = historyBuckets.search(end, AndBefore(minGapSize)) ?: return clearLabels()


      val historyConfiguration = data.historyConfiguration

      //Calculate the y location
      style.actualVisibleDecimalSeriesIndices.fastForEachIndexed { index, dataSeriesIndex ->
        //Find the value for this at the given location
        @Domain val valueAtCrossWire = searchResult.chunk.getDecimalValue(dataSeriesIndex, searchResult.timeStampIndex)
        domainValuesCache[index] = valueAtCrossWire

        @DomainRelative val relativeValueAtCrossWire = style.lineValueRanges.valueAt(dataSeriesIndex.value).toDomainRelative(valueAtCrossWire)
        locationsYCache[index] = chartCalculator.domainRelative2windowY(relativeValueAtCrossWire)

        val formatForLabel = style.crossWireDecimalFormat.valueAt(dataSeriesIndex) ?: decimalFormat()
        labelsCache[index] = formatForLabel.format(valueAtCrossWire)

        //Update the formats
        boxStylesCache[index] = style.crossWireDecimalsLabelBoxStyles.valueAt(dataSeriesIndex)
        labelTextColorCache[index] = style.crossWireDecimalsLabelTextColors.valueAt(dataSeriesIndex)
      }
    }

    /**
     * Clears all labels
     */
    private fun clearLabels() {
      prepare(0)
    }

    private fun prepare(visibleLinesCount: Int) {
      locationsYCache.prepare(visibleLinesCount)
      domainValuesCache.prepare(visibleLinesCount)
      labelsCache.prepare(visibleLinesCount)
      boxStylesCache.prepare(visibleLinesCount)
      labelTextColorCache.prepare(visibleLinesCount)
    }

    override fun locationAt(index: Int): Double {
      return locationsYCache[index]
    }

    override fun labelAt(index: Int, textService: TextService, i18nConfiguration: I18nConfiguration): String {
      return labelsCache[index]
    }

    override fun size(): Int {
      return locationsYCache.size
    }
  }

  /**
   * The cross wire layer for the decimal values
   */
  val crossWireLayerDecimalValues: CrossWireLayer = CrossWireLayer(
    CrossWireLayer.Data(
      valueLabelsProvider = crossWireDecimalValuesLabelsProvider,
      currentLocationLabelTextProvider = { paintingContext: LayerPaintingContext, crossWireLocation: @Window Double ->
        val chartCalculator = paintingContext.chartCalculator
        val time = chartCalculator.window2timeX(crossWireLocation, style.contentAreaTimeRange)
        style.currentPositionLabelFormat.format(time, paintingContext.i18nConfiguration)
      })
  ) {
    valueLabelPlacementStrategy = LabelPlacementStrategy.preferOnRightSide { 150.0 }
    wireWidth = 1.0
    showValueLabels = true
    showCurrentLocationLabel = false

    valueLabelsEnd = { it.height - viewportSupport.decimalsAreaViewportMarginBottom() } //keep the labels within the decimals area

    locationX = {
      it.chartCalculator.windowRelative2WindowX(style.crossWirePositionX)
    }

    valueLabelBoxStyle = MultiProvider.invoke { labelIndex: @LabelIndex Int ->
      crossWireDecimalValuesLabelsProvider.boxStylesCache[labelIndex]
    }
    valueLabelTextColor = MultiProvider.invoke { labelIndex: @LabelIndex Int ->
      crossWireDecimalValuesLabelsProvider.labelTextColorCache[labelIndex]
    }
  }

  /**
   * Required as field to be able to access the painting properties
   */
  private val crossWireEnumValuesLabelsProvider = object : CrossWireLayer.ValueLabelsProvider {
    var windowHeight: @Zoomed Double = 0.0
    var enumAreaViewportMarginTop: @Window Double = Double.NaN

    /**
     * Contains the y locations for each label.
     *
     * This size of this cache is used to get the number of labels
     */
    val locationsYCache: @Window @MayBeNaN DoubleCache = DoubleCache()

    /**
     * Contains the history enums at the given location
     */
    val historyEnumsCache = ObjectCache(HistoryEnum.Boolean)

    /**
     * The enum set at the cross wire
     */
    val valuesAtCrossWireCache: @HistoryEnumSetInt IntCache = IntCache()

    /**
     * The translated labels
     */
    val labelsCache = StringCache()

    /**
     * Contains the box style for the label
     */
    val boxStylesCache = ObjectCache(BoxStyle.modernBlue)

    /**
     * Contains the label text colors
     */
    val labelTextColorCache = ObjectCache<Color>(Color.pink)

    /**
     * Clears all labels
     */
    private fun clearLabels() {
      prepare(0)
    }

    private fun prepare(visibleLinesCount: Int) {
      locationsYCache.prepare(visibleLinesCount)
      labelsCache.prepare(visibleLinesCount)
      valuesAtCrossWireCache.prepare(visibleLinesCount)
      historyEnumsCache.prepare(visibleLinesCount)
      boxStylesCache.prepare(visibleLinesCount)
      labelTextColorCache.prepare(visibleLinesCount)
    }

    override fun layout(wireLocation: @Window Double, paintingContext: LayerPaintingContext) {
      val chartCalculator = paintingContext.chartCalculator
      val chartSupport = paintingContext.chartSupport
      val textService = paintingContext.chartSupport.textService
      val i18nConfiguration = paintingContext.chartSupport.i18nConfiguration
      val timeChartCalculator = chartSupport.timeChartCalculator(style.contentAreaTimeRange)

      @Time @ms val timeStampUnderCrossWire = timeChartCalculator.window2timeX(wireLocation)


      windowHeight = paintingContext.height
      enumAreaViewportMarginTop = viewportSupport.enumAreaViewportMarginTop(windowHeight)

      prepare(style.actualVisibleEnumSeriesIndices.size()) //prepare for the max number

      //The max time until a value is interpreted as gap
      //TODO different gap for cross wire and lines
      @ms val minGapSize = chartSupport.paintingProperties.retrieve(PaintingPropertyKey.MinGapDistance)

      //We look a little to the left to ensure there are some values visible
      @ms val start = timeStampUnderCrossWire - minGapSize
      //TODO add some values to end, too?
      @ms val end = timeStampUnderCrossWire

      //The sampling period that is used for the currently visible tiles
      @ms val samplingPeriod = chartSupport.paintingProperties.retrieve(PaintingPropertyKey.SamplingPeriod)

      //TODO add some kind of caching(?)
      //Use the same sampling period as the tiles visualize, to ensure the cross wire labels have the same values as the painted lines
      val historyBuckets = data.historyStorage.query(start, end, samplingPeriod)
      if (historyBuckets.isEmpty()) {
        return clearLabels()
      }

      //We have to find the best time stamp / value for end.
      val searchResult = historyBuckets.search(end, AndBefore(minGapSize)) ?: return clearLabels()

      val historyConfiguration = data.historyConfiguration
      //endregion

      val historyEnumPaintingProperties = historyEnumLayer.paintingVariables()
      val layout = historyEnumPaintingProperties.stripesLayout

      style.actualVisibleEnumSeriesIndices.fastForEachIndexed(maxSize = data.historyConfiguration.enumDataSeriesCount) { visibleSeriesIndex, dataSeriesIndex ->
        //Find the value for this at the given location
        @MayBeNoValueOrPending val valueAtCrossWire: HistoryEnumSet = searchResult.chunk.getEnumValue(dataSeriesIndex, searchResult.timeStampIndex)
        if (valueAtCrossWire.isNoValue() || valueAtCrossWire.isPending()) {
          //Skip no value
          return@fastForEachIndexed
        }

        valuesAtCrossWireCache[visibleSeriesIndex] = valueAtCrossWire.bitset

        @Zoomed val center = layout.calculateCenter(BoxIndex(visibleSeriesIndex))
        locationsYCache[visibleSeriesIndex] = center + enumAreaViewportMarginTop

        val historyEnum: HistoryEnum = historyConfiguration.enumConfiguration.getEnum(dataSeriesIndex)
        historyEnumsCache[visibleSeriesIndex] = historyEnum

        val firstSetOrdinal = valueAtCrossWire.firstSetOrdinal()
        val firstValue = historyEnum.value(firstSetOrdinal)
        labelsCache[visibleSeriesIndex] = firstValue.key.resolve(textService, i18nConfiguration)

        //Update the formats
        boxStylesCache[visibleSeriesIndex] = style.crossWireEnumsLabelBoxStyles.valueAt(dataSeriesIndex).withFillIfNull {
          guessFillColor(dataSeriesIndex, firstSetOrdinal, historyEnum)
        }

        labelTextColorCache[visibleSeriesIndex] = style.crossWireEnumsLabelTextColors.valueAt(dataSeriesIndex)
      }
    }

    /**
     * Guesses the fill color for the given data series index and values
     */
    private fun guessFillColor(
      dataSeriesIndex: EnumDataSeriesIndex,
      firstSetOrdinal: HistoryEnumOrdinal,
      historyEnum: HistoryEnum,
    ): Color {
      //Get the painter and "guess" the type
      val painter = historyEnumLayer.configuration.enumStripePainter.valueAt(dataSeriesIndex)

      return if (painter is RectangleEnumStripePainter) {
        painter.configuration.fillProvider(firstSetOrdinal, historyEnum)
      } else {
        Color.silver
      }
    }

    override fun locationAt(index: Int): @Window @MayBeNaN Double {
      return locationsYCache[index]
    }

    override fun labelAt(index: Int, textService: TextService, i18nConfiguration: I18nConfiguration): String {
      return labelsCache[index]
    }

    override fun size(): Int {
      return locationsYCache.size
    }
  }

  /**
   * Cross wire layer for the enum values
   */
  val crossWireLayerEnumValues: CrossWireLayer = CrossWireLayer(
    CrossWireLayer.Data(
      valueLabelsProvider = crossWireEnumValuesLabelsProvider,
      currentLocationLabelTextProvider = { paintingContext: LayerPaintingContext, crossWireLocation: @Window Double ->
        val chartCalculator = paintingContext.chartCalculator
        val time = chartCalculator.window2timeX(crossWireLocation, style.contentAreaTimeRange)
        style.currentPositionLabelFormat.format(time, paintingContext.i18nConfiguration)
      },
    )
  ) {
    valueLabelPlacementStrategy = LabelPlacementStrategy.preferOnRightSide { 150.0 }
    wireWidth = 1.0
    showValueLabels = true
    showCurrentLocationLabel = false

    locationX = {
      it.chartCalculator.windowRelative2WindowX(style.crossWirePositionX)
    }

    valueLabelsStart = {
      viewportSupport.enumAreaViewportMarginTop(it.height)
    }
    valueLabelsEnd = {
      it.height - viewportSupport.enumsAreaViewportMarginBottom()
    }

    valueLabelBoxStyle = MultiProvider.invoke { index: @LabelIndex Int ->
      //The provided box style
      crossWireEnumValuesLabelsProvider.boxStylesCache[index]
    }

    valueLabelTextColor = MultiProvider.invoke { labelIndex: @LabelIndex Int ->
      crossWireEnumValuesLabelsProvider.labelTextColorCache[labelIndex]
    }
  }

  /**
   * Sets the insets of the TranslateOverTimeService in accordance with the position of the cross wire along the x-axis
   */
  private fun updateTranslateOverTime(chartSupport: ChartSupport) {
    @Zoomed val insetsRight = chartSupport.currentChartState.windowSize.width * (1.0 - style.crossWirePositionX)
    chartSupport.translateOverTime.insets = Insets.onlyRight(insetsRight)
  }

  init {
    data.minimumSamplingPeriodProperty.consumeImmediately {
      //adjust the content area in order to display about 600 samples
      style.applySamplingPeriod(it)
    }

    style.contentAreaTimeRangeProperty.consumeImmediately {
      timeAxisLayer.data.contentAreaTimeRange = it
      tileProvider.clear()
    }

    style.requestedVisibleDecimalSeriesIndicesProperty.consume {
      tileProvider.clear()
    }

    style.lineStylesProperty.consumeImmediately {
      tileProvider.clear()
    }

    style.requestedVisibleValueAxesIndicesProperty.consumeImmediately {
      //updateValueAxisLayers()
    }

    style.timeAxisSizeProperty.consumeImmediately {
      timeAxisLayer.style.size = it
      //updateValueAxisLayers()
    }

    style.showTimeAxisProperty.consumeImmediately {
      //The margins used for the value axes depends on the visibility of the time axis
      //TODO
      //updateValueAxisLayers()
    }

    data.historyGapCalculatorProperty.consume {
      tileProvider.clear()
    }

    //Apply the configuration again - when it is updated
    style.valueAxisStyleConfigurationProperty.consume { configuration ->
      valueAxisSupport.foreachAxisLayer { decimalDataSeriesIndex, valueAxisLayer ->
        configuration(valueAxisLayer.style, decimalDataSeriesIndex)
      }
    }

    style.valueAxisTopTitleStyleConfigurationProperty.consume { configuration ->
      valueAxisSupport.foreachTopTitleLayer { decimalDataSeriesIndex, layer ->
        configuration(layer.configuration, decimalDataSeriesIndex)
      }
    }
  }

  /**
   * The viewport for the complete diagram.
   * View port does *not* contain:
   * - space at top (e.g. for title)
   * - space at bottom for time axis
   */
  private val contentViewportGestalt = ContentViewportGestalt(Insets.of(25.0, 0.0, 0.0, 0.0))

  /**
   * The content viewport margin
   */
  var contentViewportMargin: Insets by contentViewportGestalt::contentViewportMargin

  init {
    configureBuilder { meisterChartBuilder ->
      chartRefreshGestalt.configure(meisterChartBuilder)

      contentViewportGestalt.configure(meisterChartBuilder)

      with(meisterChartBuilder) {
        configureAsTimeChart()
        configureAsTiledTimeChart()

        zoomAndTranslationDefaults {
          DelegatingZoomAndTranslationDefaults(
            MoveDomainValueToLocation(
              domainRelativeValueProvider = { style.contentAreaTimeRange.time2relative(nowMillis()) },
              targetLocationProvider = { chartCalculator -> chartCalculator.windowRelative2WindowX(style.crossWirePositionX) }
            ),
            FittingWithMargin { viewportSupport.decimalsAreaViewportMargin() }
          )
        }

        zoomAndTranslationModifier {
          minZoom(0.00001, 0.000001) //the x zoom works with 24 hours and 1 millis for the applied sampling rate
          maxZoom(16.0, 500.0)
        }

        configure {
          chartSupport.devicePixelRatioSupport.devicePixelRatioProperty.consume {
            //Delete the tiles on device pixel ratio change
            tileProvider.clear()
          }

          chartSupport.rootChartState.windowSizeProperty.consumeImmediately {
            updateTranslateOverTime(chartSupport)
          }
          style.crossWirePositionXProperty.consumeImmediately {
            updateTranslateOverTime(chartSupport)
          }

          chartSupport.rootChartState.contentAreaSizeProperty.consume {
            tileProvider.clear()
          }
          chartSupport.rootChartState.axisOrientationXProperty.consume {
            tileProvider.clear()
          }
          chartSupport.rootChartState.axisOrientationYProperty.consume {
            tileProvider.clear()
          }

          style.contentAreaTimeRangeProperty.consumeImmediately {
            chartSupport.translateOverTime.contentAreaTimeRangeX = it
          }

          style.lineValueRangesProperty.consume {
            tileProvider.clear()
          }

          // So far a gestalt does not call markAsDirty by itself. This is always done the
          // client that uses the gestalt. However, this automatic set-up might be useful
          // for every client.
          val tileInvalidator: HistoryTileInvalidator = DefaultHistoryTileInvalidator()
          (data.historyStorage as? ObservableHistoryStorage)?.observe { _, updateInfo ->
            val validationResult = tileInvalidator.historyHasBeenUpdated(updateInfo, tileProvider.canvasTiles(), chartSupport)

            if (validationResult == HistoryTilesInvalidationResult.TilesInvalidated) {
              markAsDirty()
            }
          }

          layers.addClearBackground()
          layers.addLayer(historyRenderPropertiesCalculatorLayer)

          layers.addLayer(tilesLayerTransformed)

          layers.addLayer(historyEnumLayerTransformed)

          @PaintingOrder val valueAxesLayerIndex = layers.addLayer(multiValueAxisLayer.clipped {
            //Do not paint behind the enum layer
            viewportSupport.decimalsAreaViewportClipMargin()
          })

          layers.addLayer(multiValueAxisTopTitleLayer.visibleIf { valueAxisSupport.preferredAxisTitleLocation == AxisTitleLocation.AtTop })

          @PaintingOrder val hudLayersDelegateIndex = layers.addLayer(hudLayersDelegate.clipped { viewportSupport.decimalsAreaViewportClipMargin() })
          layers.addLayerAt(thresholdLinesLayersDelegate.clipped { viewportSupport.decimalsAreaViewportClipMargin() }, valueAxesLayerIndex, hudLayersDelegateIndex + 1)


          //Must be painted above the value axis - because of the background
          layers.addLayer(enumCategoryAxisLayerTransformed)
          layers.addLayer(horizontalLineBetweenEnumAndDecimalsLayer.visibleIf {
            //Paint only if the enums layer is visible
            totalHeightRequiredForEnumsLayer() > 0.0
          })

          layers.addLayer(timeAxisLayer.visibleIf(style.showTimeAxisProperty))
          layers.addLayer(crossWireLayerDecimalValues.clipped {
            //Do not paint behind the enum layer
            viewportSupport.decimalsAreaViewportClipMargin()
          }.visibleIf(style.showCrossWireProperty))

          layers.addLayer(crossWireLayerEnumValues.clipped {
            //Do not paint behind the decimals and timeline layer
            viewportSupport.enumsAreaViewportMargin(it.height)
          }.visibleIf(style.showCrossWireProperty))

          layers.addTilesDebugLayer(chartSupport.debug)

          layers.addVersionNumberHidden()
        }
      }
    }
  }

  class Data(
    /**
     * The history storage this chart is based on
     */
    val historyStorage: HistoryStorage = InMemoryHistoryStorage(),

    /**
     * The history configuration for the gestalt
     */
    historyConfiguration: HistoryConfiguration = HistoryConfiguration.empty,
  ) {
    /**
     * Used to calculate gaps
     */
    val historyGapCalculatorProperty: ObservableObject<HistoryGapCalculator> = ObservableObject(DefaultHistoryGapCalculator())
    var historyGapCalculator: HistoryGapCalculator by historyGapCalculatorProperty

    /**
     * The smallest sampling period that is used when creating the tiles
     */
    val minimumSamplingPeriodProperty: ObservableObject<SamplingPeriod> = ObservableObject(defaultMinimumSamplingPeriod)
    var minimumSamplingPeriod: SamplingPeriod by minimumSamplingPeriodProperty

    /**
     * The history configuration that is used to store and access the history
     */
    val historyConfigurationProperty: ObservableObject<HistoryConfiguration> = ObservableObject(historyConfiguration)
    var historyConfiguration: HistoryConfiguration by historyConfigurationProperty


    //Thresholds

    /**
     * Provides the threshold values for *each* data series
     * The size will be interpreted as [HudElementIndex]
     */
    var thresholdValueProvider: @Domain DoublesProvider1<DecimalDataSeriesIndex> = DoublesProvider1.empty()

    /**
     * Provides the labels for each threshold.
     * The parameter identifies the decimal data series. The index corresponds to [HudElementIndex] - depending on the size of [thresholdValueProvider]
     */
    var thresholdLabelProvider: @Domain MultiProvider2<HudElementIndex, List<String>, DecimalDataSeriesIndex, LayerPaintingContext> = MultiProvider2.empty()
  }

  @StyleDsl
  inner class Style {
    /**
     * Value axis style configuration - is called when a new value axis is instantiated
     */
    var valueAxisStyleConfigurationProperty: ObservableObject<ValueAxisStyleConfiguration> = ObservableObject { _, _ -> }
    var valueAxisStyleConfiguration: ValueAxisStyleConfiguration by valueAxisStyleConfigurationProperty

    var valueAxisTopTitleStyleConfigurationProperty: ObservableObject<ValueAxisTopTitleStyleConfiguration> = ObservableObject { _, _ -> }
    var valueAxisTopTitleStyleConfiguration: ValueAxisTopTitleStyleConfiguration by valueAxisTopTitleStyleConfigurationProperty

    /**
     * Provides the labels for enum categories
     */
    var enumCategoryAxisLabelProvider: MultiProvider<EnumDataSeriesIndex, TextKey> = MultiProvider.invoke { dataSeriesIndexAsInt ->
      val dataSeriesIndex = EnumDataSeriesIndex(dataSeriesIndexAsInt)

      //The default implementation returns the display name from the history configuration
      data.historyConfiguration.enumConfiguration.getDisplayName(dataSeriesIndex)
    }

    /**
     * Formats the current position label
     */
    var currentPositionLabelFormat: DateTimeFormat = TimeFormatWithMillis()

    /**
     * The duration of the content area
     *
     * @see contentAreaTimeRangeProperty
     */
    val contentAreaDurationProperty: ObservableObject<@ms @Positive Double> = ObservableObject(1000.0 * 60).also {
      it.consume { updatedValue ->
        require(updatedValue.isPositive()) {
          "Value must be positive but was $updatedValue"
        }
      }
    }
    var contentAreaDuration: @ms @Positive Double by contentAreaDurationProperty

    /**
     * The time range of the content area
     *
     * @see contentAreaDurationProperty
     */
    val contentAreaTimeRangeProperty: ReadOnlyObservableObject<TimeRange> = contentAreaDurationProperty.map { duration -> TimeRange.fromStartAndDuration(TimeConstants.referenceTimestamp, duration) }
    val contentAreaTimeRange: TimeRange by contentAreaTimeRangeProperty

    /**
     * Provides the value range for a line of this chart; the parameter is the index of the line
     */
    val lineValueRangesProperty: ObservableObject<MultiProvider<DecimalDataSeriesIndex, ValueRange>> = ObservableObject(MultiProvider.always(ValueRange.default))
    var lineValueRanges: MultiProvider<DecimalDataSeriesIndex, ValueRange> by lineValueRangesProperty

    /**
     * The style for each line (decimal values).
     * The index parameter corresponds to the index returned by [requestedVisibleValueAxesIndices]
     */
    val lineStylesProperty: ObservableObject<MultiProvider<DecimalDataSeriesIndex, LineStyle>> = ObservableObject(
      MultiProvider {
        LineStyle(Theme.chartColors().valueAt(it), 1.0, null)
      }
    )
    var lineStyles: MultiProvider<DecimalDataSeriesIndex, LineStyle> by lineStylesProperty

    /**
     * The indices of the lines whose value axis should be visible.
     *
     * Attention: Might contain *more* elements than there exist in the history
     *
     * Use [actualVisibleValueAxesIndices] to get the current value.
     */
    var requestedVisibleValueAxesIndicesProperty: ObservableObject<DecimalDataSeriesIndexProvider> = ObservableObject(DecimalDataSeriesIndexProvider.indices { 1 })

    /**
     * Use [actualVisibleValueAxesIndices] to get the current value.
     */
    var requestedVisibleValueAxesIndices: DecimalDataSeriesIndexProvider by requestedVisibleValueAxesIndicesProperty
      @Deprecated("Do not read! Use actualVisibleValueAxesIndices instead", level = DeprecationLevel.WARNING)
      get

    /**
     * Contains the *actual* visible indices.
     * Respects the current decimal data series count.
     */
    val actualVisibleValueAxesIndices: DecimalDataSeriesIndexProvider = ::requestedVisibleValueAxesIndices.atMost {
      data.historyConfiguration.decimalDataSeriesCount
    }

    /**
     * The background color of the value axes
     */
    var valueAxesBackground: Color = Color.web("rgba(255,255,255,0.5)")

    /**
     * The indices of the lines that should be visible.
     * The default is that all lines are visible.
     *
     * ATTENTION: in init{} [showAllDecimalSeries] is called
     *
     * ATTENTION: Might contain *more* elements than there exist in the history!
     */
    val requestedVisibleDecimalSeriesIndicesProperty: ObservableObject<DecimalDataSeriesIndexProvider> = ObservableObject(DecimalDataSeriesIndexProvider.empty())

    var requestedVisibleDecimalSeriesIndices: DecimalDataSeriesIndexProvider by requestedVisibleDecimalSeriesIndicesProperty
      @Deprecated("Do not read! Use actualVisibleDecimalSeriesIndices instead", level = DeprecationLevel.WARNING)
      get

    /**
     * The actual visible decimal series indices - respects the current history configuration
     */
    val actualVisibleDecimalSeriesIndices: DecimalDataSeriesIndexProvider = ::requestedVisibleDecimalSeriesIndices.atMost {
      data.historyConfiguration.decimalDataSeriesCount
    }

    /**
     * Shows all lines even if the history configuration changes later on.
     */
    fun showAllDecimalSeries() {
      requestedVisibleDecimalSeriesIndices = DecimalDataSeriesIndexProvider.indices { data.historyConfiguration.decimalDataSeriesCount }
    }

    /**
     * The indices of the enums that are visible (one stripe is shown for each visible enum series).
     *
     * The enums series are visualized using the [TimeLineChartGestalt.historyEnumLayer].
     *
     * ATTENTION: Might contain *more* elements than there exist in the history!
     */
    val requestedVisibleEnumSeriesIndicesProperty: ObservableObject<EnumDataSeriesIndexProvider> = ObservableObject(EnumDataSeriesIndexProvider.empty())

    var requestVisibleEnumSeriesIndices: EnumDataSeriesIndexProvider by requestedVisibleEnumSeriesIndicesProperty
      @Deprecated("Do not read! Use actualVisibleDecimalSeriesIndices instead", level = DeprecationLevel.WARNING)
      get

    val actualVisibleEnumSeriesIndices: EnumDataSeriesIndexProvider = ::requestVisibleEnumSeriesIndices.atMost {
      data.historyConfiguration.enumDataSeriesCount
    }

    /**
     * Shows all stripes - even if the history configuration is changed later
     */
    fun showAllEnumSeries() {
      requestVisibleEnumSeriesIndices = EnumDataSeriesIndexProvider.indices { data.historyConfiguration.enumDataSeriesCount }
    }

    /**
     * Shows at most the given number of enum series
     */
    fun showEnumSeriesAtMost(maxCount: Int) {
      requestVisibleEnumSeriesIndices = EnumDataSeriesIndexProvider.indices { maxCount.coerceAtMost(data.historyConfiguration.enumDataSeriesCount) }
    }

    /**
     * The size of the time axis
     */
    val timeAxisSizeProperty: ObservableObject<@Zoomed Double> = ObservableObject(60.0)
    var timeAxisSize: @Zoomed Double by timeAxisSizeProperty


    /**
     * The insets of the enum layer (*within* the layer)
     */
    var enumLayerInsets: @Zoomed Insets = Insets.of(10.0, 0.0, 10.0, 0.0)

    /**
     * Whether to show the time axis
     */
    val showTimeAxisProperty: ObservableBoolean = ObservableBoolean(true)
    var showTimeAxis: Boolean by showTimeAxisProperty

    /**
     * Configures this chart to visualize the value axis title on top of the value axis
     */
    fun applyValueAxisTitleOnTop(contentAreaMarginTop: @px Double = 40.0) {
      valueAxisSupport.preferredAxisTitleLocation = AxisTitleLocation.AtTop
      contentViewportMargin = contentViewportMargin.withTop(contentViewportMargin.top.coerceAtLeast(contentAreaMarginTop))
    }

    /**
     * Whether the cross wire layer (for decimals and enums) is visible.
     */
    val showCrossWireProperty: ObservableBoolean = ObservableBoolean(true)
    var showCrossWire: Boolean by showCrossWireProperty

    /**
     * The position of the cross wire along the x-axis
     */
    val crossWirePositionXProperty: ObservableObject<@WindowRelative Double> = ObservableObject(0.7)
    var crossWirePositionX: @WindowRelative Double by crossWirePositionXProperty

    /**
     * The cross wire label styles - for the cross wire for decimal values
     */
    var crossWireDecimalsLabelBoxStyles: MultiProvider<DecimalDataSeriesIndex, BoxStyle> = MultiProvider {
      BoxStyle(fill = Theme.chartColors().valueAt(it), borderColor = Color.white, padding = CrossWireLayer.Style.DefaultLabelBoxPadding, radii = BorderRadius.all2, shadow = Shadow.LightDrop)
    }

    /**
     * The format to be used for the labels at the cross wire
     *
     * If null a format suitable for the given data series will be used
     */
    var crossWireDecimalFormat: MultiProvider<DecimalDataSeriesIndex, CachedNumberFormat?> = MultiProvider.alwaysNull()

    /**
     * The text colors for the cross wire label
     */
    var crossWireDecimalsLabelTextColors: MultiProvider<DecimalDataSeriesIndex, Color> = MultiProvider.always(Color.white)

    /**
     * The cross wire label styles - for the cross wire for enum values.
     *
     * If the background is set to null, the color for the current value will be used (as provided by [RectangleEnumStripePainter.Configuration.fillProvider])
     */
    var crossWireEnumsLabelBoxStyles: MultiProvider<EnumDataSeriesIndex, BoxStyle> = MultiProvider {
      BoxStyle(
        fill = null, //use color for current value
        borderColor = Color.white,
        padding = CrossWireLayer.Style.DefaultLabelBoxPadding,
        radii = BorderRadius.all2,
        shadow = Shadow.LightDrop
      )
    }

    var crossWireEnumsLabelTextColors: MultiProvider<EnumDataSeriesIndex, Color> = MultiProvider.always(Color.white)

    /**
     * Computes the duration of the content area by ensuring that at least 600 samples are visible for the given sampling period.
     */
    fun applySamplingPeriod(samplingPeriod: SamplingPeriod = defaultMinimumSamplingPeriod) {
      contentAreaDuration = samplingPeriod.distance * 600 //600 samples
    }

    init {
      applySamplingPeriod()
      showAllDecimalSeries()
      showEnumSeriesAtMost(3)
    }
  }

  companion object {
    val defaultMinimumSamplingPeriod: SamplingPeriod = SamplingPeriod.EveryHundredMillis
  }
}
