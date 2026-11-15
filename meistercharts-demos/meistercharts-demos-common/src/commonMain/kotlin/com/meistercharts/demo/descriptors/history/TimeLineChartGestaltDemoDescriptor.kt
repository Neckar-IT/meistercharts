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
package com.meistercharts.demo.descriptors.history

import com.meistercharts.demo.configurable
import kotlin.uuid.Uuid
import com.meistercharts.algorithms.layers.AxisConfiguration
import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.algorithms.layers.TilesLayer
import com.meistercharts.algorithms.layers.axis.HudElementIndex
import com.meistercharts.algorithms.layers.axis.ValueAxisLayer
import com.meistercharts.algorithms.layers.barchart.DefaultCategoryAxisLabelPainter
import com.meistercharts.algorithms.layers.linechart.Dashes
import com.meistercharts.algorithms.layers.linechart.LineStyle
import com.meistercharts.algorithms.layers.linechart.PointStyle
import com.meistercharts.algorithms.tile.CachedTileProvider
import com.meistercharts.algorithms.tile.DefaultHistoryGapCalculator
import com.meistercharts.algorithms.tile.MinDistanceSamplingPeriodCalculator
import com.meistercharts.algorithms.tile.withMinimum
import com.meistercharts.animation.Easing
import com.meistercharts.canvas.RoundingStrategy
import com.meistercharts.canvas.SnapConfiguration
import com.meistercharts.canvas.TargetRefreshRate
import com.meistercharts.canvas.pixelSnapSupport
import com.meistercharts.canvas.translateOverTime
import com.meistercharts.charts.timeline.TimeLineChartGestalt
import com.meistercharts.charts.timeline.setUpDemo
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.DemoQuality
import com.meistercharts.demo.MeisterchartsDemo
import com.meistercharts.demo.MeisterchartsDemoDescriptor
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.TimeBasedValueGeneratorBuilder
import com.meistercharts.demo.VariabilityType
import com.meistercharts.demo.configurableBoolean
import com.meistercharts.demo.configurableColorProvider
import com.meistercharts.demo.configurableDouble
import com.meistercharts.demo.configurableEnum
import com.meistercharts.demo.configurableFontProvider
import com.meistercharts.demo.configurableIndices
import com.meistercharts.demo.configurableList
import com.meistercharts.demo.configurableListWithProperty
import com.meistercharts.demo.section
import com.meistercharts.demo.toList
import com.meistercharts.demo.toMutableList
import com.meistercharts.design.Theme
import com.meistercharts.design.valueAt
import com.meistercharts.history.DataSeriesId
import com.meistercharts.history.DecimalDataSeriesIndex
import com.meistercharts.history.DecimalDataSeriesIndexProvider
import com.meistercharts.history.EnumDataSeriesIndex
import com.meistercharts.history.EnumDataSeriesIndexProvider
import com.meistercharts.history.HistoryConfiguration
import com.meistercharts.history.HistoryConfigurationBuilder
import com.meistercharts.history.HistoryEnum
import com.meistercharts.history.HistoryStorageCache
import com.meistercharts.history.HistoryStorageQueryMonitor
import com.meistercharts.history.HistoryUnit
import com.meistercharts.history.InMemoryHistoryStorage
import com.meistercharts.history.SamplingPeriod
import com.meistercharts.history.WritableHistoryStorage
import com.meistercharts.history.cleanup.MaxHistorySizeConfiguration
import com.meistercharts.history.generator.DecimalValueGenerator
import com.meistercharts.history.generator.EnumValueGenerator
import com.meistercharts.history.generator.HistoryChunkGenerator
import com.meistercharts.history.generator.ReferenceEntryGenerator
import com.meistercharts.history.historyConfiguration
import com.meistercharts.history.impl.DecimalHistoryValues
import com.meistercharts.history.impl.HistoryChunk
import com.meistercharts.history.impl.chunk
import com.meistercharts.history.withQueryMonitor
import com.meistercharts.model.Vicinity
import com.meistercharts.painter.PointPainter
import com.meistercharts.painter.PointStylePainter
import com.meistercharts.range.ValueRange
import com.meistercharts.time.TimeRange
import it.neckar.datetime.minimal.TimeConstants.millisPerCentury
import it.neckar.datetime.minimal.TimeConstants.millisPerDecade
import it.neckar.geometry.Side
import it.neckar.logging.Logger
import it.neckar.logging.LoggerFactory
import it.neckar.logging.trace
import it.neckar.open.collections.map2
import it.neckar.open.formatting.format
import it.neckar.open.i18n.TextKey
import it.neckar.open.kotlin.lang.fastMap
import it.neckar.open.kotlin.lang.getModulo
import it.neckar.open.kotlin.lang.random
import it.neckar.open.provider.DoublesProvider1
import it.neckar.open.provider.MultiProvider
import it.neckar.open.provider.MultiProvider2
import it.neckar.open.provider.cached
import it.neckar.open.time.nowMillis
import it.neckar.open.unit.si.ms
import kotlin.math.roundToInt
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes

typealias TimelineChartGestaltDemoConfiguration = TimeLineChartGestalt.(historyStorage: InMemoryHistoryStorage) -> Unit

