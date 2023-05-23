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

import com.meistercharts.algorithms.LinearValueRange
import com.meistercharts.algorithms.ValueRange
import com.meistercharts.algorithms.layers.AxisStyle
import com.meistercharts.algorithms.layers.AxisTitleLocation
import com.meistercharts.algorithms.layers.AxisTopTopTitleLayer
import com.meistercharts.algorithms.layers.CategoryLinesLayer
import com.meistercharts.algorithms.layers.ClippingLayer
import com.meistercharts.algorithms.layers.DefaultCategoryLayouter
import com.meistercharts.algorithms.layers.DomainRelativeGridLayer
import com.meistercharts.algorithms.layers.GridLayer
import com.meistercharts.algorithms.layers.HudLabelsProvider
import com.meistercharts.algorithms.layers.Layer
import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.algorithms.layers.PaintingVariables
import com.meistercharts.algorithms.layers.TooltipInteractionLayer
import com.meistercharts.algorithms.layers.ValueAxisLayer
import com.meistercharts.algorithms.layers.addClearBackground
import com.meistercharts.algorithms.layers.barchart.CategoryAxisLayer
import com.meistercharts.algorithms.layers.barchart.createAxisLayer
import com.meistercharts.algorithms.layers.clipped
import com.meistercharts.algorithms.layers.createGrid
import com.meistercharts.algorithms.layers.crosswire.CrossWireLayer
import com.meistercharts.algorithms.layers.debug.addVersionNumberHidden
import com.meistercharts.algorithms.layers.visibleIf
import com.meistercharts.algorithms.layout.BoxIndex
import com.meistercharts.algorithms.model.Category
import com.meistercharts.algorithms.model.CategoryIndex
import com.meistercharts.algorithms.model.CategorySeriesModel
import com.meistercharts.algorithms.model.DefaultCategorySeriesModel
import com.meistercharts.algorithms.model.DefaultSeries
import com.meistercharts.algorithms.model.SeriesIndex
import com.meistercharts.algorithms.model.createCategoryLabelsProvider
import com.meistercharts.algorithms.model.delegate
import com.meistercharts.algorithms.tooltip.balloon.BalloonTooltipLayer
import com.meistercharts.algorithms.tooltip.balloon.CategoryBalloonTooltipPlacementSupport
import com.meistercharts.algorithms.tooltip.balloon.CategorySeriesModelBalloonTooltipSupport
import com.meistercharts.annotations.ContentArea
import com.meistercharts.annotations.Domain
import com.meistercharts.annotations.DomainRelative
import com.meistercharts.annotations.Window
import com.meistercharts.annotations.Zoomed
import com.meistercharts.canvas.ChartSupport
import com.meistercharts.canvas.DirtyReason
import com.meistercharts.canvas.FontDescriptorFragment
import com.meistercharts.canvas.MeisterChartBuilder
import com.meistercharts.canvas.ConfigurationDsl
import com.meistercharts.canvas.layout.cache.DoubleCache
import com.meistercharts.charts.BarChartGroupedGestalt.Style
import com.meistercharts.charts.support.threshold.ThresholdsSupport
import com.meistercharts.charts.support.ValueAxisSupport
import com.meistercharts.charts.support.addLayers
import com.meistercharts.charts.support.threshold.addLayers
import com.meistercharts.charts.support.getTopTitleLayer
import com.meistercharts.charts.support.getValueAxisLayer
import com.meistercharts.charts.support.threshold.thresholdsSupportSingle
import com.meistercharts.model.Insets
import com.meistercharts.model.Orientation
import com.meistercharts.model.Side
import com.meistercharts.model.Size
import com.meistercharts.model.Vicinity
import it.neckar.open.kotlin.lang.fastFor
import it.neckar.open.provider.DoubleProvider
import it.neckar.open.provider.DoublesProvider
import it.neckar.open.provider.MultiProvider
import it.neckar.open.provider.MultiProvider1
import it.neckar.open.provider.SizedProvider
import it.neckar.open.provider.delegate
import it.neckar.open.provider.mapped
import it.neckar.open.formatting.CachedNumberFormat
import it.neckar.open.formatting.decimalFormat
import it.neckar.open.formatting.intFormat
import it.neckar.open.i18n.I18nConfiguration
import it.neckar.open.i18n.TextKey
import it.neckar.open.i18n.TextService
import it.neckar.open.observable.ObservableBoolean
import it.neckar.open.observable.ObservableObject
import it.neckar.open.unit.number.MayBeNaN
import it.neckar.open.unit.other.px
import kotlin.jvm.JvmOverloads

