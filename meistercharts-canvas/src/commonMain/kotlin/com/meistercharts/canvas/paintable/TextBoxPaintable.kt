package com.meistercharts.canvas.paintable

import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.canvas.i18nConfiguration
import com.meistercharts.canvas.paintTextBox
import com.meistercharts.canvas.textService
import com.meistercharts.model.Direction
import it.neckar.open.i18n.I18nConfiguration
import it.neckar.open.i18n.TextService

/**
 * Paints a text box
 */
@Deprecated("Fix the calculation of bounds")
class TextBoxPaintable(
  lines: (textService: TextService, i18nConfiguration: I18nConfiguration) -> List<String>,
  additionalConfiguration: Configuration.() -> Unit = {},
) : AbstractPaintable() {

  val configuration: Configuration = Configuration(lines).also(additionalConfiguration)

  override fun paintingVariables(): PaintablePaintingVariables {
    return paintingVariables
  }

  private val paintingVariables = object : PaintablePaintingVariablesImpl() {
    override fun calculate(paintingContext: LayerPaintingContext) {
      super.calculate(paintingContext)

      val gc = paintingContext.gc
      val chartSupport = paintingContext.chartSupport

      val texts = configuration.lines(chartSupport.textService, chartSupport.i18nConfiguration)
      //TODO find a better way to calculate the text box!
      boundingBox = gc.paintTextBox(texts, configuration.anchorDirection)
    }
  }

  override fun paintAfterLayout(paintingContext: LayerPaintingContext, x: Double, y: Double) {
    val gc = paintingContext.gc
    val chartSupport = paintingContext.chartSupport

    gc.translate(x, y)

    val texts = configuration.lines(chartSupport.textService, chartSupport.i18nConfiguration)
    gc.paintTextBox(texts, configuration.anchorDirection)
  }

  class Configuration(
    /**
     * Provides the lines
     */
    var lines: (textService: TextService, i18nConfiguration: I18nConfiguration) -> List<String>,
  ) {
    var anchorDirection: Direction = Direction.TopLeft
  }
}
