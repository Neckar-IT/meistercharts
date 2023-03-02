package com.meistercharts.demo.descriptors

import com.meistercharts.algorithms.layers.addClearBackground
import com.meistercharts.algorithms.layers.barchart.CategoryAxisLayer
import com.meistercharts.algorithms.layers.barchart.DebugCategoryAxisLabelPainter
import com.meistercharts.algorithms.layers.barchart.DefaultCategoryAxisLabelPainter
import com.meistercharts.algorithms.layers.barchart.GreedyCategoryAxisLabelPainter
import com.meistercharts.algorithms.layout.BoxLayoutCalculator
import com.meistercharts.algorithms.layout.LayoutDirection
import com.meistercharts.algorithms.model.CategoryIndex
import com.meistercharts.algorithms.painter.Color
import com.meistercharts.canvas.i18nConfiguration
import com.meistercharts.canvas.paintable.Paintable
import com.meistercharts.canvas.textService
import com.meistercharts.charts.ContentViewportGestalt
import com.meistercharts.demo.ChartingDemo
import com.meistercharts.demo.ChartingDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.configurableBoolean
import com.meistercharts.demo.configurableBooleanProvider
import com.meistercharts.demo.configurableColorPickerProvider
import com.meistercharts.demo.configurableColorPickerProviderNullable
import com.meistercharts.demo.configurableDouble
import com.meistercharts.demo.configurableEnum
import com.meistercharts.demo.configurableFont
import com.meistercharts.demo.configurableInsets
import com.meistercharts.demo.configurableInsetsSeparate
import com.meistercharts.demo.configurableInt
import com.meistercharts.demo.configurableList
import com.meistercharts.demo.section
import com.meistercharts.model.Insets
import com.meistercharts.model.Side
import com.meistercharts.model.Vicinity
import com.meistercharts.provider.SizedLabelsProvider
import it.neckar.open.collections.fastForEach
import it.neckar.open.provider.MultiProvider
import it.neckar.open.i18n.I18nConfiguration
import it.neckar.open.i18n.TextService
import it.neckar.open.observable.ObservableObject
import com.meistercharts.resources.Icons

/**
 * Demonstrates the usage of the [CategoryAxisLayer]
 */
class CategoryAxisDemoDescriptor : ChartingDemoDescriptor<CategoryAxisDemoConfig> {
  override val name: String = "Category axis"

  //language=HTML
  override val description: String = "Category axis"
  override val category: DemoCategory = DemoCategory.Axis

  override val predefinedConfigurations: List<PredefinedConfiguration<CategoryAxisDemoConfig>> = CategoryAxisDemoConfig.createConfigs()

  override fun createDemo(configuration: PredefinedConfiguration<CategoryAxisDemoConfig>?): ChartingDemo {
    require(configuration != null)

    val initialAxisSide: Side = configuration.payload.side
    val initialAxisTickOrientation: Vicinity = configuration.payload.axisTickOrientation


    val axisSideProperty = ObservableObject(initialAxisSide)

    return ChartingDemo {
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
          val data = CategoryAxisLayer.Data(
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

            background = { Color.lightpink }
          }

          layers.addLayer(categoryAxisLayer)

          axisSideProperty.consumeImmediately {
            categoryAxisLayer.style.side = it
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

          configurableEnum("Paint Range", categoryAxisLayer.style::paintRange, enumValues()) {
          }

          section("Content Viewport Margin")
          configurableInsetsSeparate("CWP", contentViewportGestalt::contentViewportMargin)

          configurableList("Axis Painter", categoryAxisLayer.style.axisLabelPainter, axisPainters) {
            this.converter = { painter ->
              painter::class.simpleName ?: painter.toString()
            }
            onChange {
              categoryAxisLayer.style.axisLabelPainter = it
              markAsDirty()
            }
          }

          configurableBoolean("With images") {
            value = false
            onChange {
              if (it) {
                MultiProvider<CategoryIndex, Paintable?> { index ->
                  when (index) {
                    0 -> Icons.autoScale(fill = categoryAxisLayer.style.tickLabelColor())
                    1 -> Icons.legend(fill = categoryAxisLayer.style.tickLabelColor())
                    2 -> Icons.error(fill = categoryAxisLayer.style.tickLabelColor())
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

          configurableEnum("Axis side", axisSideProperty, enumValues())

          configurableDouble("Axis size", categoryAxisLayer.style::size) {
            max = 300.0
            onChange {
              categoryAxisLayer.style.size = it
              markAsDirty()
            }
          }

          configurableInsets("Axis margin", categoryAxisLayer.style::margin) {
            max = 300.0
          }

          configurableInsets("Content Viewport Margin", chartSupport.rootChartState::contentViewportMargin)

          configurableBooleanProvider("Axis title visible", categoryAxisLayer.style::titleVisible) {
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
              categoryAxisLayer.style.titleProvider = { _, _ -> it }
              markAsDirty()
            }
          }

          configurableDouble("Axis title gap", categoryAxisLayer.style::titleGap) {
            max = 100.0
          }

          configurableColorPickerProvider("Axis title color", categoryAxisLayer.style::titleColor) {
          }

          configurableFont("Axis title font", categoryAxisLayer.style::titleFont) {
          }

          configurableDouble("Axis line width", categoryAxisLayer.style::axisLineWidth) {
            max = 10.0
          }

          configurableColorPickerProvider("Tick label color", categoryAxisLayer.style::tickLabelColor)

          configurableEnum("Tick orientation", categoryAxisLayer.style::tickOrientation, enumValues()) {
          }

          configurableDouble("Tick line width", categoryAxisLayer.style::tickLineWidth) {
            max = 20.0
          }

          configurableDouble("Tick length", categoryAxisLayer.style::tickLength) {
            max = 20.0
          }

          configurableDouble("Tick label gap", categoryAxisLayer.style::tickLabelGap) {
            min = 0.0
            max = 20.0
          }

          configurableColorPickerProviderNullable("Background", categoryAxisLayer.style::background) {
          }

          configurableColorPickerProvider("Line color", categoryAxisLayer.style::lineColor) {
          }

          configurableFont("Tick font", categoryAxisLayer.style::tickFont) {
          }


          section("Default Label Painter")
          configurableEnum("Line Mode", defaultCategoryAxisLabelPainter.style::wrapMode)
          configurableDouble("Lines Gap", defaultCategoryAxisLabelPainter.style::twoLinesGap) {
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
        Side.values().fastForEach { side ->
          Vicinity.values().fastForEach { axisTickOrientation ->
            add(PredefinedConfiguration(CategoryAxisDemoConfig(side, axisTickOrientation)))
          }
        }
      }
    }
  }
}
