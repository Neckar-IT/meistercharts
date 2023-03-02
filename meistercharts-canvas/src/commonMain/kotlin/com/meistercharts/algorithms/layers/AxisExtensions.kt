package com.meistercharts.algorithms.layers

import com.meistercharts.annotations.Zoomed
import com.meistercharts.canvas.FontMetrics
import com.meistercharts.canvas.textService
import com.meistercharts.model.Orientation
import it.neckar.open.kotlin.lang.nullIfBlank
import it.neckar.open.i18n.DefaultTextService
import it.neckar.open.i18n.I18nConfiguration
import it.neckar.open.i18n.TextService

/**
 * Returns the title from the axis style.
 * Returns null if there is no title or the title is blank
 */
fun AxisStyle.resolveTitle(paintingContext: LayerPaintingContext): String? {
  return resolveTitle(paintingContext.chartSupport.textService, paintingContext.i18nConfiguration)
}

/**
 * Returns the title from the axis style.
 * Returns null if there is no title or the title is blank
 */
fun AxisStyle.resolveTitle(textService: TextService, i18nConfiguration: I18nConfiguration): String? {
  if (titleVisible().not()) {
    return null
  }
  val titleProvider = titleProvider ?: return null
  return titleProvider(textService, i18nConfiguration).nullIfBlank()
}
