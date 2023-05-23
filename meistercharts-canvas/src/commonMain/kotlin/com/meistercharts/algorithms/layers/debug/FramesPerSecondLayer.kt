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
package com.meistercharts.algorithms.layers.debug

import com.meistercharts.algorithms.layers.AbstractLayer
import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.algorithms.layers.LayerType
import com.meistercharts.algorithms.layers.Layers
import com.meistercharts.algorithms.layers.PaintingVariables
import com.meistercharts.algorithms.painter.Color
import com.meistercharts.canvas.ConfigurationDsl
import com.meistercharts.canvas.FontDescriptorFragment
import com.meistercharts.canvas.paintTextBox
import com.meistercharts.model.Direction
import com.meistercharts.model.Insets
import com.meistercharts.style.BoxStyle
import it.neckar.open.formatting.percentageFormat
import it.neckar.open.kotlin.lang.SpecialChars.nnbsp
import it.neckar.open.kotlin.lang.or0ifNaN
import it.neckar.open.unit.other.fps
import it.neckar.open.unit.other.pct
import it.neckar.open.unit.other.px
import it.neckar.open.unit.si.ms
import kotlin.math.roundToInt

/**
 * Displays the frames painted per second.
 */
class FramesPerSecondLayer(
  /**
   * The update rate in milliseconds (how much time must be passed before the label showing the FPS is updated)
   */
  @ms
  val updateRate: Double = 500.0,
  configurationConfiguration: Configuration.() -> Unit = {},
) : AbstractLayer() {

  val configuration: Configuration = Configuration().also(configurationConfiguration)

  override val type: LayerType
    get() = LayerType.Notification

  private val paintingVariables = object : PaintingVariables {
    /**
     * The timestamp when the layer has been updated the last time
     */
    @ms
    private var lastUpdatedTimestamp = 0.0

    /**
     * The current value for frames per second
     */
    @fps
    var paintedFPS = 0

    var optimizedFramesPercentage: @pct Double = 0.0

    @fps var minFps = 0
    @fps var maxFps = 0
    @fps var fps = 0

    override fun calculate(paintingContext: LayerPaintingContext) {
      val paintStatisticsSupport = paintingContext.layerSupport.paintStatisticsSupport

      //Only update the FPS every [updateRate] milliseconds
      if (paintingContext.frameTimestamp - lastUpdatedTimestamp > updateRate) {
        paintedFPS = paintStatisticsSupport.paintedFPS.or0ifNaN().roundToInt()

        minFps = paintStatisticsSupport.minFps.or0ifNaN().roundToInt()
        maxFps = paintStatisticsSupport.maxFps.or0ifNaN().roundToInt()
        fps = paintStatisticsSupport.fps.or0ifNaN().roundToInt()

        optimizedFramesPercentage = 1.0 - (1.0 / paintStatisticsSupport.fps * paintStatisticsSupport.paintedFPS)

        lastUpdatedTimestamp = paintingContext.frameTimestamp
      }
    }
  }

  override fun layout(paintingContext: LayerPaintingContext) {
    super.layout(paintingContext)
    paintingVariables.calculate(paintingContext)
  }

  override fun paint(paintingContext: LayerPaintingContext) {
    val gc = paintingContext.gc

    val text = "FPS:$nnbsp${paintingVariables.fps}, Max$nnbsp${paintingVariables.maxFps}, Min$nnbsp${paintingVariables.minFps}, PFPS:$nnbsp${paintingVariables.paintedFPS}, Optimized:$nnbsp${percentageFormat.format(paintingVariables.optimizedFramesPercentage)}"

    gc.font(configuration.font)
    gc.paintTextBox(text, Direction.TopLeft, anchorGapHorizontal = configuration.x, anchorGapVertical = configuration.y, boxStyle = configuration.boxStyle)
  }

  @ConfigurationDsl
  open class Configuration {
    var x: @px Double = 10.0
    val y: @px Double = 10.0


    var boxStyle: BoxStyle = BoxStyle.gray

    /**
     * The color to be used for the background
     */
    val background: Color = Color.web("rgba(255, 255, 255, 0.7)")

    /**
     * The color to be used for the text
     */
    val text: Color = Color.web("#888888")

    /**
     * The font to be used for the text
     */
    var font: FontDescriptorFragment = FontDescriptorFragment.empty

    /**
     * The padding to be used
     */
    val padding: Insets = Insets.of(3.0, 5.0)
  }
}

/**
 * Adds a [FramesPerSecondLayer] to this [Layers]
 */
fun Layers.addFramesPerSecond() {
  addLayer(FramesPerSecondLayer())
}
