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

import com.meistercharts.algorithms.painter.Color
import com.meistercharts.algorithms.painter.stripe.refentry.RectangleReferenceEntryStripePainter
import com.meistercharts.canvas.translateOverTime
import com.meistercharts.charts.refs.DiscreteTimelineChartGestalt
import com.meistercharts.demo.ChartingDemo
import com.meistercharts.demo.ChartingDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.configurableBoolean
import com.meistercharts.demo.configurableColorNullable
import com.meistercharts.demo.configurableDouble
import com.meistercharts.demo.configurableIndices
import com.meistercharts.demo.configurableInsetsSeparate
import com.meistercharts.demo.toList
import com.meistercharts.history.DataSeriesId
import com.meistercharts.history.HistoryConfiguration
import com.meistercharts.history.HistoryEnumOrdinal
import com.meistercharts.history.HistoryEnumSet
import com.meistercharts.history.InMemoryHistoryStorage
import com.meistercharts.history.ReferenceEntriesDataMap
import com.meistercharts.history.ReferenceEntryData
import com.meistercharts.history.ReferenceEntryDataSeriesIndex
import com.meistercharts.history.ReferenceEntryDataSeriesIndexProvider
import com.meistercharts.history.ReferenceEntryId
import com.meistercharts.history.SamplingPeriod
import com.meistercharts.history.createDefaultHistoryConfiguration
import com.meistercharts.history.generator.HistoryChunkGenerator
import com.meistercharts.history.generator.HistoryChunkProvider
import com.meistercharts.history.generator.ReferenceEntryGenerator
import com.meistercharts.history.historyConfiguration
import com.meistercharts.history.impl.HistoryChunk
import com.meistercharts.history.impl.chunk
import it.neckar.open.i18n.TextKey
import it.neckar.open.kotlin.lang.getModulo
import it.neckar.open.provider.MultiProvider
import it.neckar.open.time.nowMillis
import it.neckar.open.unit.si.ms
import kotlin.time.Duration.Companion.milliseconds

/**
 * A simple hello world demo.
 *
 * Can be used as template to create new demos
 */
class DiscreteTimelineChartGestaltDemoDescriptor : ChartingDemoDescriptor<DiscreteTimelineChartGestaltDemoDescriptor.DemoConfig> {
  override val name: String = "Reference Entries Gestalt"

  //language=HTML
  override val category: DemoCategory = DemoCategory.Gestalt

