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
package com.meistercharts.algorithms.layers.circular

import com.meistercharts.algorithms.layers.AbstractLayer
import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.algorithms.layers.LayerType
import com.meistercharts.algorithms.layers.resolve
import com.meistercharts.algorithms.painter.Color
import com.meistercharts.annotations.Domain
import com.meistercharts.canvas.FontDescriptorFragment
import com.meistercharts.canvas.PaintableLocation
import com.meistercharts.canvas.ConfigurationDsl
import com.meistercharts.canvas.paintTextWithPaintable
import com.meistercharts.canvas.paintable.Paintable
import com.meistercharts.canvas.paintable.TransparentPaintable
import com.meistercharts.canvas.saved
import com.meistercharts.model.Anchoring
import com.meistercharts.model.Coordinates
import com.meistercharts.model.Corner
import com.meistercharts.model.Direction
import com.meistercharts.model.Insets
import com.meistercharts.model.Size
import it.neckar.open.provider.DoublesProvider
import it.neckar.open.provider.MultiProvider
import it.neckar.open.provider.MultiProviderIndexContextAnnotation
import it.neckar.open.provider.fastForEachIndexed
import it.neckar.open.formatting.CachedNumberFormat
import it.neckar.open.formatting.decimalFormat
import it.neckar.open.i18n.TextKey
import com.meistercharts.style.Palette
import it.neckar.open.unit.other.px


/**
 * Shows the circular chart legend.
 */
class CircularChartLegendLayer(
  val data: Data,
  styleConfiguration: Style.() -> Unit = {}
) : AbstractLayer() {

  constructor(
    valuesProvider: @Domain DoublesProvider,
    styleConfiguration: Style.() -> Unit = {}
  ) : this(Data(valuesProvider), styleConfiguration)

  val style: Style = Style().also(styleConfiguration)

  override val type: LayerType
    get() = LayerType.Content

  override fun paint(paintingContext: LayerPaintingContext) {
    val gc = paintingContext.gc
    if (data.valuesProvider.isEmpty()) {
      return
    }

    //iterate over at most 4 elements
    data.valuesProvider.fastForEachIndexed(4) { circleSegmentIndex, value ->
      val corner = Corner.get(circleSegmentIndex)

      val paintable = style.segmentsImageProvider.valueAt(circleSegmentIndex) ?: TransparentPaintable(style.paintableSize)

      //The anchor point where the values are painted
      val anchor = corner.getAnchor(gc.canvasSize)

      gc.font(style.font)
      gc.fill(style.fontColor)
      gc.saved {
        val valueFormatted = style.valueFormat.format(value)
        val captionToPaint = if (style.showCaption) {
          ((style.segmentsLabelProvider.valueAt(circleSegmentIndex)?.resolve(paintingContext) ?: "-") + ": ") + valueFormatted
        } else {
          valueFormatted
        }

        //Move to the anchor point
        gc.translate(anchor.anchor.x, anchor.anchor.y)
        paintingContext.paintTextWithPaintable(captionToPaint, paintable, PaintableLocation.PaintableOutside, anchor.anchorDirection)
      }
    }
  }

  /**
   * Returns the paintable location
   * @param areaSize the size of the area the corner describes (e.g. the window or content area)
   */
  private fun Corner.getAnchor(@px areaSize: Size): Anchoring {
    val location = when (this) {
      Corner.TopLeft     -> Coordinates(style.padding.left, style.padding.top)
      Corner.TopRight    -> Coordinates(areaSize.width - style.padding.right, style.padding.top)
      Corner.BottomLeft  -> Coordinates(style.padding.left, areaSize.height - style.padding.bottom)
      Corner.BottomRight -> Coordinates(areaSize.width - style.padding.right, areaSize.height - style.padding.bottom)
    }

    val direction = when (this) {
      Corner.TopLeft -> Direction.TopLeft
      Corner.TopRight -> Direction.TopRight
      Corner.BottomLeft -> Direction.BottomLeft
      Corner.BottomRight -> Direction.BottomRight
    }

    return Anchoring(
      anchor = location,
      gapHorizontal = 0.0,
      gapVertical = 0.0,
      anchorDirection = direction
    )
  }

  class Data(
    val valuesProvider: @Domain DoublesProvider
  )

  @ConfigurationDsl
  open class Style {
    var padding: Insets = Insets.of(20.0)

    var font: FontDescriptorFragment = FontDescriptorFragment.empty

    /**
     * The color of the font
     */
    var fontColor: Color = Palette.defaultGray

    var paintableSize: Size = Size(40.0, 40.0)

    /**
     * Provides the label for a segment at a given index
     */
    var segmentsLabelProvider: MultiProvider<CircleSegmentIndex, TextKey?> = MultiProvider.alwaysNull()

    /**
     * Provides an image for a segment at a given index
     */
    var segmentsImageProvider: MultiProvider<CircleSegmentIndex, Paintable?> = MultiProvider.alwaysNull()

    /**
     * The format used to format the value from the chart segment
     */
    var valueFormat: CachedNumberFormat = decimalFormat

    /**
     * Whether the caption is shown or not. If set to false, only the value is displayed
     */
    var showCaption: Boolean = true
  }

  @MustBeDocumented
  @Retention(AnnotationRetention.SOURCE)
  @MultiProviderIndexContextAnnotation
  annotation class CircleSegmentIndex

}
