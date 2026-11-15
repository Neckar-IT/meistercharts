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
import com.meistercharts.range.ValueRange
import com.meistercharts.algorithms.layers.addClearBackground
import com.meistercharts.animation.Easing
import com.meistercharts.canvas.translateOverTime
import com.meistercharts.charts.timeline.TimeLineChartGestalt
import com.meistercharts.demo.MeisterchartsDemo
import com.meistercharts.demo.MeisterchartsDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.DemoQuality
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.TimeBasedValueGeneratorBuilder
import com.meistercharts.demo.VariabilityType
import com.meistercharts.history.DataSeriesId
import com.meistercharts.history.InMemoryHistoryStorage
import com.meistercharts.history.SamplingPeriod
import com.meistercharts.history.generator.DecimalValueGenerator
import com.meistercharts.history.generator.HistoryChunkGenerator
import com.meistercharts.history.historyConfiguration
import it.neckar.open.observable.ObservableBoolean
import it.neckar.open.provider.MultiProvider
import it.neckar.open.random.Perlin
import it.neckar.open.time.repeat
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlin.time.DurationUnit

/**
 * A simple hello world demo.
 *
 * Can be used as template to create new demos
 */
class TimeLineChartRandomDemoDescriptor : MeisterchartsDemoDescriptor<Nothing> {
  override val uuid: Uuid = Uuid.parse("592731d0-65bf-4646-b334-31aaa50f5345")
  override val name: String = "Random Generators"

  override val category: DemoCategory = DemoCategory.Calculations
  override val quality: DemoQuality = DemoQuality.High
  override val variabilityType: VariabilityType = VariabilityType.Stable

  override fun prepareDemo(configuration: PredefinedConfiguration<Nothing>?): MeisterchartsDemo {
    return MeisterchartsDemo(this) {
      meistercharts {

        configure {
          layers.addClearBackground()
        }

        val historyStorage = InMemoryHistoryStorage()
        val historyConfiguration = historyConfiguration {
          decimalDataSeries(DataSeriesId(0), "Default")
          decimalDataSeries(DataSeriesId(1), "Default - Period: 5secs")
          decimalDataSeries(DataSeriesId(2), "Default - inOutElastic")
          decimalDataSeries(DataSeriesId(3), "Default - sin")
          decimalDataSeries(DataSeriesId(4), "Default - linear")
          decimalDataSeries(DataSeriesId(5), "Default - Perlin")
          decimalDataSeries(DataSeriesId(6), "Default - Perlin Octave")
        }

        val timeLineChartGestalt = TimeLineChartGestalt(historyStorage, historyConfiguration)
        timeLineChartGestalt.configure(this)

        timeLineChartGestalt.configuration.lineValueRanges = MultiProvider.always(ValueRange.percentage)


        val valueGenerators = listOf(
          TimeBasedValueGeneratorBuilder {
            this.valueRange = ValueRange.percentage
          }.build(),

          TimeBasedValueGeneratorBuilder {
            valueRange = ValueRange.percentage
            period = 5.seconds.toDouble(DurationUnit.MILLISECONDS)
          }.build(),

          TimeBasedValueGeneratorBuilder {
            valueRange = ValueRange.percentage
            easing = Easing.inOutElastic
          }.build(),

          TimeBasedValueGeneratorBuilder {
            valueRange = ValueRange.percentage
            easing = Easing.sin
          }.build(),

          TimeBasedValueGeneratorBuilder {
            valueRange = ValueRange.percentage
            easing = Easing.linear
          }.build(),

          DecimalValueGenerator {// Perlin Normal
            Perlin(123894454.0, 0.001, 1.0).noise(it)
          },
          DecimalValueGenerator {// Perlin with octaves
            Perlin(123894454.0, 0.001, 1.0).noiseOctave(it, 7, 0.9)
          }
        )

        val samplingPeriod = SamplingPeriod.EveryHundredMillis
        val historyChunkGenerator = HistoryChunkGenerator(historyStorage, samplingPeriod, valueGenerators, listOf(), listOf(), historyConfiguration = historyConfiguration)

        val liveDataEnabled = ObservableBoolean(true)

        repeat(samplingPeriod.distance.milliseconds) {
          if (liveDataEnabled.value) {
            //The demo generator checks automatically how many and which values have to be added
            historyChunkGenerator.next()?.let {
              historyStorage.storeWithoutCache(it, samplingPeriod)
            }
          }

        }.let {
          onDispose(it)
        }


        configure {
          chartSupport.translateOverTime.animated = true
        }
      }
    }
  }
}
