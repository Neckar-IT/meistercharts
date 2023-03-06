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

import com.meistercharts.algorithms.layers.crosswire.CrossWireLayer
import com.meistercharts.algorithms.layers.linechart.Dashes
import com.meistercharts.algorithms.layers.linechart.LineStyle
import com.meistercharts.animation.Easing
import com.meistercharts.canvas.translateOverTime
import com.meistercharts.charts.timeline.TimeLineChartGestalt
import com.meistercharts.charts.timeline.TimeLineChartWithToolbarGestalt
import com.meistercharts.demo.ChartingDemo
import com.meistercharts.demo.ChartingDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.TimeBasedValueGeneratorBuilder
import com.meistercharts.demo.configurableBoolean
import com.meistercharts.demo.configurableInt
import com.meistercharts.design.Theme
import com.meistercharts.history.DataSeriesId
import com.meistercharts.history.DecimalDataSeriesIndexProvider
import com.meistercharts.history.HistoryUnit
import com.meistercharts.history.InMemoryHistoryStorage
import com.meistercharts.history.SamplingPeriod
import com.meistercharts.history.WritableHistoryStorage
import com.meistercharts.history.cleanup.MaxHistorySizeConfiguration
import com.meistercharts.history.generator.HistoryChunkGenerator
import com.meistercharts.history.historyConfigurationOnlyDecimals
import it.neckar.open.kotlin.lang.fastMap
import it.neckar.open.kotlin.lang.getModulo
import it.neckar.open.provider.MultiProvider
import it.neckar.open.i18n.TextKey
import it.neckar.open.observable.ObservableBoolean
import com.meistercharts.style.BoxStyle
import kotlin.time.Duration.Companion.milliseconds

class TimeLineChartHistoryConfigDemoDescriptor : ChartingDemoDescriptor<Nothing> {
  override val name: String = "Time Line Chart with various history configurations"
  override val category: DemoCategory = DemoCategory.ShowCase
  override val description: String = "Demonstrates changing the history configuration at runtime"

  override fun createDemo(configuration: PredefinedConfiguration<Nothing>?): ChartingDemo {
    return ChartingDemo {
      meistercharts {
        val historyStorage = InMemoryHistoryStorage().also {
          it.maxSizeConfiguration = MaxHistorySizeConfiguration(7)
        }

        historyStorage.scheduleDownSampling()
        historyStorage.scheduleCleanupService()

        val gestalt = TimeLineChartWithToolbarGestalt(chartId, historyStorage)

        gestalt.configure(this)

        //do not show labels because we have up to 500 data series
        gestalt.timeLineChartGestalt.crossWireLayerDecimalValues.style.showValueLabels = false

        gestalt.timeLineChartGestalt.style.lineStyles = MultiProvider {
          LineStyle(Theme.chartColors().valueAt(it), 1.0 + it % 3, Dashes.predefined.getModulo(it))
        }
        gestalt.timeLineChartGestalt.style.crossWireDecimalsLabelBoxStyles = MultiProvider {
          BoxStyle(Theme.chartColors().valueAt(it), padding = CrossWireLayer.Style.DefaultLabelBoxPadding)
        }

        var historyChunkGenerator = createHistoryChunkGenerator(gestalt.timeLineChartGestalt)

        onDispose(historyStorage)

        val liveDataEnabled = ObservableBoolean(true)
        it.neckar.open.time.repeat(100.milliseconds) {
          if (liveDataEnabled.value) {
            historyChunkGenerator.next()?.let {
              gestalt.timeLineChartGestalt.writableHistoryStorage.storeWithoutCache(it, samplingPeriod)
            }
          }
        }.also {
          onDispose(it)
        }

        configure {
          chartSupport.translateOverTime.animated = true

          configurableBoolean("Live data", liveDataEnabled) {
          }

          val maxDataSeriesCount = 500

          configurableInt("Data series count") {
            value = 2
            min = 0
            max = maxDataSeriesCount
            onChange {
              val historyConfiguration = historyConfigurationOnlyDecimals(it) { dataSeriesIndex ->
                decimalDataSeries(
                  DataSeriesId(dataSeriesIndex.value + 1),
                  TextKey.simple("DS $dataSeriesIndex"),
                  HistoryUnit.None,
                )
              }
              (gestalt.timeLineChartGestalt.data.historyStorage as InMemoryHistoryStorage).clear()
              gestalt.timeLineChartGestalt.data.historyConfiguration = historyConfiguration
              historyChunkGenerator = createHistoryChunkGenerator(gestalt.timeLineChartGestalt)
              markAsDirty()
            }
          }

          configurableInt("Visible lines") {
            value = 6
            min = 0
            max = maxDataSeriesCount
            onChange { visibleLinesCount: Int ->
              gestalt.timeLineChartGestalt.style.requestedVisibleDecimalSeriesIndices = DecimalDataSeriesIndexProvider.indices { visibleLinesCount }
              markAsDirty()
            }
          }

          configurableInt("Visible value axes") {
            value = 2
            min = 0
            max = 6
            onChange { visibleValueAxesCount: Int ->
              gestalt.timeLineChartGestalt.style.requestedVisibleValueAxesIndices = DecimalDataSeriesIndexProvider.indices { visibleValueAxesCount }
              markAsDirty()
            }
          }

        }
      }
    }
  }

  private fun createHistoryChunkGenerator(gestalt: TimeLineChartGestalt): HistoryChunkGenerator {
    require(gestalt.data.historyConfiguration.enumDataSeriesCount == 0) {
      "Currently no enums supported!"
    }

    val valueGenerators = gestalt.data.historyConfiguration.decimalDataSeriesCount.fastMap { dataSeriesIndex ->
      TimeBasedValueGeneratorBuilder {
        easing = Easing.availableEasings.getModulo(dataSeriesIndex)
      }.build()
    }
    return HistoryChunkGenerator(gestalt.writableHistoryStorage, samplingPeriod, valueGenerators, emptyList(), emptyList())
  }

  companion object {
    private val samplingPeriod = SamplingPeriod.EveryHundredMillis
  }

  private val TimeLineChartGestalt.writableHistoryStorage: WritableHistoryStorage
    get() {
      return data.historyStorage as WritableHistoryStorage
    }
}

