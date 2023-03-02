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

/**
 * A simple hello world demo
 */
class SandboxDemoDescriptor : ChartingDemoDescriptor<Nothing> {
  override val name: String = "Jamal Sandbox"
  override val description: String = "## A simple Hello World demo\n\nShows just a Hello World text above a white background"
  override val category: DemoCategory = DemoCategory.Layers

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
            min = -10_000.0
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
    /*return AbstractChartingDemo {
      meisterCharts {
        configure {
          layers.addClearBackground()
          layers.addTextUnresolved("Hello World", Color.darkorange)
          layers.addLayer(object : AbstractLayer() {
            override val type: LayerType = LayerType.Content

            override fun paint(paintingContext: LayerPaintingContext) {
              val gc = paintingContext.gc
              val chartCalculator = paintingContext.chartCalculator


              gc.fillRect(
                chartCalculator.contentArea2windowX(0.0),
                chartCalculator.contentArea2windowY(0.0),
                chartCalculator.contentArea2zoomedX(300.0),
                chartCalculator.contentArea2zoomedY(200.0),
              )
            }
          })
        }
      }
    }*/
  }
}
