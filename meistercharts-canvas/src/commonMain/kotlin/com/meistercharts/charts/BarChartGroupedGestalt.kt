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
package com.meistercharts.charts

import com.meistercharts.algorithms.layers.AxisConfiguration
import com.meistercharts.algorithms.layers.AxisTitleLocation
import com.meistercharts.algorithms.layers.AxisTopTopTitleLayer
import com.meistercharts.algorithms.layers.DefaultCategoryLayouter
import com.meistercharts.algorithms.layers.DomainRelativeGridLayer
import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.algorithms.layers.PaintingVariables
import com.meistercharts.algorithms.layers.TooltipInteractionLayer
import com.meistercharts.algorithms.layers.addClearBackground
import com.meistercharts.algorithms.layers.addFillCanvasBackground
import com.meistercharts.algorithms.layers.axis.HudLabelsProvider
import com.meistercharts.algorithms.layers.axis.ValueAxisLayer
import com.meistercharts.algorithms.layers.axis.withMaxNumberOfTicks
import com.meistercharts.algorithms.layers.barchart.CategoryAxisLayer
import com.meistercharts.algorithms.layers.barchart.CategoryChartOrientation
import com.meistercharts.algorithms.layers.barchart.CategoryLayer
import com.meistercharts.algorithms.layers.barchart.CategoryModelBoxStylesProvider
import com.meistercharts.algorithms.layers.barchart.CategorySeriesModelColorsProvider
import com.meistercharts.algorithms.layers.barchart.ContentAreaBoxProvider
import com.meistercharts.algorithms.layers.barchart.FlippingValueLabelAnchorDirectionProvider
import com.meistercharts.algorithms.layers.barchart.GroupedBarsPainter
import com.meistercharts.algorithms.layers.barchart.WindowBoxProvider
import com.meistercharts.algorithms.layers.clipped
import com.meistercharts.algorithms.layers.createGrid
import com.meistercharts.algorithms.layers.crosswire.CrossWireLayer
import com.meistercharts.algorithms.layers.crosswire.LabelPlacementStrategy
import com.meistercharts.algorithms.layers.linechart.LineStyle
import com.meistercharts.algorithms.layers.visibleIf
import com.meistercharts.algorithms.layout.BoxIndex
import com.meistercharts.algorithms.painter.LabelPlacement
import com.meistercharts.algorithms.tooltip.balloon.BalloonTooltipLayer
import com.meistercharts.algorithms.tooltip.balloon.CategoryBalloonTooltipPlacementSupport
import com.meistercharts.algorithms.tooltip.balloon.CategorySeriesModelBalloonTooltipSupport
import com.meistercharts.annotations.ContentArea
import com.meistercharts.annotations.Domain
import com.meistercharts.annotations.DomainRelative
import com.meistercharts.annotations.Window
import com.meistercharts.calc.ChartCalculator
import com.meistercharts.canvas.ConfigurationDsl
import com.meistercharts.canvas.DirtyReason
import com.meistercharts.canvas.layout.cache.DoubleCache
import com.meistercharts.charts.support.CategoryAxisSupport
import com.meistercharts.charts.support.ValueAxisSupport
import com.meistercharts.charts.support.addLayers
import com.meistercharts.charts.support.createCategoryAxisSupport
import com.meistercharts.charts.support.getAxisLayer
import com.meistercharts.charts.support.getTopTitleLayer
import com.meistercharts.charts.support.threshold.ThresholdsSupport
import com.meistercharts.charts.support.threshold.addLayers
import com.meistercharts.charts.support.threshold.thresholdsSupportSingle
import com.meistercharts.color.Color
import com.meistercharts.design.Theme
import com.meistercharts.design.valueAt
import com.meistercharts.font.FontDescriptorFragment
import com.meistercharts.model.BorderRadius
import com.meistercharts.model.Insets
import com.meistercharts.model.Vicinity
import com.meistercharts.model.category.Category
import com.meistercharts.model.category.CategoryIndex
import com.meistercharts.model.category.CategorySeriesModel
import com.meistercharts.model.category.DefaultCategorySeriesModel
import com.meistercharts.model.category.DefaultSeries
import com.meistercharts.model.category.SeriesIndex
import com.meistercharts.model.category.createCategoryLabelsProvider
import com.meistercharts.provider.BoxProvider1
import com.meistercharts.range.LinearValueRange
import com.meistercharts.range.ValueRange
import com.meistercharts.style.BoxStyle
import com.meistercharts.style.Palette.chartColors
import com.meistercharts.style.Shadow
import it.neckar.geometry.Orientation
import it.neckar.geometry.Side
import it.neckar.geometry.Size
import it.neckar.open.formatting.CachedNumberFormat
import it.neckar.open.formatting.decimalFormat
import it.neckar.open.formatting.intFormat
import it.neckar.open.i18n.I18nConfiguration
import it.neckar.open.i18n.TextKey
import it.neckar.open.i18n.TextService
import it.neckar.open.kotlin.lang.asProvider
import it.neckar.open.kotlin.lang.asProvider1
import it.neckar.open.kotlin.lang.fastFor
import it.neckar.open.observable.ObservableBoolean
import it.neckar.open.provider.DoublesProvider
import it.neckar.open.provider.MultiProvider
import it.neckar.open.provider.MultiProvider1
import it.neckar.open.provider.delegate
import it.neckar.open.unit.number.MayBeNaN
import it.neckar.open.unit.other.px

