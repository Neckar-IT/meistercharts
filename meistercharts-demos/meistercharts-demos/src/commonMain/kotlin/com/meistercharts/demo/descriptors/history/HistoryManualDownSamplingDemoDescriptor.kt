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
import com.meistercharts.algorithms.layers.debug.DirtyRangesDebugLayer
import com.meistercharts.algorithms.layers.debug.ShowTimeRangeLayer
import com.meistercharts.algorithms.layers.linechart.LineStyle
import com.meistercharts.algorithms.painter.Color
import com.meistercharts.algorithms.painter.DirectLineLivePainter
import com.meistercharts.algorithms.painter.DirectLinePainter
import com.meistercharts.algorithms.tile.AverageHistoryCanvasTilePainter
import com.meistercharts.algorithms.tile.CanvasTileProvider
import com.meistercharts.algorithms.tile.DefaultHistoryTileInvalidator
import com.meistercharts.algorithms.tile.HistoryTileInvalidator
import com.meistercharts.algorithms.tile.SamplingPeriodCalculator
import com.meistercharts.algorithms.tile.cached
import com.meistercharts.algorithms.tile.canvasTiles
import com.meistercharts.annotations.PhysicalPixel
import com.meistercharts.demo.ChartingDemo
import com.meistercharts.demo.ChartingDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.configurableEnum
import com.meistercharts.demo.section
import com.meistercharts.history.DataSeriesId
import com.meistercharts.history.DecimalDataSeriesIndexProvider
import com.meistercharts.history.HistoryBucketDescriptor
import com.meistercharts.history.HistoryUnit
import com.meistercharts.history.InMemoryHistoryStorage
import com.meistercharts.history.SamplingPeriod
import com.meistercharts.history.downsampling.DownSamplingDirtyRangesCollector
import com.meistercharts.history.downsampling.DownSamplingService
import com.meistercharts.history.downsampling.observe
import com.meistercharts.history.historyConfiguration
import com.meistercharts.history.impl.chunk
import com.meistercharts.model.Insets
import com.meistercharts.model.Size
import it.neckar.open.kotlin.lang.random
import it.neckar.open.time.nowMillis
import it.neckar.open.provider.MultiProvider
import it.neckar.open.i18n.TextKey
import it.neckar.open.kotlin.lang.enumEntries
import it.neckar.open.observable.ObservableBoolean
import kotlin.time.Duration.Companion.milliseconds
import it.neckar.open.time.repeat

/**
 *
 */
class HistoryManualDownSamplingDemoDescriptor : ChartingDemoDescriptor<Nothing> {
  override val name: String = "History with manual down sampling"
  override val category: DemoCategory
    get() = DemoCategory.Calculations

  override fun createDemo(configuration: PredefinedConfiguration<Nothing>?): ChartingDemo {
    return ChartingDemo {

      val historyConfiguration = historyConfiguration {
        decimalDataSeries(DataSeriesId(101), TextKey.simple("NH₃"), HistoryUnit.ml)
        decimalDataSeries(DataSeriesId(102), TextKey.simple("HCl"), HistoryUnit.ml)
        decimalDataSeries(DataSeriesId(103), TextKey.simple("H₂0"), HistoryUnit.ml)
      }

      val resolution = SamplingPeriod.EveryHundredMillis

      meistercharts {
        configureAsTimeChart()
        configureAsTiledTimeChart()

        zoomAndTranslationDefaults {
          FittingWithMargin(Insets.of(50.0))
        }

        val contentAreaTimeRange = TimeRange.oneMinuteUntilNow()
        val valueRange = ValueRange.linear(0.0, 200.0)

        val historyStorage = InMemoryHistoryStorage()

        //Collects the time ranges that will be down sampled
        val downSamplingDirtyRangesCollector = DownSamplingDirtyRangesCollector()
        downSamplingDirtyRangesCollector.observe(historyStorage)

        val downSamplingService = DownSamplingService(historyStorage)
          .also { onDispose(it) }

        //Provides the manually configured sampling period
        val samplingPeriodCalculator = object : SamplingPeriodCalculator {
          var samplingPeriod: SamplingPeriod = SamplingPeriod.EveryHundredMillis

          override fun calculateSamplingPeriod(visibleTimeRange: TimeRange, canvasSize: Size): SamplingPeriod {
            return samplingPeriod
          }
        }

        val visibleDataSeriesIndices = DecimalDataSeriesIndexProvider.indices { historyConfiguration.totalDataSeriesCount }
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
        val cachedTileProvider = canvasTileProvider.cached(chartId)

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

          layers.addLayer(TilesLayer(cachedTileProvider))
          layers.addLayer(ShowTimeRangeLayer(contentAreaTimeRange))
          layers.addLayer(HistoryBucketsRangeDebugLayer(contentAreaTimeRange, samplingPeriodCalculator))
          layers.addLayer(DirtyRangesDebugLayer(downSamplingDirtyRangesCollector, contentAreaTimeRange))

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
          configurableEnum("Sampling Period to render", samplingPeriodCalculator::samplingPeriod, enumEntries()) {
            onChange {
              cachedTileProvider.clear()
            }
          }

          section("Data")
          declare {
            button("Clear cache") {
              cachedTileProvider.clear()
              this@ChartingDemo.markAsDirty()
            }

            button("Add 1 data point") {
              val historyChunk = historyConfiguration.chunk {
                addDecimalValues(nowMillis(), *randomValues())
              }

              historyStorage.storeWithoutCache(historyChunk, resolution)
            }

            val recordingActive = ObservableBoolean(true)
            checkBox("Recording", recordingActive)

            repeat(100.milliseconds) {
              if (recordingActive.value) {
                val now = nowMillis()

                //Check for duplicate data
                val bucket = historyStorage.get(HistoryBucketDescriptor.forTimestamp(now, resolution))
                if (bucket != null && bucket.chunk.lastTimeStampOrNull() == now) {
                  return@repeat
                }

                val historyChunk = historyConfiguration.chunk {
                  addDecimalValues(nowMillis(), *randomValues())
                }

                historyStorage.storeWithoutCache(historyChunk, resolution)
              }
            }.also {
              chartSupport.onDispose(it)
            }
          }

          section("Down sampling")

          declare {
            SamplingPeriod.entries.forEach {
              button("Recalculate Downsampling ${it.label}") {
                val dirtyRanges = downSamplingDirtyRangesCollector.remove(it) ?: return@button
                downSamplingService.recalculateDownSampling(dirtyRanges, it.toHistoryBucketRange())
              }
            }

            button("Recalculate Downsampling if necessary") {
              downSamplingService.calculateDownSamplingIfRequired(downSamplingDirtyRangesCollector)
            }
          }

          section("Cleanup")
          declare {
            button("Delete 100 millis") {
              historyStorage.deleteAndBefore(HistoryBucketDescriptor.forTimestamp(nowMillis(), SamplingPeriod.EveryHundredMillis))
            }
          }
        }
      }
    }
  }

  private fun randomValues(): DoubleArray {
    return doubleArrayOf(
      random.nextDouble(50.0, 150.0),
      random.nextDouble(80.0, 250.0),
      random.nextDouble(750.0, 1000.0),
    )
  }
}
