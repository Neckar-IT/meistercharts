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

import com.meistercharts.algorithms.ChartCalculator
import com.meistercharts.algorithms.ChartState
import com.meistercharts.algorithms.withAdditionalTranslation
import com.meistercharts.annotations.Zoomed
import com.meistercharts.model.Distance
import it.neckar.open.provider.DoubleProvider

/**
 * This layer update the chart state before delegating the calls to the provided [delegate].
 *
 * This can be used for example to:
 * * Places a layer within a part of the canvas.
 * * Layout multiple "charts" on one canvas.
 */
class TransformingChartStateLayer<T : Layer>(
  delegate: T,
  /**
   * Provides an (updated) chart state.
   * Usually the content area ([ChartCalculator.withContentAreaSize]) and the translation ([ChartCalculator.withAdditionalTranslation]) should be updated
   */
  val chartStateProvider: (ChartState) -> ChartState,
) : DelegatingLayer<T>(delegate) {

  override fun paintingVariables(): PaintingVariables {
    return paintingVariables
  }

  private val paintingVariables = object : TransformingChartStateLayerVariables {
    /**
     * The updated chart state - created in [layout] and reused in [paint]
     */
    override var updatedChartState: ChartState = ChartState.NoOp

    override fun calculate(paintingContext: LayerPaintingContext) {
      //This method is called by the super.layout method with the updated chart state
      updatedChartState = paintingContext.chartState
    }
  }

  override fun layout(paintingContext: LayerPaintingContext) {
    val updatedChartState = chartStateProvider(paintingContext.chartState)

    paintingContext.withCurrentChartState(updatedChartState) {
      //Automatically calls the calculate method of the painting properties
      super.layout(paintingContext)
    }
  }

  override fun paint(paintingContext: LayerPaintingContext) {
    paintingContext.withCurrentChartState(paintingVariables.updatedChartState) {
      delegate.paint(paintingContext)
    }
  }

  override val description: String
    get() = "TransformingChartStateLayer(${delegate.description})"
}

/**
 * Wraps the given layer into a transforming chart state layer
 */
fun <T : Layer> T.withUpdatedChartState(chartStateProvider: (ChartState) -> ChartState): TransformingChartStateLayer<T> {
  return TransformingChartStateLayer(this, chartStateProvider)
}

/**
 * Wraps the given layer into a transforming chart state layer with an additional translation
 */
fun <T : Layer> T.translatedWindow(additionalTranslation: Distance): TransformingChartStateLayer<T> {
  return TransformingChartStateLayer(this) { chartState ->
    chartState.withAdditionalTranslation(additionalTranslation)
  }
}

/**
 * Translates this [Layer] by the given offset
 */
fun <T : Layer> T.translatedWindow(translateX: @Zoomed Double = 0.0, translateY: @Zoomed Double = 0.0): TransformingChartStateLayer<T> {
  return translatedWindow(Distance(translateX, translateY))
}

/**
 * Translates this [Layer] dynamically
 */
fun <T : Layer> T.translatedWindow(translateX: @Zoomed DoubleProvider, translateY: @Zoomed DoubleProvider): TransformingChartStateLayer<T> {
  return TransformingChartStateLayer(this) { chartState ->
    chartState.withAdditionalTranslation(
      Distance(translateX(), translateY())
    )
  }
}


/**
 * Old layer name - defined as type alias to help finding the renamed class
 */
@Deprecated("Use TransformingChartStateLayer instead")
typealias CompartmentLayer<T> = TransformingChartStateLayer<T>


interface TransformingChartStateLayerVariables : PaintingVariables {
  /**
   * The updated chart state
   */
  var updatedChartState: ChartState

}
