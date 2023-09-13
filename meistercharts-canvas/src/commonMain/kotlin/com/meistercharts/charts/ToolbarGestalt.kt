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
package com.meistercharts.charts

import com.meistercharts.algorithms.layers.toolbar.ToolbarButtonFactory
import com.meistercharts.algorithms.layers.toolbar.ToolbarLayer
import com.meistercharts.algorithms.layers.toolbar.resetZoomAndTranslationButton
import com.meistercharts.algorithms.layers.toolbar.zoomInButton
import com.meistercharts.algorithms.layers.toolbar.zoomOutButton
import com.meistercharts.canvas.ConfigurationDsl
import com.meistercharts.canvas.MeisterchartBuilder
import com.meistercharts.canvas.paintable.Button

/**
 * Offers a predefined and configured toolbar
 */
class ToolbarGestalt(
  val configuration: Configuration,
  additionalConfiguration: Configuration.() -> Unit = {},
) : ChartGestalt {

  constructor(
    buttons: List<Button> = createDefaultZoomButtons(),
    additionalConfiguration: Configuration.() -> Unit = {},
    ): this(Configuration(buttons), additionalConfiguration)

  init {
    configuration.additionalConfiguration()
  }

  val toolbarLayer: ToolbarLayer = ToolbarLayer(configuration.buttons)

  override fun configure(meisterChartBuilder: MeisterchartBuilder) {
    meisterChartBuilder.configure {
      layers.addLayer(toolbarLayer)
    }
  }

  @ConfigurationDsl
  class Configuration(
    val buttons: List<Button> = createDefaultZoomButtons(),
  )
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
