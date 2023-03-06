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

import com.meistercharts.algorithms.layers.AxisStyle
import com.meistercharts.algorithms.tile.DefaultHistoryGapCalculator
import com.meistercharts.canvas.translateOverTime
import com.meistercharts.charts.timeline.TimeLineChartGestalt
import com.meistercharts.demo.ChartingDemo
import com.meistercharts.demo.ChartingDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.configurableInt
import com.meistercharts.history.DataSeriesId
import com.meistercharts.history.DecimalDataSeriesIndexProvider
import com.meistercharts.history.HistoryStorageCache
import com.meistercharts.history.HistoryUnit
import com.meistercharts.history.InMemoryHistoryStorage
import com.meistercharts.history.cleanup.MaxHistorySizeConfiguration
import com.meistercharts.history.historyConfiguration
import com.meistercharts.history.impl.chunk
import com.meistercharts.model.Side
import com.meistercharts.model.Vicinity
import it.neckar.open.kotlin.lang.random
import it.neckar.open.time.nowMillis
import it.neckar.open.i18n.TextKey
import it.neckar.open.unit.si.ms

class TimeLineChartWithOnDemandHistoryConfigurationDemoDescriptor : ChartingDemoDescriptor<Nothing> {
  override val category: DemoCategory = DemoCategory.Gestalt
  override val name: String = "Time Line Chart - with on demand history configuration"

  //language=HTML
  override val description: String = """
    <p>This demo starts with an empty history and an empty history configuration.</p>
    <p>As soon as samples are added to the history the history configuration will be updated if</p>
      <ul>
      <li>it is the first time samples are added or</li>
      <li>if the number of data series has been changed by the user.</li>
    </ul>
    """

  override fun createDemo(configuration: PredefinedConfiguration<Nothing>?): ChartingDemo {
    return ChartingDemo {
      meistercharts {
        val historyStorage = InMemoryHistoryStorage().also {
          it.maxSizeConfiguration = MaxHistorySizeConfiguration(7)
        }

        val historyStorageCache = HistoryStorageCache(historyStorage)

        historyStorage.scheduleDownSampling()
        historyStorage.scheduleCleanupService()
        onDispose(historyStorage)


        val gestalt = TimeLineChartGestalt(chartId, TimeLineChartGestalt.Data(historyStorage))
        gestalt.configure(this)

        gestalt.style.valueAxisStyleConfiguration = { style, _ ->
          style.side = Side.Left
          style.tickOrientation = Vicinity.Outside
          style.paintRange = AxisStyle.PaintRange.Continuous
        }
        gestalt.data.historyGapCalculator = DefaultHistoryGapCalculator(10.0)

        configure {
          chartSupport.translateOverTime.animated = false

          gestalt.style.requestedVisibleDecimalSeriesIndices = DecimalDataSeriesIndexProvider.empty()
          gestalt.style.requestedVisibleValueAxesIndices = DecimalDataSeriesIndexProvider.empty()

          configurableInt("Visible lines") {
            value = 0
            min = 0
            max = 10
            onChange { linesCount ->
              gestalt.style.requestedVisibleDecimalSeriesIndices = DecimalDataSeriesIndexProvider.indices { linesCount }
              markAsDirty()
            }
          }

          configurableInt("Visible value axes") {
            value = 0
            min = 0
            max = 10
            onChange { axesCount ->
              gestalt.style.requestedVisibleValueAxesIndices = DecimalDataSeriesIndexProvider.indices { axesCount }
              markAsDirty()
            }
          }

          var numberOfDataSeries = 1
          var updateHistoryConfiguration = true

          configurableInt("Number of data series of sample") {
            value = numberOfDataSeries
            min = 1
            max = 10
            onChange {
              numberOfDataSeries = it
              updateHistoryConfiguration = true
            }
          }

          @ms val initialNow = nowMillis()
          @ms var lastTimestamp = initialNow

          declare {
            button("Add 25 samples (1s)") {
              if (updateHistoryConfiguration) {
                // update the history configuration
                gestalt.data.historyConfiguration = historyConfiguration {
                  IntRange(0, numberOfDataSeries - 1).forEach {
                    decimalDataSeries(DataSeriesId(it), TextKey.empty, HistoryUnit.None)
                  }
                }
                //If the history configuration changes we need to clear the history because
                //managing different configurations in a single history is not supported yet.
                historyStorageCache.clear()
                historyStorage.clear()
                lastTimestamp = initialNow
                updateHistoryConfiguration = false
              }

              val historyConfiguration = gestalt.data.historyConfiguration
              val chunk = historyConfiguration.chunk(25) {
                lastTimestamp += 1000
                addDecimalValues(lastTimestamp) {
                  random.nextDouble(35.0, 75.0)
                }
              }
              historyStorageCache.scheduleForStore(chunk, historyStorage.naturalSamplingPeriod)
            }
          }
        }
      }
    }
  }

}

