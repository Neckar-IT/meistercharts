package com.meistercharts.demo.descriptors

import com.meistercharts.algorithms.layers.AbstractLayer
import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.algorithms.layers.LayerType
import com.meistercharts.algorithms.layers.PaintingVariables
import com.meistercharts.algorithms.layers.addClearBackground
import com.meistercharts.algorithms.layers.debug.addWhatsAtDebugLayer
import com.meistercharts.algorithms.painter.Color
import com.meistercharts.canvas.whatsAt
import com.meistercharts.demo.ChartingDemo
import com.meistercharts.demo.ChartingDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.model.Coordinates
import com.meistercharts.model.Rectangle
import com.meistercharts.model.Size
import com.meistercharts.whatsat.ResultElementType
import com.meistercharts.whatsat.WhatsAtResultElement
import it.neckar.open.formatting.decimalFormat1digit

/**
 * A simple hello world demo
 */
class WhatsAtDemoDescriptor : ChartingDemoDescriptor<Nothing> {
  override val name: String = "Whats At"
  override val category: DemoCategory = DemoCategory.Interaction

  override fun createDemo(configuration: PredefinedConfiguration<Nothing>?): ChartingDemo {
    return ChartingDemo {
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

