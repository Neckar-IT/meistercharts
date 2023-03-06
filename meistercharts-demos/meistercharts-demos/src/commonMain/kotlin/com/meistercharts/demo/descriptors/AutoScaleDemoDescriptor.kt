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
package com.meistercharts.demo.descriptors

import com.meistercharts.algorithms.ValueRange
import com.meistercharts.algorithms.layers.ValueAxisLayer
import com.meistercharts.canvas.translateOverTime
import com.meistercharts.charts.AutoScaleSupport
import com.meistercharts.charts.timeline.TimeLineChartGestalt
import com.meistercharts.charts.installAutoScaleSupport
import com.meistercharts.demo.ChartingDemo
import com.meistercharts.demo.ChartingDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.descriptors.history.setUpHistoryChunkGenerator
import com.meistercharts.history.DecimalDataSeriesIndex
import com.meistercharts.history.DecimalDataSeriesIndexProvider
import com.meistercharts.history.HistoryStorageCache
import com.meistercharts.history.InMemoryHistoryStorage
import com.meistercharts.history.SamplingPeriod
import com.meistercharts.history.WritableHistoryStorage
import com.meistercharts.history.cleanup.MaxHistorySizeConfiguration
import it.neckar.open.provider.BooleanProvider
import it.neckar.open.provider.MultiProvider
import it.neckar.logging.LoggerFactory
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.DurationUnit
import kotlin.time.measureTime

/**
 * A simple hello world demo
 */
class AutoScaleDemoDescriptor : ChartingDemoDescriptor<Nothing> {
  override val name: String = "Timeline Chart with Auto Scale"
  override val category: DemoCategory = DemoCategory.Automation

  override fun createDemo(configuration: PredefinedConfiguration<Nothing>?): ChartingDemo {
    return ChartingDemo {

      val defaultDataSeriesValueRanges = listOf(
        ValueRange.linear(0.0, 1000.0),
        ValueRange.linear(0.0, 300.0),
        ValueRange.linear(0.0, 999999.0),
        ValueRange.linear(0.0, 1000.0),
        ValueRange.linear(0.0, 999999.0),
        ValueRange.linear(0.0, 999999.0),
        ValueRange.linear(-50.0, 100.0),
        ValueRange.linear(-0.5, 17.0)
      )
      val defaultValueRangesProvider = MultiProvider.forListModulo<DecimalDataSeriesIndex, _>(defaultDataSeriesValueRanges)


      meistercharts {
        val historyStorage = InMemoryHistoryStorage().also {
          it.maxSizeConfiguration = MaxHistorySizeConfiguration(7)
          onDispose(it)
        }

        historyStorage.scheduleDownSampling()
        historyStorage.scheduleCleanupService()

        val gestalt = TimeLineChartGestalt(chartId, TimeLineChartGestalt.Data(historyStorage))

        gestalt.apply {
          val samplingPeriod = SamplingPeriod.EveryHundredMillis

          //Also sets the history configuration to the gestalt
          val historyChunkGenerator = this.setUpHistoryChunkGenerator(samplingPeriod)

          //overwrite the value ranges
          gestalt.style.lineValueRanges = MultiProvider.forListModulo(defaultDataSeriesValueRanges)
          gestalt.style.requestedVisibleValueAxesIndices = DecimalDataSeriesIndexProvider.indices { 8 }
          gestalt.style.valueAxisStyleConfiguration = { style: ValueAxisLayer.Style, _ ->
            style.titleVisible = BooleanProvider.False
            style.size = 120.0
          }


          val historyStorageCache = HistoryStorageCache(historyStorage as WritableHistoryStorage)

          it.neckar.open.time.repeat(100.milliseconds) {
            historyChunkGenerator.next()?.let {
              historyStorageCache.scheduleForStore(it, samplingPeriod)
            }
          }.also {
            onDispose(it)
          }
        }

        gestalt.configure(this)

        configure {
          chartSupport.translateOverTime.animated = true


          declare {
            button("Auto Scale 0") {
              measureTime {
                val autoScaleSupport = AutoScaleSupport(gestalt, defaultValueRangesProvider)
                autoScaleSupport.recalculateAutoScale(chartSupport, listOf(DecimalDataSeriesIndex.zero))
              }.also { logger.debug("Took ${it.toDouble(DurationUnit.MILLISECONDS)} ms") }
            }
            button("Auto Scale 1") {
              measureTime {
                val autoScaleSupport = AutoScaleSupport(gestalt, defaultValueRangesProvider)
                autoScaleSupport.recalculateAutoScale(chartSupport, listOf(DecimalDataSeriesIndex.zero))
              }.also { logger.debug("Took ${it.toDouble(DurationUnit.MILLISECONDS)} ms") }
            }

            button("Auto Scale All") {
              measureTime {
                val autoScaleSupport = AutoScaleSupport(gestalt, defaultValueRangesProvider)

                autoScaleSupport.recalculateAutoScale(chartSupport, List(8) {
                  DecimalDataSeriesIndex(it)
                })
              }.also { logger.debug("Took ${it.toDouble(DurationUnit.MILLISECONDS)} ms") }
            }

            button("Enable Auto Scale") {
              val dataSeriesIndices = List(8) {
                DecimalDataSeriesIndex(it)
              }
              gestalt.installAutoScaleSupport(chartSupport, dataSeriesIndices, defaultValueRangesProvider)
            }
          }
        }
      }
    }
  }

  companion object {
    private val logger = LoggerFactory.getLogger("com.meistercharts.demo.descriptors.AutoScaleDemoDescriptor")
  }
}
