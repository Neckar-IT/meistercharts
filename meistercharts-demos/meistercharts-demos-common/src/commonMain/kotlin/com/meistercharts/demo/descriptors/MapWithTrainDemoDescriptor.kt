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
import com.meistercharts.algorithms.layers.addTilesDebugLayer
import com.meistercharts.algorithms.layers.slippymap.SlippyMapProvider
import com.meistercharts.annotations.DomainRelative
import com.meistercharts.annotations.Window
import com.meistercharts.calc.ChartCalculator
import com.meistercharts.calc.domainRelative2WindowX
import com.meistercharts.canvas.CanvasRenderingContext
import com.meistercharts.canvas.ChartSupport
import com.meistercharts.canvas.events.CanvasMouseEventHandler
import com.meistercharts.canvas.paintMark
import com.meistercharts.canvas.paintable.ButtonColorProvider
import com.meistercharts.canvas.paintable.ButtonState
import com.meistercharts.charts.MapGestalt
import com.meistercharts.color.Color
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.DemoQuality
import com.meistercharts.demo.MeisterchartsDemo
import com.meistercharts.demo.MeisterchartsDemoDescriptor
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.VariabilityType
import com.meistercharts.demo.configurableBoolean
import com.meistercharts.demo.configurableDouble
import com.meistercharts.demo.configurableList
import com.meistercharts.demo.descriptors.MapWithTrainDemoDescriptor.Locations.controlPointsRoute1
import com.meistercharts.demo.descriptors.MapWithTrainDemoDescriptor.Locations.controlPointsRoute2
import com.meistercharts.demo.descriptors.MapWithTrainDemoDescriptor.Locations.route1
import com.meistercharts.demo.descriptors.MapWithTrainDemoDescriptor.Locations.route2
import com.meistercharts.events.EventConsumption
import it.neckar.events.MouseDownEvent
import it.neckar.geometry.Coordinates
import com.meistercharts.maps.MapCoordinates
import com.meistercharts.maps.fromDomainRelative
import com.meistercharts.maps.latitude2DomainRelative
import com.meistercharts.maps.longitude2DomainRelative
import com.meistercharts.maps.toDomainRelativeY
import com.meistercharts.style.MaterialColor
import it.neckar.open.collections.fastForEachIndexed
import it.neckar.open.unit.other.pct

/**
 * A demo descriptor for the [MapGestalt]
 */
@Suppress("NonAsciiCharacters")
class MapWithTrainDemoDescriptor : MeisterchartsDemoDescriptor<Nothing> {
  override val uuid: Uuid = Uuid.parse("d42007e0-4e6d-44a6-8c2a-2088a2414387")
  override val name: String = "Map Train"
  override val category: DemoCategory = DemoCategory.Other

  override val quality: DemoQuality = DemoQuality.Low
  override val variabilityType: VariabilityType = VariabilityType.Unstable


  object Locations {
    val Mössingen: MapCoordinates = MapCoordinates(48.40413179659085, 9.046460126797058)
    val Nehren: MapCoordinates = MapCoordinates(48.42396430526483, 9.066936110416748)
    val BetweenNehrenDusslingen1: MapCoordinates = MapCoordinates(48.43815920217683, 9.082879160802236)
    val BetweenNehrenDusslingen2: MapCoordinates = MapCoordinates(48.45068500420565, 9.068888758581494)
    val Dusslingen: MapCoordinates = MapCoordinates(48.452307438914495, 9.05847642318389)
    val Derendingen: MapCoordinates = MapCoordinates(48.50341247909833, 9.050644372860319)


    val NehrenWeiche = MapCoordinates(48.4379314317271, 9.082750414769976)
    val Höhnisch = MapCoordinates(48.44447942475893, 9.078587626377924)
    val Gomaringen1 = MapCoordinates(48.44948949282545, 9.079574679295945)
    val Gomaringen2 = MapCoordinates(48.453403264605114, 9.089992379108992)
    val Gomaringen3 = MapCoordinates(48.46065361914569, 9.102877711217474)
    val Ohmenhausen = MapCoordinates(48.4731809862556, 9.124099348943133)
    val Betzingen = MapCoordinates(48.49504133690874, 9.167679881015403)

