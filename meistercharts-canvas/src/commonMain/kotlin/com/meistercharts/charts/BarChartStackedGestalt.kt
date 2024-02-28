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
import com.meistercharts.algorithms.layers.addClearBackground
import com.meistercharts.algorithms.layers.addFillCanvasBackground
import com.meistercharts.algorithms.layers.axis.ConstantTicksProvider
import com.meistercharts.algorithms.layers.axis.TickProvider
import com.meistercharts.algorithms.layers.axis.ValueAxisLayer
import com.meistercharts.algorithms.layers.barchart.CategoryAxisLayer
import com.meistercharts.algorithms.layers.barchart.CategoryChartOrientation
import com.meistercharts.algorithms.layers.barchart.CategoryLayer
import com.meistercharts.algorithms.layers.barchart.StackedBarsPainter
import com.meistercharts.algorithms.layers.clipped
import com.meistercharts.algorithms.layers.createGrid
import com.meistercharts.algorithms.layers.linechart.LineStyle
import com.meistercharts.algorithms.layers.visibleIf
import com.meistercharts.annotations.Domain
import com.meistercharts.annotations.DomainRelative
import com.meistercharts.canvas.ConfigurationDsl
import com.meistercharts.charts.support.CategoryAxisSupport
import com.meistercharts.charts.support.ValueAxisSupport
import com.meistercharts.charts.support.addLayers
import com.meistercharts.charts.support.createCategoryAxisSupport
import com.meistercharts.charts.support.getAxisLayer
import com.meistercharts.charts.support.getTopTitleLayer
import com.meistercharts.color.Color
import com.meistercharts.font.FontDescriptorFragment
import com.meistercharts.model.Insets
import com.meistercharts.model.Vicinity
import com.meistercharts.model.category.Category
import com.meistercharts.model.category.CategorySeriesModel
import com.meistercharts.model.category.DefaultCategorySeriesModel
import com.meistercharts.model.category.DefaultSeries
import com.meistercharts.model.category.createCategoryLabelsProvider
import com.meistercharts.range.LinearValueRange
import com.meistercharts.range.ValueRange
import it.neckar.geometry.Orientation
import it.neckar.geometry.Side
import it.neckar.open.i18n.TextKey
import it.neckar.open.kotlin.lang.asProvider
import it.neckar.open.kotlin.lang.asProvider1
import it.neckar.open.observable.ObservableBoolean
import it.neckar.open.unit.other.px
import kotlin.jvm.JvmOverloads

/**
 * Configuration for a bar chart with stacked bars
 */
