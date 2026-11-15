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
import com.meistercharts.demo.section
import kotlin.uuid.Uuid
import com.meistercharts.time.TimeRange
import com.meistercharts.zoom.UpdateReason
import com.meistercharts.range.ValueRange
import com.meistercharts.algorithms.layers.crosswire.CrossWireLayer
import com.meistercharts.algorithms.layers.debug.ShowTimeRangeLayer
import com.meistercharts.algorithms.layers.linechart.Dashes
import com.meistercharts.algorithms.layers.linechart.LineStyle
import com.meistercharts.color.Color
import com.meistercharts.algorithms.tile.DefaultHistoryGapCalculator
import com.meistercharts.canvas.RoundingStrategy
import com.meistercharts.canvas.TargetRefreshRate
import com.meistercharts.canvas.translateOverTime
import com.meistercharts.charts.timeline.TimeLineChartGestalt
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.DemoQuality
import com.meistercharts.demo.MeisterchartsDemo
import com.meistercharts.demo.MeisterchartsDemoDescriptor
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.VariabilityType
import com.meistercharts.demo.configurableBoolean
import com.meistercharts.demo.configurableDouble
import com.meistercharts.demo.configurableInt
import com.meistercharts.demo.configurableList
import com.meistercharts.demo.configurableListWithProperty
import com.meistercharts.history.DecimalDataSeriesIndexProvider
import com.meistercharts.history.InMemoryHistoryStorage
import com.meistercharts.history.SamplingPeriod
import com.meistercharts.history.cleanup.MaxHistorySizeConfiguration
import com.meistercharts.history.generator.DecimalValueGenerator
import com.meistercharts.history.generator.HistoryChunkGenerator
import com.meistercharts.history.generator.offset
import com.meistercharts.history.generator.scaled
import com.meistercharts.style.BoxStyle
import com.meistercharts.style.Palette.getChartColor
import it.neckar.open.kotlin.lang.asProvider
import it.neckar.open.kotlin.lang.random
import it.neckar.open.observable.ObservableBoolean
import it.neckar.open.provider.MultiProvider
import it.neckar.open.time.nowMillis
import it.neckar.open.time.repeat
import kotlin.math.PI
import kotlin.math.roundToInt
import kotlin.time.Duration.Companion.milliseconds

class TimeLineChartDemoDescriptor : MeisterchartsDemoDescriptor<SamplingPeriod> {
  override val uuid: Uuid = Uuid.parse("cc7c4863-6231-4b86-9a3d-43fb3c82eb52")
  override val name: String = "Time Line Chart (Layer)"
  override val category: DemoCategory = DemoCategory.ShowCase
  override val quality: DemoQuality = DemoQuality.High
  override val variabilityType: VariabilityType = VariabilityType.Stable

  override val predefinedConfigurations: List<PredefinedConfiguration<SamplingPeriod>> = listOf(
    PredefinedConfiguration(Uuid.parse("1121a4d3-eaf4-41bd-9851-8c6538a5fabb"), SamplingPeriod.EveryHundredMillis),
    PredefinedConfiguration(Uuid.parse("7689f0a2-7d29-482f-95d8-6eb7a71d6706"), SamplingPeriod.Every24Hours),
  )

