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

import com.meistercharts.algorithms.axis.AxisOrientationY
import com.meistercharts.algorithms.impl.ZoomAndTranslationModifiersBuilder
import com.meistercharts.algorithms.layers.addClearBackground
import com.meistercharts.algorithms.layers.debug.ContentAreaDebugLayer
import com.meistercharts.algorithms.layers.slippymap.OpenStreetMap
import com.meistercharts.algorithms.layers.slippymap.OpenStreetMapBlackAndWhite
import com.meistercharts.algorithms.layers.slippymap.OpenStreetMapDe
import com.meistercharts.algorithms.layers.slippymap.OpenStreetMapGrayscale
import com.meistercharts.algorithms.layers.slippymap.OpenStreetMapHumanitarian
import com.meistercharts.algorithms.layers.slippymap.OpenStreetMapTerrain
import com.meistercharts.algorithms.layers.slippymap.SlippyMapCenter
import com.meistercharts.algorithms.layers.slippymap.SlippyMapCenter.Companion.neckarItCenter
import com.meistercharts.algorithms.layers.slippymap.SlippyMapProvider
import com.meistercharts.algorithms.layers.slippymap.WikimediaMaps
import com.meistercharts.algorithms.layers.slippymap.addSlippyMap
import com.meistercharts.algorithms.layers.slippymap.calculateSlippyMapContentAreaSize
import com.meistercharts.algorithms.layers.slippymap.withSlippyMapZoom
import com.meistercharts.algorithms.layers.text.addText
import com.meistercharts.algorithms.tile.TileIndex
import com.meistercharts.canvas.FixedContentAreaSize
import com.meistercharts.canvas.FontDescriptorFragment
import com.meistercharts.canvas.SnapConfiguration
import com.meistercharts.canvas.pixelSnapSupport
import com.meistercharts.demo.ChartingDemo
import com.meistercharts.demo.ChartingDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.configurableList
import com.meistercharts.model.Direction
import com.meistercharts.model.DirectionBasedBasePointProvider
import com.meistercharts.style.Palette

/**
 */
class SlippyMapDemoDescriptor(val center: SlippyMapCenter = neckarItCenter) : ChartingDemoDescriptor<Nothing> {
  override val name: String = "Slippy Map layer"
  override val description: String = "## A demo of the slippy map layer"
  override val category: DemoCategory = DemoCategory.Layers

  override fun createDemo(configuration: PredefinedConfiguration<Nothing>?): ChartingDemo {
    return ChartingDemo {
      meistercharts {

        contentAreaSizingStrategy = FixedContentAreaSize(calculateSlippyMapContentAreaSize())

        zoomAndTranslationDefaults { center }
        zoomAndTranslationModifier = ZoomAndTranslationModifiersBuilder()
          .withSlippyMapZoom()
          .build()

        // This is important! This is the change factor between slippy map zoom levels
        zoomChangeFactor = 2.0

        val slippyMapProvider = MutableSlippyMapProvider(OpenStreetMapDe)

        configure {
          chartSupport.pixelSnapSupport.snapConfiguration = SnapConfiguration.None

          chartSupport.rootChartState.axisOrientationY = AxisOrientationY.OriginAtTop

          layers.addClearBackground()
          val slippyMapLayer = layers.addSlippyMap(slippyMapProvider) {
            showTileBorder = true
            showTileCoordinates = true
            showTileIndex = true
            showTileUrl = false
          }
          layers.addLayer(ContentAreaDebugLayer())

          // see https://www.openstreetmap.org/copyright/en
          // TODO add a Link-Layer
          slippyMapProvider.legalNotice?.let {
            layers.addText({ _, _ ->
              listOf(slippyMapProvider.delegate.legalNotice.orEmpty())
            }) {
              textColor = Palette.defaultGray
              font = FontDescriptorFragment(16.0)
              anchorDirection = Direction.BottomRight
              anchorPointProvider = DirectionBasedBasePointProvider(anchorDirection)
            }
          }

          configurableList(
            "Server",
            "OpenStreetMap.de",
            listOf(
              "OpenStreetMap",
              "OpenStreetMap.de",
              "OpenStreetMapHumanitarian",
              "OpenStreetMapGrayscale",
              "OpenStreetMapBlackAndWhite",
              "OpenStreetMapTerrain (zoom <= 16)",
              "WikimediaMaps"
            )
          ) {
            onChange {
              slippyMapProvider.delegate = when (it) {
                "OpenStreetMap" -> OpenStreetMap
                "OpenStreetMap.de" -> OpenStreetMapDe
                "OpenStreetMapHumanitarian" -> OpenStreetMapHumanitarian
                "OpenStreetMapGrayscale" -> OpenStreetMapGrayscale
                "OpenStreetMapBlackAndWhite" -> OpenStreetMapBlackAndWhite
                "OpenStreetMapTerrain (zoom <= 16)" -> OpenStreetMapTerrain
                "WikimediaMaps" -> WikimediaMaps
                else -> throw IllegalStateException("unknown server <$it>")
              }
              slippyMapLayer.tileProvider.clear()
              markAsDirty()
            }
          }
        }
      }
    }
  }

  private class MutableSlippyMapProvider(var delegate: SlippyMapProvider) : SlippyMapProvider {
    override fun url(tileIndex: TileIndex, zoom: Int): String {
      return delegate.url(tileIndex, zoom)
    }

    override val legalNotice: String?
      get() = delegate.legalNotice
  }

}
