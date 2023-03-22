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

import com.meistercharts.algorithms.LinearValueRange
import com.meistercharts.algorithms.ValueRange
import com.meistercharts.charts.ChartId
import com.meistercharts.charts.refs.DiscreteTimelineChartGestalt
import com.meistercharts.charts.timeline.TimeLineChartGestalt
import com.meistercharts.demo.TimeBasedValueGeneratorBuilder
import com.meistercharts.demo.descriptors.HistoryReferenceScenarios.Units.degreeCelsius
import com.meistercharts.history.DataSeriesId
import com.meistercharts.history.DecimalDataSeriesIndex
import com.meistercharts.history.DecimalDataSeriesIndexProvider
import com.meistercharts.history.EnumDataSeriesIndexProvider
import com.meistercharts.history.HistoryConfiguration
import com.meistercharts.history.HistoryEnum
import com.meistercharts.history.HistoryEnumSet
import com.meistercharts.history.HistoryUnit
import com.meistercharts.history.InMemoryHistoryStorage
import com.meistercharts.history.ReferenceEntryDataSeriesIndexProvider
import com.meistercharts.history.ReferenceEntryId
import com.meistercharts.history.SamplingPeriod
import com.meistercharts.history.generator.DecimalValueGenerator
import com.meistercharts.history.generator.EnumValueGenerator
import com.meistercharts.history.generator.HistoryChunkGenerator
import com.meistercharts.history.generator.ReferenceEntryGenerator
import com.meistercharts.history.historyConfiguration
import it.neckar.open.dispose.Disposable
import it.neckar.open.dispose.DisposeSupport
import it.neckar.open.dispose.OnDispose
import it.neckar.open.observable.ObservableBoolean
import it.neckar.open.provider.MultiProvider
import it.neckar.open.time.repeat
import it.neckar.open.unit.si.degC
import it.neckar.open.unit.si.mm
import it.neckar.open.unit.si.ms
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlin.time.DurationUnit

/**
 * Contains code that creates sample data for a kind of semi-realistic history
 */
object HistoryReferenceScenarios {
  /**
   * A scenario where CI builds are executed on three workers.
   * Each build has a build number + additional information.
   * And a state of the build (that is only known at the end of the build)
   */
  class CiBuilds : Disposable, OnDispose {
    private val disposeSupport = DisposeSupport()

    override fun dispose() {
      disposeSupport.dispose()
    }

    override fun onDispose(disposable: Disposable) {
      disposeSupport.onDispose(disposable)
    }

    override fun onDispose(action: () -> Unit) {
      disposeSupport.onDispose(action)
    }

    val samplingPeriod: SamplingPeriod = SamplingPeriod.EveryHundredMillis

    val historyStorage: InMemoryHistoryStorage = InMemoryHistoryStorage().also { historyStorage ->
      historyStorage.scheduleCleanupService()
      historyStorage.scheduleDownSampling()

      onDispose(historyStorage)
    }

    val historyConfiguration: HistoryConfiguration = historyConfiguration {
      decimalDataSeries(DataSeriesId(11), "Worker 1: CPU Temperature", unit = degreeCelsius)
      decimalDataSeries(DataSeriesId(12), "Worker 2: CPU Temperature", unit = degreeCelsius)
      decimalDataSeries(DataSeriesId(13), "Worker 3: CPU Temperature", unit = degreeCelsius)

      decimalDataSeries(DataSeriesId(21), "Worker 1: CPU %", unit = HistoryUnit.pct)
      decimalDataSeries(DataSeriesId(22), "Worker 2: CPU %", unit = HistoryUnit.pct)
      decimalDataSeries(DataSeriesId(23), "Worker 3: CPU %", unit = HistoryUnit.pct)

      enumDataSeries(DataSeriesId(31), "Worker 1: State", enumConfiguration = workerStateEnum)
      enumDataSeries(DataSeriesId(32), "Worker 2: State", enumConfiguration = workerStateEnum)
      enumDataSeries(DataSeriesId(33), "Worker 3: State", enumConfiguration = workerStateEnum)

      referenceEntryDataSeries(DataSeriesId(41), "Worker 1: Job", jobStateEnum)
      referenceEntryDataSeries(DataSeriesId(42), "Worker 2: Job", jobStateEnum)
      referenceEntryDataSeries(DataSeriesId(43), "Worker 3: Job", jobStateEnum)
    }

    val temperatureGenerators: List<DecimalValueGenerator> = listOf(
      TimeBasedValueGeneratorBuilder {
        valueRange = ValueRanges.temperature
        period = 40.seconds.toDouble(DurationUnit.MILLISECONDS)
        startValue = 80.0
      }.build(),

      TimeBasedValueGeneratorBuilder {
        valueRange = ValueRanges.temperature
        period = 45.seconds.toDouble(DurationUnit.MILLISECONDS)
        startValue = 63.0
      }.build(),

      TimeBasedValueGeneratorBuilder {
        valueRange = ValueRanges.temperature
        period = 50.seconds.toDouble(DurationUnit.MILLISECONDS)
        startValue = 48.0
      }.build(),
    )

