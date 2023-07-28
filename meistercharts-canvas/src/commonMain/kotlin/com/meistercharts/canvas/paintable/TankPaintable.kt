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
package com.meistercharts.canvas.paintable

import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.color.Color
import com.meistercharts.canvas.saved
import it.neckar.geometry.Coordinates
import it.neckar.geometry.Rectangle
import it.neckar.geometry.Size
import com.meistercharts.resources.LocalResourcePaintable
import it.neckar.open.http.Url
import it.neckar.open.unit.other.pct

/**
 * Paints a tank with a water-like liquid inside
 */
class TankPaintable : Paintable {

  /**
   * The images of which the tank is made of are scaled with this value
   */
  private val scalingFactor: Double = 0.5

  val size: Size = Size(602.0 + 356.0 + 602.0, 286.0 + 518.0 + 1892.0 + 474.0).times(scalingFactor)

  /**
   * The thickness of the wall of the tank
   */
  private val tankWallThickness = 76.0 * scalingFactor

  val boundingBox: Rectangle = Rectangle(Coordinates.origin, size)

  /**
   * How much the tank is filled
   */
  var fillLevel: @pct Double = 0.33333

  /**
   * The color to be used for the liquid
   */
  private val liquidColor = Color.web("#76c7ee")

  override fun boundingBox(paintingContext: LayerPaintingContext): Rectangle {
    return boundingBox
  }

  private val sensorUltrasonic: Paintable = LocalResourcePaintable(Url("tank/sensor-ultrasonic.png"), Size(272.0, 320.0).times(scalingFactor), Coordinates(-272.0 * scalingFactor * 0.5, -286.0 * scalingFactor))
  private val sensorVibration: Paintable = LocalResourcePaintable(Url("tank/sensor-vibration.png"), Size(273.0, 818.0).times(scalingFactor), Coordinates(-273.0 * scalingFactor * 0.5, -286.0 * scalingFactor))
  private val tankCornerBottomLeft: Paintable = LocalResourcePaintable(Url("tank/tank-corner-bottom.png"), Size(602.0, 474.0).times(scalingFactor))
  private val tankCornerBottomRight: Paintable = tankCornerBottomLeft.mirrorOnY()
  private val tankCornerTopLeft: Paintable = LocalResourcePaintable(Url("tank/tank-corner-top.png"), Size(602.0, 518.0).times(scalingFactor))
  private val tankCornerTopRight: Paintable = tankCornerTopLeft.mirrorOnY()
  private val tankSideBottom: Paintable = LocalResourcePaintable(Url("tank/tank-side-bottom.png"), Size(356.0, 474.0).times(scalingFactor))
  private val tankSideTop: Paintable = LocalResourcePaintable(Url("tank/tank-side-top.png"), Size(356.0, 518.0).times(scalingFactor))
  private val tankSideVertical: Paintable = LocalResourcePaintable(Url("tank/tank-side-vertical.png"), Size(80.0, 1892.0).times(scalingFactor))
  private val waves: Paintable = LocalResourcePaintable(Url("tank/waves.png"), Size(1400.0, 82.0).times(scalingFactor), Coordinates(-1400.0 * scalingFactor * 0.5, -82.0 * scalingFactor))

