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
package com.meistercharts.algorithms.layers.legend

import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.algorithms.layers.invoke
import com.meistercharts.canvas.FontDescriptorFragment
import com.meistercharts.canvas.FontSize
import com.meistercharts.canvas.FontWeight
import com.meistercharts.canvas.paintable.LabelPaintable
import com.meistercharts.canvas.paintable.Paintable
import com.meistercharts.model.Rectangle
import it.neckar.open.provider.SizedProvider1
import it.neckar.open.i18n.I18nConfiguration
import it.neckar.open.i18n.TextService

/**
 * A paintable that shows:
 * - a headline (e.g. based on a category)
 * - symbols and labels
 *
 * If the headline is null or blank, no headline (and no gap) will be painted
 */
class HeadlineAbovePaintable<Delegate : Paintable>(
  headline: (textService: TextService, i18nConfiguration: I18nConfiguration) -> String?,
  /**
   * The paintable that is painted below the headline
   */
  val delegate: Delegate,
) : Paintable {
  /**
   * The headline paintable
   */
  val headlinePaintable: LabelPaintable = LabelPaintable(headline) {
    font = FontDescriptorFragment(size = FontSize.Default, weight = FontWeight.Bold)
  }

  /**
   * The delegate that is used to paint everything
   */
  val stackedPaintablesPaintable: StackedPaintablesPaintable = StackedPaintablesPaintable(object : SizedProvider1<Paintable, LayerPaintingContext> {
    override fun size(param1: LayerPaintingContext): Int {
      val headlineVisible = headlinePaintable.configuration.label.invoke(param1.chartSupport).isNullOrBlank()

      if (headlineVisible) {
        //Only show the delegate - do *not* show the headline
        return 1
      }

      return 2
    }

    override fun valueAt(index: Int, param1: LayerPaintingContext): Paintable {
      val headline = headlinePaintable.configuration.label.invoke(param1.chartSupport)
      if (headline.isNullOrBlank()) {
        //Only show the delegate - do *not* show the headline
        require(index == 0) {
          "Invalid index $index"
        }
        return delegate
      }

      return when (index) {
        0 -> headlinePaintable
        1 -> delegate
        else -> throw IllegalArgumentException("Invalid index $index")
      }
    }
  })

  override fun boundingBox(paintingContext: LayerPaintingContext): Rectangle {
    return stackedPaintablesPaintable.boundingBox(paintingContext)
  }

  override fun paint(paintingContext: LayerPaintingContext, x: Double, y: Double) {
    stackedPaintablesPaintable.paint(paintingContext, x, y)
  }
}

/**
 * Wraps this paintable in a [HeadlineAbovePaintable]
 */
fun <Delegate : Paintable> Delegate.withHeadline(headline: (textService: TextService, i18nConfiguration: I18nConfiguration) -> String?): HeadlineAbovePaintable<Delegate> {
  return HeadlineAbovePaintable(headline, this)
}