  override val predefinedConfigurations: List<PredefinedConfiguration<DemoConfig>> = listOf(
    PredefinedConfiguration(
      object : DemoConfig {
        override val samplingPeriod: SamplingPeriod = SamplingPeriod.EverySecond

        override fun historyChunkProvider(historyStorage: InMemoryHistoryStorage): HistoryChunkGenerator {
          return HistoryChunkGenerator(
            historyStorage = historyStorage,
            samplingPeriod = samplingPeriod,
            decimalValueGenerators = emptyList(),
            enumValueGenerators = emptyList(),
            referenceEntryGenerators = List(12) {
              ReferenceEntryGenerator.random()
            },
          )
        }

        override val config: DiscreteTimelineChartGestalt.() -> Unit = {}

      }, "Random - 100ms"
    ),

    PredefinedConfiguration(
      object : DemoConfig {
        override val samplingPeriod: SamplingPeriod = SamplingPeriod.EverySecond

        override fun historyChunkProvider(historyStorage: InMemoryHistoryStorage): HistoryChunkGenerator {
          return HistoryChunkGenerator(
            historyStorage = historyStorage,
            samplingPeriod = samplingPeriod,
            decimalValueGenerators = emptyList(),
            enumValueGenerators = emptyList(),
            referenceEntryGenerators = List(12) {
              ReferenceEntryGenerator.increasing(513.milliseconds)
            },
          )
        }

        override val config: DiscreteTimelineChartGestalt.() -> Unit = {}
      }, "Increasing (every 513 ms)"
    ),

    PredefinedConfiguration(
      object : DemoConfig {
        override val samplingPeriod: SamplingPeriod = SamplingPeriod.EverySecond

        override fun historyChunkProvider(historyStorage: InMemoryHistoryStorage): HistoryChunkGenerator {
          return HistoryChunkGenerator(
            historyStorage = historyStorage,
            samplingPeriod = samplingPeriod,
            decimalValueGenerators = emptyList(),
            enumValueGenerators = emptyList(),
            referenceEntryGenerators = List(12) {
              ReferenceEntryGenerator.increasing(46_437.milliseconds)

            },
          )
        }

        override val config: DiscreteTimelineChartGestalt.() -> Unit = {}
      }, "Increasing (every 46.437 s)"
    ),

    PredefinedConfiguration(
      object : DemoConfig {
        override val samplingPeriod: SamplingPeriod = SamplingPeriod.EverySecond

        override fun historyChunkProvider(historyStorage: InMemoryHistoryStorage): HistoryChunkGenerator {
          val refEntryDataSeriesCount = 12

          return HistoryChunkGenerator(historyStorage = historyStorage, samplingPeriod = samplingPeriod, decimalValueGenerators = emptyList(), enumValueGenerators = emptyList(), referenceEntryGenerators = List(refEntryDataSeriesCount) {
            ReferenceEntryGenerator.increasing(46_437.milliseconds, factor = it)
          }, referenceEntryStatusProvider = { referenceEntryId, millis ->
            val factor = (millis / 5000.0 + referenceEntryId.id / 77.4).toInt()
            val ordinal: HistoryEnumOrdinal = HistoryReferenceScenarios.jobStateEnum.values.getModulo(factor).ordinal
            HistoryEnumSet.forEnumOrdinal(ordinal)
          }, historyConfiguration = createDefaultHistoryConfiguration(0, 0, refEntryDataSeriesCount, referenceEntryStatusEnumProvider = { referenceEntryDataSeriesIndex -> HistoryReferenceScenarios.jobStateEnum })
          )
        }

        override val config: DiscreteTimelineChartGestalt.() -> Unit = {
          referenceEntryStripePainters = MultiProvider.always(RectangleReferenceEntryStripePainter() {
            val jobStateEnumColors = listOf(Color.red, Color.green, Color.blue)

            fillProvider = { value, statusEnumSet, historyConfiguration ->
              if (statusEnumSet.isNoValue()) {
                Color.silver
              }
              if (statusEnumSet.isPending()) {
                Color.blueviolet
              }

              //HistoryReferenceScenarios.jobStateEnum
              val firstSetOrdinal = statusEnumSet.firstSetOrdinal()
              jobStateEnumColors[firstSetOrdinal.value]
            }
          })
        }

      }, "EnumStatus - Increasing (every 46.437 s)"
    ),

    PredefinedConfiguration(
      object : DemoConfig {
        override val config: DiscreteTimelineChartGestalt.() -> Unit = {}

        override val samplingPeriod: SamplingPeriod = SamplingPeriod.EverySecond

        override val playModeEnabled: Boolean
          get() = false

        override fun historyChunkProvider(historyStorage: InMemoryHistoryStorage): HistoryChunkProvider {
          return object : HistoryChunkProvider {
            override val historyConfiguration: HistoryConfiguration = historyConfiguration {
              referenceEntryDataSeries(DataSeriesId(10), "Discrete 10", null)
              referenceEntryDataSeries(DataSeriesId(11), "Discrete 11", null)
            }

            val chunk = historyConfiguration.chunk {
              val now = nowMillis()

              @ms val distance = samplingPeriod.distance
              @ms val start = now - distance * 2
              @ms val end = now

              val referenceEntriesDataMap = ReferenceEntriesDataMap.of(
                ReferenceEntryData(ReferenceEntryId(6), TextKey.simple("label 6"), start = start, end = end),
                ReferenceEntryData(ReferenceEntryId(7), TextKey.simple("label 7")),
                ReferenceEntryData(ReferenceEntryId(8), TextKey.simple("label 8")),
              )

              addReferenceEntryValues(
                timestamp = start,
                referenceEntryValues = intArrayOf(6, 7),
                referenceEntryIdsCount = null,
                referenceEntryStatuses = intArrayOf(HistoryEnumSet.NoValueAsInt, HistoryEnumSet.NoValueAsInt),
                referenceEntriesDataMap = referenceEntriesDataMap,
              )
              addReferenceEntryValues(
                timestamp = start + distance,
                referenceEntryValues = intArrayOf(6, 8),
                referenceEntryIdsCount = null,
                referenceEntryStatuses = intArrayOf(HistoryEnumSet.NoValueAsInt, HistoryEnumSet.NoValueAsInt),
                referenceEntriesDataMap = referenceEntriesDataMap,
              )
              addReferenceEntryValues(
                timestamp = now,
                referenceEntryValues = intArrayOf(6, 8),
                referenceEntryIdsCount = null,
                referenceEntryStatuses = intArrayOf(HistoryEnumSet.NoValueAsInt, HistoryEnumSet.NoValueAsInt),
                referenceEntriesDataMap = referenceEntriesDataMap,
              )
            }

            override fun next(until: Double): HistoryChunk {
              return chunk
            }
          }
        }
      }, "Fixed with start/end"
    ),

    )

