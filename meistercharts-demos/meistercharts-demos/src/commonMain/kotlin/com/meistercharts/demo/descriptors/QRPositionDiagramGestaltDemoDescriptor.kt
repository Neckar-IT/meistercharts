package com.meistercharts.demo.descriptors

import com.meistercharts.algorithms.ValueRange
import com.meistercharts.algorithms.painter.Color
import com.meistercharts.charts.QRPositionDiagramGestalt
import com.meistercharts.demo.ChartingDemo
import com.meistercharts.demo.ChartingDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.configurableDouble
import com.meistercharts.model.Size
import com.meistercharts.resources.Icons
import kotlin.math.PI

class QRPositionDiagramGestaltDemoDescriptor : ChartingDemoDescriptor<Nothing> {

  override val name: String = "QR Code Position Diagram"
  override val description: String = "QR Code Position Diagram"
  override val category: DemoCategory = DemoCategory.Automation

  override fun createDemo(configuration: PredefinedConfiguration<Nothing>?): ChartingDemo {

    return ChartingDemo {
      val image = Icons.neckarItQr(Size.PX_120, Color.black)

      val data = QRPositionDiagramGestalt.Data().apply {
        x = 35.0
        y = 40.0

        valueRangeX = ValueRange.linear(0.0, 70.0)
        valueRangeY = ValueRange.linear(0.0, 90.0)

        xAxisCaption = "X/mm"
        yAxisCaption = "Y/mm"

        angle = -0.2
      }

      val qrCode = QRPositionDiagramGestalt(data) {
        this.image = image
      }

      meistercharts {
        qrCode.configure(this)

        configure {

          configurableDouble("angle (rad)", data.angle) {
            min = -PI * 2
            max = PI * 2

            onChange {
              data.angle = it
              markAsDirty()
            }
          }

          configurableDouble("x", data.x) {
            min = 0.0
            max = 70.0

            onChange {
              data.x = it
              markAsDirty()
            }
          }

          configurableDouble("y", data.y) {
            min = 0.0
            max = 90.0

            onChange {
              data.y = it
              markAsDirty()
            }
          }
        }
      }
    }
  }
}
