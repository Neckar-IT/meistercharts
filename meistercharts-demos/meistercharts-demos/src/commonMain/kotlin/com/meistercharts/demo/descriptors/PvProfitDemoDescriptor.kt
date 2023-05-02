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
