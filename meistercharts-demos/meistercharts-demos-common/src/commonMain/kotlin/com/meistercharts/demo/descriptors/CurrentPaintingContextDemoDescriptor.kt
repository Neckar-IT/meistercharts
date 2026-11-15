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
import com.meistercharts.algorithms.layers.addClearBackground
import com.meistercharts.algorithms.layers.text.addTexts
import com.meistercharts.canvas.CurrentPaintingContext
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.DemoQuality
import com.meistercharts.demo.MeisterchartsDemo
import com.meistercharts.demo.MeisterchartsDemoDescriptor
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.VariabilityType
import com.meistercharts.font.FontDescriptorFragment
import it.neckar.open.i18n.CurrentI18nConfiguration
import it.neckar.open.kotlin.lang.asProvider
import it.neckar.open.time.formatUtcForDebug

/**
 */
class CurrentPaintingContextDemoDescriptor : MeisterchartsDemoDescriptor<Nothing> {
  override val uuid: Uuid = Uuid.parse("aba8f57c-5c60-481b-9b9c-6ab51af135f9")
  override val name: String = "Current Painting Context"
  override val quality: DemoQuality = DemoQuality.High
  override val variabilityType: VariabilityType = VariabilityType.Stable

  //language=HTML
  override val description: String = "A simple Hello World demo showing text above a white background"
  override val category: DemoCategory = DemoCategory.Framework

  override fun prepareDemo(configuration: PredefinedConfiguration<Nothing>?): MeisterchartsDemo {
    return MeisterchartsDemo(this) {
      meistercharts {
        configure {
          layers.addClearBackground()
          layers.addTexts { _, i18nConfiguration ->
            listOf(
              "Frame timestamp: ${CurrentPaintingContext.frameTimestamp.formatUtcForDebug()}",
              "paintingLoopIndex: ${CurrentPaintingContext.paintingLoopIndex}",
              "chartId: ${CurrentPaintingContext.chartId}",
              "default - i18nConfiguration: $CurrentI18nConfiguration",
            )
          }.also {
            it.configuration.font = FontDescriptorFragment.DefaultSize.asProvider()
          }
        }
      }
    }
  }
}
