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

import com.meistercharts.algorithms.layers.DefaultCategoryLayouter
import com.meistercharts.demo.ChartingDemo
import com.meistercharts.demo.ChartingDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.configurableDouble
import com.meistercharts.demo.configurableMoney
import com.meistercharts.demo.section
import com.meistercharts.design.initCorporateDesign
import com.meistercharts.charts.lizergy.solar.LizergyDesign
import com.meistercharts.charts.lizergy.solar.PvExpensesPerMonthGestalt

class PvExpensesPerMonthDemoDescriptor : ChartingDemoDescriptor<Nothing> {
  override val name: String = "Photovoltaics expenses per month"
  override val category: DemoCategory = DemoCategory.Lizergy

  override fun createDemo(configuration: PredefinedConfiguration<Nothing>?): ChartingDemo {
    return ChartingDemo {
      meistercharts {
        val pvExpensesPerMonthGestalt = PvExpensesPerMonthGestalt()
        pvExpensesPerMonthGestalt.configure(this)

        configure {
          initCorporateDesign(LizergyDesign)

          section("sizes")
          configurableDouble("Min category size", (pvExpensesPerMonthGestalt.barChartStackedGestalt.categoryLayer.style.layoutCalculator as DefaultCategoryLayouter).style::minCategorySize) {
            min = 0.0
            max = 300.0
          }
          configurableDouble(
            "Max category size",
            (pvExpensesPerMonthGestalt.barChartStackedGestalt.categoryLayer.style.layoutCalculator as DefaultCategoryLayouter).style.maxCategorySize ?: 300.0
          ) {
            min = 0.0
            max = 300.0
            onChange {
              (pvExpensesPerMonthGestalt.barChartStackedGestalt.categoryLayer.style.layoutCalculator as DefaultCategoryLayouter).style.maxCategorySize = it
              markAsDirty()
            }
          }
          configurableDouble("Max bar size", pvExpensesPerMonthGestalt.barChartStackedGestalt.stackedBarsPainter.style::maxBarSize) {
            min = 0.0
            max = 50.0
          }

          section("Values are on a per month basis in EUR cents")
          configurableMoney("Expenses without PV", pvExpensesPerMonthGestalt.data::expensesPerMonthWithoutPv) {
            min = 0.0
            max = 1000.0
          }
          configurableMoney("Expenses with PV", pvExpensesPerMonthGestalt.data::expensesPerMonthWithPv) {
            min = 0.0
            max = 1000.0
          }
          configurableMoney("Photovoltaic costs", pvExpensesPerMonthGestalt.data::photovoltaicCostsPerMonth) {
            min = -1000.0
            max = 1000.0
          }
        }
      }
    }
  }

}
