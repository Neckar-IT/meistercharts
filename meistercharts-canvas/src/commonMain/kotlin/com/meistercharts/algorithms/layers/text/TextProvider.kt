package com.meistercharts.algorithms.layers.text

import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.canvas.textService
import it.neckar.open.i18n.I18nConfiguration
import it.neckar.open.i18n.TextService

/**
 * Text provider that returns texts (a list of lines). This can be used for the message layer
 */
typealias LinesProvider = (textService: TextService, i18nConfiguration: I18nConfiguration) -> List<String>

/**
 * Resolves the text for the text provider using the [TextService] and [I18nConfiguration] from the provided painting context
 */
fun LinesProvider.resolve(paintingContext: LayerPaintingContext): List<String> {
  return this(paintingContext.chartSupport.textService, paintingContext.i18nConfiguration)
}
