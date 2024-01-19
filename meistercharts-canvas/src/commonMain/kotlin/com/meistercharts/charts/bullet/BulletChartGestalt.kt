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
package com.meistercharts.charts.bullet

import com.meistercharts.range.LinearValueRange
import com.meistercharts.resize.ResetToDefaultsOnWindowResize
import com.meistercharts.range.ValueRange
import com.meistercharts.algorithms.layers.AxisConfiguration
import com.meistercharts.algorithms.layers.AxisTitleLocation
import com.meistercharts.algorithms.layers.AxisTopTopTitleLayer
import com.meistercharts.algorithms.layers.DefaultCategoryLayouter
import com.meistercharts.algorithms.layers.DomainRelativeGridLayer
import com.meistercharts.algorithms.layers.GridLayer
import com.meistercharts.algorithms.layers.axis.HudLabelsProvider
import com.meistercharts.algorithms.layers.TooltipInteractionLayer
import com.meistercharts.algorithms.layers.axis.ValueAxisLayer
import com.meistercharts.algorithms.layers.addClearBackground
import com.meistercharts.algorithms.layers.addFillCanvasBackground
import com.meistercharts.algorithms.layers.barchart.CategoryAxisLayer
import com.meistercharts.algorithms.layers.barchart.CategoryChartOrientation
import com.meistercharts.algorithms.layers.barchart.CategoryLayer
import com.meistercharts.algorithms.layers.clipped
import com.meistercharts.algorithms.layers.createGrid
import com.meistercharts.algorithms.layers.linechart.LineStyle
import com.meistercharts.algorithms.layers.visibleIf
import com.meistercharts.algorithms.layers.axis.withMaxNumberOfTicks
import com.meistercharts.model.category.CategoryIndex
import com.meistercharts.model.category.valueAt
import com.meistercharts.color.Color
import com.meistercharts.algorithms.tooltip.balloon.BalloonTooltipLayer
import com.meistercharts.algorithms.tooltip.balloon.BulletChartBalloonTooltipSupport
import com.meistercharts.algorithms.tooltip.balloon.CategoryBalloonTooltipPlacementSupport
import com.meistercharts.annotations.Domain
import com.meistercharts.annotations.DomainRelative
import com.meistercharts.annotations.Zoomed
import com.meistercharts.canvas.DirtyReason
import com.meistercharts.font.FontDescriptorFragment
import com.meistercharts.canvas.MeisterchartBuilder
import com.meistercharts.charts.AbstractChartGestalt
import com.meistercharts.charts.FixedChartGestalt
import com.meistercharts.charts.support.CategoryAxisSupport
import com.meistercharts.charts.support.threshold.ThresholdsSupport
import com.meistercharts.charts.support.ValueAxisSupport
import com.meistercharts.charts.support.addLayers
import com.meistercharts.charts.support.threshold.addLayers
import com.meistercharts.charts.support.createCategoryAxisSupport
import com.meistercharts.charts.support.getAxisLayer
import com.meistercharts.charts.support.getTopTitleLayer
import com.meistercharts.charts.support.threshold.thresholdsSupportSingle
import com.meistercharts.model.Insets
import it.neckar.geometry.Orientation
import it.neckar.geometry.Side
import it.neckar.geometry.Size
import com.meistercharts.model.Vicinity
import it.neckar.open.kotlin.lang.asProvider1
import it.neckar.open.provider.DoublesProvider
import it.neckar.open.provider.MultiProvider
import it.neckar.open.provider.MultiProvider1
import it.neckar.open.provider.delegate
import it.neckar.open.formatting.CachedNumberFormat
import it.neckar.open.formatting.decimalFormat
import it.neckar.open.i18n.I18nConfiguration
import it.neckar.open.i18n.TextService
import it.neckar.open.unit.number.MayBeNaN
import it.neckar.open.unit.other.px

/**
 * Attention: Does *not* correspond to the bullet chart as described by wikipedia!
 *
 * The current value is visualized as horizontal line!
 */
