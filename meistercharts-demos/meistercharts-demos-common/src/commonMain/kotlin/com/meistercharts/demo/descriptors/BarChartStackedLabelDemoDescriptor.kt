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

import com.meistercharts.demo.configurable
import kotlin.uuid.Uuid
import com.meistercharts.range.ValueRange
import com.meistercharts.model.category.Category
import com.meistercharts.model.category.DefaultCategorySeriesModel
import com.meistercharts.model.category.DefaultSeries
import com.meistercharts.charts.BarChartStackedGestalt
import com.meistercharts.demo.MeisterchartsDemo
import com.meistercharts.demo.MeisterchartsDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.DemoQuality
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.VariabilityType
import com.meistercharts.demo.configurableDouble
import com.meistercharts.demo.configurableEnum
import com.meistercharts.demo.configurableFont
import it.neckar.geometry.Direction
import it.neckar.open.i18n.TextKey

class BarChartStackedLabelDemoDescriptor : MeisterchartsDemoDescriptor<Nothing> {
  override val uuid: Uuid = Uuid.parse("32e9fc1c-b61f-4b26-ae98-a6b0f2bd406b")
  override val name: String = "Bar Chart Stacked Labels"
  override val quality: DemoQuality = DemoQuality.High
  override val variabilityType: VariabilityType = VariabilityType.Stable

  //language=HTML
  override val description: String = "A stacked bar chart with labels"
  override val category: DemoCategory = DemoCategory.Calculations

  override fun prepareDemo(configuration: PredefinedConfiguration<Nothing>?): MeisterchartsDemo {

    return MeisterchartsDemo(this) {

      val gestalt = BarChartStackedGestalt(
        initialCategorySeriesModel = DefaultCategorySeriesModel(
          listOf(
            Category(TextKey.simple("A")),
            Category(TextKey.simple("B")),
            Category(TextKey.simple("C")),
            Category(TextKey.simple("D")),
            Category(TextKey.simple("E")),
            Category(TextKey.simple("F")),
            Category(TextKey.simple("G"))
          ),
          listOf(
            DefaultSeries("1", listOf(12.25, 16.0, 23.5, 01.0, 01.0, 01.0, 23.5)),
            DefaultSeries("2", listOf(12.25, 01.0, 01.0, 16.0, 01.0, 01.0, 23.5)),
            DefaultSeries("3", listOf(01.00, 01.0, 01.0, 16.0, 46.0, 01.0, 01.0)),
            DefaultSeries("4", listOf(12.25, 16.0, 01.0, 16.0, 01.0, 23.5, 01.0)),
            DefaultSeries("5", listOf(12.25, 16.0, 23.5, 01.0, 01.0, 23.5, 01.0))
          )
        ).apply {
        }).apply {
        stackedBarsPainter.stackedBarPaintable.configuration.valueRange = ValueRange.linear(0.0, 50.0)
        stackedBarsPainter.stackedBarPaintable.configuration.showRemainderAsSegment = false
      }

      meistercharts {
        gestalt.configure(this)

        configure {
          configurable("Segments gap", gestalt.stackedBarsPainter.stackedBarPaintable.style::segmentsGap) {
            max = 100.0
          }

          configurable("Label anchor direction", gestalt.stackedBarsPainter.stackedBarPaintable.style::valueLabelAnchorDirection, kotlin.enums.enumEntries<Direction>().toList())

          configurable("Label gap Horizontal", gestalt.stackedBarsPainter.stackedBarPaintable.style::valueLabelGapHorizontal) {
            min = 0.0
            max = 100.0
          }
          configurable("Label gap Vertical", gestalt.stackedBarsPainter.stackedBarPaintable.style::valueLabelGapVertical) {
            min = 0.0
            max = 100.0
          }

          configurableFont("Value label font", gestalt.stackedBarsPainter.stackedBarPaintable.configuration.valueLabelFont) {
            onChange {
              gestalt.configuration.applyValueLabelFont(it)
              markAsDirty()
            }
          }

        }
      }
    }
  }
}
