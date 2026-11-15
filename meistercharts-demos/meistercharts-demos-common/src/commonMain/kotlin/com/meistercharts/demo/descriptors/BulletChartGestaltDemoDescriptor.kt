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
import com.meistercharts.demo.configurableSeparate
import kotlin.uuid.Uuid
import com.meistercharts.range.ValueRange
import com.meistercharts.charts.bullet.BulletChartGestalt
import com.meistercharts.demo.MeisterchartsDemo
import com.meistercharts.demo.MeisterchartsDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.DemoQuality
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.VariabilityType
import com.meistercharts.demo.configurableDouble
import com.meistercharts.demo.configurableInsetsSeparate
import it.neckar.open.provider.DoublesProvider
import it.neckar.open.provider.MultiProvider
import it.neckar.open.provider.MultiProvider1
import it.neckar.open.formatting.decimalFormat

/**
 * A simple hello world demo.
 *
 * Can be used as template to create new demos
 */
class BulletChartGestaltDemoDescriptor : MeisterchartsDemoDescriptor<(gestalt: BulletChartGestalt) -> Unit> {
  override val uuid: Uuid = Uuid.parse("f30729eb-e18b-4d54-ab00-4bc29f930a0a")
  override val name: String = "Bullet Chart"
  override val category: DemoCategory = DemoCategory.Gestalt

  override val quality: DemoQuality = DemoQuality.High
  override val variabilityType: VariabilityType = VariabilityType.Stable

  override val predefinedConfigurations: List<PredefinedConfiguration<(gestalt: BulletChartGestalt) -> Unit>> = listOf(
    PredefinedConfiguration(Uuid.parse("ce83832c-e684-49d6-bb64-01a307ecfed5"), { gestalt ->
      gestalt.configuration.categoryNames = MultiProvider {
        if (it == 2) {
          ""
        } else {
          "Category $it"
        }
      }

    }, "Vertical"),
    PredefinedConfiguration(Uuid.parse("262411aa-58c9-41d0-bb5d-0ff8e65a8a65"), { gestalt ->
      gestalt.configuration.applyAxisTitleOnTop()
      gestalt.valueAxisLayer.configuration.titleProvider = { _, _ -> "MyValue Axis Title" }
      gestalt.configuration.thresholdValues = DoublesProvider.forValues(12.0, 37.0)
      gestalt.configuration.thresholdLabels = MultiProvider1 { index, _ ->
        listOf("Threshold value", decimalFormat.format(gestalt.configuration.thresholdValues.valueAt(index)))
      }
    }, "Vertical - title top"),

    PredefinedConfiguration(Uuid.parse("91fea5da-4554-48ac-b4b2-bdcd46d30460"), { gestalt ->
      gestalt.configuration.currentValues = DoublesProvider.forValues(5.0, Double.NaN, 55.0, Double.NaN, 22.4)
      gestalt.configuration.areaValueRanges = MultiProvider.modulo(
        ValueRange.linear(-15.0, 25.0),
        null,
        null,
        ValueRange.linear(11.0, 55.0),
        null,
      )
      gestalt.configuration.applyAxisTitleOnTop()
      gestalt.valueAxisLayer.configuration.titleProvider = { _, _ -> "MyValue Axis Title" }
      gestalt.configuration.thresholdValues = DoublesProvider.forValues(12.0, Double.NaN)
      gestalt.configuration.thresholdLabels = MultiProvider1 { index, _ ->
        listOf("Threshold value", decimalFormat.format(gestalt.configuration.thresholdValues.valueAt(index)))
      }
    }, "Vertical - invalid values"),

    PredefinedConfiguration(Uuid.parse("9f852e7d-d23d-40ed-972d-f219ed4dc846"), { gestalt ->
      gestalt.configuration.applyHorizontalConfiguration()
      gestalt.configuration.thresholdValues = DoublesProvider.forValues(12.0, 37.0)
      gestalt.configuration.thresholdLabels = MultiProvider1 { index, _ ->
        listOf("Threshold value", decimalFormat.format(gestalt.configuration.thresholdValues.valueAt(index)))
      }
    }, "Horizontal"),

    PredefinedConfiguration(Uuid.parse("3e50c71d-5a05-4139-83c0-5ae945745e33"), { gestalt ->
      gestalt.configuration.applyHorizontalConfiguration()
      gestalt.valueAxisLayer.configuration.titleProvider = { _, _ -> "MyValue Axis Title" }
      gestalt.categoryAxisLayer.configuration.titleProvider = { _, _ -> "My Category Title" }
      gestalt.configuration.thresholdValues = DoublesProvider.forValues(12.0, 37.0)
      gestalt.configuration.thresholdLabels = MultiProvider1 { index, _ ->
        listOf("Threshold value", decimalFormat.format(gestalt.configuration.thresholdValues.valueAt(index)))
      }
      gestalt.configuration.applyAxisTitleOnTop()
    }, "Horizontal - title top"),


    PredefinedConfiguration(Uuid.parse("d141633e-9c1f-42bc-b3f4-6ceb235dc142"), { gestalt ->
      gestalt.configuration.applyValueRange(ValueRange.logarithmic(0.1, 100.0))
    }, "Vertical - Logarithmic"),

    PredefinedConfiguration(Uuid.parse("a3d8f32b-29c7-4a50-850d-e70b3e7a52da"), { gestalt ->
      gestalt.configuration.applyHorizontalConfiguration()
      gestalt.configuration.applyValueRange(ValueRange.logarithmic(0.1, 100.0))
    }, "Horizontal - Logarithmic"),

    PredefinedConfiguration(Uuid.parse("3a88edf2-c0de-4afe-a2cd-a03bef1b8975"), { gestalt ->
      gestalt.configuration.thresholdValues = DoublesProvider.forValues(1_010.0, 5_000.0)
      gestalt.configuration.applyValueRange(ValueRange.logarithmic(1000.0, 10_000.0))
      gestalt.configuration.currentValues = DoublesProvider.forValues(1200.0, 1300.0, 1120.0, 4800.0, 3755.0)
      gestalt.configuration.areaValueRanges = MultiProvider.modulo(
        ValueRange.linear(900.0, 1350.0),
        ValueRange.linear(1100.0, 1370.0),
        ValueRange.linear(1120.0, 1230.0),
        ValueRange.linear(4000.0, 5250.0),
        ValueRange.linear(4000.0, 4500.0),
      )
    }, "Vertical - Logarithmic 1000-10000"),
  )

