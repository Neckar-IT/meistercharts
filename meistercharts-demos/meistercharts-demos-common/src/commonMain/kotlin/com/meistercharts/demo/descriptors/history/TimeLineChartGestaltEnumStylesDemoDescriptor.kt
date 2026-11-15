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
import com.meistercharts.algorithms.layers.barchart.DefaultCategoryAxisLabelPainter
import com.meistercharts.algorithms.painter.stripe.enums.EnumAggregationMode
import com.meistercharts.algorithms.painter.stripe.enums.RectangleEnumStripePainter
import com.meistercharts.canvas.SnapConfiguration
import com.meistercharts.canvas.translateOverTime
import com.meistercharts.charts.timeline.TimeLineChartGestalt
import com.meistercharts.color.Color
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.DemoQuality
import com.meistercharts.demo.MeisterchartsDemo
import com.meistercharts.demo.MeisterchartsDemoDescriptor
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.VariabilityType
import com.meistercharts.demo.configurableBoolean
import com.meistercharts.demo.configurableDouble
import com.meistercharts.demo.configurableEnum
import com.meistercharts.demo.configurableIndices
import com.meistercharts.demo.configurableList
import com.meistercharts.demo.descriptors.history.TimeLineChartGestaltDemoDescriptor.Companion.oneSampleEvery100ms
import com.meistercharts.demo.section
import com.meistercharts.demo.toList
import com.meistercharts.design.Theme
import com.meistercharts.history.EnumDataSeriesIndex
import com.meistercharts.history.EnumDataSeriesIndexProvider
import com.meistercharts.history.HistoryEnum
import com.meistercharts.history.HistoryEnumOrdinal
import com.meistercharts.history.InMemoryHistoryStorage
import com.meistercharts.history.cleanup.MaxHistorySizeConfiguration
import it.neckar.open.kotlin.lang.asProvider
import it.neckar.open.kotlin.lang.fastFor
import it.neckar.open.kotlin.lang.getModulo
import it.neckar.open.provider.MultiProvider

class TimeLineChartGestaltEnumStylesDemoDescriptor : MeisterchartsDemoDescriptor<TimelineChartGestaltDemoConfiguration> {
  override val uuid: Uuid = Uuid.parse("de77a72c-9f64-433d-904e-18b959891f73")
  override val name: String = "Time Line Chart - Enum Style"
  override val category: DemoCategory = DemoCategory.Gestalt
  override val quality: DemoQuality = DemoQuality.High
  override val variabilityType: VariabilityType = VariabilityType.Stable

  override val predefinedConfigurations: List<PredefinedConfiguration<TimelineChartGestaltDemoConfiguration>> = listOf(
    oneSampleEvery100ms.copy(uuid = Uuid.parse("3076463a-0ea7-4e81-9d95-d61daf9ad791")),
  )

  override fun prepareDemo(configuration: PredefinedConfiguration<TimelineChartGestaltDemoConfiguration>?): MeisterchartsDemo {
    require(configuration != null) { "config required" }
    val gestaltConfig: TimelineChartGestaltDemoConfiguration = configuration.payload

    // The color schemes for an enum
    val colorSchemes = listOf(
      Theme.enumColors.resolve().toList().map { it.asProvider() }, //TODO improve me!
      listOf(Color.silver, Color.gray, Color.darkgray),
      listOf(Color.orange, Color.beige, Color.bisque),
      listOf(Color.red, Color.blue, Color.orange, Color.lightsalmon),
    )

    return MeisterchartsDemo(this) {
      meistercharts {
        val historyStorage = InMemoryHistoryStorage().also {
          it.maxSizeConfiguration = MaxHistorySizeConfiguration(7)
          onDispose(it)
        }

        historyStorage.scheduleDownSampling()
        historyStorage.scheduleCleanupService()


        val gestalt = TimeLineChartGestalt(initialHistoryStorage = historyStorage)

        val historyChunkBuilder = MyHistoryChunkBuilder {
          gestalt.configuration.historyConfiguration
        }

        gestalt.configure(this)
        gestalt.gestaltConfig(historyStorage)

        val enumDataSeriesCount = gestalt.configuration.historyConfiguration.enumDataSeriesCount
        require(enumDataSeriesCount.value > 0) { "TimeLineChartGestaltEnumStylesDemo requires at least one enum data series, but historyConfiguration has [$enumDataSeriesCount]" }

        val props = object {
          var snapConfiguration = SnapConfiguration.None
        }

        val enumBarPainters = List(enumDataSeriesCount.value) { index ->
          val colors = colorSchemes.getModulo(index)

          RectangleEnumStripePainter {
            aggregationMode = EnumAggregationMode.entries.getModulo(index)
            fillProvider = { value: HistoryEnumOrdinal, _: HistoryEnum ->
              colors.getModulo(value.value)()
            }
            this.snapConfiguration = { props.snapConfiguration }
          }
        }

        gestalt.historyEnumLayer.configuration.stripePainters = MultiProvider.forListModulo(enumBarPainters)

        configure {
          configurable("Play Mode", chartSupport.translateOverTime::animated) {
            value = true
          }

          declare {
            button("Show all enums") {
              gestalt.configuration.requestVisibleEnumSeriesIndices = EnumDataSeriesIndexProvider.indices {
                gestalt.configuration.historyConfiguration.enumDataSeriesCount.value
              }
            }
            button("Hide all enums") {
              gestalt.configuration.requestVisibleEnumSeriesIndices = EnumDataSeriesIndexProvider.empty()
            }
          }

          configurableIndices(
            this@MeisterchartsDemo,
            "Visible Enum lines",
            "enum visible",
            initial = gestalt.configuration.requestVisibleEnumSeriesIndices.toList().map { it.value },
            maxSize = enumDataSeriesCount.value,
          ) {
            gestalt.configuration.requestVisibleEnumSeriesIndices = EnumDataSeriesIndexProvider.forList(it.map { EnumDataSeriesIndex(it) })
          }

          enumDataSeriesCount.value.fastFor { index ->

            section("Enum $index")

            configurableList("Color Scheme", colorSchemes.getModulo(index), colorSchemes) {
              converter {
                it.joinToString { it().web }
              }
              onChange {
                enumBarPainters[index].configuration.fillProvider = { value, historyEnum ->
                  it.getModulo(value.value)()
                }
                markAsDirty()
              }
            }

            configurableEnum("Aggregation Mode", EnumAggregationMode.entries.getModulo(index)) {
              onChange {
                enumBarPainters[index].configuration.aggregationMode = it
                markAsDirty()
              }
            }
          }

          section("Enum")
          configurable("Enum Height", gestalt.historyEnumLayer.configuration::stripeHeight) {
            max = 50.0
          }
          configurable("Bar distance", gestalt.historyEnumLayer.configuration::stripesDistance) {
            max = 30.0
          }

          section("Enum-Axis")
          configurable("Two Lines Gap", (gestalt.enumCategoryAxisLayer.configuration.axisLabelPainter as DefaultCategoryAxisLabelPainter).style::twoLinesGap) {
            max = 5.0
            min = -5.0
          }
          configurable("Wrap Mode", (gestalt.enumCategoryAxisLayer.configuration.axisLabelPainter as DefaultCategoryAxisLabelPainter).style::wrapMode) {
          }

          configurable("Snap Configuration", props::snapConfiguration)
        }
      }
    }
  }
}
