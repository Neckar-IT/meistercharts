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

import com.meistercharts.model.BinaryValueRange
import com.meistercharts.algorithms.painter.BinaryPainter
import com.meistercharts.color.Color
import com.meistercharts.annotations.DomainRelative
import com.meistercharts.canvas.ConfigurationDsl
import it.neckar.open.provider.BooleanValuesProvider

/**
 * Paints a binary curve (0..1)
 */
class BinaryLayer(
  val data: Data,
  styleConfiguration: Style.() -> Unit = {}
) : AbstractLayer() {
  override val type: LayerType = LayerType.Content

  val style: Style = Style().also(styleConfiguration)

  override fun paint(paintingContext: LayerPaintingContext) {
    val gc = paintingContext.gc
    val chartCalculator = paintingContext.chartCalculator

    //Translate to the origin of the content area
    gc.translate(chartCalculator.contentAreaRelative2windowX(0.0), chartCalculator.contentAreaRelative2windowY(0.0))

    val baseLine = chartCalculator.domainRelative2zoomedY(0.0)

    val maxHeight = chartCalculator.contentAreaRelative2zoomedY(1.0)
    val maxWidth = chartCalculator.contentAreaRelative2zoomedX(1.0)

    val binaryPainter = BinaryPainter(false, false, baseLine, maxWidth, maxHeight).also {
      it.lineWidth = style.lineWidth
      it.stroke = style.stroke
      it.shadow = style.shadow
      it.areaFill = style.areaFill
      it.shadowOffsetX = style.shadowOffset
      it.shadowOffsetY = style.shadowOffset
    }

    for (i in 0 until data.valuesProvider.size()) {
      val value = data.valuesProvider.valueAt(i)

      @DomainRelative val domainRelativeY = BinaryValueRange.toDomainRelative(value)
      val y = chartCalculator.domainRelative2zoomedY(domainRelativeY)

      binaryPainter.addCoordinate(gc, chartCalculator.domainRelative2zoomedX(0.1 * i), y)
    }

    binaryPainter.finish(gc)
  }

  class Data(
    val valuesProvider: BooleanValuesProvider
  )

  @ConfigurationDsl
  class Style {
    var lineWidth: Double = 5.0
    var stroke: Color = Color.rgba(10, 10, 10, 0.5)
    var shadow: Color? = null
    var areaFill: Color? = null
    var shadowOffset: Double = 4.0
  }

}
