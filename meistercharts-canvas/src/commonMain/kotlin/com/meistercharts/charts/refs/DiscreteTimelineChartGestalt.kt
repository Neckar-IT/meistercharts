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
package com.meistercharts.charts.refs

import com.meistercharts.algorithms.KeepOriginOnWindowResize
import com.meistercharts.algorithms.TimeRange
import com.meistercharts.algorithms.axis.AxisSelection
import com.meistercharts.algorithms.impl.DelegatingZoomAndTranslationDefaults
import com.meistercharts.algorithms.impl.FittingWithMargin
import com.meistercharts.algorithms.impl.MoveDomainValueToLocation
import com.meistercharts.algorithms.layers.AxisStyle
import com.meistercharts.algorithms.layers.HistoryReferenceEntryLayer
import com.meistercharts.algorithms.layers.TimeAxisLayer
import com.meistercharts.algorithms.layers.addClearBackground
import com.meistercharts.algorithms.layers.barchart.CategoryAxisLayer
import com.meistercharts.algorithms.layers.barchart.DefaultCategoryAxisLabelPainter
import com.meistercharts.algorithms.layers.barchart.LabelWrapMode
import com.meistercharts.algorithms.layers.clippedToContentViewport
import com.meistercharts.algorithms.layers.debug.addVersionNumberHidden
import com.meistercharts.algorithms.layers.visibleIf
import com.meistercharts.algorithms.painter.Color
import com.meistercharts.algorithms.painter.stripe.refentry.ReferenceEntryStripePainter
import com.meistercharts.algorithms.tile.DefaultHistoryGapCalculator
import com.meistercharts.algorithms.tile.HistoryGapCalculator
import com.meistercharts.algorithms.tile.HistoryRenderPropertiesCalculatorLayer
import com.meistercharts.algorithms.tile.MinDistanceSamplingPeriodCalculator
import com.meistercharts.algorithms.tile.withMinimum
import com.meistercharts.annotations.WindowRelative
import com.meistercharts.annotations.Zoomed
import com.meistercharts.canvas.ChartSupport
import com.meistercharts.canvas.translateOverTime
import com.meistercharts.charts.AbstractChartGestalt
import com.meistercharts.charts.ChartRefreshGestalt
import com.meistercharts.charts.ContentViewportGestalt
import com.meistercharts.charts.timeline.TimeLineChartGestalt
import com.meistercharts.history.EnumDataSeriesIndexProvider
import com.meistercharts.history.HistoryConfiguration
import com.meistercharts.history.HistoryStorage
import com.meistercharts.history.InMemoryHistoryStorage
import com.meistercharts.history.ObservableHistoryStorage
import com.meistercharts.history.ReferenceEntryDataSeriesIndex
import com.meistercharts.history.ReferenceEntryDataSeriesIndexProvider
import com.meistercharts.history.SamplingPeriod
import com.meistercharts.history.atMost
import com.meistercharts.history.valueAt
import com.meistercharts.model.Insets
import com.meistercharts.model.Side
import com.meistercharts.model.Vicinity
import com.meistercharts.provider.SizedLabelsProvider
import com.meistercharts.provider.delegate
import it.neckar.open.dispose.DisposeSupport
import it.neckar.open.i18n.I18nConfiguration
import it.neckar.open.i18n.TextKey
import it.neckar.open.i18n.TextService
import it.neckar.open.i18n.resolve
import it.neckar.open.kotlin.lang.isPositive
import it.neckar.open.kotlin.lang.percent
import it.neckar.open.observable.ObservableBoolean
import it.neckar.open.observable.ObservableDouble
import it.neckar.open.observable.ObservableObject
import it.neckar.open.observable.ReadOnlyObservableObject
import it.neckar.open.provider.MultiProvider
import it.neckar.open.time.TimeConstants
import it.neckar.open.time.nowMillis
import it.neckar.open.unit.number.Positive
import it.neckar.open.unit.other.px
import it.neckar.open.unit.si.ms

/**
 * Visualizes (only) reference entries.
 */
