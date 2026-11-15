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
import com.meistercharts.algorithms.layers.barchart.CategorySeriesModelColorsProvider
import com.meistercharts.charts.BarChartGroupedGestalt
import com.meistercharts.color.Color
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.DemoQuality
import com.meistercharts.demo.MeisterchartsDemo
import com.meistercharts.demo.MeisterchartsDemoDescriptor
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.VariabilityType
import com.meistercharts.model.category.Category
import com.meistercharts.model.category.DefaultCategorySeriesModel
import com.meistercharts.model.category.DefaultSeries
import com.meistercharts.range.ValueRange
import it.neckar.open.i18n.TextKey
import it.neckar.open.kotlin.lang.asProvider

/**
 * A demo descriptor for BioExP, using the BarChartGroupedGestalt. It displays amount of detected cell classes.
 */
class RbcBarChartDemoDescriptor : MeisterchartsDemoDescriptor<(gestalt: BarChartGroupedGestalt) -> Unit> {
  override val uuid: Uuid = Uuid.parse("3f4201ee-d617-45b2-9871-a2a429da7164")
  override val name: String = "RBC Bar Chart"
  override val category: DemoCategory = DemoCategory.Bioexp
  override val quality: DemoQuality = DemoQuality.High
  override val variabilityType: VariabilityType = VariabilityType.Stable

  override fun prepareDemo(configuration: PredefinedConfiguration<(gestalt: BarChartGroupedGestalt) -> Unit>?): MeisterchartsDemo {
    return MeisterchartsDemo(this) {
      val gestalt = BarChartGroupedGestalt()
      configuration?.payload?.invoke(gestalt)

      meistercharts {
        gestalt.configure(this)

        gestalt.configuration.categorySeriesModel = positiveCategoryModel
        gestalt.configuration.valueRange = ValueRange.linear(0.0, 1000.0)

        gestalt.configuration.showGrid = true

        gestalt.groupedBarsPainter.configuration.showValueLabel = true

        gestalt.groupedBarsPainter.configuration.valueLabelColor = Color.black

        gestalt.groupedBarsPainter.configuration.colorsProvider = CategorySeriesModelColorsProvider.onlyCategoryColorsProvider(
          listOf(
            Color("#ff0000").asProvider(), // Red
            Color("#00ff00").asProvider(), // Green
            Color("#0000ff").asProvider(), // Blue
            Color("#FFFF00").asProvider(), // Yellow
            Color("#FF00FF").asProvider(), // Magenta
            Color("#00FFFF").asProvider(), // Cyan
            Color("#000000").asProvider(), // Black
          )
        )
      }
    }

  }

  private val positiveCategoryModel = DefaultCategorySeriesModel(
    listOf(
      Category(TextKey.simple("RBC")),
      Category(TextKey.simple("RBCL")),
      Category(TextKey.simple("RBCc")),
      Category(TextKey.simple("RBCe")),
      Category(TextKey.simple("RBCg")),
      Category(TextKey.simple("RBCr")),
      Category(TextKey.simple("RBCs")),
    ),
    listOf(
      DefaultSeries("Detection Count", listOf(34.0, 470.0, 190.0, 12.0, 17.0, 617.0, 915.0)),
    )
  )


}
