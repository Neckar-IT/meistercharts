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
import com.meistercharts.algorithms.layers.linechart.Dashes
import com.meistercharts.algorithms.layers.linechart.LineStyle
import com.meistercharts.charts.CategoryLineChartGestalt
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.DemoQuality
import com.meistercharts.demo.MeisterchartsDemo
import com.meistercharts.demo.MeisterchartsDemoDescriptor
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.VariabilityType
import com.meistercharts.demo.configurableColorPicker
import com.meistercharts.demo.configurableColorPickerProvider
import com.meistercharts.demo.configurableDouble
import com.meistercharts.demo.configurableList
import com.meistercharts.model.category.CategoryIndex
import com.meistercharts.painter.CategoryLinePainter
import com.meistercharts.painter.CircleCategoryPointPainter
import com.meistercharts.painter.Cross45DegreesCategoryPointPainter
import com.meistercharts.painter.CrossCategoryPointPainter
import com.meistercharts.painter.DotCategoryPointPainter
import com.meistercharts.painter.XyCategoryLinePainter
import com.meistercharts.painter.emptyCategoryLinePainter
import com.meistercharts.painter.emptyCategoryPointPainter
import com.meistercharts.style.BoxStyle
import it.neckar.open.kotlin.lang.asProvider
import it.neckar.open.provider.MultiProvider

/**
 * Demonstrates category lines and points
 */
class CategoryLineChartGestaltWithLinesAndPointsDemoDescriptor : MeisterchartsDemoDescriptor<Nothing> {
  override val uuid: Uuid = Uuid.parse("e3727ad6-32dd-4c5a-9b07-1b0e0f6d3c86")
  override val name: String = "Category Line Chart: lines and points"
  override val category: DemoCategory = DemoCategory.Gestalt

  // tooltip buggy behind the knot at the beginning - has probably be fixed
  // Improve colors / styles
  override val quality: DemoQuality = DemoQuality.Low
  override val variabilityType: VariabilityType = VariabilityType.Stable

  override fun prepareDemo(configuration: PredefinedConfiguration<Nothing>?): MeisterchartsDemo {

    return MeisterchartsDemo(this) {
      val gestalt = CategoryLineChartGestalt()

      meistercharts {
        gestalt.configure(this)

        val xyLinePainter = XyCategoryLinePainter(snapXValues = false, snapYValues = false)
        var lineStyle = LineStyle()
        var boxStyle = BoxStyle.gray
        var linePainter: CategoryLinePainter = xyLinePainter

        gestalt.categoryLinesLayer.configuration.linePainters = MultiProvider { linePainter }

        gestalt.categoryLinesLayer.configuration.lineStyles = MultiProvider { lineStyle }
        gestalt.crossWireLabelsLayer.configuration.valueLabelBoxStyle = MultiProvider { boxStyle }

        configure {

          configurableList(
            "Cross wire category", CategoryIndex(0), listOf(
              CategoryIndex(0),
              CategoryIndex(1),
              CategoryIndex(2),
              CategoryIndex(3),
              null,
            )
          ) {

            converter {
              it?.value?.toString() ?: "-"
            }

            onChange {
              gestalt.categoryLinesLayer.configuration.activeCategoryIndex = it
              markAsDirty()
            }
          }

          configurableList("Line painter", "XY", listOf("XY", "empty")) {
            onChange {
              linePainter = if (it == "empty") {
                emptyCategoryLinePainter
              } else {
                xyLinePainter
              }
              markAsDirty()
            }
          }

          configurableColorPicker("Line color", lineStyle.color()) {
            onChange {
              lineStyle = lineStyle.copy(color = it.asProvider())
              boxStyle = boxStyle.copy(fill = it.asProvider())
              markAsDirty()
            }
          }

          configurableDouble("Line width", lineStyle.lineWidth) {
            min = 0.0
            max = 20.0
            onChange {
              lineStyle = lineStyle.copy(lineWidth = it)
              markAsDirty()
            }
          }

          configurableList("Dashes", lineStyle.dashes, Dashes.predefined) {
            onChange {
              lineStyle = lineStyle.copy(dashes = it)
              markAsDirty()
            }
          }

          val dotCategoryPointPainter = DotCategoryPointPainter(snapXValues = false, snapYValues = false)
          val crossCategoryPointPainter = CrossCategoryPointPainter(snapXValues = false, snapYValues = false)
          val cross45CategoryPointPainter = Cross45DegreesCategoryPointPainter(snapXValues = false, snapYValues = false)
          val circleCategoryPointPainter = CircleCategoryPointPainter(snapXValues = false, snapYValues = false)

          val categoryPointPainters = listOf(emptyCategoryPointPainter, dotCategoryPointPainter, circleCategoryPointPainter, crossCategoryPointPainter, cross45CategoryPointPainter)

          configurableList("Point painter", dotCategoryPointPainter, categoryPointPainters) {
            converter {
              when (it) {
                is DotCategoryPointPainter -> "Dot"
                is CircleCategoryPointPainter -> "Circle"
                is CrossCategoryPointPainter -> "Cross"
                is Cross45DegreesCategoryPointPainter -> "Cross45"
                else -> "None"
              }
            }

            onChange {
              gestalt.categoryLinesLayer.configuration.pointPainters = MultiProvider.always(it)
              markAsDirty()
            }
          }

          configurableDouble("Point size", circleCategoryPointPainter.circlePointPainter.pointSize) {
            min = 1.0
            max = 50.0
            onChange {
              circleCategoryPointPainter.circlePointPainter.pointSize = it
              dotCategoryPointPainter.pointStylePainter.pointSize = it
              crossCategoryPointPainter.pointStylePainter.pointSize = it
              cross45CategoryPointPainter.pointStylePainter.pointSize = it
              markAsDirty()
            }
          }

          configurableDouble("Point line width", circleCategoryPointPainter.circlePointPainter.lineWidth) {
            min = 0.1
            max = 10.0
            onChange {
              circleCategoryPointPainter.circlePointPainter.lineWidth = it
              dotCategoryPointPainter.pointStylePainter.lineWidth = it
              crossCategoryPointPainter.pointStylePainter.lineWidth = it
              cross45CategoryPointPainter.pointStylePainter.lineWidth = it
              markAsDirty()
            }
          }

          configurable("Point fill", circleCategoryPointPainter.circlePointPainter::fill) {
            onChange {
              circleCategoryPointPainter.circlePointPainter.fill = it.asProvider()
              dotCategoryPointPainter.pointStylePainter.color = it.asProvider()
              crossCategoryPointPainter.pointStylePainter.color = it.asProvider()
              cross45CategoryPointPainter.pointStylePainter.color = it.asProvider()
              markAsDirty()
            }
          }

          configurable("Point stroke", circleCategoryPointPainter.circlePointPainter::stroke) {
          }

        }
      }
    }
  }
}
