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
package com.meistercharts.algorithms.layers.text

import com.meistercharts.algorithms.layers.AbstractLayer
import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.algorithms.layers.LayerType
import com.meistercharts.color.Color
import com.meistercharts.annotations.Window
import com.meistercharts.font.FontDescriptorFragment
import com.meistercharts.canvas.ConfigurationDsl
import com.meistercharts.geometry.Coordinates
import com.meistercharts.model.Direction
import com.meistercharts.model.Direction.BottomRight
import it.neckar.open.provider.MultiProvider
import it.neckar.open.provider.MultiProviderIndexContextAnnotation
import it.neckar.open.provider.SizedProvider
import it.neckar.open.provider.fastForEachIndexed

/**
 * Shows a text on the canvas
 */
class TextsLayer(
  val data: Data = Data(),
  styleConfiguration: Style.() -> Unit = {}
) : AbstractLayer() {

  /**
   * The style
   */
  val style: Style = Style().apply(styleConfiguration)

  override var type: LayerType = LayerType.Content

  override fun paint(paintingContext: LayerPaintingContext) {
    val gc = paintingContext.gc
    gc.font(style.font)
    gc.fill(style.textColor)

    data.texts.fastForEachIndexed { index, value ->
      @Window val location = data.locationProvider.valueAt(index)
      gc.fillText(value, location.x, location.y, style.anchorDirection, style.anchorGapHorizontal, style.anchorGapVertical, style.maxWidth, style.maxHeight)
    }
  }

  class Data(
    /**
     * Provides the texts that are painted
     */
    var texts: SizedProvider<String> = SizedProvider.forValues("one", "two", "three"),
    /**
     * Provides the location for each label
     */
    var locationProvider: MultiProvider<TextIndex, @Window Coordinates> = MultiProvider { Coordinates((it + 1) * 100.0, (it + 1) * 200.0) },
  )

  @MustBeDocumented
  @Retention(AnnotationRetention.SOURCE)
  @MultiProviderIndexContextAnnotation
  annotation class TextIndex

  /**
   * Style configuration for the text layer
   */
  @ConfigurationDsl
  open class Style {
    /**
     * The color of the text
     */
    var textColor: Color = Color.black

    /**
     * Describes the font
     */
    var font: FontDescriptorFragment = FontDescriptorFragment.DefaultSize

    /**
     * The anchor direction - describes where the text is painted relative to the base point
     */
    var anchorDirection: Direction = BottomRight

    /**
     * The gap from the anchor point into the [anchorDirection]
     */
    var anchorGapHorizontal: Double = 0.0
    var anchorGapVertical: Double = 0.0

    /**
     * Maximum width for the text
     */
    var maxWidth: Double? = null

    /**
     * Maximum height for the text
     */
    var maxHeight: Double? = null
  }
}
