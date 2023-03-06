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

import com.meistercharts.algorithms.painter.stripe.enums.EnumAggregationMode
import com.meistercharts.algorithms.painter.stripe.enums.RectangleEnumStripePainter
import com.meistercharts.algorithms.layers.barchart.DefaultCategoryAxisLabelPainter
import com.meistercharts.algorithms.painter.Color
import com.meistercharts.canvas.SnapConfiguration
import com.meistercharts.canvas.translateOverTime
import com.meistercharts.charts.timeline.TimeLineChartGestalt
import com.meistercharts.demo.ChartingDemo
import com.meistercharts.demo.ChartingDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.PredefinedConfiguration
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
import it.neckar.open.collections.getModulo
import it.neckar.open.kotlin.lang.fastFor
import it.neckar.open.kotlin.lang.getModulo
import it.neckar.open.provider.MultiProvider

class TimeLineChartGestaltEnumStylesDemoDescriptor : ChartingDemoDescriptor<TimeLineChartGestalt.() -> Unit> {
  override val name: String = "Time Line Chart - Enum Style"
  override val category: DemoCategory = DemoCategory.Gestalt

  override val predefinedConfigurations: List<PredefinedConfiguration<TimeLineChartGestalt.() -> Unit>> = listOf(
    oneSampleEvery100ms
  )

  override fun createDemo(configuration: PredefinedConfiguration<TimeLineChartGestalt.() -> Unit>?): ChartingDemo {
    require(configuration != null) { "config required" }
    val gestaltConfig: TimeLineChartGestalt.() -> Unit = configuration.payload

    // The color schemes for an enum
    val colorSchemes = listOf<List<Color>>(
      Theme.enumColors().toList(),
      listOf(Color.silver, Color.gray, Color.darkgray),
      listOf(Color.orange, Color.beige, Color.bisque),
      listOf(Color.red, Color.blue, Color.orange, Color.lightsalmon),
    )

    return ChartingDemo {
      meistercharts {
        val historyStorage = InMemoryHistoryStorage().also {
          it.maxSizeConfiguration = MaxHistorySizeConfiguration(7)
          onDispose(it)
        }

        historyStorage.scheduleDownSampling()
        historyStorage.scheduleCleanupService()


        val gestalt = TimeLineChartGestalt(chartId, TimeLineChartGestalt.Data(historyStorage))

        val historyChunkBuilder = MyHistoryChunkBuilder {
          gestalt.data.historyConfiguration
        }

        gestalt.configure(this)
        gestalt.gestaltConfig()

        val enumDataSeriesCount = gestalt.data.historyConfiguration.enumDataSeriesCount
        require(enumDataSeriesCount > 0)

        val props = object {
          var snapConfiguration = SnapConfiguration.None
        }

        val enumBarPainters = List(enumDataSeriesCount) { index ->
          val colors = colorSchemes.getModulo(index)

          RectangleEnumStripePainter {
            aggregationMode = EnumAggregationMode.values().getModulo(index)
            fillProvider = { value: HistoryEnumOrdinal, _: HistoryEnum ->
              colors.getModulo(value.value)
            }
            this.snapConfiguration = { props.snapConfiguration }
          }
        }

        gestalt.historyEnumLayer.configuration.enumStripePainter = MultiProvider.forListModulo(enumBarPainters)

        configure {
          configurableBoolean("Play Mode", chartSupport.translateOverTime::animated) {
            value = true
          }

          declare {
            button("Show all enums") {
              gestalt.style.requestVisibleEnumSeriesIndices = EnumDataSeriesIndexProvider.indices {
                gestalt.data.historyConfiguration.enumDataSeriesCount
              }
            }
            button("Hide all enums") {
              gestalt.style.requestVisibleEnumSeriesIndices = EnumDataSeriesIndexProvider.empty()
            }
          }

          configurableIndices(
            this@ChartingDemo,
            "Visible Enum lines",
            "enum visible",
            initial = gestalt.style.requestVisibleEnumSeriesIndices.toList().map { it.value },
            maxSize = enumDataSeriesCount,
          ) {
            gestalt.style.requestVisibleEnumSeriesIndices = EnumDataSeriesIndexProvider.forList(it.map { EnumDataSeriesIndex(it) })
          }

          enumDataSeriesCount.fastFor { index ->

            section("Enum $index")

            configurableList("Color Scheme", colorSchemes.getModulo(index), colorSchemes) {
              onChange {
                enumBarPainters[index].configuration.fillProvider = { value, historyEnum ->
                  it.getModulo(value.value)
                }
                markAsDirty()
              }
            }

            configurableEnum("Aggregation Mode", EnumAggregationMode.values().getModulo(index)) {
              onChange {
                enumBarPainters[index].configuration.aggregationMode = it
                markAsDirty()
              }
            }
          }

          section("Enum")
          configurableDouble("Enum Height", gestalt.historyEnumLayer.configuration::stripeHeight) {
            max = 50.0
          }
          configurableDouble("Bar distance", gestalt.historyEnumLayer.configuration::stripesDistance) {
            max = 30.0
          }

          section("Enum-Axis")
          configurableDouble("Two Lines Gap", (gestalt.enumCategoryAxisLayer.style.axisLabelPainter as DefaultCategoryAxisLabelPainter).style::twoLinesGap) {
            max = 5.0
            min = -5.0
          }
          configurableEnum("Wrap Mode", (gestalt.enumCategoryAxisLayer.style.axisLabelPainter as DefaultCategoryAxisLabelPainter).style::wrapMode) {
          }

          configurableEnum("Snap Configuration", props::snapConfiguration)
        }
      }
    }
  }
}
