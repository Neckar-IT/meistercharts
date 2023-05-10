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
package com.meistercharts.algorithms.layers

import com.meistercharts.algorithms.layers.linechart.LineStyle
import com.meistercharts.algorithms.painter.Color
import com.meistercharts.annotations.DomainRelative
import com.meistercharts.annotations.Window
import com.meistercharts.model.Insets
import com.meistercharts.model.Orientation
import it.neckar.open.kotlin.lang.asProvider
import it.neckar.open.kotlin.lang.asProvider1
import it.neckar.open.provider.DoublesProvider
import it.neckar.open.provider.fastForEach
import it.neckar.open.unit.other.px
import kotlin.jvm.JvmOverloads

/**
 * Paints a grid for domain-relative values
 */
class DomainRelativeGridLayer @JvmOverloads constructor(
  /**
   * Returns the domain relative values where grid lines will be placed
   */
  valuesProvider: @DomainRelative DoublesProvider = DoublesProvider.empty,

  /**
   * Provides the orientation of the grid lines.
   *
   * - Vertical: The grid lines are painted from top to bottom
   * - Horizontal: The grid lines are painted from left to right
   */
  orientationProvider: () -> Orientation = { Orientation.Vertical },

  additionalConfiguration: Configuration.() -> Unit = {},
) : AbstractLayer() {

  constructor(
    orientation: Orientation,
    valuesProvider: @DomainRelative DoublesProvider,
    additionalConfiguration: Configuration.() -> Unit = {},
  ) : this(valuesProvider, orientation.asProvider(), additionalConfiguration)

  val configuration: Configuration = Configuration(valuesProvider, orientationProvider).also(additionalConfiguration)


  override val type: LayerType
    get() = LayerType.Background

  override fun paint(paintingContext: LayerPaintingContext) {
    val gc = paintingContext.gc
    val chartCalculator = paintingContext.chartCalculator

    @Window val topY = configuration.passpartout.top
    @Window val bottomY = gc.height - configuration.passpartout.bottom

    @Window val leftX = configuration.passpartout.left
    @Window val rightX = gc.width - configuration.passpartout.right

    when (configuration.orientationProvider()) {
      Orientation.Horizontal -> {
        //Paint horizontal lines

        //Lines calculated from @DomainRelative values
        configuration.valuesProvider.fastForEach { value: @DomainRelative Double ->
          @Window val locationY = chartCalculator.domainRelative2windowY(value)
          if (locationY < topY || locationY > bottomY) {
            return@fastForEach
          }

          configuration.lineStyles(value).apply(gc)
          gc.strokeLine(leftX, locationY, rightX, locationY)
        }
      }

      Orientation.Vertical -> {
        //paint vertical lines

        //Lines calculated from @DomainRelative values
        configuration.valuesProvider.fastForEach { value: @DomainRelative Double ->
          @Window val locationX = chartCalculator.domainRelative2windowX(value)
          if (locationX < leftX || locationX > rightX) {
            return@fastForEach
          }

          configuration.lineStyles(value).apply(gc)
          gc.strokeLine(locationX, topY, locationX, bottomY)
        }
      }
    }
  }

  class Configuration(
    /**
     * Returns the domain relative values where grid lines will be placed
     */
    var valuesProvider: @DomainRelative DoublesProvider,

    /**
     * Provides the orientation of the grid lines.
     *
     * - Vertical: The grid lines are painted from top to bottom
     * - Horizontal: The grid lines are painted from left to right
     */
    var orientationProvider: () -> Orientation,
  ) {

    /**
     * The style to be used for each grid line at a certain domain-relative value
     */
    var lineStyles: (@DomainRelative Double) -> LineStyle = LineStyle(color = Color.lightgray, lineWidth = 1.0).asProvider1()

    /**
     * The passpartout for the grid lines.
     * Calculated form the outside of the window
     */
    @px
    @Window
    //TODO somehow replace with the content viewport margin - look at the value axis
    var passpartout: Insets = Insets.empty
  }
}

/**
 * Creates a grid for a value axis layer.
 *
 * The grid layer shows *only* grid lines for the visible ticks of the value axis!
 * If the value axis does not have any visible ticks, the grid layer does not show anything.
 */
@JvmOverloads
fun ValueAxisLayer.createGrid(styleConfiguration: DomainRelativeGridLayer.Configuration.() -> Unit = {}): DomainRelativeGridLayer {
  return DomainRelativeGridLayer(valuesProvider = object : DoublesProvider {
    override fun size(): Int {
      return tickDomainValues.size
    }

    override fun valueAt(index: Int): @DomainRelative Double {
      return data.valueRangeProvider().toDomainRelative(tickDomainValues[index])
    }
  }, orientationProvider = {
    style.orientation.opposite()
  }, additionalConfiguration = styleConfiguration
  )
}
