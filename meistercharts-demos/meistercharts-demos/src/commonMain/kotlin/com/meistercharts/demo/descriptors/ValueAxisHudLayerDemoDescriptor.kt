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

import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.algorithms.layers.ValueAxisHudLayer
import com.meistercharts.algorithms.layers.addClearBackground
import com.meistercharts.canvas.FontDescriptorFragment
import com.meistercharts.demo.ChartingDemo
import com.meistercharts.demo.ChartingDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.configurableColor
import com.meistercharts.demo.configurableDouble
import com.meistercharts.demo.configurableEnum
import com.meistercharts.demo.configurableFont
import com.meistercharts.demo.configurableList
import com.meistercharts.model.Direction
import it.neckar.open.provider.CoordinatesProvider1
import it.neckar.open.provider.DoubleProvider
import it.neckar.open.provider.MultiDoublesProvider
import it.neckar.open.provider.MultiDoublesProvider1
import it.neckar.open.provider.MultiProvider
import it.neckar.open.formatting.decimalFormat
import com.meistercharts.style.BoxStyle
import it.neckar.open.kotlin.lang.enumEntries

class ValueAxisHudLayerDemoDescriptor : ChartingDemoDescriptor<Nothing> {
  override val name: String = "Value axis HUD"

  override val category: DemoCategory = DemoCategory.Layers

  override fun createDemo(configuration: PredefinedConfiguration<Nothing>?): ChartingDemo {
    return ChartingDemo {

      meistercharts {
        configure {

          val config = object {
            var locationX: Double = 100.0
            var locationY: Double = 100.0
            var anchorDirection: Direction = Direction.CenterLeft
            var font = FontDescriptorFragment.empty
          }

          layers.addClearBackground()
          val hudLayer = ValueAxisHudLayer(
            locations = object : CoordinatesProvider1<LayerPaintingContext> {
              override fun size(param1: LayerPaintingContext): Int {
                return 1
              }

              override fun xAt(index: Int, param1: LayerPaintingContext): Double {
                return config.locationX
              }

              override fun yAt(index: Int, param1: LayerPaintingContext): Double {
                return config.locationY
              }
            },
            labels = { _, _ -> listOf("DaValue", decimalFormat.format(config.locationX)) }

          ) {
            anchorDirections = MultiProvider { config.anchorDirection }
            textFonts = MultiProvider { config.font }
          }
          layers.addLayer(
            hudLayer
          )

          configurableDouble("x", config::locationX) {
            max = 1000.0
          }
          configurableDouble("y", config::locationY) {
            max = 1000.0
          }

          configurableEnum("Anchor Direction", config::anchorDirection, enumEntries())

          configurableList("Box Style", hudLayer.configuration.boxStyles.valueAt(0), listOf(BoxStyle.black, BoxStyle.modernGray, BoxStyle.gray, BoxStyle.none)) {
            onChange {
              hudLayer.configuration.boxStyles = MultiProvider.always(it)
              this@ChartingDemo.markAsDirty()
            }
          }

          configurableDouble("Triangle width", hudLayer.configuration.arrowHeadWidth.valueAt(0)) {
            max = 40.0
            onChange {
              hudLayer.configuration.arrowHeadWidth = MultiDoublesProvider.always(it)
            }
          }
          configurableDouble("Triangle height", hudLayer.configuration.arrowHeadLength.valueAt(0)) {
            max = 40.0
            onChange {
              hudLayer.configuration.arrowHeadLength = MultiDoublesProvider.always(it)
            }
          }

          configurableColor("Text", hudLayer.configuration.textColors.valueAt(0)) {
            onChange {
              hudLayer.configuration.textColors = MultiProvider.always(it)
            }
          }

          configurableFont("Label", config::font) {
          }
        }
      }
    }
  }
}


/**
 * Do NOT move to production.
 * This method uses a lambda which introduces boxing!
 */
fun <IndexContext, P1> MultiDoublesProvider1.Companion.always(value: DoubleProvider): MultiDoublesProvider1<IndexContext, P1> {
  return MultiDoublesProvider1 { _, _ -> value() }
}
