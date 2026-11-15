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
import kotlin.uuid.Uuid
import com.meistercharts.color.RadialGradient
import com.meistercharts.canvas.i18nConfiguration
import com.meistercharts.canvas.textService
import com.meistercharts.charts.NeckarITSloganWithFlowGestalt
import com.meistercharts.demo.MeisterchartsDemo
import com.meistercharts.demo.MeisterchartsDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.DemoQuality
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.VariabilityType
import com.meistercharts.demo.configurableBoolean
import com.meistercharts.demo.configurableColor
import com.meistercharts.demo.configurableColorPickerProvider
import com.meistercharts.demo.configurableDouble
import com.meistercharts.demo.configurableEnum
import com.meistercharts.demo.configurableFontProvider
import com.meistercharts.demo.configurableList
import it.neckar.geometry.Direction

class NeckarITSloganWithFlowGestaltDemoDescriptor : MeisterchartsDemoDescriptor<Nothing> {
  override val uuid: Uuid = Uuid.parse("373311ac-0fc7-4e8f-b8b5-4f3bcbf0907d")

  override val name: String = "Neckar IT Slogan With Flow Chart"
  override val description: String = "The Neckar IT slogan combined with the flow animation"
  override val category: DemoCategory = DemoCategory.Gestalt
  override val variabilityType: VariabilityType = VariabilityType.Stable

  // font size slider not working
  override val quality: DemoQuality = DemoQuality.Low

  override fun prepareDemo(configuration: PredefinedConfiguration<Nothing>?): MeisterchartsDemo {

    return MeisterchartsDemo(this) {
      val gestalt = NeckarITSloganWithFlowGestalt()

      meistercharts {
        gestalt.configure(this)

        configure {
          val slogans = listOf(
            gestalt.sloganLayer.configuration.sloganProvider(chartSupport.textService, chartSupport.i18nConfiguration),
            "We make everything flow",
            "Reach to the stars",
            ""
          )
          configurableList("slogan", slogans.first(), slogans) {
            onChange {
              gestalt.sloganLayer.configuration.sloganProvider = { _, _ -> it }
              this@MeisterchartsDemo.markAsDirty()
            }
          }

          configurable("slogan foreground", gestalt.sloganLayer.configuration::foreground)

          configurable("slogan background", gestalt.backgroundLayer.configuration::background)

          configurableColor("slogan glow color", gestalt.sloganLayer.configuration.glowGradient.color0) {
            onChange {
              gestalt.sloganLayer.configuration.glowGradient = RadialGradient(it, gestalt.sloganLayer.configuration.glowGradient.color1)
              this@MeisterchartsDemo.markAsDirty()
            }
          }

          configurable("glow scaleX", gestalt.sloganLayer.configuration::glowScaleX) {
            min = 0.1
            max = 10.0
          }

          configurable("glow scaleY", gestalt.sloganLayer.configuration::glowScaleY) {
            min = 0.1
            max = 10.0
          }

          configurable("slogan font", gestalt.sloganLayer.configuration::sloganFont)

          configurable("fit font size", gestalt.sloganLayer.configuration::keepSloganInBounds)

          configurable("slogan offset-y (px)", gestalt.sloganLayer.configuration::sloganOffsetY) {
            min = -200.0
            max = 200.0
          }

          configurable("Slogan Anchor", gestalt.sloganLayer.configuration::anchorDirection, kotlin.enums.enumEntries<Direction>().toList())
        }
      }
    }
  }
}
