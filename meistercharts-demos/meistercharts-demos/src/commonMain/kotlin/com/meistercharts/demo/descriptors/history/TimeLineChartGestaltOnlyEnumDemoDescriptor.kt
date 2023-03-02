package com.meistercharts.demo.descriptors.history

import com.meistercharts.algorithms.TimeRange
import com.meistercharts.charts.ChartId
import com.meistercharts.charts.timeline.TimeLineChartGestalt
import com.meistercharts.demo.ChartingDemo
import com.meistercharts.demo.ChartingDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.history.HistoryEnum
import com.meistercharts.history.InMemoryHistoryStorage
import com.meistercharts.history.SamplingPeriod
import com.meistercharts.history.generator.EnumValueGenerator
import com.meistercharts.history.generator.HistoryChunkGenerator
import it.neckar.open.time.nowMillis
import it.neckar.open.i18n.TextKey

class TimeLineChartGestaltOnlyEnumDemoDescriptor : ChartingDemoDescriptor<Nothing> {
  override val name: String = "Timeline - only enums"
  override val category: DemoCategory = DemoCategory.Gestalt

  override fun createDemo(configuration: PredefinedConfiguration<Nothing>?): ChartingDemo {
    return ChartingDemo {
      meistercharts {
        val enumDataSeriesCount = 7

        val historyStorage = InMemoryHistoryStorage()

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
          ChartId.next(), TimeLineChartGestalt.Data(
            historyStorage, historyConfiguration
          )
        ) {}
        gestalt.configure(this@meistercharts)
        gestalt.style.showAllEnumSeries()
      }
    }
  }
}

private fun createDemoEnumConfiguration(optionsCount: Int = 3): HistoryEnum {
  return HistoryEnum.create("demo Enum", List(optionsCount) { TextKey.simple("EnumOption $it") })
}
