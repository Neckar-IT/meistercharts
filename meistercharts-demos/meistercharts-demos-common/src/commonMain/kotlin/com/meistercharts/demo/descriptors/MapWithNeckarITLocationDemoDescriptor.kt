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


import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.algorithms.layers.slippymap.PaintableOnSlippyMap
import com.meistercharts.algorithms.painter.UrlPaintable
import com.meistercharts.canvas.paintLocation
import com.meistercharts.canvas.paintable.CombinedPaintable
import com.meistercharts.canvas.paintable.Paintable
import com.meistercharts.charts.MapWithPaintablesGestalt
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.DemoQuality
import com.meistercharts.demo.MeisterchartsDemo
import com.meistercharts.demo.MeisterchartsDemoDescriptor
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.VariabilityType
import com.meistercharts.maps.SlippyMapCenter
import com.meistercharts.resources.svg.SvgPaintableProviders
import it.neckar.geometry.Distance
import it.neckar.geometry.Rectangle
import it.neckar.geometry.Size
import it.neckar.open.http.Url
import it.neckar.open.provider.asSizedProvider
import kotlin.uuid.Uuid

/**
 *
 */
class MapWithNeckarITLocationDemoDescriptor : MeisterchartsDemoDescriptor<Nothing> {
  override val uuid: Uuid = Uuid.parse("c0333a14-e521-4ead-99df-b872c7230247")
  override val name: String = "Map with Neckar IT logo"
  override val description: String = "How to add a Neckar IT logo to a map"
  override val category: DemoCategory = DemoCategory.Layers
  override val quality: DemoQuality = DemoQuality.High

  /**
   * OpenStreetMap returns different tiles - they have very small differences sometimes.
   */
  override val variabilityType: VariabilityType = VariabilityType.Unstable


  override fun prepareDemo(configuration: PredefinedConfiguration<Nothing>?): MeisterchartsDemo {
    return MeisterchartsDemo(this) {
      meistercharts {

        val logoSize = Size(65, 45)
        val logo = UrlPaintable.fixedSize(Url.absolute("https://neckar.it/logo/nit-logo-n-pos.png"), logoSize)

        val mapMarker = SvgPaintableProviders.mapMarker

        val markerSize = Size.PX_120
        val secondaryOffset = Distance.of(
          -10.0,
          -20.0
        )

        /**
         * ATTENTION:
         * This code is just a POC. It is not production ready currently!
         */

        val combinedPaintable = CombinedPaintable(mapMarker.get(markerSize), logo, secondaryOffset).atBottomCenter()
        val neckarItLogoPaintable = PaintableOnSlippyMap(SlippyMapCenter.neckarItCenter.latitude, SlippyMapCenter.neckarItCenter.longitude, combinedPaintable)

        val mapGestalt = MapWithPaintablesGestalt(MapWithPaintablesGestalt.Configuration(chartId).also {
          it.paintables = listOf(neckarItLogoPaintable).asSizedProvider()
        })

        mapGestalt.configure(this)
      }
    }
  }
}

private fun Paintable.atBottomCenter(): Paintable {
  val originalPaintable = this

  return object : Paintable {
    override fun boundingBox(paintingContext: LayerPaintingContext): Rectangle {
      return originalPaintable.boundingBox(paintingContext).let {
        it.move(-it.getWidth() / 2.0, -it.getHeight())
      }
    }

    override fun paint(paintingContext: LayerPaintingContext, x: Double, y: Double) {
      val boundingBox = boundingBox(paintingContext)

      paintingContext.gc.translate(-boundingBox.getWidth() / 2.0, -boundingBox.getHeight())
      paintingContext.gc.paintLocation(x, y)
      originalPaintable.paint(paintingContext, x, y)
    }
  }
}

