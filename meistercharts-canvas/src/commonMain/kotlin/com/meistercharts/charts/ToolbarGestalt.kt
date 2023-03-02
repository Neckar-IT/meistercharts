package com.meistercharts.charts

import com.meistercharts.algorithms.layers.toolbar.ToolbarButtonFactory
import com.meistercharts.algorithms.layers.toolbar.ToolbarLayer
import com.meistercharts.algorithms.layers.toolbar.resetZoomAndTranslationButton
import com.meistercharts.algorithms.layers.toolbar.zoomInButton
import com.meistercharts.algorithms.layers.toolbar.zoomOutButton
import com.meistercharts.canvas.MeisterChartBuilder
import com.meistercharts.canvas.paintable.Button

/**
 * Offers a predefined and configured toolbar
 */
class ToolbarGestalt(
  val data: Data = Data(),
  styleConfiguration: Style.() -> Unit = {}
) : ChartGestalt {

  val style: Style = Style().also(styleConfiguration)

  val toolbarLayer: ToolbarLayer = ToolbarLayer(data.buttons)

  override fun configure(meisterChartBuilder: MeisterChartBuilder) {
    meisterChartBuilder.configure {
      layers.addLayer(toolbarLayer)
    }
  }

  class Data(
    val buttons: List<Button> = createDefaultZoomButtons(),
  )

  class Style {
  }
}


/**
 * Returns the default buttons used for zooming
 */
fun createDefaultZoomButtons(toolbarButtonFactory: ToolbarButtonFactory = ToolbarButtonFactory()): List<Button> {
  return listOf(
    toolbarButtonFactory.zoomInButton(),
    toolbarButtonFactory.zoomOutButton(),
    toolbarButtonFactory.resetZoomAndTranslationButton(),
  )
}
