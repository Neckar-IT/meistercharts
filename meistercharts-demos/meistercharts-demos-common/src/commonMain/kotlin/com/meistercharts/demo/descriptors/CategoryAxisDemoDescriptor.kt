/*
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
package com.meistercharts.demo.descriptors

import com.meistercharts.demo.configurable
import com.meistercharts.demo.configurableSeparate
import kotlin.uuid.Uuid
import com.meistercharts.algorithms.layers.AxisConfiguration
import com.meistercharts.algorithms.layers.addClearBackground
import com.meistercharts.algorithms.layers.barchart.CategoryAxisLayer
import com.meistercharts.algorithms.layers.barchart.DebugCategoryAxisLabelPainter
import com.meistercharts.algorithms.layers.barchart.DefaultCategoryAxisLabelPainter
import com.meistercharts.algorithms.layers.barchart.GreedyCategoryAxisLabelPainter
import com.meistercharts.algorithms.layout.BoxLayoutCalculator
import com.meistercharts.algorithms.layout.LayoutDirection
import com.meistercharts.canvas.i18nConfiguration
import com.meistercharts.canvas.paintable.Paintable
import com.meistercharts.canvas.textService
import com.meistercharts.charts.ContentViewportGestalt
import com.meistercharts.color.Color
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.DemoQuality
import com.meistercharts.demo.MeisterchartsDemo
import com.meistercharts.demo.MeisterchartsDemoDescriptor
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.VariabilityType
import com.meistercharts.demo.configurableBoolean
import com.meistercharts.demo.configurableBooleanProvider
import com.meistercharts.demo.configurableColorPickerProvider
import com.meistercharts.demo.configurableColorPickerProviderNullable
import com.meistercharts.demo.configurableDouble
import com.meistercharts.demo.configurableEnum
import com.meistercharts.demo.configurableFontProvider
import com.meistercharts.demo.configurableInsets
import com.meistercharts.demo.configurableInsetsSeparate
import com.meistercharts.demo.configurableInt
import com.meistercharts.demo.configurableList
import com.meistercharts.demo.section
import com.meistercharts.model.Insets
import com.meistercharts.model.Vicinity
import com.meistercharts.model.category.CategoryIndex
import com.meistercharts.provider.SizedLabelsProvider
import com.meistercharts.resources.Icons
import it.neckar.geometry.Side
import it.neckar.open.collections.fastForEach
import it.neckar.open.i18n.I18nConfiguration
import it.neckar.open.i18n.TextService
import it.neckar.open.observable.ObservableObject
import it.neckar.open.provider.MultiProvider
import it.neckar.uuid.plus

/**
 * Demonstrates the usage of the [CategoryAxisLayer]
 */
class CategoryAxisDemoDescriptor : MeisterchartsDemoDescriptor<CategoryAxisDemoConfig> {
  override val uuid: Uuid = Uuid.parse("102c2853-5d9c-43a4-a606-68650bb4e656")
  override val name: String = "Category axis"

  //language=HTML
  override val description: String = "Category axis"
  override val category: DemoCategory = DemoCategory.Axis
  override val quality: DemoQuality = DemoQuality.High
  override val variabilityType: VariabilityType = VariabilityType.Stable

  override val predefinedConfigurations: List<PredefinedConfiguration<CategoryAxisDemoConfig>> = CategoryAxisDemoConfig.createConfigs()

