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

import com.meistercharts.algorithms.ResetToDefaultsOnWindowResize
import com.meistercharts.algorithms.ValueRange
import com.meistercharts.algorithms.impl.FittingWithMargin
import com.meistercharts.algorithms.layers.ValueAxisLayer
import com.meistercharts.algorithms.layers.addClearBackground
import com.meistercharts.algorithms.layers.addPasspartout
import com.meistercharts.algorithms.layers.bind
import com.meistercharts.algorithms.layers.debug.ContentAreaDebugLayer
import com.meistercharts.algorithms.painter.Color
import com.meistercharts.demo.ChartingDemo
import com.meistercharts.demo.ChartingDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.configurableBooleanProvider
import com.meistercharts.demo.configurableColor
import com.meistercharts.demo.configurableDouble
import com.meistercharts.demo.configurableEnum
import com.meistercharts.demo.configurableFont
import com.meistercharts.demo.configurableInsets
import com.meistercharts.model.Insets
import com.meistercharts.model.Side
import com.meistercharts.model.Vicinity
import it.neckar.open.provider.BooleanProvider

/**
 */
class ValueAxisAtContentAreaDemoDescriptor(
) : ChartingDemoDescriptor<Nothing> {
  override val name: String = "Value axis @ ContentArea"
  override val description: String = "## How to align a value axis at the content area"
  override val category: DemoCategory = DemoCategory.Axis

  override fun createDemo(configuration: PredefinedConfiguration<Nothing>?): ChartingDemo {
    return ChartingDemo {

      meistercharts {
        val chartMargin = Insets(20.0, 10.0, 80.0, 120.0)
        zoomAndTranslationDefaults {
          FittingWithMargin(chartMargin)
        }

        configure {
          chartSupport.windowResizeBehavior = ResetToDefaultsOnWindowResize
        }

        configure {
          layers.addClearBackground()
          val leftPasspartoutLayer = layers.addPasspartout(chartMargin, Color.silver)
          val bottomPasspartoutLayer = layers.addPasspartout(chartMargin, Color.silver)
          layers.addLayer(ContentAreaDebugLayer())

          val valueRange = ValueRange.linear(0.0, 100.0)
          val leftValueAxisLayer = ValueAxisLayer(ValueAxisLayer.Data(valueRangeProvider = { valueRange })) {
            titleProvider = { _, _ -> "The left axis [m²/h]" }
            side = Side.Left
            tickOrientation = Vicinity.Outside
            size = chartMargin.left - margin.left
          }.also {
            layers.addLayer(it)
          }

          val bottomValueAxisLayer = ValueAxisLayer(ValueAxisLayer.Data(valueRangeProvider = { valueRange })) {
            titleProvider = { _, _ -> "The bottom axis [m²/h]" }
            side = Side.Bottom
            tickOrientation = Vicinity.Outside
            size = chartMargin.bottom - margin.bottom
          }.also {
            layers.addLayer(it)
          }

          leftPasspartoutLayer.style.bind(leftValueAxisLayer.style)
          bottomPasspartoutLayer.style.bind(bottomValueAxisLayer.style)

          declare {
            section("Layout")
          }

          configurableInsets("Margin", leftValueAxisLayer.style.margin) {
            onChange {
              //Update the size based upon the new margin
              leftValueAxisLayer.style.margin = it
              bottomValueAxisLayer.style.margin = it

              leftValueAxisLayer.style.size = chartMargin.left - leftValueAxisLayer.style.margin.left
              bottomValueAxisLayer.style.size = chartMargin.bottom - bottomValueAxisLayer.style.margin.bottom

              markAsDirty()
            }
          }

          declare {
            section("Title")
          }

          configurableBooleanProvider("Show Title", leftValueAxisLayer.style::titleVisible) {
            onChange {
              bottomValueAxisLayer.style.titleVisible = BooleanProvider(it)
              markAsDirty()
            }
          }

          configurableDouble("Title Gap", leftValueAxisLayer.style::titleGap) {
            max = 20.0
            onChange {
              bottomValueAxisLayer.style.titleGap = it
              markAsDirty()
            }
          }

          declare {
            section("Axis Config")
          }

          configurableEnum("Paint Range", leftValueAxisLayer.style::paintRange, enumValues()) {
            onChange {
              bottomValueAxisLayer.style.paintRange = it
              markAsDirty()
            }
          }
          configurableEnum("Tick Orientation", leftValueAxisLayer.style::tickOrientation, enumValues()) {
            onChange {
              bottomValueAxisLayer.style.tickOrientation = it
              markAsDirty()
            }
          }
          configurableEnum("Axis End", leftValueAxisLayer.style::axisEndConfiguration, enumValues()) {
            onChange {
              bottomValueAxisLayer.style.axisEndConfiguration = it
              markAsDirty()
            }
          }

          declare {
            section("Widths")
          }

          configurableDouble("Axis line width", leftValueAxisLayer.style::axisLineWidth) {
            max = 20.0
            onChange {
              bottomValueAxisLayer.style.axisLineWidth = it
              markAsDirty()
            }
          }
          configurableDouble("Tick length", leftValueAxisLayer.style.tickLength) {
            max = 20.0
            onChange {
              leftValueAxisLayer.style.tickLength = it
              bottomValueAxisLayer.style.tickLength = it
              markAsDirty()
            }
          }
          configurableDouble("Tick width", leftValueAxisLayer.style.tickLineWidth) {
            max = 20.0
            onChange {
              leftValueAxisLayer.style.tickLineWidth = it
              bottomValueAxisLayer.style.tickLineWidth = it
              markAsDirty()
            }
          }
          configurableDouble("Tick Label Gap", leftValueAxisLayer.style.tickLabelGap) {
            max = 20.0
            onChange {
              leftValueAxisLayer.style.tickLabelGap = it
              bottomValueAxisLayer.style.tickLabelGap = it
              markAsDirty()
            }
          }

          configurableColor("Background Color", leftPasspartoutLayer.style.color()) {
            onChange {
              leftPasspartoutLayer.style.color = { it }
              bottomPasspartoutLayer.style.color = { it }
              markAsDirty()
            }
          }

          configurableFont("Tick font", leftValueAxisLayer.style.tickFont) {
            onChange {
              leftValueAxisLayer.style.tickFont = it
              bottomValueAxisLayer.style.tickFont = it
              markAsDirty()
            }
          }

          configurableFont("Title font", leftValueAxisLayer.style.titleFont) {
            onChange {
              leftValueAxisLayer.style.titleFont = it
              bottomValueAxisLayer.style.titleFont = it
              markAsDirty()
            }
          }
        }
      }
    }
  }
}
