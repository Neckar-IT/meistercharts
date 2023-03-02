package com.meistercharts.algorithms.layers

import com.meistercharts.annotations.Zoomed
import com.meistercharts.canvas.saved
import it.neckar.open.provider.DoubleProvider

/**
 * Translates a wrapped [Layer]
 *
 * Translation does only occur during painting not while computing the layout.
 */
@Deprecated("Use TransformingChartStateLayer instead - if possible")
class TranslationLayer(
  /**
   * The translated layer
   */
  val delegate: Layer,
  additionalConfiguration: Configuration.() -> Unit = {},
) : AbstractLayer() {
  val configuration: Configuration = Configuration(delegate).also(additionalConfiguration)

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
