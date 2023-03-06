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

import com.meistercharts.algorithms.ValueRange
import com.meistercharts.canvas.RoundingStrategy
import com.meistercharts.canvas.TargetRefreshRate
import com.meistercharts.canvas.translateOverTime
import com.meistercharts.charts.timeline.TimeLineChartGestalt
import com.meistercharts.demo.ChartingDemo
import com.meistercharts.demo.ChartingDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.configurableBoolean
import com.meistercharts.demo.configurableList
import com.meistercharts.history.HistoryStorageCache
import com.meistercharts.history.InMemoryHistoryStorage
import com.meistercharts.history.SamplingPeriod
import com.meistercharts.history.WritableHistoryStorage
import com.meistercharts.history.cleanup.MaxHistorySizeConfiguration
import com.meistercharts.history.generator.DecimalValueGenerator
import com.meistercharts.history.generator.HistoryChunkGenerator
import kotlin.math.PI
import kotlin.math.roundToInt
import kotlin.time.Duration.Companion.milliseconds

class ExpectedSamplingPeriodDemoDescriptor : ChartingDemoDescriptor<Nothing> {
  override val name: String = "Expected sampling period"
  override val category: DemoCategory = DemoCategory.ShowCase

  override fun createDemo(configuration: PredefinedConfiguration<Nothing>?): ChartingDemo {

    return ChartingDemo {
      meistercharts {
        val historyStorage = InMemoryHistoryStorage().also {
          it.maxSizeConfiguration = MaxHistorySizeConfiguration(7)
        }

        historyStorage.scheduleDownSampling()
        historyStorage.scheduleCleanupService()


        val gestalt = TimeLineChartGestalt(chartId, TimeLineChartGestalt.Data(historyStorage))

        val writableHistoryStorage = historyStorage as WritableHistoryStorage
        val historyStorageCache = HistoryStorageCache(writableHistoryStorage)

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

          //Set the preferred refresh rate
          chartSupport.targetRefreshRate = TargetRefreshRate.veryFast60

          configurableBoolean("Play Mode", chartSupport.translateOverTime::animated) {
            value = true
          }

          configurableList(
            "Content area duration (sec)", (gestalt.style.contentAreaDuration / 1000.0).roundToInt(), listOf(
              10,
              60,
              60 * 10,
              60 * 60,
              60 * 60 * 24
            )
          ) {
            onChange {
              gestalt.style.contentAreaDuration = it * 1000.0
              markAsDirty()
            }
          }

          configurableList("Expected sampling period", SamplingPeriod.EveryHundredMillis, SamplingPeriod.values().toList()) {
            onChange {
              historyStorageCache.clear()
              historyStorage.clear()


              gestalt.data.minimumSamplingPeriod = it

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

