package com.meistercharts.layer

import com.meistercharts.algorithms.layers.AbstractLayer
import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.algorithms.layers.LayerType
import com.meistercharts.model.Direction
import it.neckar.open.collections.fastForEach
import it.neckar.open.kotlin.lang.props

class DumpPaintingVariablesLayer(
  additionalConfiguration: Configuration.() -> Unit = {},
) : AbstractLayer() {

  val configuration: Configuration = Configuration().also(additionalConfiguration)

  override val type: LayerType = LayerType.Notification

  override fun paint(paintingContext: LayerPaintingContext) {
    val gc = paintingContext.gc

    gc.translate(configuration.originX, configuration.originY)

    paintingContext.layerSupport.layers.layers.fastForEach { layer ->
      val paintingVariables = layer.paintingVariables() ?: return@fastForEach

      gc.fillText(layer.description, 0.0, 0.0, Direction.TopLeft)
      gc.translate(0.0, configuration.lineHeight)

      props(paintingVariables).forEach {
        val valueFormatted = when (val value = it.value) {
          else -> value.toString()
        }

        gc.fillText(it.key + ": " + valueFormatted, configuration.indentX, 0.0, Direction.TopLeft)
        gc.translate(0.0, configuration.lineHeight)
      }
    }
  }

  class Configuration {
    var originX: Double = 120.0
    var originY: Double = 50.0

    var indentX: Double = 20.0
    var lineHeight: Double = 20.0
  }
}
