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

import com.meistercharts.annotations.Zoomed
import com.meistercharts.canvas.ConfigurationDsl
import com.meistercharts.canvas.saved
import it.neckar.open.provider.DoubleProvider

/**
 * Translates a wrapped [Layer]
 *
 * Translation does only occur during painting not while computing the layout.
 */
@Deprecated("Use TransformingChartStateLayer instead - if possible")
class TranslationLayer(
  val configuration: Configuration,
  additionalConfiguration: Configuration.() -> Unit = {},
  ) : AbstractLayer() {

  constructor(
    /**
     * The translated layer
     */
    delegate: Layer,
    additionalConfiguration: Configuration.() -> Unit = {},
  ): this(Configuration(delegate), additionalConfiguration)

  init {
    configuration.additionalConfiguration()
  }

  override val type: LayerType = LayerType.Content

  override fun layout(paintingContext: LayerPaintingContext) {
    super.layout(paintingContext)
    configuration.delegate.layout(paintingContext)
  }

  override fun paint(paintingContext: LayerPaintingContext) {
    paintingContext.gc.saved {
      it.translate(configuration.translateX(), configuration.translateY())
      configuration.delegate.paint(paintingContext)
    }
  }

  @ConfigurationDsl
  class Configuration(
    /**
     * The translated layer
     */
    val delegate: Layer,
  ) {
    /**
     * The translation along the x-axis
     */
    var translateX: () -> @Zoomed Double = { 0.0 }

    /**
     * The translation along the y-axis
     */
    var translateY: () -> @Zoomed Double = { 0.0 }
  }
}

/**
 * Translates this [Layer] by the given offset
 */
fun Layer.translate(translateX: @Zoomed Double = 0.0, translateY: @Zoomed Double = 0.0): TranslationLayer {
  return TranslationLayer(this) {
    this.translateX = { translateX }
    this.translateY = { translateY }
  }
}

/**
 * Translates this [Layer] dynamically
 */
fun Layer.translate(translateX: @Zoomed DoubleProvider, translateY: @Zoomed DoubleProvider): TranslationLayer {
  return TranslationLayer(this) {
    this.translateX = { translateX() }
    this.translateY = { translateY() }
  }
}