  override fun createDemo(configuration: PredefinedConfiguration<DemoConfig>?): ChartingDemo {
    val payload = requireNotNull(configuration?.payload)

    return ChartingDemo {
      meistercharts {

        val historyStorage = InMemoryHistoryStorage()
        onDispose(historyStorage)

        historyStorage.scheduleDownSampling()
        historyStorage.scheduleCleanupService()


        val historyChunkGenerator = payload.historyChunkProvider(historyStorage)
        val historyConfiguration = historyChunkGenerator.historyConfiguration

        it.neckar.open.time.repeat(payload.samplingPeriod.distance.milliseconds) {
          historyChunkGenerator.next()?.let {
            historyStorage.storeWithoutCache(it, payload.samplingPeriod)
          }
        }.also {
          onDispose(it)
        }

        val gestalt = DiscreteTimelineChartGestalt(historyStorage, {
          historyConfiguration
        })
        gestalt.configure(this@meistercharts)

        //Set the zoom level to show the recording duration
        gestalt.configuration.contentAreaDuration = payload.samplingPeriod.distance * 20
        gestalt.configuration.minimumSamplingPeriod = payload.samplingPeriod

        payload.config.invoke(gestalt)

        val referenceEntryDataSeriesCount = gestalt.configuration.historyConfiguration().referenceEntryDataSeriesCount

        configure {
          configurableBoolean("Play Mode", chartSupport.translateOverTime::animated) {
            value = payload.playModeEnabled
          }

          configurableBoolean("ShowTimeAxis", gestalt.configuration::showTimeAxis) {
          }

          configurableIndices(
            this@ChartingDemo,
            "Visible Data Series",
            "data series visible",
            initial = gestalt.configuration.requestedVisibleReferenceEntrySeriesIndices.toList().map { it.value },
            maxSize = referenceEntryDataSeriesCount,
          ) {
            gestalt.configuration.requestedVisibleReferenceEntrySeriesIndices = ReferenceEntryDataSeriesIndexProvider.forList(it.map { ReferenceEntryDataSeriesIndex(it) })
          }

          configurableColorNullable("Background", gestalt.historyReferenceEntryLayer.configuration::background)

          configurableInsetsSeparate("Viewport", gestalt::contentViewportMargin)

          configurableDouble("Stripe height", gestalt.historyReferenceEntryLayer.configuration::stripeHeight) {
            max = 50.0
          }
          configurableDouble("Stripe distance", gestalt.historyReferenceEntryLayer.configuration::stripesDistance) {
            max = 50.0
          }
        }
      }
    }
  }

  interface DemoConfig {
    val playModeEnabled: Boolean
      get() {
        return true
      }

    val config: DiscreteTimelineChartGestalt.() -> Unit

    val samplingPeriod: SamplingPeriod

    /**
     * Returns the history chunk provider
     */
    fun historyChunkProvider(historyStorage: InMemoryHistoryStorage): HistoryChunkProvider
  }
}