    val route1 = listOf(Mössingen, Nehren, BetweenNehrenDusslingen1, BetweenNehrenDusslingen2, Dusslingen, Derendingen)
    val route2 = listOf(NehrenWeiche, Höhnisch, Gomaringen1, Gomaringen2, Gomaringen3, Ohmenhausen, Betzingen)


    val controlPointsRoute1: List<MapCoordinates> = listOf(
      Mössingen,
      Mössingen,

      MapCoordinates(48.415604973692176, 9.049405192295893), // between Mössingen and Nehren
      MapCoordinates(48.419136852175576, 9.052194689671381), // between Mössingen and Nehren

      MapCoordinates(48.42793699795106, 9.077686404149944),
      MapCoordinates(48.42967406347825, 9.079488848608008),

      MapCoordinates(48.445959723585446, 9.087556933325146),
      MapCoordinates(48.45253514490006, 9.080475901524608),

      MapCoordinates(48.44977414094432, 9.062837695043669),
      MapCoordinates(48.45003032288755, 9.061249827306852),

      MapCoordinates(48.45862590102894, 9.054855441014666),
      MapCoordinates(48.47649549046503, 9.067386721532472),
    )

    val controlPointsRoute2: List<MapCoordinates> = listOf(
      NehrenWeiche,
      NehrenWeiche,

      MapCoordinates(48.44093507245232, 9.079767798344392),
      MapCoordinates(48.44245106063594, 9.07893094913149),

      MapCoordinates(48.446842188894315, 9.07792243854297),
      MapCoordinates(48.44850744456483, 9.078802203099855),

      MapCoordinates(48.45147488020887, 9.08081922427803),
      MapCoordinates(48.452300323085005, 9.085110758701859),

      MapCoordinates(48.45425713836154, 9.093157385747617),
      MapCoordinates(48.4553813835711, 9.095453356664365),

      MapCoordinates(48.46572611705243, 9.110645388525683),
      MapCoordinates(48.47052069105327, 9.119013880650414),

      MapCoordinates(48.478088651481634, 9.130601023592106),
      MapCoordinates(48.48923937775426, 9.147338007845036),
    )
  }