  override fun prepareDemo(configuration: PredefinedConfiguration<(gestalt: BulletChartGestalt) -> Unit>?): MeisterchartsDemo {
    requireNotNull(configuration) { "BulletChartGestaltDemoDescriptor.prepareDemo requires a predefined configuration" }

    return MeisterchartsDemo(this) {
      meistercharts {
        val gestalt = BulletChartGestalt().also {
          it.configuration.thresholdValues = DoublesProvider.forDoubles(25.0, 50.0, 75.0)
          it.configuration.thresholdLabels = MultiProvider1 { index, _ ->
            val value = it.configuration.thresholdValues.valueAt(index)
            listOf("Value:", decimalFormat.format(value))
          }
        }

        configuration.payload.invoke(gestalt)
        gestalt.configure(this)

        configurable("Min Category Gap", gestalt.configuration::minCategoryGap) {
          max = 200.0
        }
        configurable("Max Category Gap", gestalt.configuration::maxCategoryGap) {
          max = 300.0
        }

        configurable("Current Value Indicator Size", gestalt.bulletChartPainter.configuration::currentValueIndicatorSize) {
          max = 100.0
        }
        configurable("Area Size", gestalt.bulletChartPainter.configuration::barSize) {
          max = 150.0
        }

        configurable("Current Line width", gestalt.bulletChartPainter.configuration::currentValueIndicatorWidth) {
          max = 10.0
        }
        configurable("Current Line outline", gestalt.bulletChartPainter.configuration::currentValueIndicatorOutlineWidth) {
          max = 10.0
        }

        configurableSeparate("Content Viewport Margin", gestalt::contentViewportMargin)
      }
    }
  }
}
