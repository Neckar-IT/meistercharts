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
import com.meistercharts.algorithms.layers.PaintingVariables
import com.meistercharts.algorithms.layers.addClearBackground
import com.meistercharts.algorithms.layers.debug.addWhatsAtDebugLayer
import com.meistercharts.canvas.fill
import com.meistercharts.color.Color
import com.meistercharts.canvas.whatsAt
import com.meistercharts.demo.MeisterchartsDemo
import com.meistercharts.demo.MeisterchartsDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.DemoQuality
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.VariabilityType
import it.neckar.geometry.Coordinates
import it.neckar.geometry.Rectangle
import it.neckar.geometry.Size
import com.meistercharts.whatsat.ResultElementType
import com.meistercharts.whatsat.WhatsAtResultElement
import it.neckar.open.formatting.decimalFormat1digit

/**
 * A simple hello world demo
 */
class WhatsAtDemoDescriptor : MeisterchartsDemoDescriptor<Nothing> {
  override val uuid: Uuid = Uuid.parse("9575cba1-ea16-4ec9-9246-ff94c4d6f589")
  override val name: String = "Whats At"
  override val category: DemoCategory = DemoCategory.Interaction
  override val quality: DemoQuality = DemoQuality.High
  override val variabilityType: VariabilityType = VariabilityType.Unstable

  override fun prepareDemo(configuration: PredefinedConfiguration<Nothing>?): MeisterchartsDemo {
    return MeisterchartsDemo(this) {
      meistercharts {
        configure {
          layers.addClearBackground()
          layers.addLayer(WhatsAtProviderDemoLayer())
          layers.addWhatsAtDebugLayer()
        }
      }
    }
  }

  /**
   * Provides the information for WhatsAt
   */
  private class WhatsAtProviderDemoLayer : AbstractLayer() {
    override val type: LayerType = LayerType.Content


    /**
     * The diameter of the dot
     */
    var dotDiameter: Double = 20.0

    override fun paintingVariables(): PaintingVariables {
      return paintingVariables
    }

    private val paintingVariables = object : PaintingVariables {
      //The location for the dot
      var location: Coordinates = Coordinates.none

      override fun calculate(paintingContext: LayerPaintingContext) {
        location = paintingContext.gc.center
      }
    }

    override fun initialize(paintingContext: LayerPaintingContext) {
      val whatsAt = paintingContext.chartSupport.whatsAt

      whatsAt.registerResolverAsFirst { where, precision, chartSupport ->
        //Distance between requested location and dot location
        val distance = (where.distanceTo(paintingVariables.location) - dotDiameter).coerceAtLeast(0.0)

        if (precision.matches(distance)) {
          listOf(
            WhatsAtResultElement(
              ResultElementType.pointOfInterest,
              label = "Red Dot",
              location = paintingVariables.location,
              boundingBox = Rectangle(paintingVariables.location, Size.of(dotDiameter, dotDiameter)),
              value = distance,
              valueFormatted = decimalFormat1digit.format(distance),
              data = "The Red Dot data...",
            )
          )
        } else {
          emptyList()
        }
      }
    }

    override fun paint(paintingContext: LayerPaintingContext) {
      val chartSupport = paintingContext.chartSupport
      val gc = paintingContext.gc

      gc.fill(Color.red)
      gc.fillOvalCenter(paintingVariables.location, dotDiameter)
    }
  }
}