class TimeLineChartGestaltDemoDescriptor : MeisterchartsDemoDescriptor<TimelineChartGestaltDemoConfiguration> {
  override val uuid: Uuid = Uuid.parse("b174cc32-e64d-4e1d-84f0-4bd0a412f276")
  override val name: String = "Time Line Chart"
  override val category: DemoCategory = DemoCategory.Gestalt

  override val quality: DemoQuality = DemoQuality.High
  override val variabilityType: VariabilityType = VariabilityType.Stable

  override val predefinedConfigurations: List<PredefinedConfiguration<TimelineChartGestaltDemoConfiguration>> = listOf(
    neckarITHomePage,
    PredefinedConfiguration(Uuid.parse("dddd8f08-855d-4b6d-a140-d1a8a9ec3f36"), { this.setUpDemo(it) }, "demo setup"),
    oneSampleEvery100ms,
    oneSampleEvery100msSick,
    oneSampleEvery100msPoints,
    oneSampleEvery100msLogarithmic,
    oneSampleEvery16msCached500ms,
    oneSampleEvery16msCached50ms,
    oneSampleEvery100msCached100ms,
    oneSampleEvery24h,
    oneSampleEvery16msCached500msAverages,
    candle,
    minMaxArea,
    minMaxAreaPoints,
    withAxisTitle,
    outwardsTicks,
    valueAxisTitleOnTop,
    PredefinedConfiguration(Uuid.parse("66baa35f-ee62-4dc2-a788-f4fc6c333cbe"), {}, "empty"),
    _100years,
    dashed,
    manualDownSampling,
  )

  override fun prepareDemo(configuration: PredefinedConfiguration<TimelineChartGestaltDemoConfiguration>?): MeisterchartsDemo {
    require(configuration != null) { "config required" }
    val gestaltConfig: TimelineChartGestaltDemoConfiguration = configuration.payload


    return MeisterchartsDemo(this) {
      meistercharts {
        val historyStorage = InMemoryHistoryStorage().also {
          it.maxSizeConfiguration = MaxHistorySizeConfiguration(7)
          onDispose(it)
        }

        historyStorage.scheduleDownSampling()
        historyStorage.scheduleCleanupService()

        val historyStorageQueryMonitor = historyStorage.withQueryMonitor()
        historyStorageQueryMonitor.onQuery {
          MeisterchartsDemo.logger.trace { "query($it)" }
        }

        val gestalt = TimeLineChartGestalt(historyStorageQueryMonitor)

        val historyChunkBuilder = MyHistoryChunkBuilder {
          gestalt.configuration.historyConfiguration
        }

        gestalt.gestaltConfig(historyStorage)
        gestalt.configure(this)

        configure {
          configurable("Play Mode", chartSupport.translateOverTime::animated) {
            value = true
          }

          configurableList(
            "Content area duration (sec)", (gestalt.configuration.contentAreaDuration / 1000.0).roundToInt(), listOf(
              10,
              60,
              60 * 10,
              60 * 60,
              60 * 60 * 24
            )
          ) {
            onChange {
              gestalt.configuration.contentAreaDuration = it * 1000.0
              markAsDirty()
            }
          }

          configurableDouble("Cross-wire location", gestalt.configuration.crossWirePositionXProperty)

          val visibleDecimalLines = gestalt.configuration.requestedVisibleDecimalSeriesIndices.toMutableList().toMutableSet()
          if (visibleDecimalLines.isNotEmpty()) {
            section("Visible lines")

            visibleDecimalLines.forEach { lineIndex ->
              configurableBoolean("${lineIndex.value + 1}. line visible") {
                value = true
                onChange {
                  if (it) {
                    visibleDecimalLines.add(lineIndex)
                  } else {
                    visibleDecimalLines.remove(lineIndex)
                  }
                  gestalt.configuration.requestedVisibleDecimalSeriesIndices = DecimalDataSeriesIndexProvider.forList(visibleDecimalLines.toList())
                  markAsDirty()
                }
              }
            }
          }

          configurableList("Visible value axes", -1, listOf(0, 1, 2, 5, 8, 10, 15)) {
            converter { if (it == -1) "initial" else it.toString() }
            onChange { visibleValueAxes ->
              if (visibleValueAxes == -1) {
                // initial -> do nothing
              } else {
                gestalt.configuration.requestedVisibleValueAxesIndices = DecimalDataSeriesIndexProvider.indices { visibleValueAxes }
                markAsDirty()
              }
            }
          }

          val enumDataSeriesCount = gestalt.configuration.historyConfiguration.enumDataSeriesCount
          if (enumDataSeriesCount.value > 0) {
            declare {
              button("Show all enums") {
                gestalt.configuration.requestVisibleEnumSeriesIndices = EnumDataSeriesIndexProvider.indices {
                  gestalt.configuration.historyConfiguration.enumDataSeriesCount.value
                }
              }
              button("Show 100 all enums") {
                gestalt.configuration.requestVisibleEnumSeriesIndices = EnumDataSeriesIndexProvider.indices {
                  100
                }
              }
              button("Hide all enums") {
                gestalt.configuration.requestVisibleEnumSeriesIndices = EnumDataSeriesIndexProvider.empty()
              }
            }

            configurableIndices(
              this@MeisterchartsDemo,
              "Visible Enum lines",
              "enum visible",
              initial = gestalt.configuration.requestVisibleEnumSeriesIndices.toList().map { it.value },
              maxSize = enumDataSeriesCount.value,
            ) {
              gestalt.configuration.requestVisibleEnumSeriesIndices = EnumDataSeriesIndexProvider.forList(it.map { EnumDataSeriesIndex(it) })
            }
          }

          declare {
            section("Samples / Data")

            button("Add sample") {
              historyStorage.storeWithoutCache(historyChunkBuilder.createHistoryChunk(1, SamplingPeriod.EveryHundredMillis), SamplingPeriod.EveryHundredMillis)
            }
            button("Add 10 samples (100ms)") {
              historyStorage.storeWithoutCache(historyChunkBuilder.createHistoryChunk(10, SamplingPeriod.EveryHundredMillis), SamplingPeriod.EveryHundredMillis)
            }
            button("Add 100 samples (100ms)") {
              historyStorage.storeWithoutCache(historyChunkBuilder.createHistoryChunk(100, SamplingPeriod.EveryHundredMillis), SamplingPeriod.EveryHundredMillis)
            }
          }

          configurable("Time axis tick font", gestalt.timeAxisLayer.configuration::tickFont) {
          }

          configurableListWithProperty("Refresh rate", chartSupport::targetRenderRate, TargetRefreshRate.predefined)
          configurableListWithProperty("Translation Anim Rounding", chartSupport.translateOverTime::roundingStrategy, RoundingStrategy.predefined) {
            converter {
              when (it) {
                RoundingStrategy.exact -> "exact"
                RoundingStrategy.round -> "1 px"
                RoundingStrategy.half -> "0.5 px"
                RoundingStrategy.quarter -> "0.25 px"
                RoundingStrategy.tenth -> "0.1 px"
                else -> it.toString()
              }
            }
          }
          declare {
            button("Clear Tiles cache") {
              chartSupport.layerSupport.layers.layers.firstOrNull {
                it is TilesLayer
              }?.let {
                val tileProvider = (it as TilesLayer).tileProvider
                val cachedTileProvider = tileProvider as CachedTileProvider
                cachedTileProvider.clear()
              }
            }
          }

          configurableColorProvider("Value Axis Background", gestalt.configuration::valueAxesBackground)

          section("Enum")
          configurable("Enum Height", gestalt.historyEnumLayer.configuration::stripeHeight) {
            max = 50.0
          }
          configurable("Bar distance", gestalt.historyEnumLayer.configuration::stripesDistance) {
            max = 30.0
          }

          section("Enum-Axis")
          configurable("Lines Gap", (gestalt.enumCategoryAxisLayer.configuration.axisLabelPainter as DefaultCategoryAxisLabelPainter).style::twoLinesGap) {
            max = 5.0
            min = -5.0
          }
          configurable("Split lines mode", (gestalt.enumCategoryAxisLayer.configuration.axisLabelPainter as DefaultCategoryAxisLabelPainter).style::wrapMode) {
          }

          configurableEnum("snap", chartSupport.pixelSnapSupport.snapConfiguration, SnapConfiguration.entries) {
            onChange {
              chartSupport.pixelSnapSupport.snapConfiguration = it
              markAsDirty()
            }
          }
        }
      }
    }
  }

