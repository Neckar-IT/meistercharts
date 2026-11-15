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
package com.meistercharts.demo.descriptors


import kotlin.uuid.Uuid
import com.meistercharts.algorithms.layers.addClearBackground
import com.meistercharts.charts.timeline.TimeLineChartGestalt
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.DemoQuality
import com.meistercharts.demo.MeisterchartsDemo
import com.meistercharts.demo.MeisterchartsDemoDescriptor
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.VariabilityType
import com.meistercharts.history.DataSeriesId
import com.meistercharts.history.DecimalDataSeriesIndexProvider
import com.meistercharts.history.HistoryStorageCache
import com.meistercharts.history.InMemoryHistoryStorage
import com.meistercharts.history.SamplingPeriod
import com.meistercharts.history.TimestampIndex
import com.meistercharts.history.historyConfiguration
import com.meistercharts.history.impl.chunk
import com.meistercharts.range.ValueRange
import it.neckar.csv.loadCSV
import it.neckar.datetime.minimal.LocalDate
import it.neckar.datetime.minimal.TimeConstants
import it.neckar.datetime.minimal.TimeZone
import it.neckar.datetime.minimal.toMillisAtStartOfDay
import it.neckar.open.formatting.appendUnit
import it.neckar.open.formatting.cached
import it.neckar.open.formatting.intFormat
import it.neckar.open.formatting.percentageFormat
import it.neckar.open.provider.MultiProvider
import it.neckar.open.unit.si.ms
import kotlinx.coroutines.*

/**
 * A simple hello world demo.
 *
 * Can be used as template to create new demos
 */
class TimeLineChartExampleDataDeathsDemoDescriptor : MeisterchartsDemoDescriptor<Nothing> {
  override val uuid: Uuid = Uuid.parse("2e59925a-6d20-499d-ba4e-0a18617e5ff2")
  override val name: String = "Timeline chart - Example Data - Deaths"

  override val category: DemoCategory = DemoCategory.Calculations
  override val quality: DemoQuality = DemoQuality.High
  override val variabilityType: VariabilityType = VariabilityType.Stable

  override fun prepareDemo(configuration: PredefinedConfiguration<Nothing>?): MeisterchartsDemo {

    return MeisterchartsDemo(this) {
      meistercharts {
        val historyStorage = InMemoryHistoryStorage()
        val historyStorageCache = HistoryStorageCache(historyStorage)
        historyStorage.scheduleDownSampling()

        val historyConfiguration = historyConfiguration {
          decimalDataSeries(DataSeriesId(0), "Sterbestatistik - Total")
          decimalDataSeries(DataSeriesId(1), "Sterbestatistik - m %")
          decimalDataSeries(DataSeriesId(2), "Sterbestatistik - f %")
        }

        zoomAndTranslationModifier {
          minZoom(0.02, 0.000001)
          maxZoom(12.0, 500.0)
        }

        onDispose(historyStorage)

        val gestalt = TimeLineChartGestalt(historyStorage, historyConfiguration) {
          this.contentAreaDuration = TimeConstants.millisPerDecade

          this.lineValueRanges = MultiProvider.cached {
            when (it) {
              0 -> ValueRange.linear(20_000.0, 130_000.0) //deaths total
              1 -> ValueRange.linear(0.3, 0.7) //deaths male
              2 -> ValueRange.linear(0.3, 0.7) //deaths female
              else -> ValueRange.default
            }
          }

          requestedVisibleValueAxesIndices = DecimalDataSeriesIndexProvider.indices { 1 }
          valueAxisStyleConfiguration = { style, dataSeriesIndex ->
            style.size = 120.0
            style.ticksFormat = intFormat
          }

          this.crossWireDecimalFormat = MultiProvider.cached {
            when (it) {
              0 -> {
                intFormat.appendUnit("Tode - Total").cached()
              }

              1 -> percentageFormat.appendUnit("Tode - ♂").cached()
              2 -> percentageFormat.appendUnit("Tode - ♀").cached()
              else -> intFormat
            }
          }

        }.also {
          it.configuration.minimumSamplingPeriod = SamplingPeriod.Every30Days
        }

        GlobalScope.launch {
          val combinedCsv: List<List<String>> = loadCSV("data/Combined.csv")
          var year = 1950

          val chunk = historyConfiguration.chunk(combinedCsv.size) { it: TimestampIndex ->
            val monthInt = (it.value + 1) % 12 // 1 is Jan, 0 is Dec
            val month = if (monthInt == 0) 12 else monthInt

            val dateTime = LocalDate(year, month, 1)
            @ms val timestamp = dateTime.toMillisAtStartOfDay(TimeZone.Berlin)


            addValues(
              timestamp = timestamp,
              decimalValues = doubleArrayOf(
                combinedCsv[it.value][7].toDouble(),// Deaths - Total
                combinedCsv[it.value][5].toDouble() / combinedCsv[it.value][7].toDouble(),// Deaths - male
                combinedCsv[it.value][6].toDouble() / combinedCsv[it.value][7].toDouble(), // Deaths - female
              ),
              enumValues = intArrayOf(),
              referenceEntryIds = intArrayOf(), referenceEntryStatuses = intArrayOf(), entryDataSet = emptySet()
            )

            if (monthInt == 0) {
              year++
            }
          }
          historyStorageCache.scheduleForStore(chunk, SamplingPeriod.Every30Days)
        }


        gestalt.configure(this)

        configure {
          layers.addClearBackground()

        }

      }
    }
  }


}
