package com.meistercharts.charts

import com.meistercharts.algorithms.layers.FillBackgroundLayer
import com.meistercharts.algorithms.layers.NeckarItFlowLayer
import com.meistercharts.algorithms.layers.SloganLayer
import com.meistercharts.algorithms.layers.addClearBackground
import com.meistercharts.algorithms.layers.debug.addVersionNumberHidden
import com.meistercharts.algorithms.painter.Color
import com.meistercharts.algorithms.painter.RadialGradient
import com.meistercharts.canvas.MeisterChartBuilder
import com.meistercharts.canvas.StyleDsl

/**
 * Configuration for a slogan above the Neckar IT 'flow'
 */
class NeckarITSloganWithFlowGestalt(
  val data: Data = Data(),
  val style: Style = Style()
) : ChartGestalt {

  val sloganLayer: SloganLayer = SloganLayer {
    keepSloganInBounds = true
    glowScaleX = 1.4
    glowGradient = RadialGradient(
      Color.rgba(255, 255, 255, 0.15),
      Color.rgba(255, 255, 255, 0.0)
    )
  }

  val flowLayer: NeckarItFlowLayer = NeckarItFlowLayer()

  val backgroundLayer: FillBackgroundLayer = FillBackgroundLayer {
    dark()
  }

  override fun configure(meisterChartBuilder: MeisterChartBuilder) {
    meisterChartBuilder.apply {
      enableZoomAndTranslation = false

      configure {
        layers.addClearBackground()
        layers.addLayer(backgroundLayer)
        layers.addLayer(flowLayer)
        layers.addLayer(sloganLayer)

        layers.addVersionNumberHidden()
      }
    }
  }

  open class Data {
    // TODO
  }

  @StyleDsl
  open class Style {
    // TODO
  }
}