  companion object {
    private val logger: Logger = LoggerFactory.getLogger("com.meistercharts.demo.descriptors.history.TimeLineChartGestaltDemoDescriptor")

    /**
     * Configures a [TimeLineChartGestalt] to receive a sample every 100 milliseconds stored into a [WritableHistoryStorage]
     */
    val oneSampleEvery100ms: PredefinedConfiguration<TimelineChartGestaltDemoConfiguration> = PredefinedConfiguration(
      Uuid.parse("a49d6a88-f453-4b32-b87b-2f9c1b51fb73"),
      { historyStorage ->
        val samplingPeriod = SamplingPeriod.EveryHundredMillis
        val historyChunkGenerator = this.setUpHistoryChunkGenerator(historyStorage, samplingPeriod)

        this.configuration.applyMinimumSamplingPeriod(samplingPeriod)

        it.neckar.open.time.repeat(100.milliseconds) {
          historyChunkGenerator.next()?.let {
            historyStorage.storeWithoutCache(it, samplingPeriod)
          }
        }.also {
          onDispose(it)
        }

        this.configuration.thresholdValueProvider = object : DoublesProvider1<DecimalDataSeriesIndex> {
          override fun size(param1: DecimalDataSeriesIndex): Int {
            return 1
          }

          override fun valueAt(index: Int, param1: DecimalDataSeriesIndex): Double {
            return (param1.value + 3) * 10.0
          }
        }

        this.configuration.thresholdLabelProvider = object : MultiProvider2<HudElementIndex, List<String>, DecimalDataSeriesIndex, LayerPaintingContext> {
          override fun valueAt(index: Int, param1: DecimalDataSeriesIndex, param2: LayerPaintingContext): List<String> {
            return listOf(
              "Threshold for $param1",
              configuration.thresholdValueProvider.valueAt(index, param1).format(3)
            )
          }
        }
      }, "1 sample / 100 ms"
    )

    val oneSampleEvery100msSick: PredefinedConfiguration<TimelineChartGestaltDemoConfiguration> = PredefinedConfiguration(
      Uuid.parse("f6c5c179-1163-4f81-9e44-ec586c6f923b"),
      {
        oneSampleEvery100ms.payload(this, it)

        //Make three axis visible
        configuration.requestedVisibleValueAxesIndices = DecimalDataSeriesIndexProvider.indices { 3 }

        //Title on top
        configuration.applyValueAxisTitleOnTop(40.0)

        configuration.valueAxisStyleConfiguration = { style: ValueAxisLayer.Configuration, dataSeriesIndex: DecimalDataSeriesIndex ->
          style.side = Side.Left
          style.tickOrientation = Vicinity.Outside
          style.paintRange = AxisConfiguration.PaintRange.Continuous
        }
      },
      "1 sample / 100 ms [SICK]"
    )

    val oneSampleEvery100msPoints: PredefinedConfiguration<TimelineChartGestaltDemoConfiguration> = PredefinedConfiguration(
      Uuid.parse("df609fe3-a422-44c1-8d0d-6d7eee64736a"),
      {
        oneSampleEvery100ms.payload(this, it)

        //Make three axis visible
        configuration.requestedVisibleValueAxesIndices = DecimalDataSeriesIndexProvider.indices { 3 }

        configuration.pointPainters = MultiProvider<DecimalDataSeriesIndex, PointPainter?> {
          PointStylePainter(PointStyle.Dot, 2.0, snapXValues = false, snapYValues = false).apply {
            color = configuration.lineStyles.valueAt(it).color
          }
        }.cached() //*We* know what the line styles are, therefore caching is ok here.

        historyRenderPropertiesCalculatorLayer.samplingPeriodCalculator = MinDistanceSamplingPeriodCalculator(10.0).withMinimum { configuration.minimumSamplingPeriod }
      },
      "1 sample / 100 ms Points"
    )

    val dashed: PredefinedConfiguration<TimelineChartGestaltDemoConfiguration> = PredefinedConfiguration(
      Uuid.parse("49a9363b-5c15-4860-911e-a7ef12a4192d"),
      {
        oneSampleEvery100ms.payload(this, it)

        //Make three axis visible
        configuration.requestedVisibleValueAxesIndices = DecimalDataSeriesIndexProvider.indices { 3 }

        configuration.lineStyles = MultiProvider<DecimalDataSeriesIndex, LineStyle> {
          LineStyle(Theme.chartColors.valueAt(it), 1.0, Dashes.LargeDashes)
        }.cached()
      },
      "1 sample / 100 ms - with dashes"
    )

    val manualDownSampling: PredefinedConfiguration<TimelineChartGestaltDemoConfiguration> = PredefinedConfiguration(
      Uuid.parse("381d2133-2dca-44c8-b44c-7761b6bace7f"),
      { historyStorage ->

        val generators = SamplingPeriod.entries.associateWith {
          setUpHistoryChunkGenerator(historyStorage, it)
        }

        //stop downsampling - we are adding all values manually
        historyStorage.stopDownSampling()

        val queryMonitor = this.configuration.historyStorage as HistoryStorageQueryMonitor<*>
        queryMonitor.onQueryForNewDescriptor { descriptor ->

          val generator = generators[descriptor.samplingPeriod] ?: throw IllegalStateException("No generator for ${descriptor.samplingPeriod}")
          val chunk = generator.forTimeRange(descriptor.timeRange) ?: throw IllegalStateException("No chung generated for ${descriptor}")

          logger.debug("Storing chunk for ${descriptor.samplingPeriod} - ${descriptor.timeRange}")

          historyStorage.storeWithoutCache(chunk, descriptor.samplingPeriod)
        }

      },
      "manual downsampling"
    )

    val withAxisTitle: PredefinedConfiguration<TimelineChartGestaltDemoConfiguration> = PredefinedConfiguration(
      Uuid.parse("77fc45be-2da7-411f-b852-a109556c3f48"),
      { historyStorage ->
        val samplingPeriod = SamplingPeriod.EveryHundredMillis
        val historyChunkGenerator = this.setUpHistoryChunkGenerator(historyStorage, samplingPeriod)

        this.configuration.applyMinimumSamplingPeriod(samplingPeriod)

        it.neckar.open.time.repeat(100.milliseconds) {
          historyChunkGenerator.next()?.let {
            historyStorage.storeWithoutCache(it, samplingPeriod)
          }
        }.also {
          onDispose(it)
        }

        timeAxisLayer.configuration.titleProvider = { _, _ -> "The Axis title!!! - Axis size is ${this.configuration.timeAxisSize}" }
        configuration.timeAxisSize = 100.0
      }, "with axis title"
    )

    val outwardsTicks: PredefinedConfiguration<TimelineChartGestaltDemoConfiguration> = PredefinedConfiguration(
      Uuid.parse("6b5efe00-1a83-4604-8eab-83d7f2596e35"),
      {
        oneSampleEvery100ms.payload(this, it)

        this.enumCategoryAxisLayer.configuration.tickOrientation = Vicinity.Outside
        this.enumCategoryAxisLayer.configuration.showAxisLine()
        this.configuration.valueAxisStyleConfiguration = { style: ValueAxisLayer.Configuration, lineIndex: DecimalDataSeriesIndex ->
          style.tickOrientation = Vicinity.Outside
        }

      }, "Outwards ticks"
    )

    val valueAxisTitleOnTop: PredefinedConfiguration<TimelineChartGestaltDemoConfiguration> = PredefinedConfiguration(
      Uuid.parse("6e9f8ff9-d0f3-4eb2-b685-0e75efda8b30"),
      {
        oneSampleEvery100ms.payload(this, it)

        this.configuration.applyValueAxisTitleOnTop()
      }, "Outwards ticks"
    )

    val oneSampleEvery100msLogarithmic: PredefinedConfiguration<TimelineChartGestaltDemoConfiguration> = PredefinedConfiguration(
      Uuid.parse("ef1662a5-342e-4614-a42d-3b17c9b3a55c"),
      {
        oneSampleEvery100ms.payload(this, it)

        val originalConfig = configuration.valueAxisStyleConfiguration

        this.configuration.valueAxisStyleConfiguration = { style, lineIndex ->
          originalConfig(style, lineIndex)
          style.applyLogarithmicScale()
        }

        this.configuration.historyConfiguration.let { historyConfiguration ->
          this.configuration.lineValueRanges = MultiProvider.forListModulo(historyConfiguration.decimalDataSeriesCount.value.fastMap { index ->
            val original = configuration.lineValueRanges.valueAt(index)
            ValueRange.logarithmic(1.0, original.end)
          })
        }
      }, "1 sample / 100 ms - logarithmic"
    )

    /**
     * Configures a [TimeLineChartGestalt] to receive a sample every 100 milliseconds stored into a [HistoryStorageCache]
     */
    val oneSampleEvery16msCached500ms: PredefinedConfiguration<TimelineChartGestaltDemoConfiguration> = PredefinedConfiguration(
      Uuid.parse("7da82187-ebbb-4eb8-9c15-380b3da6d0c9"),
      { historyStorage ->
        val samplingPeriod = SamplingPeriod.EveryTenMillis
        val historyChunkGenerator = this.setUpHistoryChunkGenerator(historyStorage, samplingPeriod)
        val historyStorageCache = HistoryStorageCache(historyStorage, 500.milliseconds)

        this.configuration.applyMinimumSamplingPeriod(samplingPeriod)

        it.neckar.open.time.repeat(16.milliseconds) {
          historyChunkGenerator.next()?.let {
            historyStorageCache.scheduleForStore(it, samplingPeriod)
          }
        }.also {
          onDispose(it)
        }
      }, "1 sample / 16 ms (stored every 500 ms)"
    )

    val oneSampleEvery16msCached500msAverages: PredefinedConfiguration<TimelineChartGestaltDemoConfiguration> = PredefinedConfiguration(
      Uuid.parse("7a95f653-0bb6-42e2-b222-02d2ae4251f8"),
      { historyStorage ->
        val samplingPeriod = SamplingPeriod.EveryTenMillis
        val historyChunkGenerator = this.setUpHistoryChunkGenerator(historyStorage, samplingPeriod)
        val historyStorageCache = HistoryStorageCache(historyStorage, 500.milliseconds)

        this.configuration.applyMinimumSamplingPeriod(samplingPeriod)

        it.neckar.open.time.repeat(16.milliseconds) {
          historyChunkGenerator.next()?.let {
            historyStorageCache.scheduleForStore(it, samplingPeriod)
          }
        }.also {
          onDispose(it)
        }
      }, "1 sample / 16 ms (stored every 500 ms)"
    )

    /**
     * Configures a [TimeLineChartGestalt] to receive a sample every 100 milliseconds stored into a [HistoryStorageCache]
     */
    val oneSampleEvery16msCached50ms: PredefinedConfiguration<TimelineChartGestaltDemoConfiguration> = PredefinedConfiguration(
      Uuid.parse("6d7d2e4f-d983-4792-9008-afa5cb334e24"),
      { historyStorage ->
        val samplingPeriod = SamplingPeriod.EveryMillisecond
        val historyChunkGenerator = this.setUpHistoryChunkGenerator(historyStorage, samplingPeriod)
        val historyStorageCache = HistoryStorageCache(historyStorage, 50.milliseconds)

        this.configuration.applyMinimumSamplingPeriod(samplingPeriod)

        it.neckar.open.time.repeat(16.milliseconds) {
          historyChunkGenerator.next()?.let {
            historyStorageCache.scheduleForStore(it, samplingPeriod)
          }
        }.also {
          onDispose(it)
        }
      }, "1 sample / 1 ms (stored every 50 ms)"
    )

    val oneSampleEvery100msCached100ms: PredefinedConfiguration<TimelineChartGestaltDemoConfiguration> = PredefinedConfiguration(
      Uuid.parse("38639244-0d06-463b-82f9-b3f36c1bab1e"),
      { historyStorage ->
        val samplingPeriod = SamplingPeriod.EveryHundredMillis
        val historyChunkGenerator = this.setUpHistoryChunkGenerator(historyStorage, samplingPeriod)
        val historyStorageCache = HistoryStorageCache(historyStorage, 100.milliseconds)

        this.configuration.applyMinimumSamplingPeriod(samplingPeriod)

        it.neckar.open.time.repeat(100.milliseconds) {
          historyChunkGenerator.next()?.let {
            historyStorageCache.scheduleForStore(it, samplingPeriod)
          }
        }.also {
          onDispose(it)
        }
      }, "1 sample / 100 ms (stored every 100 ms)"
    )

    val oneSampleEvery24h: PredefinedConfiguration<TimelineChartGestaltDemoConfiguration> = PredefinedConfiguration(
      Uuid.parse("383989ab-0075-4307-8093-db88b3e4c1da"),
      { historyStorage ->
        val samplingPeriod = SamplingPeriod.Every24Hours

        historyStorage.naturalSamplingPeriod = samplingPeriod

        configuration.applyMinimumSamplingPeriod(samplingPeriod)
        configuration.minimumSamplingPeriod = samplingPeriod

        configuration.historyConfiguration = historyConfiguration {
          configureDecimalDataSeries()
        }

        val baseMillis = nowMillis() - samplingPeriod.distance
        val decimalValueGenerator = DecimalValueGenerator.cosine(ValueRange.default)

        configuration.historyConfiguration.chunk(100) { timestampIndex ->
          this.addDecimalValues(
            timestamp = baseMillis + timestampIndex.value * samplingPeriod.distance,
            decimalValueGenerator.generate(timestampIndex.value.toDouble()),
            decimalValueGenerator.generate(timestampIndex.value.toDouble()),
            decimalValueGenerator.generate(timestampIndex.value.toDouble()),
            decimalValueGenerator.generate(timestampIndex.value.toDouble()),
            decimalValueGenerator.generate(timestampIndex.value.toDouble()),
            decimalValueGenerator.generate(timestampIndex.value.toDouble()),
            decimalValueGenerator.generate(timestampIndex.value.toDouble()),
            decimalValueGenerator.generate(timestampIndex.value.toDouble()),
          )
        }.let {
          historyStorage.storeWithoutCache(it, samplingPeriod)
        }
      }, "1 sample / 24h"
    )

    /**
     * Configures a [TimeLineChartGestalt] for the Neckar IT home page
     */
    val neckarITHomePage: PredefinedConfiguration<TimelineChartGestaltDemoConfiguration> = PredefinedConfiguration(
      Uuid.parse("d6c83352-1243-4b8b-94ef-12704a268c0d"),
      { historyStorage ->
        val samplingPeriod = SamplingPeriod.EveryHundredMillis
        val historyChunkGenerator = this.setUpHistoryChunkGenerator(historyStorage, samplingPeriod)

        //adjust the position of the cross wire
        configuration.crossWirePositionX = 0.85

        //add some samples for the last hour and set the max history size accordingly
        historyStorage.maxSizeConfiguration = MaxHistorySizeConfiguration.forDuration(70.0.minutes, samplingPeriod.toHistoryBucketRange())
        historyChunkGenerator.forTimeRange(TimeRange.oneHourUntilNow())?.let {
          historyStorage.storeWithoutCache(it, samplingPeriod)
        }

        it.neckar.open.time.repeat(100.milliseconds) {
          historyChunkGenerator.next()?.let {
            historyStorage.storeWithoutCache(it, samplingPeriod)
          }
        }.also {
          onDispose(it)
        }

        //we want three lines and three value axes to be visible
        this.configuration.requestedVisibleDecimalSeriesIndices = DecimalDataSeriesIndexProvider.indices { 3 }
        this.configuration.requestedVisibleValueAxesIndices = DecimalDataSeriesIndexProvider.indices { 3 }
      }, "Neckar IT Home Page"
    )

    val candle: PredefinedConfiguration<TimelineChartGestaltDemoConfiguration> = PredefinedConfiguration(
      Uuid.parse("52d11f70-5da8-4726-b7c6-ecafc46458cb"),
      { historyStorage ->
        val samplingPeriod = SamplingPeriod.EveryHundredMillis
        val historyChunkGenerator = this.setUpHistoryChunkGenerator(historyStorage, samplingPeriod)

        //adjust the position of the cross wire
        configuration.crossWirePositionX = 0.85

        //add some samples for the last hour and set the max history size accordingly
        historyStorage.maxSizeConfiguration = MaxHistorySizeConfiguration.forDuration(70.minutes, samplingPeriod.toHistoryBucketRange())
        historyChunkGenerator.forTimeRange(TimeRange.oneHourUntilNow())?.let {
          historyStorage.storeWithoutCache(it, samplingPeriod)
        }

        it.neckar.open.time.repeat(100.milliseconds) {
          historyChunkGenerator.next()?.let {
            historyStorage.storeWithoutCache(it, samplingPeriod)
          }
        }.also {
          onDispose(it)
        }

        //we want three lines and three value axes to be visible
        configuration.requestedVisibleDecimalSeriesIndices = DecimalDataSeriesIndexProvider.indices { 3 }
        configuration.requestedVisibleValueAxesIndices = DecimalDataSeriesIndexProvider.indices { 3 }

        configureForCandle()
      }, "Candle"
    )

    val minMaxArea: PredefinedConfiguration<TimelineChartGestaltDemoConfiguration> = PredefinedConfiguration(
      Uuid.parse("1ca06a5b-400a-430f-a2b8-3335eb18afbc"),
      { historyStorage ->
        val samplingPeriod = SamplingPeriod.EveryHundredMillis

        //adjust the position of the cross wire
        configuration.crossWirePositionX = 0.85
        configuration.minimumSamplingPeriod = samplingPeriod

        configuration.historyConfiguration = historyConfiguration {
          configureDecimalDataSeries()
          //Enum data series intentionally omitted in the minMaxArea variant — only
          //decimal series are visualised here. Other variants (line ~825) include them.
        }

        val historyChunkGenerator = setUpHistoryChunkGenerator(historyStorage, samplingPeriod)

        historyStorage.maxSizeConfiguration = MaxHistorySizeConfiguration.forDuration(70.minutes, samplingPeriod.toHistoryBucketRange())

        it.neckar.open.time.repeat(100.milliseconds) {
          historyChunkGenerator.next()?.let { chunkWithoutMinMax ->
            val decimalValuesWithMinMax: DecimalHistoryValues = chunkWithoutMinMax.values.decimalHistoryValues.withGeneratedMinMaxValues()

            val valuesWithMinMax = chunkWithoutMinMax.values.copy(decimalHistoryValues = decimalValuesWithMinMax)

            val withMinMax = chunkWithoutMinMax.copy(values = valuesWithMinMax)
            historyStorage.storeWithoutCache(withMinMax, samplingPeriod)
          }
        }.also {
          onDispose(it)
        }

        //we want three lines and three value axes to be visible
        configuration.requestedVisibleDecimalSeriesIndices = DecimalDataSeriesIndexProvider.indices { 3 }
        configuration.requestedVisibleValueAxesIndices = DecimalDataSeriesIndexProvider.indices { 1 }

        configuration.applyMinMaxAreaVisibility(true)
      }, "Min/Max Area"
    )

    val minMaxAreaPoints: PredefinedConfiguration<TimelineChartGestaltDemoConfiguration> = PredefinedConfiguration(
      Uuid.parse("ea03d494-666e-41d6-8564-c51b3cc1d48f"),
      {
        minMaxArea.payload(this, it)

        configuration.pointPainters = MultiProvider<DecimalDataSeriesIndex, PointPainter?> {
          PointStylePainter(PointStyle.Dot, 2.0, snapXValues = false, snapYValues = false).apply {
            color = configuration.lineStyles.valueAt(it).color
          }
        }.cached()

      }, "Min/Max Area + Points"
    )

    val _100years: PredefinedConfiguration<TimelineChartGestaltDemoConfiguration> = PredefinedConfiguration(
      Uuid.parse("e175f302-21d0-4237-9bce-eaeee0b63a28"),
      { historyStorage ->
        val samplingPeriod = SamplingPeriod.Every24Hours
        val historyChunkGenerator = this.setUpHistoryChunkGenerator(historyStorage, samplingPeriod)

        //adjust the position of the cross wire
        configuration.crossWirePositionX = 0.85

        //add some samples for the last hour and set the max history size accordingly
        historyStorage.maxSizeConfiguration = MaxHistorySizeConfiguration.forDuration(70.minutes, samplingPeriod.toHistoryBucketRange())

        historyChunkGenerator.forTimeRange(TimeRange.fromEndAndDuration(nowMillis(), millisPerCentury))?.let {
          historyStorage.storeWithoutCache(it, samplingPeriod)
        }

        configureBuilder { builder ->
          builder.zoomAndTranslationModifier {
            minZoom(0.02, 0.000001)
            maxZoom(12.0, 500.0)
          }
        }

        configuration.contentAreaDuration = millisPerDecade

        //we want three lines and three value axes to be visible
        configuration.requestedVisibleDecimalSeriesIndices = DecimalDataSeriesIndexProvider.indices { 3 }
        configuration.requestedVisibleValueAxesIndices = DecimalDataSeriesIndexProvider.indices { 3 }

        configuration.applyMinMaxAreaVisibility(true)
      }, "100 years"
    )
  }
}

