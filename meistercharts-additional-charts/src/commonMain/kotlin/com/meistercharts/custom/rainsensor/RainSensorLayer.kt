package com.meistercharts.custom.rainsensor

import com.meistercharts.algorithms.layers.AbstractLayer
import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.algorithms.layers.LayerType
import com.meistercharts.algorithms.paintable.ObjectFit
import com.meistercharts.algorithms.painter.Color
import com.meistercharts.annotations.Zoomed
import com.meistercharts.canvas.saved
import com.meistercharts.model.Direction
import it.neckar.open.kotlin.lang.distance
import it.neckar.open.provider.DoubleProvider
import com.meistercharts.resources.LocalResourcePaintable
import it.neckar.open.unit.other.deg

/**
 * Demonstration of a rain sensor
 */
class RainSensorLayer(
  val data: Data,
  styleConfiguration: Style.() -> Unit = {}
) : AbstractLayer() {

  val style: Style = Style().also(styleConfiguration)

  override val type: LayerType = LayerType.Content


  override fun paint(paintingContext: LayerPaintingContext) {
    val gc = paintingContext.gc
    val chartCalculator = paintingContext.chartCalculator

    //Paint the roof
    gc.saved {
      style.roofResource.paintInBoundingBox(
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

    //Paint the sensor
    gc.saved {
      if (data.sensorHeated()) {
        gc.saved {
          style.sensorHeatResource.paintInBoundingBox(
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
        style.sensorResource.paintInBoundingBox(
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


    //Paint the window base
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
        gc.rotateDegrees(RainSensorResources.roofAngle)
        gc.fill(style.windowBaseColor)
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

      //Paint the window itself
      gc.saved {
        gc.rotateDegrees(data.openAngle() + RainSensorResources.roofAngle)
        gc.fill(style.windowBaseColor)
        gc.fillRect(0.0, 0.0, distance, height)
      }
    }
  }

  class Style {
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


  class Data(
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
  }
}

