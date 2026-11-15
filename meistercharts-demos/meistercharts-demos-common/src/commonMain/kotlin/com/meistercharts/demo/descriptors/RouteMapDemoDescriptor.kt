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
import com.meistercharts.algorithms.layers.AbstractLayer
import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.algorithms.layers.LayerType
import com.meistercharts.algorithms.layers.addClearBackground
import com.meistercharts.annotations.ContentAreaRelative
import com.meistercharts.annotations.Window
import com.meistercharts.calc.contentAreaRelative2WindowX
import com.meistercharts.calc.contentAreaRelative2WindowY
import com.meistercharts.canvas.CanvasRenderingContext
import com.meistercharts.canvas.circle
import com.meistercharts.canvas.fill
import com.meistercharts.canvas.rectCenter
import com.meistercharts.color.Color
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.DemoQuality
import com.meistercharts.demo.MeisterchartsDemo
import com.meistercharts.demo.MeisterchartsDemoDescriptor
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.VariabilityType
import it.neckar.geometry.Coordinates
import it.neckar.geometry.Direction
import com.meistercharts.style.MaterialColor
import it.neckar.open.collections.fastForEach
import it.neckar.open.collections.fastZipWithNext

class RouteMapDemoDescriptor : MeisterchartsDemoDescriptor<Nothing> {
  override val uuid: Uuid = Uuid.parse("e3de93cc-7eec-429c-bddc-83579a30c679")
  override val name: String = "Route Map"

  override val category: DemoCategory = DemoCategory.Layers
  override val quality: DemoQuality = DemoQuality.High
  override val variabilityType: VariabilityType = VariabilityType.Stable

