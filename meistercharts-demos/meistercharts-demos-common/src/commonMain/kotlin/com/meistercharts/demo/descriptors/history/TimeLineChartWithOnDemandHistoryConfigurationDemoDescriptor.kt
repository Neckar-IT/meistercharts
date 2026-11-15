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

import kotlin.uuid.Uuid
import com.meistercharts.algorithms.layers.AxisConfiguration
import com.meistercharts.algorithms.tile.DefaultHistoryGapCalculator
import com.meistercharts.canvas.translateOverTime
import com.meistercharts.charts.timeline.TimeLineChartGestalt
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.DemoQuality
import com.meistercharts.demo.MeisterchartsDemo
import com.meistercharts.demo.MeisterchartsDemoDescriptor
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.VariabilityType
import com.meistercharts.demo.configurableInt
import com.meistercharts.history.DataSeriesId
import com.meistercharts.history.DecimalDataSeriesIndexProvider
import com.meistercharts.history.HistoryStorageCache
import com.meistercharts.history.HistoryUnit
import com.meistercharts.history.InMemoryHistoryStorage
import com.meistercharts.history.cleanup.MaxHistorySizeConfiguration
import com.meistercharts.history.historyConfiguration
import com.meistercharts.history.impl.chunk
import it.neckar.geometry.Side
import com.meistercharts.model.Vicinity
import it.neckar.open.i18n.TextKey
import it.neckar.open.kotlin.lang.random
import it.neckar.open.time.nowMillis
import it.neckar.open.unit.si.ms

class TimeLineChartWithOnDemandHistoryConfigurationDemoDescriptor : MeisterchartsDemoDescriptor<Nothing> {
  override val uuid: Uuid = Uuid.parse("98896ca1-d70d-4aa6-a66c-16b1e688dc11")
  override val category: DemoCategory = DemoCategory.Gestalt
  override val name: String = "Time Line Chart - with on demand history configuration"

  // usability: lines should be visible initial
  override val quality: DemoQuality = DemoQuality.Low
  override val variabilityType: VariabilityType = VariabilityType.Unstable

  override val description: String = "Starts empty and updates the history configuration on demand as samples arrive."

  override fun prepareDemo(configuration: PredefinedConfiguration<Nothing>?): MeisterchartsDemo {
    return MeisterchartsDemo(this) {
      meistercharts {
        val historyStorage = InMemoryHistoryStorage().also {
          it.maxSizeConfiguration = MaxHistorySizeConfiguration(7)
        }

        val historyStorageCache = HistoryStorageCache(historyStorage)

        historyStorage.scheduleDownSampling()
        historyStorage.scheduleCleanupService()
        onDispose(historyStorage)


        val gestalt = TimeLineChartGestalt(initialHistoryStorage = historyStorage)
        gestalt.configure(this)

        gestalt.configuration.valueAxisStyleConfiguration = { style, _ ->
          style.side = Side.Left
          style.tickOrientation = Vicinity.Outside
          style.paintRange = AxisConfiguration.PaintRange.Continuous
        }
        gestalt.configuration.historyGapCalculator = DefaultHistoryGapCalculator(10.0)

        configure {
          chartSupport.translateOverTime.animated = false

          gestalt.configuration.requestedVisibleDecimalSeriesIndices = DecimalDataSeriesIndexProvider.empty()
          gestalt.configuration.requestedVisibleValueAxesIndices = DecimalDataSeriesIndexProvider.empty()

          configurableInt("Visible lines") {
            value = 0
            min = 0
            max = 10
            onChange { linesCount ->
              gestalt.configuration.requestedVisibleDecimalSeriesIndices = DecimalDataSeriesIndexProvider.indices { linesCount }
              markAsDirty()
            }
          }

          configurableInt("Visible value axes") {
            value = 0
            min = 0
            max = 10
            onChange { axesCount ->
              gestalt.configuration.requestedVisibleValueAxesIndices = DecimalDataSeriesIndexProvider.indices { axesCount }
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
                gestalt.configuration.historyConfiguration = historyConfiguration {
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

              val historyConfiguration = gestalt.configuration.historyConfiguration
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

