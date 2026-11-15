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
import com.meistercharts.algorithms.layers.HistoryBucketsRangeDebugLayer
import com.meistercharts.algorithms.layers.HistoryUpdatesVisualizationLayer
import com.meistercharts.algorithms.layers.TilesLayer
import com.meistercharts.algorithms.layers.axis.ValueAxisLayer
import com.meistercharts.algorithms.layers.addClearBackground
import com.meistercharts.algorithms.layers.axis.time.addTimeAxis
import com.meistercharts.algorithms.layers.debug.CleanupServiceDebugLayer
import com.meistercharts.algorithms.layers.debug.DirtyRangesDebugLayer
import com.meistercharts.algorithms.layers.debug.ShowTimeRangeLayer
import com.meistercharts.algorithms.layers.linechart.LineStyle
import com.meistercharts.algorithms.painter.DirectLinePainter
import com.meistercharts.algorithms.tile.AverageMinMaxHistoryCanvasTilePainter
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
import com.meistercharts.color.Color
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.DemoQuality
import com.meistercharts.demo.MeisterchartsDemo
import com.meistercharts.demo.MeisterchartsDemoDescriptor
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.VariabilityType
import com.meistercharts.demo.configurableBoolean
import com.meistercharts.demo.configurableDouble
import com.meistercharts.demo.configurableInt
import com.meistercharts.demo.section
import com.meistercharts.history.DecimalDataSeriesIndexProvider
import com.meistercharts.history.InMemoryHistoryStorage
import com.meistercharts.history.SamplingPeriod
import com.meistercharts.history.cleanup.MaxHistorySizeConfiguration
import com.meistercharts.history.downsampling.DownSamplingService
import com.meistercharts.history.generator.DecimalValueGenerator
import com.meistercharts.history.generator.HistoryChunkGenerator
import com.meistercharts.history.generator.offset
import com.meistercharts.model.Insets
import it.neckar.geometry.Size
import com.meistercharts.range.ValueRange
import com.meistercharts.time.TimeRange
import com.meistercharts.zoom.FittingWithMargin
import it.neckar.open.kotlin.lang.fastMap
import it.neckar.open.observable.ObservableBoolean
import it.neckar.open.provider.MultiProvider
import it.neckar.open.time.repeat
import kotlin.time.Duration.Companion.milliseconds

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
class HistoryRecordingDemoDescriptor : MeisterchartsDemoDescriptor<HistoryRecordingDemoConfig> {
  override val uuid: Uuid = Uuid.parse("b080cbb1-d8d7-4221-941f-93e15432c35e")
  override val name: String = "History Recording"
  override val variabilityType: VariabilityType = VariabilityType.Unstable

  override val category: DemoCategory
    get() = DemoCategory.Calculations

  override val quality: DemoQuality = DemoQuality.High

  override val predefinedConfigurations: List<PredefinedConfiguration<HistoryRecordingDemoConfig>> = listOf(
    PredefinedConfiguration(Uuid.parse("2e387738-e237-4f16-be3d-871d78414430"), HistoryRecordingDemoConfig(SamplingPeriod.EveryHundredMillis, 3)),
    PredefinedConfiguration(Uuid.parse("851f6c97-0b7a-422d-8a40-da9b467bb7ad"), HistoryRecordingDemoConfig(SamplingPeriod.EveryHundredMillis, 100)),
    PredefinedConfiguration(Uuid.parse("8549ae3a-da28-4f13-beb2-16cb6dbb4d65"), HistoryRecordingDemoConfig(SamplingPeriod.EveryMillisecond, 3)),
  )

  override fun prepareDemo(configuration: PredefinedConfiguration<HistoryRecordingDemoConfig>?): MeisterchartsDemo {
    require(configuration != null) { "HistoryRecordingDemoDescriptor.prepareDemo requires a predefined configuration" }

    val recordingSamplingPeriod: SamplingPeriod = configuration.payload.recordingSamplingPeriod
    val dataSeriesCount = configuration.payload.dataSeriesCount

    return MeisterchartsDemo(this) {
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

        val downSamplingService = DownSamplingService(historyStorage)
          .also { onDispose(it) }

        downSamplingService.scheduleDownSampling()

        //Provides the manually configured sampling period
        val samplingPeriodCalculator: SamplingPeriodCalculator = MaxDistanceSamplingPeriodCalculator().withMinimum(recordingSamplingPeriod)

        val historyGapCalculator = object : HistoryGapCalculator {
          var factor: Double = 3.0

          override fun calculateMinGapDistance(renderedSamplingPeriod: SamplingPeriod): Double {
            return renderedSamplingPeriod.distance * factor
          }
        }

        val tilePainter = AverageMinMaxHistoryCanvasTilePainter(
          AverageMinMaxHistoryCanvasTilePainter.Configuration(
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
          layers.addLayer(DirtyRangesDebugLayer(downSamplingService.dirtyRangesCollector, contentAreaTimeRange))
          layers.addLayer(CleanupServiceDebugLayer(historyStorage, contentAreaTimeRange))

          layers.addLayer(ValueAxisLayer("Sin", valueRange))
          layers.addTimeAxis(contentAreaTimeRange)

          val historyUpdateVisualizationLayer = HistoryUpdatesVisualizationLayer(contentAreaTimeRange)
          layers.addLayer(historyUpdateVisualizationLayer)


          val tileInvalidator: HistoryTileInvalidator = DefaultHistoryTileInvalidator()

          historyStorage.observe { updateInfo ->
            historyUpdateVisualizationLayer.lastUpdateInfo = updateInfo
            tileInvalidator.historyHasBeenUpdated(updateInfo, cachedTileProvider.canvasTiles(), chartSupport)

            this@MeisterchartsDemo.markAsDirty()
          }

          section("Visualization")

          chartSupport.translateOverTime.contentAreaTimeRangeX = contentAreaTimeRange
          configurableBoolean("Play", chartSupport.translateOverTime.animatedProperty)
          configurableBoolean("Tiles Age", tilesCountOverlayVisible)

          configurable("Gap Factor", historyGapCalculator::factor) {
            max = 100.0
          }

          section("Data")
          declare {
            button("Clear cache") {
              cachedTileProvider.clear()
              this@MeisterchartsDemo.markAsDirty()
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
