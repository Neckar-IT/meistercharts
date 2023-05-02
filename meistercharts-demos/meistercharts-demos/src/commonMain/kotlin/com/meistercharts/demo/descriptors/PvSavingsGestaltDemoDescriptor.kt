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

import com.meistercharts.charts.lizergy.solar.PvSavingsGestalt
import com.meistercharts.demo.ChartingDemo
import com.meistercharts.demo.ChartingDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.configurableDouble
import com.meistercharts.demo.section
import it.neckar.financial.currency.euro

/**
 *
 */
class PvSavingsGestaltDemoDescriptor : ChartingDemoDescriptor<Nothing> {
  override val name: String = "PV Savings"
  override val category: DemoCategory = DemoCategory.Lizergy

  override fun createDemo(configuration: PredefinedConfiguration<Nothing>?): ChartingDemo {
    return ChartingDemo {
      meistercharts {

        val values = object {
          var feedInCompensation = 150.0
          var savingsOwnConsumption = 120.0
          var saved = 130.12
        }

        val pvSavingsGestalt = PvSavingsGestalt(
          PvSavingsGestalt.Data(
            feedInCompensation = { values.feedInCompensation.euro },
            savingsOwnConsumption = { values.savingsOwnConsumption.euro },
            saved = { values.saved.euro }
          )
        )
        pvSavingsGestalt.configure(this)


        configurableDouble("Feed in compensation", values::feedInCompensation) {
          max = 1000.0
        }
        configurableDouble("Savings on consumption", values::savingsOwnConsumption) {
          max = 1000.0
        }
        configurableDouble(values::saved) {
          max = 1000.0
        }

        section("Style")

        configurableDouble(pvSavingsGestalt.pvSavingsLayer.style::barWidth) {
          max = 100.0
        }
        configurableDouble(pvSavingsGestalt.pvSavingsLayer.style::gapBetweenBars) {
          max = 200.0
        }
        configurableDouble(pvSavingsGestalt.pvSavingsLayer.style::gapBetweenSegments) {
          max = 200.0
        }

        configurableDouble(pvSavingsGestalt.pvSavingsLayer.style::savingsLineWidth) {
          max = 10.0
        }
        configurableDouble(pvSavingsGestalt.pvSavingsLayer.style::savingsEndMarkerWidth) {
          max = 30.0
        }
      }
    }
  }
}