  override fun prepareDemo(configuration: PredefinedConfiguration<SamplingPeriod>?): MeisterchartsDemo {
    require(configuration != null) { "configuration must not be null" }

    val samplingPeriod: SamplingPeriod = configuration.payload

    return MeisterchartsDemo(this) {
      meistercharts {
        val historyStorage = InMemoryHistoryStorage()
        historyStorage.scheduleDownSampling()
        historyStorage.scheduleCleanupService()

        val dataSeriesCount = 8
        val valueRanges = listOf(
          ValueRange.linear(PI - 0.1, PI + 0.1),
          ValueRange.linear(0.0, 10.0),
          ValueRange.linear(-10.0, 10.0),
          ValueRange.linear(0.0, 100.0),
          ValueRange.linear(0.0, 100.0),
          ValueRange.linear(0.0, 100.0),
          ValueRange.linear(0.0, 100.0),
          ValueRange.linear(0.0, 100.0)
        )

        val decimalValueGenerators = mutableListOf(
          DecimalValueGenerator.always(PI),
          DecimalValueGenerator.sine(valueRanges[1]),
          DecimalValueGenerator.cosine(valueRanges[2]),
          DecimalValueGenerator.normality(valueRanges[3]).offset(40.0),
          DecimalValueGenerator.normality(valueRanges[4], valueRanges[4].delta * 0.05).scaled(1.2, 20.0),
          DecimalValueGenerator.normality(valueRanges[5], valueRanges[5].delta * 0.075),
          DecimalValueGenerator.normality(valueRanges[6], valueRanges[6].delta * 0.005).offset(-20.0),
          DecimalValueGenerator.normality(valueRanges[7]).offset(-40.0)
        )

        val historyChunkGenerator = HistoryChunkGenerator(historyStorage = historyStorage, samplingPeriod = samplingPeriod, decimalValueGenerators = decimalValueGenerators, enumValueGenerators = emptyList(), referenceEntryGenerators = emptyList())
        val historyConfiguration = historyChunkGenerator.historyConfiguration


        // fill history with 50 samples
        historyChunkGenerator.forTimeRange(TimeRange.fromEndAndDuration(nowMillis(), samplingPeriod.distance * 50))?.let {
          historyStorage.storeWithoutCache(it, samplingPeriod)
        }

        val colors = IntRange(0, dataSeriesCount - 1).map { getChartColor(it) }

        val gestalt = TimeLineChartGestalt(historyStorage, historyConfiguration) {
          lineValueRanges = MultiProvider.forListModulo(valueRanges)
          lineStyles = MultiProvider.forListModulo(colors.map { LineStyle(color = it) })
          crossWireDecimalsLabelBoxStyles = MultiProvider.forListModulo(colors.map { BoxStyle(fill = it, padding = CrossWireLayer.Configuration.DefaultLabelBoxPadding) })
          requestedVisibleValueAxesIndices = DecimalDataSeriesIndexProvider.indices { 2 }
          requestedVisibleDecimalSeriesIndices = DecimalDataSeriesIndexProvider.indices { 2 }
        }

        gestalt.configuration.minimumSamplingPeriod = samplingPeriod
        historyStorage.maxSizeConfiguration = MaxHistorySizeConfiguration.forDuration(samplingPeriod.distance * 36000, samplingPeriod.toHistoryBucketRange())

        gestalt.configure(this)

        onDispose(historyStorage)

        configure {
          chartSupport.translateOverTime.animated = true

          layers.addLayer(ShowTimeRangeLayer(gestalt.configuration.contentAreaTimeRange))

          declare {
            button("Play/Pause") {
              chartSupport.translateOverTime.animated = !chartSupport.translateOverTime.animated
            }

            button("Home") {
              chartSupport.zoomAndTranslationSupport.resetToDefaults(reason = UpdateReason.UserInteraction)
            }
          }


          val liveDataEnabled = ObservableBoolean(true)

          //Repeat in accordance to sampling period
          repeat(samplingPeriod.distance.milliseconds) {
            if (liveDataEnabled.value) {
              //The demo generator checks automatically how many and which values have to be added
              historyChunkGenerator.next()?.let {
                historyStorage.storeWithoutCache(it, samplingPeriod)
              }
            }

          }.let {
            chartSupport.onDispose(it)
          }

          section("Model")

          configurableBoolean("Live data", liveDataEnabled) {
          }

          configurableInt("History length (s)") {
            min = 1
            max = (samplingPeriod.distance * 7.2).roundToInt()

            value = (historyStorage.maxSizeConfiguration.getGuaranteedDuration(samplingPeriod.toHistoryBucketRange()) / 1000.0).roundToInt()

            onChange {
              historyStorage.maxSizeConfiguration = MaxHistorySizeConfiguration.forDuration(it * 1000.0, samplingPeriod.toHistoryBucketRange())
              markAsDirty()
            }
          }

          configurableDouble("Gap factor", (gestalt.configuration.historyGapCalculator as DefaultHistoryGapCalculator).factor) {
            min = 0.1
            max = 20.0
            onChange {
              gestalt.configuration.historyGapCalculator = DefaultHistoryGapCalculator(it)
              markAsDirty()
            }
          }

          section("Visibility")

          configurableInt("Visible value axes") {
            min = 0
            max = dataSeriesCount
            value = gestalt.configuration.requestedVisibleValueAxesIndices.size()
            onChange {
              gestalt.configuration.requestedVisibleValueAxesIndices = DecimalDataSeriesIndexProvider.indices { it }
              markAsDirty()
            }
          }

          configurableInt("Visible lines") {
            min = 0
            max = dataSeriesCount
            value = gestalt.configuration.requestedVisibleDecimalSeriesIndices.size()
            onChange {
              gestalt.configuration.requestedVisibleDecimalSeriesIndices = DecimalDataSeriesIndexProvider.indices { it }
              markAsDirty()
            }
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

          section("X-Axis")

          configurable("Cross wire position", gestalt.configuration::crossWirePositionX) {
            min = 0.0
            max = 1.0
          }

          configurable("Time axis size", gestalt.configuration::timeAxisSize) {
            min = 0.0
            max = 200.0
          }

          declare {
            section("Style")

            button("Random colors and dashes") {
              val newLineStyles = listOf(
                LineStyle(color = Color.random().asProvider(), dashes = Dashes.predefined[random.nextInt(Dashes.predefined.size)]),
                LineStyle(color = Color.random().asProvider(), dashes = Dashes.predefined[random.nextInt(Dashes.predefined.size)]),
                LineStyle(color = Color.random().asProvider(), dashes = Dashes.predefined[random.nextInt(Dashes.predefined.size)]),
                LineStyle(color = Color.random().asProvider(), dashes = Dashes.predefined[random.nextInt(Dashes.predefined.size)])
              )
              gestalt.configuration.lineStyles = MultiProvider.forListModulo(
                newLineStyles
              )
              gestalt.configuration.crossWireDecimalsLabelBoxStyles = MultiProvider.forListModulo(newLineStyles.map {
                BoxStyle(fill = it.color, padding = CrossWireLayer.Configuration.DefaultLabelBoxPadding)
              })

              markAsDirty()
            }
          }

          configurableListWithProperty("Refresh rate", chartSupport::targetRenderRate, TargetRefreshRate.predefined)
          configurableListWithProperty("Translation Anim Rounding", chartSupport.translateOverTime::roundingStrategy, RoundingStrategy.predefined) {
            converter {
              when (it) {
                RoundingStrategy.exact -> "exact"
                RoundingStrategy.round -> "1 px"
                RoundingStrategy.half -> "0.5 px"
                RoundingStrategy.quarter -> "0.25 px"
                RoundingStrategy.tenth -> "0.1 px"
                else -> it.toString()
              }
            }
          }

          declare {
            button("Clear cache") {
              gestalt.tileProvider.clear()
              markAsDirty()
            }
          }
        }
      }
    }
  }
}

