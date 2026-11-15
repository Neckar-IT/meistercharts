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
import com.meistercharts.algorithms.layers.debug.ContentAreaDebugLayer
import com.meistercharts.algorithms.layers.slippymap.OpenStreetMapDe
import com.meistercharts.algorithms.layers.slippymap.SlippyMapProvider
import com.meistercharts.algorithms.layers.slippymap.addSlippyMap
import com.meistercharts.algorithms.layers.text.addText
import it.neckar.geometry.AxisOrientationY
import com.meistercharts.canvas.FixedContentAreaSize
import com.meistercharts.canvas.SnapConfiguration
import com.meistercharts.canvas.pixelSnapSupport
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.DemoQuality
import com.meistercharts.demo.MeisterchartsDemo
import com.meistercharts.demo.MeisterchartsDemoDescriptor
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.VariabilityType
import com.meistercharts.demo.configurableList
import com.meistercharts.demo.configurableListWithProperty
import com.meistercharts.font.FontDescriptorFragment
import com.meistercharts.geometry.DirectionBasedBasePointProvider
import com.meistercharts.maps.SlippyMapCenter
import com.meistercharts.maps.SlippyMapCenter.Companion.neckarItCenter
import com.meistercharts.maps.calculateSlippyMapContentAreaSize
import com.meistercharts.maps.withSlippyMapZoom
import it.neckar.geometry.Direction
import com.meistercharts.style.Palette
import com.meistercharts.zoom.ZoomAndTranslationModifiersBuilder
import it.neckar.open.kotlin.lang.asProvider

/**
 */
class SlippyMapDemoDescriptor(val center: SlippyMapCenter = neckarItCenter) : MeisterchartsDemoDescriptor<Nothing> {
  override val uuid: Uuid = Uuid.parse("c3153c26-b148-4599-812b-5b5012ad8de7")
  override val name: String = "Slippy Map layer"
  override val description: String = "Shows the slippy map layer."
  override val category: DemoCategory = DemoCategory.Layers
  override val quality: DemoQuality = DemoQuality.High
  override val variabilityType: VariabilityType = VariabilityType.Unstable //OpenStreetMap sometimes returns different tiles

  override fun prepareDemo(configuration: PredefinedConfiguration<Nothing>?): MeisterchartsDemo {
    return MeisterchartsDemo(this) {
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
              font = FontDescriptorFragment(16.0).asProvider()
              anchorDirection = Direction.BottomRight
              anchorPointProvider = DirectionBasedBasePointProvider(anchorDirection)
            }
          }

          configurableListWithProperty("Map provider", slippyMapProvider::delegate, SlippyMapProvider.all) {
            onChange {
              slippyMapLayer.tileProvider.clear()
              this@MeisterchartsDemo.markAsDirty()
            }
          }
        }
      }
    }
  }
}