  override fun prepareDemo(configuration: PredefinedConfiguration<Nothing>?): MeisterchartsDemo {
    return MeisterchartsDemo(this) {
      meistercharts {
        val gestalt = MapGestalt(chartId)

        val colors = object {
          var disabledToolbarButtonBackgroundColor = gestalt.configuration.toolbarButtonBackgroundProvider(ButtonState(enabled = false))
          var pressedToolbarButtonBackgroundColor = gestalt.configuration.toolbarButtonBackgroundProvider(ButtonState(pressed = true))
          var hoverToolbarButtonBackgroundColor = gestalt.configuration.toolbarButtonBackgroundProvider(ButtonState(hover = true))
          var defaultToolbarButtonBackgroundColor = gestalt.configuration.toolbarButtonBackgroundProvider(ButtonState())
        }

        val toolbarButtonBackgroundProvider = ButtonColorProvider { state ->
          when {
            state.disabled -> colors.disabledToolbarButtonBackgroundColor
            state.pressed -> colors.pressedToolbarButtonBackgroundColor
            state.hover -> colors.hoverToolbarButtonBackgroundColor
            else -> colors.defaultToolbarButtonBackgroundColor
          }
        }
        gestalt.configuration.toolbarButtonBackgroundProvider = toolbarButtonBackgroundProvider

        gestalt.configure(this)
        configure {
          layers.addTilesDebugLayer(debug)

          val layer = object : AbstractLayer() {
            override val type: LayerType = LayerType.Content

            var paintPoints = false
            var trainLocation: @pct Double = 0.5

            override val mouseEventHandler: CanvasMouseEventHandler = object : CanvasMouseEventHandler {
              override fun onDown(event: MouseDownEvent, chartSupport: ChartSupport): EventConsumption {
                val chartCalculator = chartSupport.chartCalculator

                @DomainRelative val domainRelativeX = chartCalculator.window2domainRelativeX(event.x)
                @DomainRelative val domainRelativeY = chartCalculator.window2domainRelativeY(event.y)

                val mapCoordinates = MapCoordinates.fromDomainRelative(domainRelativeX, domainRelativeY)
                println("Clicked on $mapCoordinates")

                return super.onDown(event, chartSupport)
              }
            }

            override fun paint(paintingContext: LayerPaintingContext) {
              val gc = paintingContext.gc
              val chartCalculator = paintingContext.chartCalculator

              val currentZoomX = paintingContext.chartState.zoomX

              val bezierEnabled = currentZoomX > 4

              addPath(gc, chartCalculator, route1, controlPointsRoute1, bezierEnabled)
              gc.lineWidth = 5.0
              gc.stroke(MaterialColor.AMBER_800)
              gc.stroke()

              addPath(gc, chartCalculator, route2, controlPointsRoute2, bezierEnabled)
              gc.lineWidth = 5.0
              gc.stroke(MaterialColor.BLUE_A700)
              gc.stroke()


              val routeWindow = route1.map {
                val x = chartCalculator.domainRelative2windowX(it.longitude2DomainRelative())
                val y = chartCalculator.domainRelative2windowY(it.latitude.toDomainRelativeY())
                Coordinates(x, y)
              }

              val controlPointsWindow = controlPointsRoute1.map {
                val x = chartCalculator.domainRelative2windowX(it.longitude2DomainRelative())
                val y = chartCalculator.domainRelative2windowY(it.latitude.toDomainRelativeY())
                Coordinates(x, y)
              }
            }

            private fun addPath(gc: CanvasRenderingContext, chartCalculator: ChartCalculator, points: List<MapCoordinates>, controlPoints: List<MapCoordinates>, bezierEnabled: Boolean) {
              gc.beginPath()
              points.fastForEachIndexed { index, location ->
                @Window val x = chartCalculator.domainRelative2windowX(location.longitude2DomainRelative())
                @Window val y = chartCalculator.domainRelative2windowY(location.latitude.toDomainRelativeY())

                if (paintPoints) {
                  gc.paintMark(x, y, label = "P $index", color = Color.blue())
                }

                if (index == 0) {
                  gc.moveTo(x, y)
                } else {
                  val controlPoint1 = controlPoints[index * 2]
                  val controlPoint2 = controlPoints[index * 2 + 1]

                  val controlPoint1X = controlPoint1.longitude2DomainRelative().domainRelative2WindowX(chartCalculator)
                  val controlPoint1Y = controlPoint1.latitude2DomainRelative().domainRelative2WindowX(chartCalculator)
                  val controlPoint2X = controlPoint2.longitude2DomainRelative().domainRelative2WindowX(chartCalculator)
                  val controlPoint2Y = controlPoint2.latitude2DomainRelative().domainRelative2WindowX(chartCalculator)

                  if (paintPoints) {
                    gc.paintMark(controlPoint1X, controlPoint1Y, label = "CP1 $index", color = Color.red())
                    gc.paintMark(controlPoint2X, controlPoint2Y, label = "CP2 $index", color = Color.orangered())
                  }

                  when (bezierEnabled) {
                    true -> gc.bezierCurveTo(
                      controlX1 = controlPoint1X,
                      controlY1 = controlPoint1Y,

                      controlX2 = controlPoint2X,
                      controlY2 = controlPoint2Y,

                      x2 = x,
                      y2 = y,
                    )

                    false -> gc.lineTo(
                      x = x,
                      y = y,
                    )
                  }
                }
              }
            }
          }
          layers.addLayer(layer)

          configurable("paintPoints", layer::paintPoints)
          configurable("Location", layer::trainLocation)

          configurableList("Map provider", gestalt.configuration.slippyMapProvider, SlippyMapProvider.all) {
            onChange {
              gestalt.configuration.slippyMapProvider = it
              this@MeisterchartsDemo.markAsDirty()
            }
          }
        }
      }
    }
  }
}

