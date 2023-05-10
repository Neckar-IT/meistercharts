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

import com.meistercharts.algorithms.layers.AbstractLayer
import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.algorithms.layers.LayerType
import com.meistercharts.algorithms.layers.addClearBackground
import com.meistercharts.algorithms.layers.linechart.Dashes
import com.meistercharts.algorithms.layers.linechart.apply
import com.meistercharts.algorithms.layers.slippymap.OpenStreetMapDe
import com.meistercharts.algorithms.layers.slippymap.PaintableOnSlippyMap
import com.meistercharts.algorithms.layers.slippymap.SlippyMapLayer
import com.meistercharts.algorithms.layers.slippymap.domainRelative2latitude
import com.meistercharts.algorithms.layers.slippymap.domainRelative2longitude
import com.meistercharts.algorithms.painter.Color
import com.meistercharts.annotations.DomainRelative
import com.meistercharts.annotations.Window
import com.meistercharts.annotations.Zoomed
import com.meistercharts.canvas.CanvasRenderingContext
import com.meistercharts.canvas.paintable.Paintable
import com.meistercharts.canvas.strokeCross45Degrees
import com.meistercharts.charts.SlippyMapBaseGestalt
import com.meistercharts.demo.ChartingDemo
import com.meistercharts.demo.ChartingDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.model.MapCoordinates
import com.meistercharts.model.Rectangle
import it.neckar.open.collections.Cache
import it.neckar.open.collections.cache
import it.neckar.open.collections.fastForEachIndexed
import it.neckar.open.collections.fastForEachIndexedReverse
import it.neckar.open.kotlin.lang.toRadians
import it.neckar.open.concurrent.CoAsync
import it.neckar.ktor.client.createHttpClient
import it.neckar.open.observable.ObservableObject
import it.neckar.open.unit.other.deg
import it.neckar.open.unit.other.pct
import it.neckar.eusciencehub.api.EuScienceHubClient
import it.neckar.eusciencehub.api.HeightProfileForLocation
import it.neckar.eusciencehub.api.HeightProfilesForLocation
import it.neckar.eusciencehub.api.HorizonProfileCalcOptions
import it.neckar.eusciencehub.result.horizon.HorizonResult
import it.neckar.logging.LoggerFactory
import kotlinx.coroutines.*
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 *
 */
class PvHorizonDemoDescriptor : ChartingDemoDescriptor<Nothing> {
  override val name: String = "PV Horizon"
  override val category: DemoCategory = DemoCategory.Layers

  private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)

  override fun createDemo(configuration: PredefinedConfiguration<Nothing>?): ChartingDemo {
    return ChartingDemo {
      meistercharts {

        SlippyMapBaseGestalt().configure(this)

        configure {
          layers.addClearBackground()

          val slippyMapLayer = SlippyMapLayer(chartId, OpenStreetMapDe)
          layers.addLayer(slippyMapLayer)

          layers.addLayer(object : AbstractLayer() {
            override val type: LayerType = LayerType.Content

            val horizonService: HorizonService = HorizonService().also {
              it.currentResultProperty.consume {
                //Repaint when the new result is available
                this@ChartingDemo.markAsDirty()
              }
            }

            val horizonPaintable = PaintableOnSlippyMap(MapCoordinates.neckarIt, HorizonPaintable())

            override fun paint(paintingContext: LayerPaintingContext) {
              val gc = paintingContext.gc
              val chartCalculator = paintingContext.chartCalculator

              @DomainRelative val center = chartCalculator.window2domainRelative(gc.center)

              val latitude = domainRelative2latitude(center.y)
              val longitude = domainRelative2longitude(center.x)

              //Request the current coordinates
              scope.launch {
                horizonService.request(MapCoordinates(latitude, longitude))
              }

              horizonService.currentResult?.let {
                horizonPaintable.location = it.horizon.location
                horizonPaintable.paintable.profiles = it
                horizonPaintable.paint(paintingContext)
              }
            }
          })
        }
      }
    }
  }
}

class HorizonPaintable : Paintable {
  var profiles: HeightProfilesForLocation? = null

  val width: @Zoomed Double = 400.0
  val height: @Zoomed Double = 400.0

  override fun boundingBox(paintingContext: LayerPaintingContext): Rectangle {
    return Rectangle.centered(width, height)
  }