/**
 * Represents a non-interactive simple line chart.
 *
 * This type of line chart can be used in dashboards where no interaction is required.
 *
 * They are simple to use but offer only reduced functionality.
 *
 */
class CategoryLineChartGestalt @JvmOverloads constructor(
  /**
   * The current category model for this line chart.
   *
   * The category model must contain exactly one series.
   *
   * The number of categories does not matter.
   */
  categorySeriesModel: CategorySeriesModel = createDefaultCategoryModel(),

  /**
   * The tooltip type
   */
  toolTipType: ToolTipType = ToolTipType.CrossWire,

  additionalConfiguration: Configuration.() -> Unit = {},
) : AbstractChartGestalt() {

  val configuration: Configuration = Configuration(categorySeriesModel, toolTipType)

  init {
    this.configuration.also(additionalConfiguration)
  }

  val fixedChartGestalt: FixedChartGestalt = FixedChartGestalt(Insets.of(10.0, 80.0, 40.0, 75.0))
  var contentViewportMargin: @Zoomed Insets by fixedChartGestalt::contentViewportMargin

  private val defaultCategoryLayouter = DefaultCategoryLayouter {
    minCategorySizeProvider = { configuration.minCategorySize }
    maxCategorySizeProvider = { configuration.maxCategorySize }
    gapSize = DoubleProvider { configuration.categoryGap }
  }

  val categoryLinesLayer: CategoryLinesLayer = CategoryLinesLayer(CategoryLinesLayer.Data(configuration::filteredCategorySeriesModel.delegate())) {
    layoutCalculator = defaultCategoryLayouter
  }

  /**
   * Handles the tooltip interactions (does *not* paint anything)
   */
  val tooltipInteractionLayer: TooltipInteractionLayer<CategoryIndex> = TooltipInteractionLayer.forCategories(
    orientation = { Orientation.Horizontal },
    layoutProvider = { categoryLinesLayer.paintingVariables().layout },
    selectionSink = { newCategoryIndex, chartSupport: ChartSupport ->
      if (categoryLinesLayer.style.activeCategoryIndex != newCategoryIndex) {
        categoryLinesLayer.style.activeCategoryIndex = newCategoryIndex
        chartSupport.markAsDirty(DirtyReason.ActiveElementUpdated)
      }
    })

  /**
   * Only paints the background layer
   */
  val crossWireLineLayer: CrossWireLayer = CrossWireLayer.onlyWire { paintingContext: LayerPaintingContext ->
    val activeCategoryIndex = categoryLinesLayer.style.activeCategoryIndex ?: throw IllegalStateException("no active category available")
    @ContentArea val center = categoryLinesLayer.paintingVariables().layout.calculateCenter(BoxIndex(activeCategoryIndex.value))
    paintingContext.chartCalculator.contentArea2windowX(center)
  }

  /**
   * The cross wire layer that is used to display the values at the current mouse location
   */
  val crossWireLabelsLayer: CrossWireLayer = CrossWireLayer(
    data = CrossWireLayer.Data(
      valueLabelsProvider = object : CrossWireLayer.ValueLabelsProvider {

        private val paintingVariables = object : PaintingVariables {
          val locations = @MayBeNaN DoubleCache()

          override fun calculate(paintingContext: LayerPaintingContext) {
            val chartCalculator = paintingContext.chartCalculator

            val categoryIndex = categoryLinesLayer.style.activeCategoryIndex

            if (categoryIndex == null) {
              locations.prepare(0)
              return
            }

            val categoryModel = configuration.filteredCategorySeriesModel

            locations.prepare(categoryModel.numberOfSeries)

            categoryModel.numberOfSeries.fastFor { seriesIndexAsInt ->
              val seriesIndex = SeriesIndex(seriesIndexAsInt)
              @MayBeNaN @Domain val value = categoryModel.valueAt(categoryIndex, seriesIndex)
              @MayBeNaN @DomainRelative val relativeValue = configuration.valueRange.toDomainRelative(value)

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
          val categoryIndex = categoryLinesLayer.style.activeCategoryIndex ?: throw IllegalArgumentException("No category index found")
          val categoryModel = configuration.filteredCategorySeriesModel

          @Domain val value = categoryModel.valueAt(categoryIndex, SeriesIndex(index))

          return configuration.crossWireValueLabelFormat.format(value)
        }
      },
    )
  ) {
    wireWidth = 2.0

    //Hide the cross wire line - the line is painted by crossWireLayerBackground
    showCrossWireLine = false
    locationX = crossWireLineLayer.style.locationX
  }

  /**
   * Returns the active category index - or null if no category is active
   */
  var activeCategoryIndexOrNull: CategoryIndex?
    get() = categoryLinesLayer.style.activeCategoryIndex
    private set(value) {
      categoryLinesLayer.style.activeCategoryIndex = value
    }

  /**
   * Returns the active category index.
   * Throws an exception if no category is active!
   */
  val activeCategoryIndex: CategoryIndex
    get() = activeCategoryIndexOrNull ?: throw IllegalStateException("No active category index found")


  val balloonTooltipSupport: CategorySeriesModelBalloonTooltipSupport = CategorySeriesModelBalloonTooltipSupport(
    CategoryBalloonTooltipPlacementSupport(
      orientation = { Orientation.Horizontal },
      activeCategoryIndexProvider = ::activeCategoryIndexOrNull,
      categorySize = { crossWireLineLayer.style.wireWidth },
      boxLayout = { categoryLinesLayer.paintingVariables().layout }
    ),
    { configuration.filteredCategorySeriesModel },
    valueFormat = { configuration.balloonTooltipValueLabelFormat },
    colors = categoryLinesLayer.style::lineStyles.delegate().mapped { it.color }
  )


  /**
   * Shows the tooltips as balloon.
   * This layer uses the [balloonTooltipSupport] to paint the content of the tooltip
   */
  val balloonTooltipLayer: BalloonTooltipLayer = balloonTooltipSupport.createTooltipLayer()


  val valueAxisSupport: ValueAxisSupport<Unit> = ValueAxisSupport.single(
    { configuration.valueRange }
  ) {
    valueAxisConfiguration = { _, _, _ ->
      tickOrientation = Vicinity.Outside
      paintRange = AxisStyle.PaintRange.ContentArea
      ticksFormat = defaultNumberFormat
    }
  }

  /**
   * The value axis - backed by the [valueAxisSupport]
   */
  val valueAxisLayer: ValueAxisLayer
    get() {
      return valueAxisSupport.getValueAxisLayer()
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

  /**
   * The grid layer for the value axis
   */
  val valuesGridLayer: DomainRelativeGridLayer = valueAxisLayer.createGrid {}

  /**
   * The layer that paints the labels of the bars
   */
  val categoryAxisLayer: CategoryAxisLayer = categoryLinesLayer.createAxisLayer(configuration::filteredCategorySeriesModel.createCategoryLabelsProvider()) {
    tickOrientation = Vicinity.Outside
    side = Side.Bottom
    tickLabelGap = 7.0
    hideTicks()
  }

  /**
   * The grid layer for the categories
   */
  val categoriesGridLayer: GridLayer = categoryAxisLayer.createGrid {}

  init {
    fixedChartGestalt.contentViewportMarginProperty.consumeImmediately {
      valuesGridLayer.configuration.passpartout = it

      categoriesGridLayer.data.applyPasspartout(it)

      valueAxisLayer.style.size = it[valueAxisLayer.style.side]
      categoryAxisLayer.style.size = it[categoryAxisLayer.style.side]
    }

    configuration.valueRangeProperty.consumeImmediately {
      categoryLinesLayer.style.valueRange = it
    }

    configuration.numberFormatProperty.consumeImmediately {
      valueAxisLayer.style.ticksFormat = it
    }

    configureBuilder { meisterChartBuilder: MeisterChartBuilder ->
      fixedChartGestalt.configure(meisterChartBuilder)

      meisterChartBuilder.configure {
        layers.addClearBackground()
        layers.addLayer(tooltipInteractionLayer.visibleIf(configuration.showTooltipsProperty))
        layers.addAboveBackground(valuesGridLayer.visibleIf(configuration.showValuesGridProperty))
        layers.addAboveBackground(categoriesGridLayer.visibleIf(configuration.showCategoriesGridProperty))


        //Visible for *all* tooltip types (CrossWire *and* Balloon)
        layers.addLayer(crossWireLineLayer.visibleIf { categoryLinesLayer.style.activeCategoryIndex != null }.clippedOnlyAxis())

        when (configuration.toolTipType) {
          ToolTipType.CrossWire -> {
            layers.addLayer(crossWireLabelsLayer.visibleIf { categoryLinesLayer.style.activeCategoryIndex != null }.clippedOnlyAxis())
          }

          ToolTipType.Balloon -> {
            layers.addLayer(balloonTooltipLayer.visibleIf { categoryLinesLayer.style.activeCategoryIndex != null }.clippedOnlyAxis())
          }
        }

        layers.addLayer(categoryAxisLayer)
        valueAxisSupport.addLayers(this)
        thresholdsSupport.addLayers(this)

        layers.addLayer(categoryLinesLayer.clippedOnlyAxis())

        layers.addVersionNumberHidden()
      }
    }
  }

  /**
   * Clips the layer only at the axis bounds
   */
  fun <T : Layer> T.clippedOnlyAxis(): ClippingLayer<T> {
    return this.clipped {
      /*
       * Only clip the sides where the axes are.
       * We must not clip the other sides (e.g. for labels)
       */
      val categoryAxisSide = categoryAxisLayer.style.side
      val valueAxisSide = valueAxisLayer.style.side

      contentViewportMargin.only(categoryAxisSide, valueAxisSide)
    }
  }

  @ConfigurationDsl
  inner class Configuration(
    /**
     * The current category model for this line chart.
     *
     * The category model must contain exactly one series.
     *
     * The number of categories does not matter.
     *
     * Attention: Think about using [filteredCategorySeriesModel] instead
     */
    var categorySeriesModel: CategorySeriesModel,

    /**
     * The tooltip type
     */
    val toolTipType: ToolTipType,
  ) {

    /**
     * The filtered category series model that returns [Double.NaN] for values that are hidden
     */
    val filteredCategorySeriesModel: CategorySeriesModel = object : CategorySeriesModel {
      override val numberOfCategories: Int
        get() = configuration.categorySeriesModel.numberOfCategories

      override val numberOfSeries: Int
        get() = configuration.categorySeriesModel.numberOfSeries

      override fun valueAt(categoryIndex: CategoryIndex, seriesIndex: SeriesIndex): @Domain @MayBeNaN Double {
        return if (isVisible(seriesIndex)) configuration.categorySeriesModel.valueAt(categoryIndex, seriesIndex) else Double.NaN
      }

      override fun categoryNameAt(categoryIndex: CategoryIndex, textService: TextService, i18nConfiguration: I18nConfiguration): String {
        return configuration.categorySeriesModel.categoryNameAt(categoryIndex, textService, i18nConfiguration)
      }
    }

    /**
     * Returns true if the given series is visible
     */
    fun isVisible(seriesIndex: SeriesIndex): Boolean = configuration.lineIsVisible.valueAt(seriesIndex.value)


    /**
     * Provides the threshold values
     */
    var thresholdValues: @Domain DoublesProvider = DoublesProvider.empty

    /**
     * Provides the threshold labels
     */
    var thresholdLabels: HudLabelsProvider = MultiProvider1 { index, _ -> listOf(decimalFormat.format(thresholdValues.valueAt(index))) }


    /**
     * Determines whether a line/data-series is visible (true) or not (false).
     */
    val lineIsVisibleProperty: ObservableObject<MultiProvider<SeriesIndex, Boolean>> = ObservableObject(MultiProvider.always(true))
    var lineIsVisible: MultiProvider<SeriesIndex, Boolean> by lineIsVisibleProperty


    /**
     * The value range to be used for this chart
     */
    val valueRangeProperty: ObservableObject<ValueRange> = ObservableObject(createDefaultValueRange())
    var valueRange: ValueRange by valueRangeProperty


    /**
     * The number format that is used for all formatting
     */
    val numberFormatProperty: ObservableObject<CachedNumberFormat> = ObservableObject(defaultNumberFormat)
    var numberFormat: CachedNumberFormat by numberFormatProperty

    /**
     * The min width/height of a category
     */
    var minCategorySize: @Zoomed Double = 40.0

    /**
     * The max width/height of a category
     */
    var maxCategorySize: @Zoomed Double? = 150.0

    val categoryGapProperty: @Zoomed ObservableObject<Double> = ObservableObject(0.0)
    var categoryGap: @Zoomed Double by categoryGapProperty

    /**
     * Whether to show tooltips
     */
    val showTooltipsProperty: ObservableBoolean = ObservableBoolean(true)
    var showTooltip: Boolean by showTooltipsProperty

    /**
     * Whether to show the grid lines for the value axis
     */
    val showValuesGridProperty: ObservableBoolean = ObservableBoolean(true)
    var showValuesGrid: Boolean by showValuesGridProperty

    /**
     * Whether to show the grid lines for the category axis
     */
    val showCategoriesGridProperty: ObservableBoolean = ObservableBoolean(false)
    var showCategoriesGrid: Boolean by showCategoriesGridProperty


    /**
     * Format used for the labels of the cross wire
     */
    var crossWireValueLabelFormat: CachedNumberFormat = decimalFormat


    var balloonTooltipValueLabelFormat: CachedNumberFormat = decimalFormat

    /**
     * Where the value axis title is painted
     */
    var axisTitleLocation: AxisTitleLocation
      get() = valueAxisSupport.preferredAxisTitleLocation
      set(value) {
        valueAxisSupport.preferredAxisTitleLocation = value
      }

    /**
     * Configures this chart to visualize the value axis title on top of the value axis
     */
    fun applyValueAxisTitleOnTop(contentViewportMarginTop: @px Double = 40.0) {
      axisTitleLocation = AxisTitleLocation.AtTop

      //Make space for the value axis title
      contentViewportMargin = contentViewportMargin.withTop(contentViewportMargin.top.coerceAtLeast(contentViewportMarginTop))
    }

    fun applyBalloonTooltipSymbolSize(symbolSize: Size) {
      balloonTooltipSupport.applyLegendSymbolSize(symbolSize)
    }
  }

  /**
   * Applies the given value range.
   * This method also updates styles in different layers to match the given value range.
   */
  fun applyValueRange(valueRange: ValueRange) {
    this.configuration.valueRange = valueRange

    //Update the value axis layer
    if (valueRange is LinearValueRange) {
      valueAxisLayer.style.applyLinearScale()
    } else {
      valueAxisLayer.style.applyLogarithmicScale()
    }
  }

  /**
   * Sets the given font for all tick labels of all axes
   */
  fun applyAxisTickFont(font: FontDescriptorFragment) {
    categoryAxisLayer.style.tickFont = font
    valueAxisLayer.style.tickFont = font
  }

  /**
   * Sets the given font for all titles of all axes
   */
  fun applyAxisTitleFont(font: FontDescriptorFragment) {
    categoryAxisLayer.style.titleFont = font
    valueAxisLayer.style.titleFont = font
  }


  companion object {
    private fun createDefaultCategoryModel(): CategorySeriesModel = DefaultCategorySeriesModel(
      listOf(
        Category(TextKey.simple("Jan")),
        Category(TextKey.simple("Feb")),
        Category(TextKey.simple("Mar")),
        Category(TextKey.simple("Apr")),
        Category(TextKey.simple("May")),
        Category(TextKey.simple("Jun")),
        Category(TextKey.simple("Jul")),
        Category(TextKey.simple("Aug")),
        Category(TextKey.simple("Sep")),
        Category(TextKey.simple("Oct")),
        Category(TextKey.simple("Nov")),
        Category(TextKey.simple("Dec"))
      ),
      listOf(
        DefaultSeries("Series 1", listOf(59.8, 52.2, 65.7, 66.1, 78.9, 65.2, 68.5, 75.2, 91.3, 89.2, 98.7, 92.3)),
        DefaultSeries("Series 2", listOf(49.8, 72.2, 55.7, 67.1, 71.9, 75.2, 18.5, 25.2, 31.3, 29.2, 18.7, 72.3)),
        DefaultSeries("Series 3 - with NaN", listOf(39.8, 23.2, 47.7, 21.1, Double.NaN, 35.2, 46.5, 22.2, 31.1, 19.2, 68.7, 92.3)),
      )
    )

    private fun createDefaultValueRange(): ValueRange = ValueRange.linear(0.0, 110.0)

    private val defaultNumberFormat: CachedNumberFormat = intFormat

    private fun createDefaultThresholds(): SizedProvider<Threshold> {
      return SizedProvider.forValues(
        Threshold(35.0, listOf(TextKey.simple("Min"), TextKey.simple(defaultNumberFormat.format(35.0)))),
        Threshold(95.0, listOf(TextKey.simple("Max"), TextKey.simple(defaultNumberFormat.format(95.0)))),
      )
    }
  }
}