private fun DecimalHistoryValues.withGeneratedMinMaxValues(): DecimalHistoryValues {
  require(this.maxValues == null) { "max values already set" }

  val minValues = this.values.map2 { x, y, value ->
    (value * random.nextDouble(0.85, 1.0))
  }
  val maxValues = this.values.map2 { x, y, value ->
    (value * random.nextDouble(1.0, 1.15))
  }

  return this.copy(minValues = minValues, maxValues = maxValues)
}

internal class MyHistoryChunkBuilder(val historyConfigurationProvider: () -> HistoryConfiguration) {
  fun createHistoryChunk(size: Int, samplingPeriod: SamplingPeriod): HistoryChunk {
    @ms val nowMillis = nowMillis()

    val historyConfiguration = historyConfigurationProvider()

    return historyConfiguration.chunk(size) { timestampIndex ->
      addDecimalValues(nowMillis + timestampIndex.value * samplingPeriod.distance, *randomValues(historyConfiguration.decimalDataSeriesCount.value))
    }
  }

  private fun randomValues(size: Int): DoubleArray {
    return DoubleArray(size) { random.nextDouble(35.0, 75.0) }
  }
}


fun TimeLineChartGestalt.setUpHistoryChunkGenerator(historyStorage: InMemoryHistoryStorage, samplingPeriod: SamplingPeriod): HistoryChunkGenerator {
  //Avoid gaps for the cross wire - when adding only
  configuration.historyGapCalculator = DefaultHistoryGapCalculator(10.0)

  val historyConfiguration = historyConfiguration {
    configureDecimalDataSeries()
    configureEnumDataSeries()
  }

  configuration.historyConfiguration = historyConfiguration

  val dataSeriesValueRanges = listOf(
    ValueRange.linear(0.0, 1000.0),
    ValueRange.linear(0.0, 300.0),
    ValueRange.linear(0.0, 999999.0),
    ValueRange.linear(0.0, 1000.0),
    ValueRange.linear(0.0, 999999.0),
    ValueRange.linear(0.0, 999999.0),
    ValueRange.linear(-50.0, 100.0),
    ValueRange.linear(-0.5, 17.0)
  )
  configuration.lineValueRanges = MultiProvider.forListModulo(dataSeriesValueRanges)

  configuration.valueAxisStyleConfiguration = { valueAxisStyle, lineIndex ->
    valueAxisStyle.size = when (lineIndex.value) {
      2, 4, 5 -> 165.0
      else -> 90.0
    }
  }

  val easings = listOf(
    Easing.inOut,
    Easing.smooth,
    Easing.inOutBack,
  )

  val decimalValueGenerators = historyConfiguration.decimalDataSeriesCount.value.fastMap {
    TimeBasedValueGeneratorBuilder {
      val dataSeriesValueRange = dataSeriesValueRanges[it]
      startValue = dataSeriesValueRange.center() + (random.nextDouble() - 0.5).coerceAtMost(0.2).coerceAtLeast(-0.2) * dataSeriesValueRange.delta
      minDeviation = dataSeriesValueRange.delta * (0.025 * (it + 1)).coerceAtMost(0.25)
      maxDeviation = (dataSeriesValueRange.delta * (0.05 * (it + 1)).coerceAtMost(0.25)).coerceAtLeast(minDeviation * 1.001)
      period = 2_000.0 * (it + 1)
      valueRange = dataSeriesValueRange.reduced(0.25)
      easing = easings.getModulo(it)
    }.build()
  }

  val enumValueGenerators: List<EnumValueGenerator> = historyConfiguration.enumDataSeriesCount.value.fastMap {
    EnumValueGenerator.random()
  }

  val referenceEntryGenerators: List<ReferenceEntryGenerator> = historyConfiguration.referenceEntryDataSeriesCount.value.fastMap {
    ReferenceEntryGenerator.random()
  }

  return HistoryChunkGenerator(
    historyStorage = historyStorage,
    samplingPeriod = samplingPeriod,
    decimalValueGenerators = decimalValueGenerators,
    enumValueGenerators = enumValueGenerators,
    referenceEntryGenerators = referenceEntryGenerators,
    historyConfiguration = historyConfiguration
  )
}