  override fun paint(paintingContext: LayerPaintingContext, x: Double, y: Double) {
    val gc = paintingContext.gc

    gc.translate(x, y)

    //Fill background
    gc.fill(Color.white.withAlpha(0.65))
    gc.fillOvalCenter(0.0, 0.0, width, height)

    gc.strokeCross45Degrees()

    profiles?.horizon?.let {
      it.paintHorizon(gc)
    }

    //paint the summer sun
    profiles?.sunSummer?.let {
      gc.beginPath()
      //First paint the outer circle
      it.appendProfile(gc)

      Dashes.SmallDashes.apply(gc)
      gc.stroke(Color.black)
      gc.stroke()
    }

    //Paint the winter sun
    profiles?.sunWinter?.let {
      gc.beginPath()
      //First paint the outer circle
      it.appendProfile(gc)

      gc.stroke(Color.black)
      Dashes.Dotted.apply(gc)
      gc.stroke()
    }

  }

  /**
   * Paint the horizon
   */
  private fun HeightProfileForLocation.paintHorizon(gc: CanvasRenderingContext) {
    gc.beginPath()
    //First paint the outer circle
    gc.ovalCenter(0.0, 0.0, width, height)

    appendProfile(gc)

    gc.fill(Color.darkgray)
    gc.fill()
    gc.stroke(Color.black)
    gc.stroke()
  }

  /**
   * Appends a profile to the path
   */
  private fun HeightProfileForLocation.appendProfile(gc: CanvasRenderingContext) {
    azimuthFromSouths.fastForEachIndexedReverse { index, azimuth: @deg Double, isFirst: Boolean ->
      @deg val currentHeight = heights[index]
      @pct val relativeHeight = 1.0 / 90 * currentHeight

      @Window val currentX = cos(azimuth.toRadians() + PI / 2.0) * (width / 2.0) * (1 - relativeHeight)
      @Window val currentY = sin(azimuth.toRadians() + PI / 2.0) * (height / 2.0) * (1 - relativeHeight)

      if (isFirst) {
        gc.moveTo(currentX, currentY)
      } else {
        gc.lineTo(currentX, currentY)
      }
    }
  }
}


class HorizonService {
  val client: EuScienceHubClient = EuScienceHubClient(createHttpClient())

  /**
   * Async that is used to request the horizon information
   */
  val async: CoAsync = CoAsync().also {
    it.start(CoroutineScope(Dispatchers.IO))
  }

  val resultCache: Cache<MapCoordinates, HeightProfilesForLocation> = cache("horizonServiceCache", 500)

  val currentResultProperty: ObservableObject<HeightProfilesForLocation?> = ObservableObject(null)
  var currentResult: HeightProfilesForLocation? by currentResultProperty

  suspend fun request(coordinates: MapCoordinates) {
    async.last {
      currentResult = resultCache.getOrStore(coordinates) {
        logger.info("Query for $coordinates")
        client.queryHorizon(HorizonProfileCalcOptions(coordinates)).extractHorizonProfile()
      }
    }
  }

  companion object {
    val logger = LoggerFactory.getLogger("com.meistercharts.demo.descriptors.HorizonService")
  }
}

private fun HorizonResult.extractHorizonProfile(): HeightProfilesForLocation {
  //Calculate the horizon profile
  val horizon = outputs.horizonProfile.let { profile ->
    val azimuths = DoubleArray(profile.size)
    val heights = DoubleArray(profile.size)

    profile.fastForEachIndexed { i, v ->
      azimuths[i] = v.azimuth
      heights[i] = v.horizonHeight
    }

    HeightProfileForLocation(
      inputs.location.coordinates,
      azimuths, heights
    )
  }

  val sunSummer = outputs.summerSolstice.let { profile ->
    val azimuths = DoubleArray(profile.size)
    val heights = DoubleArray(profile.size)

    profile.fastForEachIndexed { i, v ->
      azimuths[i] = v.sunAzimuth
      heights[i] = v.sunHeight
    }

    HeightProfileForLocation(
      inputs.location.coordinates,
      azimuths, heights
    )
  }

  val sunWinter = outputs.winterSolstice.let { profile ->
    val azimuths = DoubleArray(profile.size)
    val heights = DoubleArray(profile.size)

    profile.fastForEachIndexed { i, v ->
      azimuths[i] = v.sunAzimuth
      heights[i] = v.sunHeight
    }

    HeightProfileForLocation(
      inputs.location.coordinates,
      azimuths, heights
    )
  }

  sunWinter.heights.fastForEachIndexed { index, sunHeight ->
    val horizonHeight = horizon.heights[index]
    if (horizonHeight < sunHeight) {
      HorizonService.logger.debug("--> Sun visible @ ${sunWinter.azimuthFromSouths[index]}°")
    } else {
      HorizonService.logger.debug("--> Sun hidden @ ${sunWinter.azimuthFromSouths[index]}°")
    }
  }

  return HeightProfilesForLocation(horizon, sunSummer, sunWinter)
}
