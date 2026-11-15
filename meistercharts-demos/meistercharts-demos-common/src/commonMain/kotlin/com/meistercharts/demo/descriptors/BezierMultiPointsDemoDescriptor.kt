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
import com.meistercharts.geometry.BezierCalculations
import com.meistercharts.geometry.BezierCalculations.calculateCoordinatesOnPath
import com.meistercharts.geometry.BezierCalculations.calculateTangentOnPath
import com.meistercharts.geometry.BezierCalculations.estimateBezierLength
import com.meistercharts.canvas.paintMark
import com.meistercharts.canvas.stroke
import com.meistercharts.color.Color
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.DemoQuality
import com.meistercharts.demo.MeisterchartsDemo
import com.meistercharts.demo.MeisterchartsDemoDescriptor
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.VariabilityType
import com.meistercharts.demo.configurableDouble
import it.neckar.geometry.Coordinates
import it.neckar.open.collections.fastForEach
import it.neckar.open.collections.fastForEachIndexed
import it.neckar.open.kotlin.lang.toDegrees
import kotlin.math.atan2

/**
 *
 */
class BezierMultiPointsDemoDescriptor : MeisterchartsDemoDescriptor<Nothing> {
  override val uuid: Uuid = Uuid.parse("579a7b96-ff4d-4ef6-b846-69ea622593ce")
  override val name: String = "Bezier: Multi Coordinates"

  override val category: DemoCategory = DemoCategory.Primitives
  override val quality: DemoQuality = DemoQuality.High
  override val variabilityType: VariabilityType = VariabilityType.Stable

  override fun prepareDemo(configuration: PredefinedConfiguration<Nothing>?): MeisterchartsDemo {

    return MeisterchartsDemo(this) {
      meistercharts {
        configure {
          layers.addClearBackground()

          val layer = object : AbstractLayer() {
            override val type: LayerType = LayerType.Content

            var percentage: Double = 0.5

            val coordinatess = listOf(
              Coordinates(0.0, 0.0),
              Coordinates(100.0, 200.0),
              Coordinates(200.0, -100.0),
              Coordinates(300.0, 0.0),
              Coordinates(350.0, 70.0),
              Coordinates(450.0, 120.0),
              Coordinates(500.0, 0.0),
              Coordinates(510.0, 10.0),
            )

            var tightness: Double = 0.8

            override fun paint(paintingContext: LayerPaintingContext) {
              val gc = paintingContext.gc

              gc.translate(100.0, 300.0)

              coordinatess.fastForEach { coordinates ->
                gc.paintMark(coordinates.x, coordinates.y, color = Color.orangered())
              }

              val controlCoordinates = BezierCalculations.generateControlCoordinates(coordinatess, tightness = tightness)

              controlCoordinates.fastForEach { coordinates ->
                gc.paintMark(coordinates.x, coordinates.y, color = Color.blue())
              }


              gc.beginPath()
              coordinatess.fastForEachIndexed { index, coordinates ->
                if (index == 0) {
                  // An den Anfang der Kurve bewegen
                  gc.moveTo(coordinates.x, coordinates.y)
                } else {
                  // Kontrollpunkte für den aktuellen Punkt
                  val cp1 = controlCoordinates[(index - 1) * 2]
                  val cp2 = controlCoordinates[(index - 1) * 2 + 1]

                  // Zeichnen Sie den Bézier-Kurvenabschnitt
                  gc.bezierCurveTo(cp1.x, cp1.y, cp2.x, cp2.y, coordinates.x, coordinates.y)


                  try {
                    val lengthOfBezier = estimateBezierLength(coordinates, cp1, cp2, coordinatess[index + 1], precision = 100)
                    println("lengthOfBezier: $lengthOfBezier")
                  } catch (_: Exception) {
                  }
                }

                gc.stroke(Color.orangered)
                gc.lineWidth = 2.0
                gc.stroke()
              }

              calculateCoordinatesOnPath(percentage, coordinatess, controlCoordinates).let { coordinates ->
                gc.paintMark(coordinates.x, coordinates.y, color = Color.green())
              }

              val tangent = calculateTangentOnPath(percentage, coordinatess, controlCoordinates)
              //gc.paintMark(tangent.x, tangent.y, color = Color.green)


              val angleInDegrees = atan2(tangent.y, tangent.x).toDegrees()
              println("angleInDegrees: $angleInDegrees")


            }
          }

          layers.addLayer(
            layer
          )

          configurable("Tightness", layer::tightness) {
            max = 1.0
          }

          configurable("Location %", layer::percentage) {
            max = 1.0
          }
        }
      }
    }
  }
}
