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
import com.meistercharts.charts.timeline.TimeLineChartGestalt
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.DemoQuality
import com.meistercharts.demo.MeisterchartsDemo
import com.meistercharts.demo.MeisterchartsDemoDescriptor
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.VariabilityType
import com.meistercharts.history.InMemoryHistoryStorage
import com.meistercharts.history.SamplingPeriod
import com.meistercharts.history.generator.EnumValueGenerator
import com.meistercharts.history.generator.HistoryChunkGenerator
import com.meistercharts.time.TimeRange
import it.neckar.open.time.nowMillis

class TimeLineChartGestaltOnlyEnumDemoDescriptor : MeisterchartsDemoDescriptor<Nothing> {
  override val uuid: Uuid = Uuid.parse("60b570ab-ac63-484e-8431-6c0a0a9f40c1")
  override val name: String = "Timeline - only enums"
  override val category: DemoCategory = DemoCategory.Gestalt
  override val quality: DemoQuality = DemoQuality.High
  override val variabilityType: VariabilityType = VariabilityType.Stable


  override fun prepareDemo(configuration: PredefinedConfiguration<Nothing>?): MeisterchartsDemo {
    return MeisterchartsDemo(this) {
      meistercharts {
        val enumDataSeriesCount = 7

        val historyStorage = InMemoryHistoryStorage()
        historyStorage.scheduleDownSampling()
        historyStorage.scheduleCleanupService()

        onDispose(historyStorage)

        val samplingPeriod = SamplingPeriod.EveryHundredMillis
        val historyChunkGenerator = HistoryChunkGenerator(
          historyStorage = historyStorage, samplingPeriod = samplingPeriod,
          decimalValueGenerators = emptyList(),
          enumValueGenerators = List(enumDataSeriesCount) { EnumValueGenerator.random() },
          referenceEntryGenerators = emptyList(),
        )
        val historyConfiguration = historyChunkGenerator.historyConfiguration


        // fill history with 50 samples
        historyChunkGenerator.forTimeRange(TimeRange.fromEndAndDuration(nowMillis(), samplingPeriod.distance * 500))?.let {
          historyStorage.storeWithoutCache(it, samplingPeriod)
        }

        val gestalt = TimeLineChartGestalt(
          historyStorage, historyConfiguration
        ) {}
        gestalt.configure(this@meistercharts)
        gestalt.configuration.showAllEnumSeries()
      }
    }
  }
}
