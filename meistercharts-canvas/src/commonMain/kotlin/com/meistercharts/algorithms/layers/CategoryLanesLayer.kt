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

import com.meistercharts.algorithms.layers.barchart.CategoryChartOrientation
import com.meistercharts.algorithms.layout.BoxIndex
import com.meistercharts.algorithms.layout.EquisizedBoxLayout
import com.meistercharts.annotations.Domain
import com.meistercharts.annotations.Window
import com.meistercharts.canvas.ConfigurationDsl
import com.meistercharts.canvas.fillRoundedRect
import com.meistercharts.canvas.strokeRoundedRect
import com.meistercharts.color.CanvasPaintProvider
import com.meistercharts.color.Color
import com.meistercharts.color.ColorProviderNullable
import com.meistercharts.color.get
import com.meistercharts.model.BorderRadius
import com.meistercharts.model.category.CategoryIndex
import com.meistercharts.range.ValueRange
import it.neckar.open.kotlin.lang.asProvider
import it.neckar.open.provider.DoublesProvider
import it.neckar.open.provider.MultiProvider

/**
 * Paints a swim lane for each category
 */
class CategoryLanesLayer(
  val configuration: Configuration,
  additionalConfiguration: Configuration.() -> Unit = {},
) : AbstractLayer() {

  constructor(
    valuesProvider: @Domain DoublesProvider,
    additionalConfiguration: Configuration.() -> Unit = {},
  ) : this(Configuration(valuesProvider), additionalConfiguration)

  init {
    configuration.additionalConfiguration()
  }

  override val type: LayerType
    get() = LayerType.Content

  /**
   * The current layout that has been calculated
   */
  var layout: EquisizedBoxLayout? = null

  override fun layout(paintingContext: LayerPaintingContext) {
    super.layout(paintingContext)
    layout = configuration.layoutCalculator.calculateLayout(paintingContext, configuration.valuesProvider.size(), configuration.orientation)
  }

  override fun paint(paintingContext: LayerPaintingContext) {
    val layout = layout ?: return

    val gc = paintingContext.gc
    val chartCalculator = paintingContext.chartCalculator

    with(chartCalculator) {
      for (index in 0 until configuration.valuesProvider.size()) {
        @Window val centerX = zoomed2windowX(layout.calculateCenter(BoxIndex(index)))

        val lowerY = domainRelative2windowY(0.0)
        val upperY = domainRelative2windowY(1.0)

        //Fill
        configuration.fill.valueAt(index)?.let {
          gc.fill(it.toCanvasPaint(0.0, lowerY, 0.0, upperY))
          gc.fillRoundedRect(centerX - layout.boxSize / 2.0, lowerY, layout.boxSize, upperY - lowerY, configuration.borderRadius)
        }

        //Stroke
        configuration.stroke.valueAt(index).let {
          gc.stroke(it.toCanvasPaint(0.0, lowerY, 0.0, upperY))
          gc.lineWidth = 1.0
          gc.strokeRoundedRect(centerX - layout.boxSize / 2.0, lowerY, layout.boxSize, upperY - lowerY, configuration.borderRadius)
        }

        //The (optional) center line
        configuration.centerLineStroke.valueAt(index).get()?.let {
          gc.stroke(it)

          @Domain val domainValue = configuration.valuesProvider.valueAt(index)


          val valueY = domainRelative2windowY(configuration.valueRange.toDomainRelative(domainValue))
          gc.strokeLine(centerX, lowerY, centerX, valueY)
        }
      }
    }
  }

  /**
   * Style for the category lanes layer
   */
  @ConfigurationDsl
  open class Configuration(
    /**
     * Provides the domain values to be shown (one value belongs to one [CategoryIndex])
     */
    var valuesProvider: @Domain DoublesProvider,
  ) {
    /**
     * Provides the layout
     */
    var layoutCalculator: CategoryLayouter = DefaultCategoryLayouter()

    /**
     * The orientation of the categories lanes
     */
    var orientation: CategoryChartOrientation = CategoryChartOrientation.VerticalLeft

    /**
     * The [ValueRange] for the category chart.
     */
    var valueRange: @Domain ValueRange = ValueRange.default

    /**
     * The (optional) fill of the lane
     */
    var fill: MultiProvider<CategoryIndex, CanvasPaintProvider?> = MultiProvider.always(Color.silver())

    /**
     * The (optional) stroke around the lane
     */
    var stroke: MultiProvider<CategoryIndex, Color> = MultiProvider.always(Color.darkgray())

    /**
     * The (optional) stroke for the center line
     */
    var centerLineStroke: MultiProvider<CategoryIndex, ColorProviderNullable> = MultiProvider.always(Color.web("#e5f1f8").asProvider())

    /**
     * The corner radii
     */
    var borderRadius: BorderRadius = BorderRadius.none
  }

}

