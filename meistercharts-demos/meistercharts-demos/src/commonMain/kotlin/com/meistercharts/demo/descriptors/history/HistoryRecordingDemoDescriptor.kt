package com.meistercharts.demo.descriptors.history

import com.meistercharts.algorithms.TimeRange
import com.meistercharts.algorithms.ValueRange
import com.meistercharts.algorithms.impl.FittingWithMargin
import com.meistercharts.algorithms.layers.HistoryBucketsRangeDebugLayer
import com.meistercharts.algorithms.layers.HistoryUpdatesVisualizationLayer
import com.meistercharts.algorithms.layers.TilesLayer
import com.meistercharts.algorithms.layers.ValueAxisLayer
import com.meistercharts.algorithms.layers.addClearBackground
import com.meistercharts.algorithms.layers.addTimeAxis
import com.meistercharts.algorithms.layers.debug.CleanupServiceDebugLayer
import com.meistercharts.algorithms.layers.debug.DirtyRangesDebugLayer
import com.meistercharts.algorithms.layers.debug.ShowTimeRangeLayer
import com.meistercharts.algorithms.layers.linechart.LineStyle
import com.meistercharts.algorithms.painter.Color
import com.meistercharts.algorithms.painter.DirectLinePainter
import com.meistercharts.algorithms.tile.AverageHistoryCanvasTilePainter
import com.meistercharts.algorithms.tile.CanvasTileProvider
import com.meistercharts.algorithms.tile.CountingTileProvider
import com.meistercharts.algorithms.tile.DefaultHistoryTileInvalidator
import com.meistercharts.algorithms.tile.HistoryGapCalculator
import com.meistercharts.algorithms.tile.HistoryRenderPropertiesCalculatorLayer
import com.meistercharts.algorithms.tile.HistoryTileInvalidator
import com.meistercharts.algorithms.tile.MaxDistanceSamplingPeriodCalculator
import com.meistercharts.algorithms.tile.SamplingPeriodCalculator
import com.meistercharts.algorithms.tile.cached
import com.meistercharts.algorithms.tile.canvasTiles
import com.meistercharts.algorithms.tile.withMinimum
import com.meistercharts.annotations.PhysicalPixel
import com.meistercharts.canvas.translateOverTime
import com.meistercharts.demo.ChartingDemo
import com.meistercharts.demo.ChartingDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.configurableBoolean
import com.meistercharts.demo.configurableDouble
import com.meistercharts.demo.configurableInt
import com.meistercharts.demo.section
import com.meistercharts.history.DecimalDataSeriesIndexProvider
import com.meistercharts.history.InMemoryHistoryStorage
import com.meistercharts.history.SamplingPeriod
import com.meistercharts.history.cleanup.MaxHistorySizeConfiguration
import com.meistercharts.history.downsampling.DownSamplingDirtyRangesCollector
import com.meistercharts.history.downsampling.DownSamplingService
import com.meistercharts.history.downsampling.observe
import com.meistercharts.history.generator.DecimalValueGenerator
import com.meistercharts.history.generator.HistoryChunkGenerator
import com.meistercharts.history.generator.offset
import com.meistercharts.model.Insets
import com.meistercharts.model.Size
import it.neckar.open.kotlin.lang.fastMap
import it.neckar.open.provider.MultiProvider
import it.neckar.open.observable.ObservableBoolean
import kotlin.time.Duration.Companion.milliseconds
import it.neckar.open.time.repeat

data class HistoryRecordingDemoConfig(
  val recordingSamplingPeriod: SamplingPeriod = SamplingPeriod.EveryHundredMillis,
  val dataSeriesCount: Int = 3,
) {
  override fun toString(): String {
    return "${recordingSamplingPeriod.label} - $dataSeriesCount"
  }
}

/**
 *
 */
class HistoryRecordingDemoDescriptor : ChartingDemoDescriptor<HistoryRecordingDemoConfig> {
  override val name: String = "History Recording"
  override val category: DemoCategory
    get() = DemoCategory.Calculations

  override val predefinedConfigurations: List<PredefinedConfiguration<HistoryRecordingDemoConfig>> = listOf(
    PredefinedConfiguration(HistoryRecordingDemoConfig(SamplingPeriod.EveryHundredMillis, 3)),
    PredefinedConfiguration(HistoryRecordingDemoConfig(SamplingPeriod.EveryHundredMillis, 100)),
    PredefinedConfiguration(HistoryRecordingDemoConfig(SamplingPeriod.EveryMillisecond, 3)),
  )