private fun HistoryConfigurationBuilder.configureEnumDataSeries() {
  enumDataSeries(DataSeriesId(1001), TextKey.simple("Global State"), HistoryEnum.createSimple("Warning State", listOf("Ok", "Warning", "Error")))
  enumDataSeries(DataSeriesId(1002), TextKey.simple("Engine running"), HistoryEnum.Boolean)
  enumDataSeries(DataSeriesId(1003), TextKey.simple("Compliance"), HistoryEnum.createSimple("Compliance State", listOf("Compliant", "Not Compliant", "Unknown")))
  enumDataSeries(DataSeriesId(1004), TextKey.simple("Boiler"), HistoryEnum.Boolean)
  enumDataSeries(DataSeriesId(1005), TextKey.simple("Auxiliary Engine 1"), HistoryEnum.Boolean)
  enumDataSeries(DataSeriesId(1006), TextKey.simple("Auxiliary Engine 2"), HistoryEnum.Boolean)
  enumDataSeries(DataSeriesId(1007), TextKey.simple("Auxiliary Engine 3"), HistoryEnum.Boolean)
  enumDataSeries(DataSeriesId(1008), TextKey.simple("Auxiliary Engine 4"), HistoryEnum.Boolean)
}

private fun HistoryConfigurationBuilder.configureDecimalDataSeries() {
  decimalDataSeries(DataSeriesId(17), TextKey.simple("Mass Flow Rate [kg/h]"), HistoryUnit("kg/h"))
  decimalDataSeries(DataSeriesId(23), TextKey.simple("Flow Velocity [m/s]"), HistoryUnit("m/s"))
  decimalDataSeries(DataSeriesId(56), TextKey.simple("Volume [m³]"), HistoryUnit("m³"))
  decimalDataSeries(DataSeriesId(89), TextKey.simple("Volumetric Flow Rate [m³/h]"), HistoryUnit("m³/h"))
  decimalDataSeries(DataSeriesId(117), TextKey.simple("Mass [kg]"), HistoryUnit("kg"))
  decimalDataSeries(DataSeriesId(118), TextKey.simple("Energy [kWh]"), HistoryUnit("kWh"))
  decimalDataSeries(DataSeriesId(123), TextKey.simple("Temperature [°C]"), HistoryUnit("°C"))
  decimalDataSeries(DataSeriesId(143), TextKey.simple("Pressure [bar]"), HistoryUnit("bar"))
}
