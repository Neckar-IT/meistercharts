package com.meistercharts.api.circular

import com.meistercharts.algorithms.layers.circular.CircularChartLegendLayer.CircleSegmentIndex
import com.meistercharts.algorithms.painter.Color
import com.meistercharts.algorithms.painter.UrlPaintable
import com.meistercharts.canvas.paintable.Paintable
import it.neckar.open.provider.MultiProvider
import it.neckar.open.i18n.TextKey

/**
 * Converts the circular chart data
 */
object CircularChartConverter {
  fun toSegmentsColorProvider(data: CircularChartData): MultiProvider<CircleSegmentIndex, Color> {
    val segments = data.segments ?: return MultiProvider.always(Color.silver)
    val colors = segments
      .map { it.color?.let { color -> Color.web(color) } ?: Color.silver }
      .toList()
    return MultiProvider.forListModulo(colors, Color.silver)
  }

  fun toSegmentsLabelProvider(data: CircularChartData): MultiProvider<CircleSegmentIndex, TextKey?> {
    val segments = data.segments ?: return MultiProvider.alwaysNull()
    val labels = segments
      .map { it.caption ?: "?" }
      .map { TextKey.simple(it) }
      .toList()
    return MultiProvider.Companion.forListModulo(labels)
  }

  fun toSegmentsImageProvider(data: CircularChartData): MultiProvider<CircleSegmentIndex, Paintable?> {
    val segments = data.segments ?: return MultiProvider.alwaysNull()
    val images = segments
      .map { it.icon?.let { idAsString -> UrlPaintable.naturalSize(idAsString) } }
      .toList()
    return MultiProvider.Companion.forListModulo(images)
  }

  fun toValues(data: CircularChartData): List<Double> {
    val segments = data.segments ?: return listOf()

    return segments
      .filter { it.value != null }
      .map { it.value!! }
      .toList()
  }
}