  override fun paint(paintingContext: LayerPaintingContext, x: Double, y: Double) {
    val sensorUltrasonicBoundingBox = sensorUltrasonic.boundingBox(paintingContext)
    val sensorVibrationBoundingBox = sensorVibration.boundingBox(paintingContext)
    val tankCornerTopBoundingBox = tankCornerTopLeft.boundingBox(paintingContext)
    val tankCornerBottomBoundingBox = tankCornerBottomLeft.boundingBox(paintingContext)
    val tankSideTopBoundingBox = tankSideTop.boundingBox(paintingContext)
    val tankSideVerticalBoundingBox = tankSideVertical.boundingBox(paintingContext)
    val tankSideBottomBoundingBox = tankSideBottom.boundingBox(paintingContext)
    //val wavesBoundingBox = waves.boundingBox(paintingContext)

    val gc = paintingContext.gc
    gc.translate(x, y)

    val innerTankLeft = tankSideVerticalBoundingBox.right
    val innerTankTop = tankCornerTopBoundingBox.getHeight() - sensorUltrasonicBoundingBox.getY()
    val innerTankWidth = size.width - 2.0 * tankSideVerticalBoundingBox.getWidth()
    val innerTankHeight = tankSideVerticalBoundingBox.getHeight() + tankCornerBottomBoundingBox.getHeight() - tankWallThickness
    val liquidMinHeight = tankCornerBottomBoundingBox.getHeight() * 0.3
    val liquidHeight = liquidMinHeight + (innerTankHeight - liquidMinHeight) * fillLevel
    val liquidTop = innerTankTop + innerTankHeight - liquidHeight

    gc.fill(liquidColor)
    gc.beginPath()
    // upper left corner
    gc.moveTo(innerTankLeft, liquidTop)
    // bottom left corner
    gc.lineTo(innerTankLeft, liquidTop + liquidHeight - tankCornerBottomBoundingBox.getHeight() * 0.3)
    // bottom left
    gc.lineTo(innerTankLeft + tankCornerBottomBoundingBox.getWidth() * 0.25, liquidTop + liquidHeight)
    // bottom right
    gc.lineTo(innerTankLeft + innerTankWidth - tankCornerBottomBoundingBox.getWidth() * 0.25, liquidTop + liquidHeight)
    // bottom right corner
    gc.lineTo(innerTankLeft + innerTankWidth, liquidTop + liquidHeight - tankCornerBottomBoundingBox.getHeight() * 0.3)
    // upper right corner
    gc.lineTo(innerTankLeft + innerTankWidth, liquidTop)
    gc.closePath()
    gc.fill()

    waves.paint(paintingContext, size.width * 0.5, liquidTop + 1.0) // + 1.0 because of rounding errors -> ensure there is no gap between the waves and the liquid

    //gc.stroke(Color.red)

    // top left corner
    tankCornerTopLeft.paint(paintingContext, 0.0, -sensorUltrasonicBoundingBox.getY())
    //gc.strokeRect(tankCornerTopBoundingBox.move(0.0, -sensorUltrasonicBoundingBox.y))

    // top right corner
    gc.saved { tankCornerTopRight.paint(paintingContext, tankCornerTopBoundingBox.getWidth() * 2.0 + tankSideTopBoundingBox.getWidth(), -sensorUltrasonicBoundingBox.getY()) }

    // sensor on left side
    sensorUltrasonic.paint(paintingContext, tankCornerTopBoundingBox.right + sensorUltrasonicBoundingBox.left, -sensorUltrasonicBoundingBox.getY())
    //gc.strokeRect(sensorUltrasonicBoundingBox.move(tankCornerTopBoundingBox.right + sensorUltrasonicBoundingBox.left, -sensorUltrasonicBoundingBox.y))

    // sensor on right side
    sensorVibration.paint(paintingContext, tankCornerTopBoundingBox.right + tankSideTopBoundingBox.getWidth() - sensorVibrationBoundingBox.left, -sensorVibrationBoundingBox.getY())
    //gc.strokeRect(sensorVibrationBoundingBox.move(tankCornerTopBoundingBox.right + tankSideTopBoundingBox.width - sensorVibrationBoundingBox.left, -sensorVibrationBoundingBox.y))

    // top side
    tankSideTop.paint(paintingContext, tankCornerTopBoundingBox.right, -sensorUltrasonicBoundingBox.getY())
    //gc.strokeRect(tankSideTopBoundingBox.move(tankCornerTopBoundingBox.right, -sensorUltrasonicBoundingBox.y))

    // left side
    tankSideVertical.paint(paintingContext, 0.0, tankCornerTopBoundingBox.getHeight() - sensorUltrasonicBoundingBox.getY())
    //gc.strokeRect(tankSideVerticalBoundingBox.move(0.0, tankCornerTopBoundingBox.height - sensorUltrasonicBoundingBox.y))

    // right side
    tankSideVertical.paint(paintingContext, tankCornerTopBoundingBox.right + tankSideTopBoundingBox.getWidth() + tankCornerTopBoundingBox.getWidth() - tankSideVerticalBoundingBox.getWidth(), tankCornerTopBoundingBox.getHeight() - sensorUltrasonicBoundingBox.getY())
    //gc.strokeRect(tankSideVerticalBoundingBox.move(tankCornerTopBoundingBox.right + tankSideTopBoundingBox.width + tankCornerTopBoundingBox.width - tankSideVerticalBoundingBox.width, tankCornerTopBoundingBox.height - sensorUltrasonicBoundingBox.y))

    // bottom left corner
    tankCornerBottomLeft.paint(paintingContext, 0.0, tankCornerTopBoundingBox.bottom + tankSideVerticalBoundingBox.getHeight() - sensorUltrasonicBoundingBox.getY())
    //gc.strokeRect(tankCornerBottomBoundingBox.move(0.0, tankCornerTopBoundingBox.bottom + tankSideVerticalBoundingBox.height - sensorUltrasonicBoundingBox.y))

    // bottom right corner
    gc.saved { tankCornerBottomRight.paint(paintingContext, tankCornerBottomBoundingBox.getWidth() * 2.0 + tankSideBottomBoundingBox.getWidth(), tankCornerTopBoundingBox.bottom + tankSideVerticalBoundingBox.getHeight() - sensorUltrasonicBoundingBox.getY()) }

    // bottom
    tankSideBottom.paint(paintingContext, tankCornerBottomBoundingBox.getWidth(), tankCornerTopBoundingBox.bottom + tankSideVerticalBoundingBox.getHeight() - sensorUltrasonicBoundingBox.getY())
    //gc.strokeRect(tankSideBottomBoundingBox.move(tankCornerBottomBoundingBox.width, tankCornerTopBoundingBox.bottom + tankSideVerticalBoundingBox.height - sensorUltrasonicBoundingBox.y))

  }

}
