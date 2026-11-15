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
import com.meistercharts.algorithms.layers.PasspartoutLayer
import com.meistercharts.algorithms.layers.addClearBackground
import com.meistercharts.algorithms.layers.addPasspartout
import com.meistercharts.algorithms.layers.debug.ContentAreaDebugLayer
import com.meistercharts.algorithms.layers.text.addText
import com.meistercharts.annotations.Zoomed
import com.meistercharts.canvas.SnapConfiguration
import com.meistercharts.canvas.pixelSnapSupport
import com.meistercharts.color.Color
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.DemoQuality
import com.meistercharts.demo.MeisterchartsDemo
import com.meistercharts.demo.MeisterchartsDemoDescriptor
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.VariabilityType
import com.meistercharts.demo.configurableColorPickerProvider
import com.meistercharts.demo.configurableInsetsSeparate
import com.meistercharts.demo.configurableList
import com.meistercharts.demo.configurableListWithProperty
import com.meistercharts.model.Insets
import com.meistercharts.resize.KeepCenterOnWindowResize
import com.meistercharts.resize.KeepOriginOnWindowResize
import com.meistercharts.resize.ResetToDefaultsOnWindowResize
import com.meistercharts.zoom.FittingWithMargin
import com.meistercharts.zoom.debugEnabled
import it.neckar.open.kotlin.lang.asProvider

class PasspartoutDemoDescriptor : MeisterchartsDemoDescriptor<Nothing> {
  override val uuid: Uuid = Uuid.parse("ce3c8173-c3b4-46ec-ad6a-4665e3c5b747")
  override val name: String = "Passpartout"
  override val description: String = "A passpartout with sizes 10, 50, 100, 150"
  override val category: DemoCategory = DemoCategory.Calculations
  override val quality: DemoQuality = DemoQuality.High
  override val variabilityType: VariabilityType = VariabilityType.Stable

  override fun prepareDemo(configuration: PredefinedConfiguration<Nothing>?): MeisterchartsDemo {
    return MeisterchartsDemo(this) {
      meistercharts {
        @Zoomed var insets = Insets(10.0, 50.0, 100.0, 150.0)
        @Zoomed var margin = Insets.empty

        val zoomAndTranslationDefaults = FittingWithMargin(insets + margin)

        fun Insets.toStringWithInts(): String {
          return "top=${top.toInt()}, right=${right.toInt()}, bottom=${bottom.toInt()}, left=${left.toInt()}"
        }

        configure {
          chartSupport.pixelSnapSupport.snapConfiguration = SnapConfiguration.None

          layers.addClearBackground()
          val passpartoutLayer = layers.addPasspartout(insets, Color.rgba(0x1a, 0x4d, 0x4d, 0.5).asProvider()) //Transparent
          layers.addLayer(ContentAreaDebugLayer())
          layers.addText({ _, _ -> listOf("insets: ${insets.toStringWithInts()}", "margin: ${margin.toStringWithInts()}") }, { textColor = Color.gray })

          configurableList("Snap config", SnapConfiguration.None, listOf(SnapConfiguration.None, SnapConfiguration.Both, SnapConfiguration.OnlyX, SnapConfiguration.OnlyY)) {
            onChange {
              chartSupport.pixelSnapSupport.snapConfiguration = it
              markAsDirty()
            }
            converter = {
              it.name
            }
          }

          configurableListWithProperty("Resize behavior", chartSupport::windowResizeBehavior, listOf(KeepOriginOnWindowResize, KeepCenterOnWindowResize, ResetToDefaultsOnWindowResize)) {
            converter = {
              it::class.simpleName ?: it.toString()
            }
          }

          configurableInsetsSeparate("Insets") {
            value = insets
            min = 0.0
            max = 200.0
            onChange {
              insets = it
              passpartoutLayer.configuration.insets = { insets }
              zoomAndTranslationDefaults.marginProvider = { insets + margin }
              markAsDirty()
            }
          }

          configurable("Color", passpartoutLayer.configuration::color) {
          }

          configurableInsetsSeparate("Margin") {
            value = margin
            min = 0.0
            max = 200.0
            onChange {
              margin = it
              passpartoutLayer.configuration.margin = { margin }
              zoomAndTranslationDefaults.marginProvider = { insets + margin }
              markAsDirty()
            }
          }

          configurableList("Painting Strategy", passpartoutLayer.configuration.strategy, PasspartoutLayer.availableStrategies) {
            onChange {
              passpartoutLayer.configuration.strategy = it
              markAsDirty()
            }
            converter = {
              it::class.simpleName ?: it.toString()
            }
          }

        }

        zoomAndTranslationDefaults { zoomAndTranslationDefaults }
        zoomAndTranslationModifier {
          minZoom(0.1, 0.1)
          maxZoom(5.0, 5.0)
          contentAlwaysBarelyVisible()

          //If enabled only positive movements are allowed
          //.withOnlyPositivePanModifier()
          //.withDisabledPanningX()
          //.withDisabledZoomingAndPanning()
          debugEnabled()
        }

      }
    }
  }
}