  override fun prepareDemo(configuration: PredefinedConfiguration<CategoryAxisDemoConfig>?): MeisterchartsDemo {
    require(configuration != null) { "CategoryAxisDemoDescriptor.prepareDemo requires a predefined configuration" }

    val initialAxisSide: Side = configuration.payload.side
    val initialAxisTickOrientation: Vicinity = configuration.payload.axisTickOrientation


    val axisSideProperty = ObservableObject(initialAxisSide)

    return MeisterchartsDemo(this) {
      meistercharts {

        val contentViewportGestalt = ContentViewportGestalt(Insets.all15)
        contentViewportGestalt.configure(this)

        configure {
          layers.addClearBackground()

          var numberOfCategories = 10

          val labelsProvider: SizedLabelsProvider = object : SizedLabelsProvider {
            override fun size(param1: TextService, param2: I18nConfiguration): Int {
              return numberOfCategories
            }

            override fun valueAt(index: Int, param1: TextService, param2: I18nConfiguration): String {
              return "Category No. ${(index + 1)}"
            }
          }
          val data = CategoryAxisLayer.Configuration(
            labelsProvider
          ) {
            when (axisSideProperty.get()) {
              Side.Top, Side.Bottom -> BoxLayoutCalculator.layout(chartSupport.chartCalculator.contentAreaRelative2zoomedX(1.0), labelsProvider.size(chartSupport.textService, chartSupport.i18nConfiguration), layoutDirection = LayoutDirection.TopToBottom)
              Side.Left, Side.Right -> BoxLayoutCalculator.layout(chartSupport.chartCalculator.contentAreaRelative2zoomedY(1.0), labelsProvider.size(chartSupport.textService, chartSupport.i18nConfiguration), layoutDirection = LayoutDirection.LeftToRight)
            }
          }

          val debugCategoryAxisLabelPainter = DebugCategoryAxisLabelPainter()
          val defaultCategoryAxisLabelPainter = DefaultCategoryAxisLabelPainter()
          val greedyCategoryAxisLabelPainter = GreedyCategoryAxisLabelPainter()
          val axisPainters = listOf(
            debugCategoryAxisLabelPainter,
            defaultCategoryAxisLabelPainter,
            greedyCategoryAxisLabelPainter
          )

          val categoryAxisLayer = CategoryAxisLayer(data) {
            this.axisLabelPainter = defaultCategoryAxisLabelPainter
            this.tickOrientation = initialAxisTickOrientation
            this.titleProvider = { _, _ -> "Category Axis" }

            background = Color.lightpink
          }

          layers.addLayer(categoryAxisLayer)

          axisSideProperty.consumeImmediately {
            categoryAxisLayer.configuration.side = it
          }

          configurableInt("Number of categories") {
            min = 1
            value = 10
            max = 200
            onChange {
              numberOfCategories = it
              markAsDirty()
            }
          }

          configurable("Paint Range", categoryAxisLayer.configuration::paintRange, kotlin.enums.enumEntries<AxisConfiguration.PaintRange>().toList()) {
          }

          section("Content Viewport Margin")
          configurableSeparate("CWP", contentViewportGestalt::contentViewportMargin)

          configurableList("Axis Painter", categoryAxisLayer.configuration.axisLabelPainter, axisPainters) {
            this.converter = { painter ->
              painter::class.simpleName ?: painter.toString()
            }
            onChange {
              categoryAxisLayer.configuration.axisLabelPainter = it
              markAsDirty()
            }
          }

          configurableBoolean("With images") {
            value = false
            onChange {
              if (it) {
                MultiProvider<CategoryIndex, Paintable?> { index ->
                  when (index) {
                    0 -> Icons.autoScale(fill = categoryAxisLayer.configuration.tickLabelColor)
                    1 -> Icons.legend(fill = categoryAxisLayer.configuration.tickLabelColor)
                    2 -> Icons.error(fill = categoryAxisLayer.configuration.tickLabelColor)
                    else -> null
                  }
                }.apply {
                  defaultCategoryAxisLabelPainter.style.imagesProvider = this
                  greedyCategoryAxisLabelPainter.style.imagesProvider = this
                }
              } else {
                defaultCategoryAxisLabelPainter.style.imagesProvider = MultiProvider.alwaysNull()
                greedyCategoryAxisLabelPainter.style.imagesProvider = MultiProvider.alwaysNull()
              }
              markAsDirty()
            }
          }

          configurableEnum("Axis side", axisSideProperty, kotlin.enums.enumEntries<Side>().toList())

          configurable("Axis size", categoryAxisLayer.configuration::size) {
            max = 300.0
            onChange {
              categoryAxisLayer.configuration.size = it
              markAsDirty()
            }
          }

          configurable("Axis margin", categoryAxisLayer.configuration::margin) {
            max = 300.0
          }

          configurable("Content Viewport Margin", chartSupport.rootChartState::contentViewportMargin)

          configurable("Axis title visible", categoryAxisLayer.configuration::titleVisible) {
          }

          configurableList("Axis title", "Category Axis", listOf("Category Axis", "", null)) {
            converter {
              when {
                it == null -> "null"
                it.isBlank() -> "blank"
                else -> it.toString()
              }
            }
            onChange {
              categoryAxisLayer.configuration.titleProvider = { _, _ -> it }
              markAsDirty()
            }
          }

          configurable("Axis title gap", categoryAxisLayer.configuration::titleGap) {
            max = 100.0
          }

          configurable("Axis title color", categoryAxisLayer.configuration::titleColor) {
          }

          configurable("Axis title font", categoryAxisLayer.configuration::titleFont) {
          }

          configurable("Axis line width", categoryAxisLayer.configuration::axisLineWidth) {
            max = 10.0
          }

          configurable("Tick label color", categoryAxisLayer.configuration::tickLabelColor)

          configurable("Tick orientation", categoryAxisLayer.configuration::tickOrientation, kotlin.enums.enumEntries<Vicinity>().toList()) {
          }

          configurable("Tick line width", categoryAxisLayer.configuration::tickLineWidth) {
            max = 20.0
          }

          configurable("Tick length", categoryAxisLayer.configuration::tickLength) {
            max = 20.0
          }

          configurable("Tick label gap", categoryAxisLayer.configuration::tickLabelGap) {
            min = 0.0
            max = 20.0
          }

          configurable("Background", categoryAxisLayer.configuration::background) {
          }

          configurable("Line color", categoryAxisLayer.configuration::lineColor) {
          }

          configurable("Tick font", categoryAxisLayer.configuration::tickFont) {
          }


          section("Default Label Painter")
          configurable("Line Mode", defaultCategoryAxisLabelPainter.style::wrapMode)
          configurable("Lines Gap", defaultCategoryAxisLabelPainter.style::twoLinesGap) {
            max = 5.0
            min = -5.0
          }
        }
      }
    }
  }

}

data class CategoryAxisDemoConfig(
  val side: Side,
  val axisTickOrientation: Vicinity
) {
  override fun toString(): String {
    return "${side.name} - ${axisTickOrientation.name}"
  }

  companion object {
    fun createConfigs(): List<PredefinedConfiguration<CategoryAxisDemoConfig>> {
      return buildList {
        Side.entries.fastForEach { side ->
          Vicinity.entries.fastForEach { vicinity ->
            add(
              PredefinedConfiguration(
                Uuid.parse("96ba7056-152a-406d-b436-daed8010d4d6")
                  .plus(side)
                  .plus(vicinity),
                CategoryAxisDemoConfig(side, vicinity)
              )
            )
          }
        }
      }
    }
  }
}
