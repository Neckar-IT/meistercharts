package com.meistercharts.demo.descriptors

import com.meistercharts.charts.sick.beams.lisa.LisaChartGestalt
import com.meistercharts.demo.ChartingDemo
import com.meistercharts.demo.ChartingDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.configurableEnum
import com.meistercharts.design.SegoeUiDesign
import com.meistercharts.design.initCorporateDesign
import it.neckar.open.provider.DoublesProvider
import it.neckar.open.kotlin.lang.random

/**
 * Lisa Chart for SLG2
 */
class LisaChartDemoDescriptor : ChartingDemoDescriptor<Nothing> {
  override val name: String = "Lisa Chart"
  override val category: DemoCategory = DemoCategory.Automation

  override fun createDemo(configuration: PredefinedConfiguration<Nothing>?): ChartingDemo {
    return ChartingDemo {
      meistercharts {
        val data = LisaChartGestalt.Data()

        val gestalt = LisaChartGestalt(data)
        gestalt.configure(this)

        configure {
          initCorporateDesign(SegoeUiDesign)

          configurableEnum("Mode", gestalt.style.visibleCurvesModeProperty)

          declare {
            button("240") {
              fillWithRandomData(data, 240)
              markAsDirty()
            }

            button("256") {
              fillWithRandomData(data, 256)
              markAsDirty()
            }

            button("10") {
              fillWithRandomData(data, 10)
              markAsDirty()
            }

            button("40") {
              fillWithRandomData(data, 40)
              markAsDirty()
            }
          }
        }
      }
    }
  }

  private fun fillWithRandomData(data: LisaChartGestalt.Data, count: Int) {
    data.signalLevelValues = DoublesProvider.forList(List(count) {
      random.nextDouble(1000.0)
    })
    data.gainValues = DoublesProvider.forList(List(count) {
      random.nextDouble(20.0)
    })
    data.thresholdOnValues = DoublesProvider.forList(List(count) {
      random.nextDouble(650.0, 850.0)
    })
    data.thresholdOffValues = DoublesProvider.forList(List(count) {
      random.nextDouble(100.0, 300.0)
    })
    data.thresholdMonitorValues = DoublesProvider.forList(List(count) {
      random.nextDouble(350.0, 500.0)
    })
    data.teachInSignalLevelValues = DoublesProvider.forList(List(count) {
      random.nextDouble(100.0, 200.0)
    })
  }

}