/**
 * Configuration for a bar chart with grouped bars
 */
class BarChartGroupedGestalt constructor(
  /**
   * The current category model for the stacked bar chart
   */
  initialCategorySeriesModel: CategorySeriesModel = createDefaultCategoryModel(),
  /**
   * The tooltip type
   */
  initialToolTipType: ToolTipType = ToolTipType.Balloon,
  additionalConfiguration: Configuration.() -> Unit = {},
) : AbstractChartGestalt() {
  val configuration: Configuration = Configuration(initialCategorySeriesModel, initialToolTipType)

  val style: Configuration = Configuration(initialCategorySeriesModel, initialToolTipType).also(additionalConfiguration)

  /**
   * Delegate the configures the chart to have a fixed zoom and translation
   */
  private val fixedChartGestalt: FixedChartGestalt = FixedChartGestalt(Insets.of(10.0, 80.0, 40.0, 75.0))
  var contentViewportMargin: Insets by fixedChartGestalt::contentViewportMargin

  /**
   * The painter used by the [categoryLayer]
   */
  val groupedBarsPainter: GroupedBarsPainter = GroupedBarsPainter {
    valueRangeProvider = { style.valueRange }
    colorsProvider = CategorySeriesModelColorsProvider.onlySeriesColorsProvider(chartColors)
    showValueLabel = false
    barGap = 4.0
    setBarSizeRange(8.0, 22.0)
  }

  /**
   * The layer that paints the bars
   */
  val categoryLayer: CategoryLayer<CategorySeriesModel> = CategoryLayer({ configuration.categorySeriesModel }) {
    orientation = CategoryChartOrientation.VerticalLeft
    categoryPainter = groupedBarsPainter

    activeCategoryBackgroundSize = {
      //Use the actual size of the group
      groupedBarsPainter.paintingVariables().actualSize + style.activeCategoryBackgroundMargin.offsetWidth
    }

    layoutCalculator = DefaultCategoryLayouter {
      minCategorySizeProvider = {
        //The count of bars in a group
        val barsCount = configuration.categorySeriesModel.numberOfSeries

        //the minimum size of a group
        val minGroupSize = groupedBarsPainter.configuration.minBarSize * barsCount + groupedBarsPainter.configuration.barGap * (barsCount - 1)

        //Calculate the total width of category
        minGroupSize + style.minGapBetweenGroups
      }

      maxCategorySizeProvider = {
        groupedBarsPainter.configuration.maxBarSize?.let { maxBarSize ->
          //The count of bars in a group
          val barsCount = configuration.categorySeriesModel.numberOfSeries
          //the minimum size of a group
          val maxGroupSize = maxBarSize * barsCount + groupedBarsPainter.configuration.barGap * (barsCount - 1)

          //Calculate the total width of category
          maxGroupSize + style.maxGapBetweenGroups
        }
      }
    }
  }

  /**
   * Returns the active category index - or null if no category is active
   */
  var activeCategoryIndexOrNull: CategoryIndex?
    get() = categoryLayer.configuration.activeCategoryIndex
    private set(value) {
      categoryLayer.configuration.activeCategoryIndex = value
    }

  /**
   * Returns the active category index.
   * Throws an exception if no category is active!
   */
  val activeCategoryIndex: CategoryIndex
    get() = activeCategoryIndexOrNull ?: throw IllegalStateException("No active category index found")

  /**
   * Handles the mouse over - does *not* paint anything itself
   */
  val toolbarInteractionLayer: TooltipInteractionLayer<CategoryIndex> = TooltipInteractionLayer.forCategories(
    orientation = { style.orientation.layoutDirection.orientation },
    layoutProvider = { categoryLayer.paintingVariables().layout },
    selectionSink = { newSelection, chartSupport ->
      if (activeCategoryIndexOrNull != newSelection) {
        activeCategoryIndexOrNull = newSelection
        chartSupport.markAsDirty(DirtyReason.ActiveElementUpdated)
      }
    }
  )

  /**
   * Shows a cross wire for vertical bars - paints a single tooltip-like label for every bar at the right height
   */
  val crossWireLayerVertical: CrossWireLayer = CrossWireLayer(
    valueLabelsProvider = object : CrossWireLayer.ValueLabelsProvider {

      private val paintingVariables = object : PaintingVariables {
        val locations = @MayBeNaN DoubleCache()

        override fun calculate(paintingContext: LayerPaintingContext) {
          val chartCalculator = paintingContext.chartCalculator

          val categoryIndex = activeCategoryIndexOrNull
          if (categoryIndex == null) {
            locations.prepare(0)
            return
          }

          val categoryModel = configuration.categorySeriesModel

          locations.prepare(categoryModel.numberOfSeries)

          categoryModel.numberOfSeries.fastFor { seriesIndexAsInt ->
            val seriesIndex = SeriesIndex(seriesIndexAsInt)
            @MayBeNaN @Domain val value = categoryModel.valueAt(categoryIndex, seriesIndex)
            @MayBeNaN @DomainRelative val relativeValue = style.valueRange.toDomainRelative(value)

            locations[seriesIndexAsInt] = chartCalculator.domainRelative2windowY(relativeValue)
          }
        }
      }

      override fun size(): Int {
        return paintingVariables.locations.size
      }

      override fun layout(wireLocation: Double, paintingContext: LayerPaintingContext) {
        paintingVariables.calculate(paintingContext)
      }

      override fun locationAt(index: Int): @Window @MayBeNaN Double {
        return paintingVariables.locations[index]
      }

      override fun labelAt(index: Int, textService: TextService, i18nConfiguration: I18nConfiguration): String {
        val categoryIndex = activeCategoryIndex
        val categoryModel = configuration.categorySeriesModel

        @Domain val value = categoryModel.valueAt(categoryIndex, SeriesIndex(index))
        return style.crossWireValueLabelFormat.format(value)
      }
    }
  ) {
    //The active category is visualized by the category layer
    showCrossWireLine = false

    /**
     * The label placement strategy that is used to determine the "base" location
     */
    val baseValueLabelPlacementStrategy = LabelPlacementStrategy.preferOnRightSide { style.minCrossWireSpaceOnRightSide }

    /**
     * Used to combine locationX and valueLabelPlacement
     */
    val paintingVariables = object {
      var paintingOnLeftSide = false
    }

    locationX = { paintingContext ->
      val categoryIndex = activeCategoryIndex
      val layout = categoryLayer.paintingVariables().layout

      @ContentArea val centerOfCategory = layout.calculateCenter(BoxIndex(categoryIndex.value))
      @Window val center = paintingContext.chartCalculator.contentArea2windowX(centerOfCategory)

      val backgroundSize = categoryLayer.configuration.activeCategoryBackgroundSize(layout.boxSize)

      val leftSide = center - backgroundSize / 2.0
      val rightSide = center + backgroundSize / 2.0


      //verify if there is enough space for the labels on the right side
      when (baseValueLabelPlacementStrategy(rightSide, paintingContext)) {
        LabelPlacement.OnRightSide -> {
          paintingVariables.paintingOnLeftSide = false
          rightSide
        }

        LabelPlacement.OnLeftSide -> {
          paintingVariables.paintingOnLeftSide = true
          leftSide
        }
      }
    }

    valueLabelPlacementStrategy = LabelPlacementStrategy { _, _ ->
      if (paintingVariables.paintingOnLeftSide) {
        LabelPlacement.OnLeftSide
      } else {
        LabelPlacement.OnRightSide
      }
    }


    valueLabelBoxStyle = MultiProvider {
      style.crossWireLabelBoxStyles.boxStyle(activeCategoryIndex, SeriesIndex(it))
    }
    valueLabelTextColor = MultiProvider {
      style.crossWireLabelTextColors.color(activeCategoryIndex, SeriesIndex(it))
    }


  }.also {
    it.valueLabelPainter.style.gapToLabels = 10.0
  }

  /**
   * Balloon tooltip support
   */
  val balloonTooltipSupport: CategorySeriesModelBalloonTooltipSupport = CategorySeriesModelBalloonTooltipSupport(
    CategoryBalloonTooltipPlacementSupport(
      orientation = { style.orientation.categoryOrientation.opposite() }, //convert orientation of bars to orientation of category placement
      activeCategoryIndexProvider = ::activeCategoryIndexOrNull,
      categorySize = {
        val layout = categoryLayer.paintingVariables().layout
        categoryLayer.configuration.activeCategoryBackgroundSize(layout.boxSize)
      },
      boxLayout = { categoryLayer.paintingVariables().layout }
    ),
    { configuration.categorySeriesModel },
    valueFormat = { style.balloonTooltipValueLabelFormat },
    colors = { index ->
      groupedBarsPainter.configuration.colorsProvider.color(activeCategoryIndex, SeriesIndex(index))
    }
  )

  /**
   * Shows the tooltips as balloon.
   * This layer uses the [balloonTooltipSupport] to paint the content of the tooltip
   */
  val balloonTooltipLayer: BalloonTooltipLayer = balloonTooltipSupport.createTooltipLayer()

  val categoryAxisSupport: CategoryAxisSupport<Unit> = categoryLayer.createCategoryAxisSupport {
    this.axisConfiguration = { _, _, _ ->
      tickOrientation = Vicinity.Outside
      side = Side.Bottom
      hideTicks()
    }
  }

  /**
   * The layer that paints the labels of the bars
   */
  val categoryAxisLayer: CategoryAxisLayer = categoryAxisSupport.getAxisLayer()

  /**
   * Visualizes the title on top of the category layer
   *
   * ATTENTION: Is not visible by default
   * see [Style.axisTitleLocation]
   *
   * Is only visible if the [categoryAxisLayer] is placed at the left or right
   */
  val categoryAxisTopTitleLayer: AxisTopTopTitleLayer
    get() {
      return categoryAxisSupport.getTopTitleLayer()
    }

  val valueAxisSupport = ValueAxisSupport.single(
    { style.valueRange }
  ) {
    valueAxisConfiguration = { _, _, _ ->
      tickOrientation = Vicinity.Outside
      paintRange = AxisConfiguration.PaintRange.ContentArea
      ticksFormat = defaultNumberFormat
      side = Side.Left
      ticks = ticks.withMaxNumberOfTicks(10)
    }
  }

  /**
   * The value axis - backed by the [valueAxisSupport]
   */
  val valueAxisLayer: ValueAxisLayer
    get() {
      return valueAxisSupport.getAxisLayer(Unit)
    }

  /**
   * Visualizes the title on top of the value layer
   *
   * ATTENTION: Is not visible by default
   * see [Style.axisTitleLocation]
   *
   * Is only visible if the [valueAxisLayer] is placed at the left or right
   */
  val valueAxisTopTitleLayer: AxisTopTopTitleLayer
    get() {
      return valueAxisSupport.getTopTitleLayer()
    }

  val thresholdsSupport: ThresholdsSupport<Unit> = valueAxisSupport.thresholdsSupportSingle(
    thresholdValues = configuration::thresholdValues.delegate(),
    thresholdLabels = configuration::thresholdLabels.delegate(),
  ) {
  }

  val gridLayer: DomainRelativeGridLayer = valueAxisLayer.createGrid {
    lineStyles = { value: @DomainRelative Double -> style.gridLineStyles(style.valueRange.toDomain(value)) }
  }

  init {
    style.applyValueLabelsInWindowRespectingAxis()

    categoryAxisLayer.configuration.labelsProvider = configuration::categorySeriesModel.createCategoryLabelsProvider()

    fixedChartGestalt.contentViewportMarginProperty.consumeImmediately {
      gridLayer.configuration.passpartout = it

      valueAxisLayer.configuration.size = it[valueAxisLayer.configuration.side]
      categoryAxisLayer.configuration.size = it[categoryAxisLayer.configuration.side]
    }

    configureBuilder { meisterChartBuilder ->
      fixedChartGestalt.configure(meisterChartBuilder)
    }

    configure {
      layers.addClearBackground()
      layers.addFillCanvasBackground()

      layers.addAboveBackground(gridLayer.visibleIf(style.showGridProperty))
      layers.addLayer(toolbarInteractionLayer.visibleIf(style.showTooltipsProperty))

      valueAxisSupport.addLayers(this)
      thresholdsSupport.addLayers(this)

      layers.addLayer(categoryLayer.clipped {
        /*
         * Only clip the sides where the axes are.
         * We must not clip the other sides (e.g. for labels)
         */
        val categoryAxisSide = categoryAxisLayer.configuration.side
        val valueAxisSide = valueAxisLayer.configuration.side
        // FIXME: this is a workaround as long as the group-painter does not take the content area into account.
        val thresholdSide = if (style.orientation.categoryOrientation == Orientation.Vertical) Side.Right else Side.Top

        contentViewportMargin.only(categoryAxisSide, valueAxisSide, thresholdSide)
      })

      when (configuration.toolTipType) {
        ToolTipType.CrossWire -> {
          layers.addLayer(crossWireLayerVertical.visibleIf {
            style.orientation.categoryOrientation == Orientation.Vertical && activeCategoryIndexOrNull != null
          })
        }

        ToolTipType.Balloon -> {
          layers.addLayer(balloonTooltipLayer.visibleIf {
            activeCategoryIndexOrNull != null
          })
        }
      }

      //Category axis must be placed *above* categoryLayer
      categoryAxisSupport.addLayers(this)
    }
  }

  //Note that all default values are chosen in regard to a vertical chart orientation.
  @ConfigurationDsl
  open inner class Configuration(
    /**
     * The current category model for the stacked bar chart
     */
    var categorySeriesModel: CategorySeriesModel,
    /**
     * The tooltip type
     */
    val toolTipType: ToolTipType,
  ) {
    /**
     * Applies the balloon tooltip size
     */
    fun applyBalloonTooltipSize(symbolSize: Size) {
      balloonTooltipSupport.applyLegendSymbolSize(symbolSize)
    }

    /**
     * Provides the threshold values
     */
    var thresholdValues: @Domain DoublesProvider = DoublesProvider.empty

    /**
     * Provides the threshold labels
     */
    var thresholdLabels: HudLabelsProvider = MultiProvider1 { index, _ -> listOf(decimalFormat.format(thresholdValues.valueAt(index))) }

    /**
     * The margin for the active category background
     */
    var activeCategoryBackgroundMargin: Insets = Insets.all15

    /**
     * The value range to be used for this chart
     */
    var valueRange: ValueRange = defaultValueRange

    /**
     * Where the value axis title is painted
     */
    var axisTitleLocation: AxisTitleLocation
      get() = valueAxisSupport.preferredAxisTitleLocation
      set(value) {
        valueAxisSupport.preferredAxisTitleLocation = value
        categoryAxisSupport.preferredAxisTitleLocation = value
      }

    /**
     * Configures this chart to visualize the value axis title on top of the value axis
     */
    fun applyAxisTitleOnTop(contentViewportMarginTop: @px Double = 40.0) {
      axisTitleLocation = AxisTitleLocation.AtTop

      //Make space for the value axis title
      contentViewportMargin = contentViewportMargin.withTop(contentViewportMargin.top.coerceAtLeast(contentViewportMarginTop))
    }

    /**
     * Applies the given value range.
     * This method also updates styles in different layers to match the given value range.
     */
    fun applyValueRange(valueRange: ValueRange) {
      this.valueRange = valueRange

      //Update the value axis layer
      if (valueRange is LinearValueRange) {
        valueAxisLayer.configuration.applyLinearScale()
      } else {
        valueAxisLayer.configuration.applyLogarithmicScale()
      }
    }

    /**
     * Sets the given font for all tick labels of all axes
     */
    fun applyAxisTickFont(font: FontDescriptorFragment) {
      categoryAxisLayer.configuration.tickFont = font.asProvider()
      valueAxisLayer.configuration.tickFont = font.asProvider()
    }

    /**
     * Sets the given font for all titles of all axes
     */
    fun applyAxisTitleFont(font: FontDescriptorFragment) {
      categoryAxisLayer.configuration.titleFont = font.asProvider()
      valueAxisLayer.configuration.titleFont = font.asProvider()
    }

    /**
     * The minimum gap between two groups.
     * This value is used by the layouter
     */
    var minGapBetweenGroups: @px Double = 10.0

    /**
     * The max gap between two groups
     */
    var maxGapBetweenGroups: @px Double = 100.0

    /**
     * The style to be used for a grid line at a certain domain value
     */
    var gridLineStyles: (@Domain Double) -> LineStyle = LineStyle(color = Color.lightgray, lineWidth = 1.0).asProvider1()

    /**
     * Whether the grid is visible (true) or not (false)
     */
    val showGridProperty: ObservableBoolean = ObservableBoolean(true)
    var showGrid: Boolean by showGridProperty

    /**
     * The orientation of the chart
     */
    val orientation: CategoryChartOrientation
      get() = categoryLayer.configuration.orientation

    /**
     * Whether to show tooltips (using the cross wire)
     */
    val showTooltipsProperty: ObservableBoolean = ObservableBoolean(true)
    var showTooltip: Boolean by showTooltipsProperty

    /**
     * Format used for the labels of the cross wire
     */
    var crossWireValueLabelFormat: CachedNumberFormat = decimalFormat

    var balloonTooltipValueLabelFormat: CachedNumberFormat = decimalFormat

    /**
     * The cross wire label styles.
     */
    var crossWireLabelBoxStyles: CategoryModelBoxStylesProvider = CategoryModelBoxStylesProvider { _, seriesIndex ->
      BoxStyle(
        fill = Theme.chartColors.valueAt(seriesIndex.value),
        borderColor = Color.white,
        borderWidth = 2.0,
        padding = CrossWireLayer.Configuration.DefaultLabelBoxPadding,
        radii = BorderRadius.all2,
        shadow = Shadow.LightDrop
      )
    }

    /**
     * The text colors for the cross wire label
     */
    var crossWireLabelTextColors: CategorySeriesModelColorsProvider = CategorySeriesModelColorsProvider { _, _ -> Color.white() }

    /**
     * The minimum required space before the cross wire is moved to the left
     */
    val minCrossWireSpaceOnRightSide: @px Double = 150.0

    /**
     * Changes the chart orientation to horizontal.
     * This method modifies multiple layers and properties to match the new orientation
     */
    fun applyHorizontalConfiguration() {
      categoryLayer.configuration.orientation = CategoryChartOrientation.HorizontalTop
      categoryAxisLayer.configuration.side = Side.Left
      valueAxisLayer.configuration.side = Side.Bottom
      contentViewportMargin = Insets.of(40.0, 20.0, 40.0, 75.0)
      fixedChartGestalt.contentViewportMargin = Insets.of(40.0, 20.0, 40.0, 75.0)
    }

    /**
     * Changes the chart orientation to vertical.
     * This method modifies multiple layers and properties to match the new orientation
     */
    fun applyVerticalConfiguration() {
      categoryLayer.configuration.orientation = CategoryChartOrientation.VerticalLeft
      categoryAxisLayer.configuration.side = Side.Bottom
      valueAxisLayer.configuration.side = Side.Left
      contentViewportMargin = Insets.of(10.0, 80.0, 40.0, 75.0)
      fixedChartGestalt.contentViewportMargin = Insets.of(10.0, 80.0, 40.0, 75.0)
    }

    /**
     * Applies the [FlippingValueLabelAnchorDirectionProvider] with meaningful default-values.
     */
    fun applyFlippingBarsValueLabelsPlacement() {
      groupedBarsPainter.configuration.valueLabelAnchorDirectionProvider = FlippingValueLabelAnchorDirectionProvider
      groupedBarsPainter.configuration.valueLabelAnchorGapHorizontal = 5.0
      groupedBarsPainter.configuration.valueLabelAnchorGapVertical = 5.0
    }

    /**
     * Configures the value labels box to stay in the content area
     */
    fun applyValueLabelsInContentArea() {
      groupedBarsPainter.configuration.apply {
        valueLabelBoxProvider = ContentAreaBoxProvider
      }
    }

    /**
     * Configures the value labels box to stay in the window.
     *
     * ATTENTION: Does not work well with axis, since the labels might be painted within the labels
     */
    fun applyValueLabelsInWindow() {
      groupedBarsPainter.configuration.apply {
        valueLabelBoxProvider = WindowBoxProvider
      }
    }

    /**
     * Configures the value labels box to stay within:
     * * the content area on the sides where there are axis
     * * the window on the other two sides
     */
    fun applyValueLabelsInWindowRespectingAxis() {
      val categoryAxisSide = categoryAxisLayer.configuration.side
      val valueAxisSide = valueAxisLayer.configuration.side

      groupedBarsPainter.configuration.apply {
        valueLabelBoxProvider = object : BoxProvider1<ChartCalculator> {
          override fun getX(param0: ChartCalculator): Double {
            return contentViewportMargin.onlyLeft(categoryAxisSide, valueAxisSide)
          }

          override fun getY(param0: ChartCalculator): Double {
            return contentViewportMargin.onlyTop(categoryAxisSide, valueAxisSide)
          }

          override fun getWidth(param0: ChartCalculator): Double {
            //We assume that one axis is horizontal the other vertical
            return when {
              Side.Left.any(categoryAxisSide, valueAxisSide) -> param0.chartState.windowWidth - contentViewportMargin.left
              Side.Right.any(categoryAxisSide, valueAxisSide) -> param0.chartState.windowWidth - contentViewportMargin.right
              else -> param0.chartState.windowWidth
            }
          }

          override fun getHeight(param0: ChartCalculator): Double {
            //We assume that one axis is horizontal the other vertical
            return when {
              Side.Top.any(categoryAxisSide, valueAxisSide) -> param0.chartState.windowHeight - contentViewportMargin.top
              Side.Bottom.any(categoryAxisSide, valueAxisSide) -> param0.chartState.windowHeight - contentViewportMargin.bottom
              else -> param0.chartState.windowHeight
            }
          }
        }
      }
    }
  }

  companion object {
    val defaultValueRange: LinearValueRange = ValueRange.linear(0.0, 55.0)

    fun createDefaultCategoryModel(): CategorySeriesModel = DefaultCategorySeriesModel(
      listOf(
        Category(TextKey.simple("A")),
        Category(TextKey.simple("B")),
        Category(TextKey.simple("C")),
        Category(TextKey.simple("D")),
        Category(TextKey.simple("E"))
      ),
      listOf(
        DefaultSeries("1", listOf(34.0, 47.0, 19.0, 0.0, 17.0)),
        DefaultSeries("2", listOf(7.0, 10.0, 5.0, 0.0, 20.0)),
        DefaultSeries("3", listOf(7.0, 5.0, 3.0, 2.0, 8.0)),
      )
    )

    fun createValuesOutsideCategoryModel(): CategorySeriesModel = DefaultCategorySeriesModel(
      listOf(
        Category(TextKey.simple("A")),
        Category(TextKey.simple("B")),
        Category(TextKey.simple("C")),
        Category(TextKey.simple("D")),
        Category(TextKey.simple("E"))
      ),
      listOf(
        DefaultSeries("1", listOf(-34.0, 47.0, 55.0, 0.0, 17.0)),
        DefaultSeries("2", listOf(7.0, 10.0, 5.0, 56.0, 20.0)),
        DefaultSeries("3", listOf(7.0, 5.0, 88.0, 58.0, 8.0)),
      )
    )

    fun createSomeInvalidCategoryModel(): CategorySeriesModel = DefaultCategorySeriesModel(
      listOf(
        Category(TextKey.simple("A")),
        Category(TextKey.simple("B")),
        Category(TextKey.simple("C")),
        Category(TextKey.simple("D")),
        Category(TextKey.simple("E"))
      ),
      listOf(
        DefaultSeries("1", listOf(-34.0, Double.NaN, Double.NaN, 0.0, 17.0)),
        DefaultSeries("2", listOf(7.0, Double.NaN, 5.0, Double.NaN, 20.0)),
        DefaultSeries("3", listOf(7.0, Double.NaN, 88.0, 58.0, Double.NaN)),
      )
    )

    val defaultNumberFormat: CachedNumberFormat = intFormat
  }
}

/**
 * Represents a threshold
 */
data class Threshold(
  /**
   * The threshold value (maybe @DomainRelative or @Domain or @pct)
   */
  val value: Double,

  /**
   * The threshold labels
   */
  var labels: List<TextKey> = emptyList(),
)
