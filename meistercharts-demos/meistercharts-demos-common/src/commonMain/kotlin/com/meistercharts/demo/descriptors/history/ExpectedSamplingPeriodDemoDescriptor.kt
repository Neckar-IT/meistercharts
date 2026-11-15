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
import com.meistercharts.range.ValueRange
import com.meistercharts.canvas.RoundingStrategy
import com.meistercharts.canvas.TargetRefreshRate
import com.meistercharts.canvas.translateOverTime
import com.meistercharts.charts.timeline.TimeLineChartGestalt
import com.meistercharts.demo.MeisterchartsDemo
import com.meistercharts.demo.MeisterchartsDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.DemoQuality
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.VariabilityType
import com.meistercharts.demo.configurableBoolean
import com.meistercharts.demo.configurableList
import com.meistercharts.history.HistoryStorageCache
import com.meistercharts.history.InMemoryHistoryStorage
import com.meistercharts.history.SamplingPeriod
import com.meistercharts.history.cleanup.MaxHistorySizeConfiguration
import com.meistercharts.history.generator.DecimalValueGenerator
import com.meistercharts.history.generator.HistoryChunkGenerator
import kotlin.math.PI
import kotlin.math.roundToInt
import kotlin.time.Duration.Companion.milliseconds

class ExpectedSamplingPeriodDemoDescriptor : MeisterchartsDemoDescriptor<Nothing> {
  override val uuid: Uuid = Uuid.parse("5e7dcf20-da8f-438f-a8e3-974e3b8fc6ee")
  override val name: String = "Expected sampling period"
  override val category: DemoCategory = DemoCategory.ShowCase
  override val quality: DemoQuality = DemoQuality.High
  override val variabilityType: VariabilityType = VariabilityType.Stable

  override fun prepareDemo(configuration: PredefinedConfiguration<Nothing>?): MeisterchartsDemo {

    return MeisterchartsDemo(this) {
      meistercharts {
        val historyStorage = InMemoryHistoryStorage().also {
          it.maxSizeConfiguration = MaxHistorySizeConfiguration(7)
        }

        historyStorage.scheduleDownSampling()
        historyStorage.scheduleCleanupService()


        val gestalt = TimeLineChartGestalt(initialHistoryStorage = historyStorage)

        val historyStorageCache = HistoryStorageCache(historyStorage)

        val valueRanges = listOf(
          ValueRange.linear(PI - 0.1, PI + 0.1),
          ValueRange.linear(0.0, 10.0),
          ValueRange.linear(-10.0, 10.0),
        )

        val decimalValueGenerators = mutableListOf(
          DecimalValueGenerator.always(PI),
          DecimalValueGenerator.sine(valueRanges[1]),
          DecimalValueGenerator.cosine(valueRanges[2]),
        )

        val historyChunkGenerator = HistoryChunkGenerator(
          historyStorage = historyStorage, samplingPeriod = historyStorage.naturalSamplingPeriod,
          decimalValueGenerators = decimalValueGenerators,
          enumValueGenerators = emptyList(),
          referenceEntryGenerators = emptyList(),
        )

        gestalt.configure(this)

        onDispose(historyStorage)

        it.neckar.open.time.repeat(100.milliseconds) {
          historyChunkGenerator.next()?.let {
            historyStorageCache.scheduleForStore(it, historyStorage.naturalSamplingPeriod)
          }
        }.also {
          onDispose(it)
        }

        configure {
          //decrease number of repaints
          chartSupport.translateOverTime.roundingStrategy = RoundingStrategy.round

          chartSupport.targetRenderRate = TargetRefreshRate.veryFast60

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

          configurableList("Expected sampling period", SamplingPeriod.EveryHundredMillis, SamplingPeriod.entries) {
            onChange {
              historyStorageCache.clear()
              historyStorage.clear()


              gestalt.configuration.minimumSamplingPeriod = it

              val naturalHistoryBucketRange = it.toHistoryBucketRange()
              historyStorage.naturalSamplingPeriod = naturalHistoryBucketRange.samplingPeriod
              historyStorage.maxSizeConfiguration = MaxHistorySizeConfiguration.forDuration(3600 * 1000.0, naturalHistoryBucketRange)

              markAsDirty()
            }
          }

        }
      }
    }
  }

}

