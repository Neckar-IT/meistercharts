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
import com.meistercharts.range.ValueRange
import com.meistercharts.color.Color
import com.meistercharts.charts.QRPositionDiagramGestalt
import com.meistercharts.demo.MeisterchartsDemo
import com.meistercharts.demo.MeisterchartsDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.DemoQuality
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.VariabilityType
import com.meistercharts.demo.configurableDouble
import it.neckar.geometry.Size
import com.meistercharts.resources.Icons
import kotlin.math.PI

class QRPositionDiagramGestaltDemoDescriptor : MeisterchartsDemoDescriptor<Nothing> {
  override val uuid: Uuid = Uuid.parse("007d8191-e4a3-4b94-b1d3-76d166ef99c4")

  override val name: String = "QR Code Position Diagram"
  override val description: String = "A QR code position diagram gestalt"
  override val category: DemoCategory = DemoCategory.Automation
  override val quality: DemoQuality = DemoQuality.High
  override val variabilityType: VariabilityType = VariabilityType.Stable


  override fun prepareDemo(configuration: PredefinedConfiguration<Nothing>?): MeisterchartsDemo {

    return MeisterchartsDemo(this) {
      val image = Icons.neckarItQr(Size.PX_120, Color.black)

      val data = QRPositionDiagramGestalt.Configuration().apply {
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
