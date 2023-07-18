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
package com.meistercharts.api.circular

import com.meistercharts.algorithms.layers.circular.CircularChartLegendLayer.CircleSegmentIndex
import com.meistercharts.color.Color
import com.meistercharts.algorithms.painter.UrlPaintable
import com.meistercharts.canvas.paintable.Paintable
import it.neckar.open.http.Url
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
      .map { it.icon?.let { idAsString -> UrlPaintable.naturalSize(Url(idAsString)) } }
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
