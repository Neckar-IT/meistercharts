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
import com.meistercharts.canvas.slider.YearSlider
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.DemoQuality
import com.meistercharts.demo.MeisterchartsDemo
import com.meistercharts.demo.MeisterchartsDemoDescriptor
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.VariabilityType
import com.meistercharts.demo.configurableDoubleProvider
import it.neckar.datetime.minimal.Year
import it.neckar.open.provider.SizedProvider


class TimeSliderDemoDescriptor : MeisterchartsDemoDescriptor<Nothing> {
  override val uuid: Uuid = Uuid.parse("23ce2d03-3e0f-4a38-a09d-d4c297f7c54b")
  override val name: String = "Time Slider"

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


            val yearSlider = YearSlider(years = SizedProvider.forValues(Year(2023), Year(2025), Year(2032), Year(2035))) {

            }

            val slider = yearSlider.slider


            override val mouseEventHandler: CanvasMouseEventHandler = slider.mouseEventHandler

            override fun paint(paintingContext: LayerPaintingContext) {
              val gc = paintingContext.gc

              gc.translate(100.0, 100.0)

              slider.paint(paintingContext)
            }
          }
          layers.addLayer(layer)
          layers.addTexts { textService, i18nConfiguration ->
            val selectedYearIndex = layer.yearSlider.selectedYearIndex

            buildList {
              add("Selected year index: ${layer.yearSlider.selectedYearIndex}")

              if (selectedYearIndex != YearSlider.YearIndex.None) {
                add("Selected year: ${layer.yearSlider.years.valueAt(selectedYearIndex.value).value}")
              }
            }
          }

          configurable("Width", layer.slider.configuration::width) {
            max = 500.0
          }
        }
      }
    }
  }
}
