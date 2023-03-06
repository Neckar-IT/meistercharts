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
import com.meistercharts.algorithms.painter.Color
import com.meistercharts.canvas.paintable.Paintable
import com.meistercharts.canvas.paintable.RectanglePaintable
import com.meistercharts.canvas.paintable.SymbolAndTextKeyPaintable
import com.meistercharts.demo.ChartingDemo
import com.meistercharts.demo.ChartingDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.configurableDouble
import com.meistercharts.model.Direction
import com.meistercharts.model.Orientation
import com.meistercharts.model.Size
import it.neckar.open.provider.SizedProvider
import it.neckar.open.i18n.TextKey
import com.meistercharts.style.Palette.getChartColor

/**
 */
class LegendLayerDemoDescriptor : ChartingDemoDescriptor<Orientation> {
  override val name: String = "Legend layer"
  override val description: String = "## How to a legend"
  override val category: DemoCategory = DemoCategory.Layers

  override val predefinedConfigurations: List<PredefinedConfiguration<Orientation>> = listOf(
    PredefinedConfiguration(Orientation.Vertical),
    PredefinedConfiguration(Orientation.Horizontal),
  )

  override fun createDemo(configuration: PredefinedConfiguration<Orientation>?): ChartingDemo {
    val layoutOrientation = requireNotNull(configuration).payload

    fun createPaintable1(text: String, index: Int): Paintable {
      val color = getChartColor(index)
      return SymbolAndTextKeyPaintable(RectanglePaintable(24.0, 24.0, color), TextKey(text, text)) {
        textColor = color
      }
    }

    val paintableProvider = SizedProvider.forList(
      listOf(
        RectanglePaintable(Size.PX_50, Color.pink),
        createPaintable1("First", 0),
        createPaintable1("Second", 1),
        RectanglePaintable(Size.PX_16, Color.orange),
        RectanglePaintable(Size.PX_24, Color.green),
      )
    )

    val legends = Direction.allButBaseline.map { direction ->
      LegendLayer(
        paintableProvider,
        layoutOrientation,
      ) {
        anchorDirection = direction
      }
    }

    return ChartingDemo {
      meistercharts {
        configure {
          layers.addClearBackground()
          legends.forEach {
            layers.addLayer(it)
          }

          configurableDouble("Horizontal Gap", legends.first().configuration.horizontalGap) {
            min = 0.0
            max = 100.0
            onChange {
              legends.forEach { legendLayer ->
                legendLayer.configuration.horizontalGap = it
              }
              markAsDirty()
            }
          }
          configurableDouble("Vertical Gap", legends.first().configuration.verticalGap) {
            min = 0.0
            max = 100.0
            onChange {
              legends.forEach { legendLayer ->
                legendLayer.configuration.verticalGap = it
              }
              markAsDirty()
            }
          }

          configurableDouble("Entries gap", legends.first().configuration.entriesGap) {
            min = 0.0
            max = 100.0
            onChange {
              legends.forEach { legendLayer ->
                legendLayer.configuration.entriesGap = it
              }
              markAsDirty()
            }
          }
        }
      }
    }
  }

}