class BarChartStackedGestalt @JvmOverloads constructor(
  /**
   * The current category model for the stacked bar chart
   */
  initialCategorySeriesModel: CategorySeriesModel = createDefaultCategoryModel(),
  additionalConfiguration: BarChartStackedGestalt.Configuration.() -> Unit = {},
) : AbstractChartGestalt() {

  /**
   * The style to be used
   */
  val configuration: Configuration = Configuration(initialCategorySeriesModel).also(additionalConfiguration)

  /**
   * Delegate the configures the chart to have a fixed zoom and translation
   */
  val fixedChartGestalt: FixedChartGestalt = FixedChartGestalt(Insets.of(10.0, 10.0, 40.0, 30.0))

  var contentViewportMargin: Insets by fixedChartGestalt::contentViewportMargin

  /**
   * The painter used by the [categoryLayer]
   */
  val stackedBarsPainter: StackedBarsPainter = StackedBarsPainter().apply {
    stackedBarPaintable.data.valueRange = createDefaultValueRange()
  }

  /**
   * The layer that paints the bars
   */
  val categoryLayer: CategoryLayer<CategorySeriesModel> = CategoryLayer({ configuration.categorySeriesModel }) {
    orientation = CategoryChartOrientation.VerticalLeft
    categoryPainter = stackedBarsPainter
    layoutCalculator = DefaultCategoryLayouter {
      minCategorySize = 40.0
      maxCategorySize = 150.0
    }
  }

  val categoryAxisSupport: CategoryAxisSupport<Unit> = categoryLayer.createCategoryAxisSupport {
    this.axisConfiguration = { _, _, _ ->
      tickOrientation = Vicinity.Outside
      side = Side.Bottom
      tickLabelGap = 5.0
      axisLineWidth = 2.0
      lineColor = Color("#C5CACC").asProvider()
      hideTicks()
    }
  }

  /**
   * The layer that paints the labels of the bars
   */
  val categoryAxisLayer: CategoryAxisLayer
    get() {
      return categoryAxisSupport.getAxisLayer()
    }

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
   * The value layer is *hidden* - only used to paint the 0 at the corresponding grid line
   */
  val valueAxisSupport: ValueAxisSupport<Unit> = ValueAxisSupport.single({ configuration.valueRange }) {
    valueAxisConfiguration = { _, _, _ ->
      tickOrientation = Vicinity.Outside
      paintRange = AxisConfiguration.PaintRange.ContentArea
      ticksFormat = BarChartGroupedGestalt.defaultNumberFormat
      side = Side.Left

      //Hide all visible elements for now
      hideAxisLine()
      hideTicks()
      ticks = ConstantTicksProvider.only0 //only show the 0.0
    }
  }

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
  val valueAxisTopTitleLayer: AxisTopTopTitleLayer = valueAxisSupport.getTopTitleLayer()

  /**
   * The layer that paints the line at the domain-value 0
   */
  val gridLayer: DomainRelativeGridLayer = valueAxisLayer.createGrid {
    lineStyles = { value: @DomainRelative Double -> configuration.gridLineStyles(configuration.valueRange.toDomain(value)) }
  }

  init {
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

      valueAxisSupport.addLayers(this, Unit)

      layers.addAboveBackground(gridLayer.visibleIf(configuration.showGridProperty))
      layers.addLayer(categoryLayer.clipped {
        /*
         * Only clip the sides where the axes are.
         * We must not clip the other sides (e.g. for labels)
         */
        val categoryAxisSide = categoryAxisLayer.configuration.side
        val valueAxisSide = valueAxisLayer.configuration.side

        it.chartState.contentViewportMargin.only(categoryAxisSide, valueAxisSide)
      })

      categoryAxisSupport.addLayers(this)
    }
  }

  @ConfigurationDsl
  open inner class Configuration(
    /**
     * The current category model for the stacked bar chart
     */
    var categorySeriesModel: CategorySeriesModel,
  ) {
    /**
     * The value range of the bar chart
     */
    var valueRange: LinearValueRange
      get() {
        return stackedBarsPainter.stackedBarPaintable.data.valueRange
      }
      set(value) {
        stackedBarsPainter.stackedBarPaintable.data.valueRange = value
      }

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
     * Makes the value axis visible
     */
    fun applyValueAxisVisible() {
      valueAxisLayer.configuration.showAxisLine()
      valueAxisLayer.configuration.showTicks()
      valueAxisLayer.configuration.ticks = TickProvider.linear

      when (valueAxisLayer.configuration.side) {
        Side.Left -> fixedChartGestalt.setMarginLeft(75.0)
        Side.Right -> fixedChartGestalt.setMarginRight(75.0)
        Side.Top -> fixedChartGestalt.setMarginTop(40.0)
        Side.Bottom -> fixedChartGestalt.setMarginBottom(40.0)
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
     * Sets the font to be used for the value labels
     */
    fun applyValueLabelFont(font: FontDescriptorFragment) {
      stackedBarsPainter.stackedBarPaintable.style.valueLabelFont = font
    }

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
     * Changes the chart orientation to horizontal
     * This method modifies multiple layers and properties to match the new orientation
     */
    fun applyHorizontalConfiguration() {
      categoryLayer.configuration.orientation = CategoryChartOrientation.HorizontalTop
      categoryAxisLayer.configuration.side = Side.Left
      valueAxisLayer.configuration.side = Side.Bottom
      stackedBarsPainter.stackedBarPaintable.style.applyOrientation(Orientation.Horizontal)
      contentViewportMargin = Insets.of(10.0, 10.0, 25.0, 80.0)
    }

    /**
     * Changes the chart orientation to vertical
     * This method modifies multiple layers and properties to match the new orientation
     */
    fun applyVerticalConfiguration() {
      categoryLayer.configuration.orientation = CategoryChartOrientation.VerticalLeft
      categoryAxisLayer.configuration.side = Side.Bottom
      valueAxisLayer.configuration.side = Side.Left
      stackedBarsPainter.stackedBarPaintable.style.applyOrientation(Orientation.Vertical)
      contentViewportMargin = Insets.of(10.0, 10.0, 40.0, 30.0)
    }

    /**
     * Applies the given value range.
     * This method also updates styles in different layers to match the given value range.
     */
    fun applyValueRange(valueRange: LinearValueRange) {
      this.valueRange = valueRange
      valueAxisLayer.configuration.applyLinearScale()
      stackedBarsPainter.stackedBarPaintable.data.valueRange = valueRange
    }

  }

  companion object {
    private fun createDefaultCategoryModel(): CategorySeriesModel = DefaultCategorySeriesModel(
      listOf(
        Category(TextKey.simple("A")),
        Category(TextKey.simple("B")),
        Category(TextKey.simple("C")),
        Category(TextKey.simple("D")),
        Category(TextKey.simple("E")),
        Category(TextKey.simple("F")),
        Category(TextKey.simple("G"))
      ),
      listOf(
        DefaultSeries("1", listOf(34.0, 47.0, 19.0, 5.0, 0.0, 0.0, 17.0)),
        DefaultSeries("2", listOf(7.0, 10.0, 5.0, 1.0, 0.0, 0.0, 20.0)),
        DefaultSeries("3", listOf(7.0, 5.0, 3.0, 0.0, 0.0, 0.0, 8.0)),
        DefaultSeries("4", listOf(0.0, 0.0, 0.0, 0.0, 2.0, 0.0, 7.0))
      )
    )

    private fun createDefaultValueRange(): LinearValueRange = ValueRange.linear(0.0, 62.0)
  }
}