class BulletChartGestalt constructor(
  /**
   * Provides the current values that are visualized in the bullet charts.
   *
   * Returns NaN if no value is available
   */
  initialCurrentValues: @Domain @MayBeNaN DoublesProvider = createDefaultCurrentValues(),

  initialAreaValueRanges: @Domain @MayBeNaN MultiProvider<CategoryIndex, LinearValueRange> = createDefaultValueRangeValues(),

  additionalConfiguration: Configuration.() -> Unit = {},
) : AbstractChartGestalt() {

  /**
   * The configuration for this gestalt
   */
  val configuration: Configuration = Configuration(initialCurrentValues, initialAreaValueRanges)
    .also(additionalConfiguration)

  private val fixedChartGestalt: FixedChartGestalt = FixedChartGestalt(Insets.of(10.0, 75.0, 40.0, 90.0))
  var contentViewportMargin: Insets by fixedChartGestalt::contentViewportMargin


  /**
   * The category   model that is responsible to paint the single bullet charts
   *
   * Each category represents a single chart.
   * The series represent the values within one bullet chart
   */
  val categoryModel: CategoryModelBulletChart = object : CategoryModelBulletChart {
    override val numberOfCategories: Int
      get() {
        return configuration.currentValues.size()
      }

    override fun currentValue(categoryIndex: CategoryIndex): @Domain Double {
      return configuration.currentValues.valueAt(categoryIndex.value)
    }

    override fun categoryNameAt(categoryIndex: CategoryIndex, textService: TextService, i18nConfiguration: I18nConfiguration): String {
      return configuration.categoryNames.valueAt(categoryIndex)
    }

    override fun barRange(categoryIndex: CategoryIndex): LinearValueRange? {
      return configuration.areaValueRanges.valueAt(categoryIndex)
    }
  }

  /**
   * Paints the single bullet chart
   */
  val bulletChartPainter: BulletChartPainter = BulletChartPainter {
    this.valueRange = { this@BulletChartGestalt.configuration.valueRange }
  }

  /**
   * Paints the bullets
   */
  val categoryLayer: CategoryLayer<CategoryModelBulletChart> = CategoryLayer({ categoryModel }) {
    orientation = CategoryChartOrientation.VerticalLeft
    categoryPainter = bulletChartPainter

    activeCategoryBackgroundSize = {
      //Use the actual size of the group
      bulletChartPainter.paintingVariables().actualSize + configuration.activeCategoryBackgroundMargin.offsetWidth
    }

    layoutCalculator = DefaultCategoryLayouter {
      minCategorySizeProvider = {
        val categoryWidth = bulletChartPainter.configuration.currentValueIndicatorSize.coerceAtLeast(
          bulletChartPainter.configuration.barSize
        )

        //we automatically get half a gap at the start and at the end
        categoryWidth + configuration.minCategoryGap
      }

      maxCategorySizeProvider = {
        val categoryWidth = bulletChartPainter.configuration.currentValueIndicatorSize.coerceAtLeast(
          bulletChartPainter.configuration.barSize
        )

        //we automatically get half a gap at the start and at the end
        categoryWidth + configuration.maxCategoryGap
      }
    }
  }

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

  /**
   * The grid layer for the value axis
   */
  val categoryAxisGridLayer: GridLayer = categoryAxisLayer.createGrid {
    lineStyles = configuration::categoryAxisGridLineStyles.delegate()
  }

  val valueAxisSupport: ValueAxisSupport<Unit> = ValueAxisSupport.single(
    { configuration.valueRange }
  ) {
    valueAxisConfiguration = { _, _, _ ->
      tickOrientation = Vicinity.Outside
      paintRange = AxisConfiguration.PaintRange.ContentArea
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

  /**
   * The grid layer for the value axis
   */
  val valueAxisGridLayer: DomainRelativeGridLayer = valueAxisLayer.createGrid {
    lineStyles = { value: @DomainRelative Double -> configuration.valueAxisGridLineStyles(configuration.valueRange.toDomain(value)) }
  }

  /**
   * Handles the mouse over - does *not* paint anything itself
   */
  val toolbarInteractionLayer: TooltipInteractionLayer<CategoryIndex> = TooltipInteractionLayer.forCategories(
    orientation = { configuration.orientation.layoutDirection.orientation },
    layoutProvider = { categoryLayer.paintingVariables().layout },
    selectionSink = { newSelection, chartSupport ->
      if (configuration.activeCategoryIndexOrNull != newSelection) {
        configuration.activeCategoryIndexOrNull = newSelection
        chartSupport.markAsDirty(DirtyReason.ActiveElementUpdated)
      }
    }
  )

  val balloonTooltipSupport: BulletChartBalloonTooltipSupport = BulletChartBalloonTooltipSupport(
    CategoryBalloonTooltipPlacementSupport(
      orientation = { configuration.orientation.categoryOrientation.opposite() }, //convert orientation of bars to orientation of category placement
      activeCategoryIndexProvider = configuration::activeCategoryIndexOrNull,
      categorySize = {
        val layout = categoryLayer.paintingVariables().layout
        categoryLayer.configuration.activeCategoryBackgroundSize(layout.boxSize)
      },
      boxLayout = {
        categoryLayer.paintingVariables().layout
      }
    ),
    model = { categoryModel },
    valueFormat = { configuration.balloonTooltipValueLabelFormat },
    currentValueSymbolColor = { bulletChartPainter.configuration.currentValueColor },
    barSymbolColor = { categoryIndex ->
      bulletChartPainter.configuration.barColors.valueAt(categoryIndex)
    }
  )

  /**
   * Shows the tooltips as balloon.
   */
  val balloonTooltipLayer: BalloonTooltipLayer = balloonTooltipSupport.createTooltipLayer()

  init {
    fixedChartGestalt.contentViewportMarginProperty.consumeImmediately {
      valueAxisGridLayer.configuration.passpartout = it
      categoryAxisGridLayer.configuration.applyPasspartout(it)

      valueAxisLayer.configuration.size = it[valueAxisLayer.configuration.side]
      categoryAxisLayer.configuration.size = it[categoryAxisLayer.configuration.side]
    }

    configureBuilder { meisterChartBuilder: MeisterchartBuilder ->
      fixedChartGestalt.configure(meisterChartBuilder)

      meisterChartBuilder.configure {
        chartSupport.windowResizeBehavior = ResetToDefaultsOnWindowResize

        layers.addClearBackground()
        layers.addFillCanvasBackground()
        valueAxisSupport.addLayers(this)
        thresholdsSupport.addLayers(this)

        layers.addAboveBackground(valueAxisGridLayer.visibleIf { configuration.showValuesGrid })
        layers.addAboveBackground(categoryAxisGridLayer.visibleIf { configuration.showCategoryGrid })

        layers.addLayer(toolbarInteractionLayer.visibleIf { configuration.showTooltips })

        layers.addLayer(categoryLayer.clipped {
          /*
           * Only clip the sides where the axes are.
           * We must not clip the other sides (e.g. for labels)
           */
          val categoryAxisSide = categoryAxisLayer.configuration.side
          val valueAxisSide = valueAxisLayer.configuration.side
          // FIXME: this is a workaround as long as the group-painter does not take the content area into account.
          val thresholdSide = if (configuration.orientation.categoryOrientation == Orientation.Vertical) Side.Right else Side.Top

          contentViewportMargin.only(categoryAxisSide, valueAxisSide, thresholdSide)
        })

        categoryAxisSupport.addLayers(this)

        layers.addLayer(balloonTooltipLayer.visibleIf {
          configuration.showTooltips && configuration.activeCategoryIndexOrNull != null
        })
      }
    }
  }

  inner class Configuration(
    /**
     * Provides the current values that are visualized in the bullet charts.
     *
     * Returns NaN if no value is available
     */
    var currentValues: @Domain @MayBeNaN DoublesProvider,

    /**
     * The area values.
     * Returns null if there is no area value range
     */
    var areaValueRanges: @Domain @MayBeNaN MultiProvider<CategoryIndex, LinearValueRange?>,
  ) {

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
    val activeCategoryBackgroundMargin: Insets = Insets.all5

    /**
     * Provides the category names
     */
    var categoryNames: MultiProvider<CategoryIndex, String> = MultiProvider {
      "Category $it"
    }

    /**
     * Returns the active category index - or null if no category is active
     */
    var activeCategoryIndexOrNull: CategoryIndex?
      get() = categoryLayer.configuration.activeCategoryIndex
      internal set(value) {
        categoryLayer.configuration.activeCategoryIndex = value
      }

    /**
     * Returns the active category index.
     * Throws an exception if no category is active!
     */
    val activeCategoryIndex: CategoryIndex
      get() = activeCategoryIndexOrNull ?: throw IllegalStateException("No active category index found")

    /**
     * Whether to show the grid - for the values axis
     */
    var showValuesGrid: Boolean = true

    /**
     * Whether to show the grid - for the category axis
     */
    var showCategoryGrid: Boolean = true

    /**
     * Returns the grid line style for a given domain value
     */
    var valueAxisGridLineStyles: (@Domain Double) -> LineStyle = LineStyle(color = Color.lightgray, lineWidth = 1.0).asProvider1()

    var categoryAxisGridLineStyles: MultiProvider<GridLayer.GridLine, LineStyle> = MultiProvider.always(LineStyle(color = Color.lightgray, lineWidth = 1.0))

    /**
     * The (min) gap between two categories
     */
    var minCategoryGap: @Zoomed Double = 10.0

    /**
     * The (max) gap between two categories
     */
    var maxCategoryGap: @Zoomed Double = 190.0

    /**
     * The value range for all values within this chart
     */
    var valueRange: @Domain ValueRange = ValueRange.linear(-10.0, 70.0)
      private set

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
      categoryAxisLayer.configuration.tickFont = font
      valueAxisLayer.configuration.tickFont = font
    }

    /**
     * Sets the given font for all titles of all axes
     */
    fun applyAxisTitleFont(font: FontDescriptorFragment) {
      categoryAxisLayer.configuration.titleFont = font
      valueAxisLayer.configuration.titleFont = font
    }

    var balloonTooltipValueLabelFormat: CachedNumberFormat = decimalFormat

    /**
     * Changes the chart orientation to horizontal.
     * This method modifies multiple layers and properties to match the new orientation
     */
    fun applyHorizontalConfiguration() {
      categoryLayer.configuration.orientation = CategoryChartOrientation.HorizontalTop
      categoryAxisLayer.configuration.side = Side.Left
      valueAxisLayer.configuration.side = Side.Bottom
      contentViewportMargin = Insets.of(40.0, 20.0, 40.0, 75.0)
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
    }


    /**
     * The orientation of the chart
     */
    val orientation: CategoryChartOrientation
      get() = categoryLayer.configuration.orientation

    var showTooltips: Boolean = true

    /**
     * Applies the updated balloon tooltip sizes
     */
    fun applyBalloonTooltipSizes(
      currentValueSymbolSize: Size,
      barSymbolSize: Size,
    ) {
      balloonTooltipSupport.applyBalloonTooltipSizes(currentValueSymbolSize, barSymbolSize)
    }
  }

  companion object {
    fun createDefaultCurrentValues(): @Domain @MayBeNaN DoublesProvider {
      return DoublesProvider.forValues(5.0, 88.0, 55.0, 43.0, 22.4)
    }

    fun createDefaultValueRangeValues(): MultiProvider<CategoryIndex, LinearValueRange> {
      return MultiProvider.modulo(
        ValueRange.linear(-15.0, 25.0),
        ValueRange.linear(30.0, 35.0),
        ValueRange.linear(48.0, 92.0),
        ValueRange.linear(11.0, 55.0),
        ValueRange.linear(33.4, 47.4),
      )
    }
  }
}
