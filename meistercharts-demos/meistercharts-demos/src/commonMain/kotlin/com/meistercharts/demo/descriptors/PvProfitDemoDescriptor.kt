package com.meistercharts.demo.descriptors

import com.meistercharts.demo.ChartingDemo
import com.meistercharts.demo.ChartingDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.configurableInsetsSeparate
import com.meistercharts.demo.configurableMoney
import com.meistercharts.design.initCorporateDesign
import it.neckar.open.provider.SizedProvider
import com.meistercharts.charts.lizergy.solar.LizergyDesign
import com.meistercharts.charts.lizergy.solar.PvProfitGestalt

class PvProfitDemoDescriptor : ChartingDemoDescriptor<Nothing> {
  override val name: String = "Photovoltaics profit"
  override val category: DemoCategory = DemoCategory.Lizergy

  override fun createDemo(configuration: PredefinedConfiguration<Nothing>?): ChartingDemo {
    return ChartingDemo {
      meistercharts {
        val data = PvProfitGestalt.Data()

        val pvProfitGestalt = PvProfitGestalt(data)
        pvProfitGestalt.configure(this)

        configure {
          initCorporateDesign(LizergyDesign)
          configurableInsetsSeparate("Margin", pvProfitGestalt.style.contentAreaMarginProperty) {
            min = 0.0
            max = 300.0
          }

          configurableMoney("Total Investment", data::totalInvestment) {
            max = 50_000.0
          }

          val demoCalculator = object {
            var targetEarning = data.accumulatedCashFlowPerYear.last()
              set(value) {
                field = value

                //recalculate
                data.accumulatedCashFlowPerYear = SizedProvider.of(20) {
                  val basePoint = -data.totalInvestment
                  val delta = value - basePoint
                  return@of basePoint + delta / 20.0 * it
                }
              }
          }

          configurableMoney("Target Earning", demoCalculator::targetEarning) {
            max = 50_000.0
          }
        }
      }
    }
  }

}