    val cpuGenerators: List<DecimalValueGenerator> = listOf(
      DecimalValueGenerator.normality(ValueRange.percentage, sigmaAbsolute = 0.08, center = 0.95),
      DecimalValueGenerator.normality(ValueRange.percentage, sigmaAbsolute = 0.09, center = 0.92),
      DecimalValueGenerator.normality(ValueRange.percentage, sigmaAbsolute = 0.10, center = 0.90),
    )

    val decimalGenerators: List<DecimalValueGenerator> = buildList {
      addAll(temperatureGenerators)
      addAll(cpuGenerators)
    }

    val stateGenerators: List<EnumValueGenerator> = listOf(
      EnumValueGenerator.modulo(step = 55.seconds),
      EnumValueGenerator.modulo(step = 65.seconds),
      EnumValueGenerator.modulo(step = 75.seconds),
    )

    val enumGenerators: List<EnumValueGenerator> = buildList {
      addAll(stateGenerators)
    }

    val jobGenerators: List<ReferenceEntryGenerator> = listOf(
      ReferenceEntryGenerator.increasing(step = 65.seconds, factor = 1),
      ReferenceEntryGenerator.increasing(step = 68.seconds, factor = 2),
      ReferenceEntryGenerator.increasing(step = 130.seconds, factor = 3),
    )

    val referenceEntryGenerators: List<ReferenceEntryGenerator> = buildList {
      addAll(jobGenerators)
    }

    val historyChunkGenerator: HistoryChunkGenerator = HistoryChunkGenerator(
      historyStorage = historyStorage,
      samplingPeriod = samplingPeriod,
      decimalValueGenerators = decimalGenerators,
      enumValueGenerators = enumGenerators,
      referenceEntryGenerators = referenceEntryGenerators,

      referenceEntryStatusProvider = { referenceEntryId: ReferenceEntryId, millis: @ms Double ->
        HistoryEnumSet.NoValue //TODO!!!
      },
      historyConfiguration = historyConfiguration
    ).also { historyChunkGenerator ->
      repeat(samplingPeriod.distance.milliseconds) {
        //The demo generator checks automatically how many and which values have to be added
        historyChunkGenerator.next()?.let {
          historyStorage.storeWithoutCache(it, samplingPeriod)
        }
      }.also {
        onDispose(it)
      }
    }

    val liveDataEnabled: ObservableBoolean = ObservableBoolean(true)

    fun createDiscreteTimelineChartGestalt(): DiscreteTimelineChartGestalt {
      return DiscreteTimelineChartGestalt(historyStorage, { historyConfiguration }) {
        this.minimumSamplingPeriod = samplingPeriod
        this.requestVisibleReferenceEntrySeriesIndices = ReferenceEntryDataSeriesIndexProvider.indices { 10 }
      }
    }

    fun createTimelineChartGestalt(chartId: ChartId): TimeLineChartGestalt {
      val data = TimeLineChartGestalt.Data(historyStorage = historyStorage, historyConfiguration).apply {
        minimumSamplingPeriod = samplingPeriod
      }

      return TimeLineChartGestalt(chartId, data).also {
        onDispose(it)

        it.style.apply {
          requestedVisibleDecimalSeriesIndices = DecimalDataSeriesIndexProvider.indices { 10 }
          requestedVisibleValueAxesIndices = requestedVisibleDecimalSeriesIndices
          requestVisibleEnumSeriesIndices = EnumDataSeriesIndexProvider.indices { 10 }

          lineValueRanges = MultiProvider.forListOr<DecimalDataSeriesIndex, ValueRange>(
            listOf(
              ValueRanges.temperature,
              ValueRanges.temperature,
              ValueRanges.temperature,

              ValueRange.percentage,
              ValueRange.percentage,
              ValueRange.percentage,
            ),
            ValueRange.default
          )
        }
      }
    }
  }

  object Units {
    val degreeCelsius: @mm HistoryUnit = HistoryUnit("°C")
  }

  object ValueRanges {
    val temperature: @degC LinearValueRange = ValueRange.linear(0.0, 120.0)
  }


  /**
   * The state for a worker itself
   */
  val workerStateEnum: HistoryEnum = HistoryEnum.createSimple("Worker State", listOf("Offline", "Idle", "Running"))

  /**
   * The state for a job
   */
  val jobStateEnum: HistoryEnum = HistoryEnum.createSimple("Job State", listOf("Failure", "Success", "Running"))

  val temperatureValueRange: @degC LinearValueRange = ValueRange.linear(10.0, 120.0)
}
