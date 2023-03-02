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
