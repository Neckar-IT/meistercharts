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
import com.meistercharts.demo.configurableSeparate
import kotlin.uuid.Uuid
import com.meistercharts.range.ValueRange
import com.meistercharts.axis.AxisEndConfiguration
import com.meistercharts.algorithms.layers.AxisConfiguration
import com.meistercharts.algorithms.layers.axis.ValueAxisLayer
import com.meistercharts.algorithms.layers.AxisTopTopTitleLayer
import com.meistercharts.algorithms.layers.addClearBackground
import com.meistercharts.charts.ContentViewportGestalt
import com.meistercharts.demo.MeisterchartsDemo
import com.meistercharts.demo.MeisterchartsDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.DemoQuality
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.VariabilityType
import com.meistercharts.demo.configurableColorPickerProvider
import com.meistercharts.demo.configurableDouble
import com.meistercharts.demo.configurableEnum
import com.meistercharts.demo.configurableFontProvider
import com.meistercharts.demo.configurableInsetsSeparate
import com.meistercharts.demo.section
import com.meistercharts.model.Insets
import com.meistercharts.model.Vicinity
import it.neckar.open.provider.BooleanProvider

/**
 * A simple hello world demo.
 *
 * Can be used as template to create new demos
 */
class ValueAxisTopTopTitleWithAxisLayerDemoDescriptor : MeisterchartsDemoDescriptor<Nothing> {
  override val uuid: Uuid = Uuid.parse("6a60c53c-4fa5-4acb-8842-6943533b8fd8")
  override val name: String = "Value Axis Top Top Title"
  override val quality: DemoQuality = DemoQuality.High
  override val variabilityType: VariabilityType = VariabilityType.Stable

  //language=HTML
  override val category: DemoCategory = DemoCategory.Layers

  override fun prepareDemo(configuration: PredefinedConfiguration<Nothing>?): MeisterchartsDemo {
    return MeisterchartsDemo(this) {
      meistercharts {

        val contentViewportGestalt = ContentViewportGestalt(Insets.of(40.0, 20.0, 20.0, 20.0))
        contentViewportGestalt.configure(this@meistercharts)


        configure {
          layers.addClearBackground()

          val valueAxisLayer = ValueAxisLayer("MyAxis has a long title", ValueRange.default) {
            tickOrientation = Vicinity.Outside
            axisEndConfiguration = AxisEndConfiguration.Default
            paintRange = AxisConfiguration.PaintRange.ContentArea

            titleVisible = BooleanProvider.False
          }

          val layer = AxisTopTopTitleLayer.forAxis(valueAxisLayer)
          layers.addLayer(valueAxisLayer)
          layers.addLayer(layer)


          section("Axis")

          configurable("Paint Range", valueAxisLayer.configuration::paintRange)
          configurable("Tick Orientation", valueAxisLayer.configuration::tickOrientation)

          configurable("Size", valueAxisLayer.configuration::size) {
            max = 200.0
          }

          configurable("Side", valueAxisLayer.configuration::side)

          section("Title")

          configurable("Gap Horizontal", layer.configuration::titleGapHorizontal) {
            max = 50.0
          }
          configurable("Gap Vertical", layer.configuration::titleGapVertical) {
            max = 50.0
          }

          configurable("Title Color", layer.configuration::titleColor)
          configurable("Title Font", layer.configuration::titleFont)

          configurableSeparate("Content viewport", contentViewportGestalt::contentViewportMargin)
        }
      }
    }
  }
}