  override fun prepareDemo(configuration: PredefinedConfiguration<Nothing>?): MeisterchartsDemo {
    return MeisterchartsDemo(this) {
      meistercharts {
        configure {
          layers.addClearBackground()


          val herrenberg = Station(
            name = "Herrenberg", location = Coordinates(0.09712, 0.131),
            style = StationStyle(StationSize.Single, Shape.Triangle, MaterialColor.CYAN_600, StationOrientation.North)
          )
          val entringen = Station(
            name = "Entringen", location = Coordinates(0.28289, 0.131),
            style = StationStyle(StationSize.Single, Shape.Square, MaterialColor.LIGHT_BLUE_600, StationOrientation.North)
          )
          val tübingenHbf = Station(
            name = "Tübingen Hbf", location = Coordinates(0.45, 0.131),
            style = StationStyle(StationSize.Single, Shape.Circle, MaterialColor.BLUE_600, StationOrientation.North)
          )
          val tübingenGüterbahnhof = Station(
            name = "TÜ Güterbahnhof", location = Coordinates(0.49693, 0.14384),
            style = StationStyle(StationSize.Single, Shape.Circle, MaterialColor.BLUE_600, StationOrientation.North)
          )
          val wannweil = Station(
            name = "Wannweil", location = Coordinates(0.60544, 0.13757),
            style = StationStyle(StationSize.Single, Shape.Circle, MaterialColor.BLUE_600, StationOrientation.North)
          )
          val reutlingenHbf = Station(
            name = "Reutlingen Hbf", location = Coordinates(0.73750, 0.131),
            style = StationStyle(StationSize.Single, Shape.Square, MaterialColor.LIGHT_BLUE_600, StationOrientation.North)
          )
          val metzingen = Station(
            //TODO 45°
            name = "Metzingen", location = Coordinates(0.78562, 0.10702),
            style = StationStyle(StationSize.Single, Shape.Square, MaterialColor.LIGHT_BLUE_600, StationOrientation.North)
          )
          val plochingen = Station(
            //TODO 45°
            name = "Plochingen", location = Coordinates(0.88102, 0.03303),
            style = StationStyle(StationSize.Single, Shape.Square, MaterialColor.LIGHT_BLUE_600, StationOrientation.North)
          )
          val badUrach = Station(
            name = "Bad Urach", location = Coordinates(0.91590, 0.11678),
            style = StationStyle(StationSize.Single, Shape.Square, MaterialColor.LIGHT_BLUE_600, StationOrientation.North)
          )


          val mössingen = Station(
            name = "Mössingen", location = Coordinates(0.45, 0.31),
            style = StationStyle(StationSize.Single, Shape.Square, MaterialColor.AMBER_600, StationOrientation.North)
          )
          val hechingen = Station(
            name = "Hechingen", location = Coordinates(0.44753, 0.397),
            style = StationStyle(StationSize.Single, Shape.Square, MaterialColor.LIGHT_BLUE_600, StationOrientation.North)
          )

          val stations = listOf(
            herrenberg,
            entringen,
            tübingenHbf,
            tübingenGüterbahnhof,
            wannweil,
            reutlingenHbf,
            metzingen,
            entringen,
            plochingen,
            badUrach,
            mössingen,
            hechingen,
          )

          val lines = listOf(
            Line("S1", listOf(hechingen, mössingen, tübingenHbf, tübingenGüterbahnhof), LineStyle(MaterialColor.GREEN_A700, LineOffset.HalfLeft)),
            Line("S2", listOf(mössingen, tübingenHbf, herrenberg, entringen), LineStyle(MaterialColor.LIME_A700, LineOffset.HalfRight)),
          )

          layers.addLayer(object : AbstractLayer() {
            override val type: LayerType = LayerType.Content

            override fun paint(paintingContext: LayerPaintingContext) {
              val gc: CanvasRenderingContext = paintingContext.gc
              val chartCalculator = paintingContext.chartCalculator

              lines.fastForEach { line ->
                gc.beginPath()

                line.stations.fastZipWithNext { startStation, endStation ->
                  @Window val x1 = startStation.location.x.contentAreaRelative2WindowX(chartCalculator)
                  @Window val y1 = startStation.location.y.contentAreaRelative2WindowY(chartCalculator)

                  gc.moveTo(x1, y1)
                  gc.lineTo(
                    endStation.location.x.contentAreaRelative2WindowX(chartCalculator),
                    endStation.location.y.contentAreaRelative2WindowY(chartCalculator)
                  )
                }

                gc.lineWidth = 5.0
                gc.stroke(line.style.color)
                gc.stroke()
              }


              stations.fastForEach { station ->
                gc.beginPath()

                @ContentAreaRelative val x = chartCalculator.contentAreaRelative2windowX(station.location.x)
                @ContentAreaRelative val y = chartCalculator.contentAreaRelative2windowY(station.location.y)

                when (station.style.shape) {
                  Shape.Circle -> {
                    gc.circle(x, y, 20.0) //TODO
                  }

                  Shape.Square -> {
                    gc.rectCenter(x, y, (station.style.size.ordinal + 1) * 10.0, (station.style.size.ordinal + 1) * 10.0)
                  }

                  Shape.Triangle -> {
                    // Wählen Sie drei Punkte für Ihr Dreieck
                    gc.moveTo(x, y - (station.style.size.ordinal + 1) * 10.0)
                    gc.lineTo(x - (station.style.size.ordinal + 1) * 10.0, y + (station.style.size.ordinal + 1) * 10.0)
                    gc.lineTo(x + (station.style.size.ordinal + 1) * 10.0, y + (station.style.size.ordinal + 1) * 10.0)
                    gc.closePath()
                  }
                }

                gc.fill(Color.white)
                gc.fill()

                gc.lineWidth = 2.0
                gc.stroke(station.style.color)
                gc.stroke()

                gc.fill(Color.black)
                gc.fillText(station.name, x, y, Direction.TopLeft)
              }
            }
          })
        }
      }
    }
  }


  data class Station(
    val name: String,
    val location: @ContentAreaRelative Coordinates,
    val style: StationStyle,
  )

  data class StationStyle(
    val size: StationSize,
    val shape: Shape,
    val color: Color,
    val orientation: StationOrientation,
  )

  data class Line(
    val name: String,
    val stations: List<Station>,
    val style: LineStyle,
  ) {
    init {
      require(stations.size >= 2) { "A line must have at least two stations" }
    }
  }

  data class LineStyle(
    val color: Color,
    val offset: LineOffset,
  )

  enum class LineOffset(val factor: Double) {
    None(0.0),
    HalfLeft(-0.5),
    Left(-1.0),

    HalfRight(0.5),
    Right(1.0),
  }

  enum class StationOrientation {
    North,
    NorthEast,
    East,
    South,
    West,
  }

  enum class StationSize {
    Single,
    Double,
    Triple,
  }

  enum class Shape {
    Circle,
    Square,
    Triangle,
    // Weitere Formen hinzufügen je nach Bedarf
  }

}
