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
import com.meistercharts.demo.section
import kotlin.uuid.Uuid
import com.meistercharts.algorithms.layers.AxisConfiguration
import com.meistercharts.algorithms.layers.addClearBackground
import com.meistercharts.algorithms.layers.addPasspartout
import com.meistercharts.algorithms.layers.axis.ValueAxisLayer
import com.meistercharts.algorithms.layers.bind
import com.meistercharts.algorithms.layers.debug.ContentAreaDebugLayer
import com.meistercharts.axis.AxisEndConfiguration
import com.meistercharts.color.Color
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.DemoQuality
import com.meistercharts.demo.MeisterchartsDemo
import com.meistercharts.demo.MeisterchartsDemoDescriptor
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.VariabilityType
import com.meistercharts.demo.configurableBooleanProvider
import com.meistercharts.demo.configurableColor
import com.meistercharts.demo.configurableDouble
import com.meistercharts.demo.configurableEnum
import com.meistercharts.demo.configurableFontProvider
import com.meistercharts.demo.configurableInsets
import com.meistercharts.model.Insets
import com.meistercharts.model.Vicinity
import com.meistercharts.range.ValueRange
import com.meistercharts.resize.ResetToDefaultsOnWindowResize
import com.meistercharts.zoom.FittingWithMargin
import it.neckar.geometry.Side
import it.neckar.open.kotlin.lang.asProvider
import it.neckar.open.provider.BooleanProvider

/**
 */
class ValueAxisAtContentAreaDemoDescriptor(
) : MeisterchartsDemoDescriptor<Nothing> {
  override val uuid: Uuid = Uuid.parse("7533ce26-d773-4bdd-aa9a-985ba8fd908a")
  override val name: String = "Value axis @ ContentArea"
  override val description: String = "Aligns a value axis at the content area edge."
  override val category: DemoCategory = DemoCategory.Axis
  override val quality: DemoQuality = DemoQuality.High
  override val variabilityType: VariabilityType = VariabilityType.Stable

  override fun prepareDemo(configuration: PredefinedConfiguration<Nothing>?): MeisterchartsDemo {
    return MeisterchartsDemo(this) {

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
          val leftValueAxisLayer = ValueAxisLayer(ValueAxisLayer.Configuration(valueRangeProvider = { valueRange })) {
            titleProvider = { _, _ -> "The left axis [m²/h]" }
            side = Side.Left
            tickOrientation = Vicinity.Outside
            size = chartMargin.left - margin.left
          }.also {
            layers.addLayer(it)
          }

          val bottomValueAxisLayer = ValueAxisLayer(ValueAxisLayer.Configuration(valueRangeProvider = { valueRange })) {
            titleProvider = { _, _ -> "The bottom axis [m²/h]" }
            side = Side.Bottom
            tickOrientation = Vicinity.Outside
            size = chartMargin.bottom - margin.bottom
          }.also {
            layers.addLayer(it)
          }

          leftPasspartoutLayer.configuration.bind(leftValueAxisLayer.configuration)
          bottomPasspartoutLayer.configuration.bind(bottomValueAxisLayer.configuration)

          section("Layout")

          configurableInsets("Margin", leftValueAxisLayer.configuration.margin) {
            onChange {
              //Update the size based upon the new margin
              leftValueAxisLayer.configuration.margin = it
              bottomValueAxisLayer.configuration.margin = it

              leftValueAxisLayer.configuration.size = chartMargin.left - leftValueAxisLayer.configuration.margin.left
              bottomValueAxisLayer.configuration.size = chartMargin.bottom - bottomValueAxisLayer.configuration.margin.bottom

              markAsDirty()
            }
          }

          section("Title")

          configurable("Show Title", leftValueAxisLayer.configuration::titleVisible) {
            onChange {
              bottomValueAxisLayer.configuration.titleVisible = BooleanProvider(it)
              markAsDirty()
            }
          }

          configurable("Title Gap", leftValueAxisLayer.configuration::titleGap) {
            max = 20.0
            onChange {
              bottomValueAxisLayer.configuration.titleGap = it
              markAsDirty()
            }
          }

          section("Axis Config")

          configurable("Paint Range", leftValueAxisLayer.configuration::paintRange, kotlin.enums.enumEntries<AxisConfiguration.PaintRange>().toList()) {
            onChange {
              bottomValueAxisLayer.configuration.paintRange = it
              markAsDirty()
            }
          }
          configurable("Tick Orientation", leftValueAxisLayer.configuration::tickOrientation, kotlin.enums.enumEntries<Vicinity>().toList()) {
            onChange {
              bottomValueAxisLayer.configuration.tickOrientation = it
              markAsDirty()
            }
          }
          configurable("Axis End", leftValueAxisLayer.configuration::axisEndConfiguration, kotlin.enums.enumEntries<AxisEndConfiguration>().toList()) {
            onChange {
              bottomValueAxisLayer.configuration.axisEndConfiguration = it
              markAsDirty()
            }
          }

          section("Widths")

          configurable("Axis line width", leftValueAxisLayer.configuration::axisLineWidth) {
            max = 20.0
            onChange {
              bottomValueAxisLayer.configuration.axisLineWidth = it
              markAsDirty()
            }
          }
          configurableDouble("Tick length", leftValueAxisLayer.configuration.tickLength) {
            max = 20.0
            onChange {
              leftValueAxisLayer.configuration.tickLength = it
              bottomValueAxisLayer.configuration.tickLength = it
              markAsDirty()
            }
          }
          configurableDouble("Tick width", leftValueAxisLayer.configuration.tickLineWidth) {
            max = 20.0
            onChange {
              leftValueAxisLayer.configuration.tickLineWidth = it
              bottomValueAxisLayer.configuration.tickLineWidth = it
              markAsDirty()
            }
          }
          configurableDouble("Tick Label Gap", leftValueAxisLayer.configuration.tickLabelGap) {
            max = 20.0
            onChange {
              leftValueAxisLayer.configuration.tickLabelGap = it
              bottomValueAxisLayer.configuration.tickLabelGap = it
              markAsDirty()
            }
          }

          configurableColor("Background Color", leftPasspartoutLayer.configuration.color()) {
            onChange {
              leftPasspartoutLayer.configuration.color = { it }
              bottomPasspartoutLayer.configuration.color = { it }
              markAsDirty()
            }
          }

          configurable("Tick font", leftValueAxisLayer.configuration::tickFont) {
            onChange {
              leftValueAxisLayer.configuration.tickFont = it.asProvider()
              bottomValueAxisLayer.configuration.tickFont = it.asProvider()
              markAsDirty()
            }
          }

          configurable("Title font", leftValueAxisLayer.configuration::titleFont) {
            onChange {
              leftValueAxisLayer.configuration.titleFont = it.asProvider()
              bottomValueAxisLayer.configuration.titleFont = it.asProvider()
              markAsDirty()
            }
          }
        }
      }
    }
  }
}
