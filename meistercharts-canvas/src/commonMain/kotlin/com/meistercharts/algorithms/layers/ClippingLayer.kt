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

import com.meistercharts.annotations.Window
import com.meistercharts.canvas.ConfigurationDsl
import com.meistercharts.model.Insets
import com.meistercharts.model.SidesSelection

/**
 * Provides the insets for a painting context.
 * The insets are applied relative to the window
 */
typealias InsetsProvider = (paintingContext: LayerPaintingContext) -> @Window Insets

/**
 * Clips a layer from the outside
 */
class ClippingLayer<T : Layer>(
  val configuration: Configuration<T>,
  additionalConfiguration: Configuration<T>.() -> Unit = {}
) : DelegatingLayer<T>(configuration.delegate) {

  constructor(
    delegate: T,
    additionalConfiguration: Configuration<T>.() -> Unit = {}
  ): this(Configuration(delegate), additionalConfiguration)

  init {
    configuration.additionalConfiguration()
  }

  override fun paint(paintingContext: LayerPaintingContext) {
    val gc = paintingContext.gc
    val insets = configuration.insets(paintingContext)
    gc.clip(insets.left, insets.top, gc.width - insets.offsetWidth, gc.height - insets.offsetHeight)
    super.paint(paintingContext)
  }

  override val description: String
    get() = "ClippingLayer{${delegate.description}}"

  @ConfigurationDsl
  class Configuration<T: Layer>(
    var delegate: T,
    ) {
    /**
     * The insets
     */
    var insets: InsetsProvider = { Insets.empty }
  }
}

/**
 * Clips the given layer with the given insets (relative to the window)
 */
fun <T : Layer> T.clipped(insets: @Window Insets = Insets.empty): ClippingLayer<T> {
  return ClippingLayer(this) {
    this.insets = { insets }
  }
}

/**
 * Clips this layer to the content area
 */
fun <T : Layer> T.clippedToContentArea(sides: SidesSelection = SidesSelection.all): ClippingLayer<T> {
  return ClippingLayer(this) {
    this.insets = {
      val gc = it.gc
      val chartSupport = it.chartSupport
      val chartCalculator = chartSupport.chartCalculator

      @Window val left = if (sides.leftSelected) chartCalculator.contentAreaRelative2windowX(0.0) else 0.0
      @Window val right = if (sides.rightSelected) gc.width - chartCalculator.contentAreaRelative2windowX(1.0) else 0.0

      @Window val top = if (sides.topSelected) chartCalculator.contentAreaRelative2windowY(0.0) else 0.0
      @Window val bottom = if (sides.bottomSelected) gc.height - chartCalculator.contentAreaRelative2windowY(1.0) else 0.0

      Insets.of(top, right, bottom, left)
    }
  }
}

/**
 * Clips the layer to the content viewport
 */
fun <T : Layer> T.clippedToContentViewport(sides: SidesSelection = SidesSelection.all): ClippingLayer<T> {
  return ClippingLayer(this) {
    this.insets = {
      val chartSupport = it.chartSupport
      chartSupport.rootChartState.contentViewportMargin.only(sides)
    }
  }
}


/**
 * Clips the given layer with the given [InsetsProvider] (relative to the window)
 */
fun <T : Layer> T.clipped(insetsProvider: @Window InsetsProvider): ClippingLayer<T> {
  return ClippingLayer(this) {
    this.insets = insetsProvider
  }
}
