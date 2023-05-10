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

import com.meistercharts.algorithms.layers.addClearBackground
import com.meistercharts.algorithms.layers.legend.LegendLayer
import com.meistercharts.canvas.paintable.RectanglePaintable
import com.meistercharts.canvas.paintable.SymbolAndTextKeyPaintable
import com.meistercharts.demo.ChartingDemo
import com.meistercharts.demo.ChartingDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.configurableDouble
import com.meistercharts.demo.configurableFont
import com.meistercharts.model.Direction
import com.meistercharts.model.Orientation
import it.neckar.open.provider.SizedProvider
import it.neckar.open.i18n.TextKey
import com.meistercharts.style.Palette
import kotlin.math.max

/**
 */
class BarChartLegendLayerDemoDescriptor : ChartingDemoDescriptor<Nothing> {
  override val name: String = "Bar Chart Legend"

  //language=HTML
  override val description: String = "## Bar Chart Legend"
  override val category: DemoCategory = DemoCategory.Layers

  override fun createDemo(configuration: PredefinedConfiguration<Nothing>?): ChartingDemo {
    return ChartingDemo {

      val textWidthForLayoutBase = 70.0

      meistercharts {
        configure {
          layers.addClearBackground()

          val labels = listOf(
            TextKey.simple("Operational"),
            TextKey.simple("Warnings"),
            TextKey.simple("Errors"),
            TextKey.simple("Offline")
          )

          val colors = listOf(
            Palette.stateSuperior,
            Palette.stateWarning,
            Palette.stateError,
            Palette.stateOffline
          )

          val legendEntries = labels.mapIndexed { index: Int, textKey: TextKey ->
            val color = colors[index]
            SymbolAndTextKeyPaintable(RectanglePaintable(16.0, 16.0, color), textKey) {
              textColor = color
              textWidthForLayout = textWidthForLayoutBase
            }
          }
          val legendLayer = LegendLayer(SizedProvider.forList(legendEntries), Orientation.Vertical) {
            anchorDirection = Direction.CenterRight
          }
          layers.addLayer(legendLayer)

          configurableDouble("Horizontal Gap", legendLayer.configuration::horizontalGap) {
            max = 100.0
          }
          configurableDouble("Vertical Gap", legendLayer.configuration::verticalGap) {
            max = 100.0
          }

          configurableDouble("Entries gap", legendLayer.configuration::entriesGap) {
            max = 50.0
          }

          configurableFont(initialValue = legendEntries.first().style.textFont) {
            onChange {
              legendEntries.forEach { legendEntry ->
                legendEntry.style.textFont = it
                legendEntry.style.textHeightForLayout = it.size.size * 1.1
                legendEntry.style.textWidthForLayout = textWidthForLayoutBase * max(1.0, it.size.size / 14.0)
              }
              markAsDirty()
            }
          }
        }
      }
    }
  }
}