class DiscreteTimelineChartGestalt(
  /**
   * The history storage this chart is based on
   */
  val historyStorage: ObservableHistoryStorage = InMemoryHistoryStorage(),
  /**
   * The history configuration
   */
  historyConfiguration: () -> HistoryConfiguration = { HistoryConfiguration.empty },

  additionalConfiguration: Configuration.() -> Unit = {},
) : AbstractChartGestalt() {

  val configuration: Configuration = Configuration(historyStorage, historyConfiguration).also(additionalConfiguration)

  val disposeSupport: DisposeSupport = DisposeSupport()


  /**
   * Configures the refresh rate of the chart
   */
  val chartRefreshGestalt: ChartRefreshGestalt = ChartRefreshGestalt()

  /**
   * Is used to calculate the history render properties
   */
  val historyRenderPropertiesCalculatorLayer: HistoryRenderPropertiesCalculatorLayer = HistoryRenderPropertiesCalculatorLayer(
    samplingPeriodCalculator = MinDistanceSamplingPeriodCalculator(MinDistanceBetweenDataPoints).withMinimum { configuration.minimumSamplingPeriod },
    historyGapCalculator = { renderedSamplingPeriod ->
      configuration.historyGapCalculator.calculateMinGapDistance(renderedSamplingPeriod)
    },
    contentAreaTimeRange = { configuration.contentAreaTimeRange }
  )

  /**
   * Paints the reference entries
   */
  val historyReferenceEntryLayer: HistoryReferenceEntryLayer = HistoryReferenceEntryLayer(HistoryReferenceEntryLayer.Configuration(
    historyStorage = historyStorage,
    historyConfiguration = configuration::historyConfiguration.delegate(),
    visibleIndices = configuration.actualVisibleReferenceEntrySeriesIndices,
    contentAreaTimeRange = { configuration.contentAreaTimeRange }
  ))

  /**
   * The stripe painters that are used by the [historyReferenceEntryLayer]
   */
  var referenceEntryStripePainters: MultiProvider<ReferenceEntryDataSeriesIndex, ReferenceEntryStripePainter> //Delegate does not work 2023-03-21, results in a compile error
    get() {
      return historyReferenceEntryLayer.configuration.stripePainters
    }
    set(value) {
      historyReferenceEntryLayer.configuration.stripePainters = value
    }

  /**
   * The time axis layer
   */
  val timeAxisLayer: TimeAxisLayer = TimeAxisLayer {
    side = Side.Bottom
  }

  /**
   * Paints the axis for the reference data series.
   *
   * ATTENTION: This layer only works with *visible* sizes/providers.
   */
  val categoryAxisLayer: CategoryAxisLayer = CategoryAxisLayer(
    CategoryAxisLayer.Data(
      labelsProvider = object : SizedLabelsProvider {
        override fun size(param1: TextService, param2: I18nConfiguration): Int {
          return historyReferenceEntryLayer.configuration.visibleIndices.size()
        }

        override fun valueAt(index: Int, param1: TextService, param2: I18nConfiguration): String {
          val dataSeriesIndex: ReferenceEntryDataSeriesIndex = this@DiscreteTimelineChartGestalt.configuration.actualVisibleReferenceEntrySeriesIndices.valueAt(index)
          val labelTextKey = this@DiscreteTimelineChartGestalt.configuration.referenceEntryCategoryAxisLabelProvider.valueAt(dataSeriesIndex)
          return labelTextKey.resolve(param1, param2)
        }
      },
      layoutProvider = {
        historyReferenceEntryLayer.paintingVariables().stripesLayout
      }
    ),
  ) {
    side = Side.Left

    tickOrientation = Vicinity.Outside
    paintRange = AxisStyle.PaintRange.Continuous
    background = {
      configuration.valueAxesBackground
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
   * Calculates the view port bottom margin
   */
  private fun viewportMarginBottom(): @Zoomed Double {
    return if (configuration.showTimeAxis) {
      timeAxisLayer.style.size + timeAxisLayer.style.margin.bottom
    } else {
      0.0
    }
  }

  /**
   * The viewport for the complete diagram.
   * View port does *not* contain:
   * - space at top (e.g. for title)
   * - space at bottom for time axis
   */
  private val contentViewportGestalt = ContentViewportGestalt(Insets.of(0.0, 0.0, viewportMarginBottom(), 0.0)).also { contentViewportGestalt ->
    configuration.showTimeAxisProperty.consumeImmediately { _ ->
      contentViewportGestalt.contentViewportMargin = Insets.of(0.0, 0.0, viewportMarginBottom(), 0.0)
    }
  }

  var contentViewportMargin: Insets by contentViewportGestalt::contentViewportMargin


  /**
   * Sets the insets of the TranslateOverTimeService in accordance with the position of the cross wire along the x-axis
   */
  private fun updateTranslateOverTime(chartSupport: ChartSupport) {
    @Zoomed val insetsRight = chartSupport.currentChartState.windowSize.width * (1.0 - configuration.nowPositionX)
    chartSupport.translateOverTime.insets = Insets.onlyRight(insetsRight)
  }

  init {
    configureBuilder { meisterChartBuilder ->
      chartRefreshGestalt.configure(meisterChartBuilder)
      contentViewportGestalt.configure(meisterChartBuilder)

      meisterChartBuilder.onDispose(disposeSupport)
      meisterChartBuilder.configureAsTimeChart()

      meisterChartBuilder.zoomAndTranslationConfiguration {
        translateAxisSelection = AxisSelection.X //disable vertical translation
        zoomWithoutModifier()
      }

      meisterChartBuilder.zoomAndTranslationModifier {
        disableZoomY()
        disableTranslationY()
      }

      meisterChartBuilder.zoomAndTranslationDefaults {
        DelegatingZoomAndTranslationDefaults(
          MoveDomainValueToLocation(
            domainRelativeValueProvider = { configuration.contentAreaTimeRange.time2relative(nowMillis()) },
            targetLocationProvider = { chartCalculator -> chartCalculator.windowRelative2WindowX(configuration.nowPositionX) }
          ),
          FittingWithMargin { contentViewportGestalt.contentViewportMargin }
        )
      }
    }

    configure {
      chartSupport.windowResizeBehavior = KeepOriginOnWindowResize //keep the top left corner

      historyStorage.observe { historyBucketDescriptor, updateInfo ->
        //TODO optimize! Only repaint if necessary
        this.markAsDirty()
      }

      configuration.contentAreaTimeRangeProperty.consumeImmediately {
        chartSupport.translateOverTime.contentAreaTimeRangeX = it
      }
      chartSupport.rootChartState.windowSizeProperty.consumeImmediately {
        updateTranslateOverTime(chartSupport)
      }
      configuration.nowPositionXProperty.consumeImmediately {
        updateTranslateOverTime(chartSupport)
      }

      contentViewportGestalt

      layers.addClearBackground()
      layers.addLayer(historyRenderPropertiesCalculatorLayer)
      layers.addLayer(historyReferenceEntryLayer.clippedToContentViewport())
      layers.addLayer(categoryAxisLayer)

      layers.addLayer(timeAxisLayer.visibleIf { configuration.showTimeAxis })

      layers.addVersionNumberHidden()
    }
  }

  class Configuration(
    /**
     * The history storage this chart is based on
     */
    val historyStorage: HistoryStorage,

    /**
     * The history configuration
     */
    var historyConfiguration: () -> HistoryConfiguration,
  ) {


    val nowPositionXProperty: @WindowRelative ObservableDouble = ObservableDouble(95.percent)
    var nowPositionX: @WindowRelative Double by nowPositionXProperty

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

    var minimumSamplingPeriod: SamplingPeriod = TimeLineChartGestalt.defaultMinimumSamplingPeriod

    var historyGapCalculator: HistoryGapCalculator = DefaultHistoryGapCalculator()

    val showTimeAxisProperty: ObservableBoolean = ObservableBoolean(true)
    var showTimeAxis: Boolean by showTimeAxisProperty

    /**
     * The background color of the value axes
     */
    var valueAxesBackground: Color = Color.web("rgba(255,255,255,0.5)")


    /**
     * The indices of the referenceEntryIds that are visible (one stripe is shown for each visible referenceEntryId series).
     *
     * ATTENTION: Might contain *more* elements than there exist in the history!
     */
    val requestedVisibleReferenceEntrySeriesIndicesProperty: ObservableObject<ReferenceEntryDataSeriesIndexProvider> = ObservableObject(ReferenceEntryDataSeriesIndexProvider.indices { 10 })

    var requestVisibleReferenceEntrySeriesIndices: ReferenceEntryDataSeriesIndexProvider by requestedVisibleReferenceEntrySeriesIndicesProperty
      @Deprecated("Do not read! Use actualVisibleDecimalSeriesIndices instead", level = DeprecationLevel.WARNING)
      get

    fun showAllReferenceEntrySeries() {
      requestVisibleReferenceEntrySeriesIndices = ReferenceEntryDataSeriesIndexProvider.indices { historyConfiguration().referenceEntryDataSeriesCount }
    }

    val actualVisibleReferenceEntrySeriesIndices: ReferenceEntryDataSeriesIndexProvider = ::requestVisibleReferenceEntrySeriesIndices.atMost {
      historyConfiguration().referenceEntryDataSeriesCount
    }

    /**
     * Provides the labels for referenceEntry categories
     */
    var referenceEntryCategoryAxisLabelProvider: MultiProvider<ReferenceEntryDataSeriesIndex, TextKey> = MultiProvider.invoke { dataSeriesIndexAsInt ->
      val dataSeriesIndex = ReferenceEntryDataSeriesIndex(dataSeriesIndexAsInt)

      //The default implementation returns the display name from the history configuration
      historyConfiguration().referenceEntryConfiguration.getDisplayName(dataSeriesIndex)
    }
  }

  companion object {
    /**
     * The minimum distance between two data points for this chart.
     * If there is less space available, the next sampling level will be chosen
     */
    const val MinDistanceBetweenDataPoints: @px Double = 30.0
  }
}