  override fun createDemo(configuration: PredefinedConfiguration<HistoryRecordingDemoConfig>?): ChartingDemo {
    require(configuration != null)

    val recordingSamplingPeriod: SamplingPeriod = configuration.payload.recordingSamplingPeriod
    val dataSeriesCount = configuration.payload.dataSeriesCount

    return ChartingDemo {
      meistercharts {
        configureAsTimeChart()
        configureAsTiledTimeChart()

        zoomAndTranslationDefaults {
          FittingWithMargin(Insets.of(50.0))
        }

        val historyStorage = InMemoryHistoryStorage().also {
          it.naturalSamplingPeriod = recordingSamplingPeriod
          it.maxSizeConfiguration = MaxHistorySizeConfiguration(7)
          it.scheduleCleanupService()

          onDispose(it)
        }

        val visibleDataSeriesIndices = DecimalDataSeriesIndexProvider.indices { dataSeriesCount }

        val valueRangeStart = 0.0
        val valueRangeEnd = 100.0
        val valueRange = ValueRange.linear(valueRangeStart, valueRangeEnd)
        val valueRanges = dataSeriesCount.fastMap { valueRange }
        val distance = valueRange.delta * 0.8 / dataSeriesCount
        val offset = valueRange.center() - dataSeriesCount * 0.5 * distance
        val decimalValueGenerators = dataSeriesCount.fastMap {
          DecimalValueGenerator.normality(valueRanges[it], valueRanges[it].delta * 0.05)
            .offset(offset + it * distance)
        }

        val historyChunkGenerator = HistoryChunkGenerator(historyStorage = historyStorage, samplingPeriod = recordingSamplingPeriod, decimalValueGenerators = decimalValueGenerators, enumValueGenerators = emptyList(), referenceEntryGenerators = emptyList())
        val contentAreaTimeRange = TimeRange.oneMinuteUntilNow()


        //Collects the time ranges that will be down sampled
        val downSamplingDirtyRangesCollector = DownSamplingDirtyRangesCollector()
        downSamplingDirtyRangesCollector.observe(historyStorage)

        val downSamplingService = DownSamplingService(historyStorage)
          .also { onDispose(it) }
        downSamplingService.scheduleDownSampling(downSamplingDirtyRangesCollector)

        //Provides the manually configured sampling period
        val samplingPeriodCalculator: SamplingPeriodCalculator = MaxDistanceSamplingPeriodCalculator().withMinimum(recordingSamplingPeriod)

        val historyGapCalculator = object : HistoryGapCalculator {
          var factor: Double = 3.0

          override fun calculateMinGapDistance(renderedSamplingPeriod: SamplingPeriod): Double {
            return renderedSamplingPeriod.distance * factor
          }
        }

        val tilePainter = AverageHistoryCanvasTilePainter(
          AverageHistoryCanvasTilePainter.Configuration(
            historyStorage,
            { contentAreaTimeRange },
            MultiProvider { valueRange },
            { visibleDataSeriesIndices },
            MultiProvider.always(LineStyle(Color.red, 1.0, null)),
            MultiProvider.always(DirectLinePainter(snapXValues = false, snapYValues = false))
          )
        )


        @PhysicalPixel val physicalTileSize = Size.of(400.0, 400.0)
        val canvasTileProvider = CanvasTileProvider(physicalTileSize, tilePainter)
        val countingTileProvider = CountingTileProvider(canvasTileProvider)
        val cachedTileProvider = countingTileProvider.cached(chartId)

        configure {
          chartSupport.rootChartState.contentAreaSizeProperty.consume {
            cachedTileProvider.clear()
          }
          chartSupport.rootChartState.axisOrientationXProperty.consume {
            cachedTileProvider.clear()
          }
          chartSupport.rootChartState.axisOrientationYProperty.consume {
            cachedTileProvider.clear()
          }


          layers.addClearBackground()

          val tilesCountOverlayVisible = ObservableBoolean(true)

          layers.addLayer(HistoryRenderPropertiesCalculatorLayer(samplingPeriodCalculator, historyGapCalculator) { contentAreaTimeRange })
          layers.addLayer(TilesLayer(cachedTileProvider))
          layers.addLayer(ShowTimeRangeLayer(contentAreaTimeRange))
          layers.addLayer(HistoryBucketsRangeDebugLayer(contentAreaTimeRange, samplingPeriodCalculator))
          layers.addLayer(DirtyRangesDebugLayer(downSamplingDirtyRangesCollector, contentAreaTimeRange))
          layers.addLayer(CleanupServiceDebugLayer(historyStorage, contentAreaTimeRange))

          layers.addLayer(ValueAxisLayer("Sin", valueRange))
          layers.addTimeAxis(contentAreaTimeRange)

          val historyUpdateVisualizationLayer = HistoryUpdatesVisualizationLayer(contentAreaTimeRange)
          layers.addLayer(historyUpdateVisualizationLayer)


          val tileInvalidator: HistoryTileInvalidator = DefaultHistoryTileInvalidator()

          historyStorage.observe { _, updateInfo ->
            historyUpdateVisualizationLayer.lastUpdateInfo = updateInfo
            tileInvalidator.historyHasBeenUpdated(updateInfo, cachedTileProvider.canvasTiles(), chartSupport)

            this@ChartingDemo.markAsDirty()
          }

          section("Visualization")

          chartSupport.translateOverTime.contentAreaTimeRangeX = contentAreaTimeRange
          configurableBoolean("Play", chartSupport.translateOverTime.animatedProperty)
          configurableBoolean("Tiles Age", tilesCountOverlayVisible)

          configurableDouble("Gap Factor", historyGapCalculator::factor) {
            max = 100.0
          }

          section("Data")
          declare {
            button("Clear cache") {
              cachedTileProvider.clear()
              this@ChartingDemo.markAsDirty()
            }

            button("Add 1 data point now") {
              historyChunkGenerator.forNow()?.let {
                historyStorage.storeWithoutCache(it, recordingSamplingPeriod)
              }
            }

            val recordingActive = ObservableBoolean(true)
            checkBox("Recording", recordingActive)

            repeat(recordingSamplingPeriod.distance.milliseconds) {
              if (recordingActive.value) {
                historyChunkGenerator.next()?.let {
                  historyStorage.storeWithoutCache(it, recordingSamplingPeriod)
                }
              }
            }.also {
              chartSupport.onDispose(it)
            }
          }

          configurableInt("History length (keptBucketsCount)") {
            max = 200
            min = 1
            value = historyStorage.maxSizeConfiguration.keptBucketsCount
            onChange {
              historyStorage.maxSizeConfiguration = MaxHistorySizeConfiguration(it)
              markAsDirty()
            }
          }
        }
      }
    }
  }

}
