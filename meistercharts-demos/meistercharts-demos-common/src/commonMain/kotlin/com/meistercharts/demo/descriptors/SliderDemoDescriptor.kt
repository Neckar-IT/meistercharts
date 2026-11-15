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
import com.meistercharts.algorithms.layers.AbstractLayer
import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.algorithms.layers.LayerType
import com.meistercharts.algorithms.layers.addClearBackground
import com.meistercharts.algorithms.layers.text.addTexts
import com.meistercharts.canvas.events.CanvasMouseEventHandler
import com.meistercharts.canvas.slider.RoundHandlePaintable
import com.meistercharts.canvas.slider.Slider
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.DemoQuality
import com.meistercharts.demo.MeisterchartsDemo
import com.meistercharts.demo.MeisterchartsDemoDescriptor
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.VariabilityType
import com.meistercharts.demo.configurableColorProvider
import com.meistercharts.demo.configurableDouble
import com.meistercharts.demo.configurableInt
import com.meistercharts.demo.section
import com.meistercharts.font.FontDescriptorFragment
import it.neckar.open.formatting.percentageFormat
import it.neckar.open.kotlin.lang.asProvider
import it.neckar.open.provider.DoublesProvider
import it.neckar.open.provider.MultiProvider
import it.neckar.open.unit.other.pct
import it.neckar.open.unit.other.px

class SliderDemoDescriptor : MeisterchartsDemoDescriptor<Nothing> {
  override val uuid: Uuid = Uuid.parse("cd74bcdc-dfec-42ba-aa4e-a2c65a559fc3")
  override val name: String = "Slider"

  override val category: DemoCategory = DemoCategory.Interaction
  override val quality: DemoQuality = DemoQuality.High
  override val variabilityType: VariabilityType = VariabilityType.Stable

  override fun prepareDemo(configuration: PredefinedConfiguration<Nothing>?): MeisterchartsDemo {
    return MeisterchartsDemo(this) {
      meistercharts {
        configure {
          layers.addClearBackground()
          val layer = object : AbstractLayer() {
            override val type: LayerType = LayerType.Content

            var tickCount: Int = 5

            var width: @px Double = 300.0
            var sliderPosition: @pct Double = 0.4

            var showLabels: Boolean = true

            val slider = Slider(
              Slider.Configuration(width = { width }, handlePosition = { sliderPosition }, handlePositionChanged = {
                sliderPosition = it
              })
            ) {
              tickLocations = object : DoublesProvider {
                override fun size(): Int {
                  return tickCount
                }

                override fun valueAt(index: Int): Double {
                  return 1.0 / tickCount * (index + 0.5)
                }
              }

              ticksLabels = MultiProvider {
                if (showLabels) {
                  percentageFormat.format(tickLocations.valueAt(it))
                } else {
                  null
                }
              }
            }

            override val mouseEventHandler: CanvasMouseEventHandler = slider.mouseEventHandler

            override fun paint(paintingContext: LayerPaintingContext) {
              val gc = paintingContext.gc
              gc.translate(100.0, 100.0)
              require(gc.translationX == 100.0) { "Expected translationX=100.0 after gc.translate(100,100) but was [${gc.translationX}]" }
              require(gc.translationY == 100.0) { "Expected translationY=100.0 after gc.translate(100,100) but was [${gc.translationY}]" }

              slider.paint(paintingContext, 0.0, 0.0)
            }
          }
          layers.addLayer(layer)
          layers.addTexts { _, _ ->
            listOf(
              "Current slider position: ${percentageFormat.format(layer.sliderPosition)}",
              "State: ${layer.slider.currentState}",
            )
          }.also {
            it.configuration.font = FontDescriptorFragment.DefaultSize.asProvider()
          }

          configurable("width", layer::width) {
            max = 800.0
          }
          configurable("height", layer.slider.configuration::sliderAreaHeight) {
            max = 50.0
          }

          configurable("location", layer::sliderPosition) {
            max = 1.0
          }

          configurableColorProvider("area", layer.slider.configuration::areaFill)

          section("Ticks")

          configurable("Tick Count", layer::tickCount) {
            max = 20
          }
          configurable("tick length", layer.slider.configuration::tickLength) {
            max = 50.0
          }
          configurable("tick label gap", layer.slider.configuration::tickLabelGap) {
            max = 20.0
          }

          section("Handle")
          val handlePaintable = layer.slider.configuration.handlePaintable as RoundHandlePaintable

          configurable("handle diameter", handlePaintable.configuration::diameter) {
            max = 50.0
          }

          configurableColorProvider("handle", handlePaintable.configuration::fill)
        }
      }
    }
  }
}
