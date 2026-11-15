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
package com.meistercharts.custom.rainsensor

import com.meistercharts.algorithms.layers.AbstractLayer
import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.algorithms.layers.LayerType
import com.meistercharts.canvas.paintable.ObjectFit
import com.meistercharts.color.Color
import com.meistercharts.annotations.Zoomed
import com.meistercharts.canvas.ConfigurationDsl
import com.meistercharts.canvas.saved
import it.neckar.geometry.Direction
import it.neckar.open.kotlin.lang.distance
import it.neckar.open.provider.DoubleProvider
import com.meistercharts.resources.LocalResourcePaintable
import it.neckar.open.unit.other.deg

/**
 * Demonstration of a rain sensor
 */
class RainSensorLayer(
  val configuration: Configuration,
  additionalConfiguration: Configuration.() -> Unit = {},
) : AbstractLayer() {

  constructor(
    openAngle: @deg DoubleProvider,
    sensorHeated: () -> Boolean,
    additionalConfiguration: Configuration.() -> Unit = {},
  ): this(Configuration(openAngle, sensorHeated), additionalConfiguration)

  init {
    configuration.additionalConfiguration()
  }

  override val type: LayerType = LayerType.Content


  override fun paint(paintingContext: LayerPaintingContext) {
    val gc = paintingContext.gc
    val chartCalculator = paintingContext.chartCalculator

    gc.saved {
      configuration.roofResource.paintInBoundingBox(
        paintingContext,
        chartCalculator.domainRelative2windowX(0.0),
        chartCalculator.domainRelative2windowY(0.0),
        Direction.TopLeft,
        0.0,
        0.0,
        chartCalculator.domainRelativeDelta2ZoomedX(1.0),
        chartCalculator.domainRelativeDelta2ZoomedY(1.0),
        objectFit = ObjectFit.Fill
      )
    }

    gc.saved {
      if (configuration.sensorHeated()) {
        gc.saved {
          configuration.sensorHeatResource.paintInBoundingBox(
            paintingContext,
            chartCalculator.domainRelative2windowX(RainSensorResources.windowOrigin.x),
            chartCalculator.domainRelative2windowY(RainSensorResources.windowOrigin.y),
            Direction.BottomRight,
            0.0,
            0.0,
            chartCalculator.domainRelativeDelta2ZoomedX(RainSensorResources.sensorWithHeatSize.width),
            chartCalculator.domainRelativeDelta2ZoomedY(RainSensorResources.sensorWithHeatSize.height),
            objectFit = ObjectFit.Fill
          )
        }
      } else {
        configuration.sensorResource.paintInBoundingBox(
          paintingContext,
          chartCalculator.domainRelative2windowX(RainSensorResources.windowOrigin.x),
          chartCalculator.domainRelative2windowY(RainSensorResources.windowOrigin.y),
          Direction.BottomRight,
          0.0,
          0.0,
          chartCalculator.domainRelativeDelta2ZoomedX(RainSensorResources.sensorSize.width),
          chartCalculator.domainRelativeDelta2ZoomedY(RainSensorResources.sensorSize.height),
          objectFit = ObjectFit.Fill
        )
      }
    }


    gc.saved {
      gc.translate(
        chartCalculator.domainRelative2windowX(RainSensorResources.windowOrigin.x),
        chartCalculator.domainRelative2windowY(RainSensorResources.windowOrigin.y)
      )

      @Zoomed val deltaX = chartCalculator.domainRelativeDelta2ZoomedX(RainSensorResources.windowDelta.x)
      @Zoomed val deltaY = chartCalculator.domainRelativeDelta2ZoomedY(RainSensorResources.windowDelta.y)
      @Zoomed val height = chartCalculator.domainRelativeDelta2ZoomedY(RainSensorResources.windowHeight)
      @Zoomed val distance = distance(deltaX, deltaY)

      gc.saved {
        gc.rotateDegrees(RainSensorResources.RoofAngle)
        gc.fill(configuration.windowBaseColor)
        gc.fillRect(0.0, 0.0, distance, height)
      }

      //Paint the white background to overlap the rain/snow
      //gc.saved {
      //  gc.beginPath()
      //  gc.moveTo(0.0, height) //bottom of the open window
      //  gc.lineTo(deltaX, deltaY)
      //
      //  gc.fill(Color.orange)
      //  gc.paintMark()
      //}

      gc.saved {
        gc.rotateDegrees(configuration.openAngle() + RainSensorResources.RoofAngle)
        gc.fill(configuration.windowBaseColor)
        gc.fillRect(0.0, 0.0, distance, height)
      }
    }
  }

  @ConfigurationDsl
  class Configuration(
    /**
     * The open angle of the window.
     * Should be between 0.0 (closed) and -30° (wide open)
     */
    var openAngle: @deg DoubleProvider,
    /**
     * Returns true if the sensor is heated
     */
    var sensorHeated: () -> Boolean,
  ) {
    /**
     * The color of the roof
     */
    val windowBaseColor: Color = Color.web("#9ED4D7")

    /**
     * The resource of the roof
     */
    val roofResource: LocalResourcePaintable = RainSensorResources.roofResource

    val sensorResource: LocalResourcePaintable = RainSensorResources.sensorResource

    val sensorHeatResource: LocalResourcePaintable = RainSensorResources.sensorWithHeatResource
  }
}

