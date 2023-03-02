package com.meistercharts.demo.descriptors

import com.meistercharts.algorithms.layers.AxisStyle
import com.meistercharts.algorithms.layers.AxisTitleLocation
import com.meistercharts.algorithms.layers.addClearBackground
import com.meistercharts.algorithms.layers.barchart.CategoryAxisLayer
import com.meistercharts.algorithms.layout.BoxLayoutCalculator
import com.meistercharts.algorithms.layout.LayoutDirection
import com.meistercharts.charts.ContentViewportGestalt
import com.meistercharts.charts.support.CategoryAxisSupport
import com.meistercharts.demo.ChartingDemo
import com.meistercharts.demo.ChartingDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.configurableEnum
import com.meistercharts.demo.configurableFont
import com.meistercharts.demo.configurableInsetsSeparate
import com.meistercharts.model.Insets
import com.meistercharts.model.Vicinity
import com.meistercharts.provider.SizedLabelsProvider
import it.neckar.open.i18n.I18nConfiguration
import it.neckar.open.i18n.TextService

class CategoryAxisSupportDemoDescriptor : ChartingDemoDescriptor<Nothing> {
  override val name: String = "Category Axes Support"

  //language=HTML
  override val category: DemoCategory = DemoCategory.Support

  override fun createDemo(configuration: PredefinedConfiguration<Nothing>?): ChartingDemo {
    return ChartingDemo {
      meistercharts {

        val myAxisConfiguration: CategoryAxisLayer.Style.(MyKeyEnum, CategoryAxisLayer, AxisTitleLocation) -> Unit = { myKeyEnum, axis, _ ->
          titleProvider = { textService, i18nConfiguration -> "Title for $myKeyEnum" }

          when (myKeyEnum) {
            MyKeyEnum.Axis0 -> size = 100.0

            MyKeyEnum.Axis1 -> {
              size = 100.0
              margin = Insets.of(120.0)
              paintRange = AxisStyle.PaintRange.ContentArea
            }
          }
        }

        val labelsProvider: SizedLabelsProvider = object : SizedLabelsProvider {
          override fun valueAt(index: Int, param1: TextService, param2: I18nConfiguration): String {
            return "Label for $index"
          }

          override fun size(param1: TextService, param2: I18nConfiguration): Int {
            return 7
          }
        }

        val layoutProvider = BoxLayoutCalculator.layout(500.0, 7, LayoutDirection.TopToBottom)

        val support = CategoryAxisSupport<MyKeyEnum>(
          labelsProvider = { labelsProvider },
          layoutProvider = { layoutProvider }
        ) {
          this.axisConfiguration = myAxisConfiguration
        }

        val contentViewportGestalt = ContentViewportGestalt(Insets.of(40.0, 10.0, 10.0, 10.0))
        contentViewportGestalt.configure(this)

        configure {
          layers.addClearBackground()

          support.addLayers(this, MyKeyEnum.Axis0)
          support.addLayers(this, MyKeyEnum.Axis1)
        }

        configurableEnum("Title Location", support::preferredAxisTitleLocation)
        configurableEnum("Tick Location", Vicinity.Outside) {
          onChange {
            support.configuration.axisConfiguration = { myKeyEnum, axis, categoryAxisTitleLocation ->
              myAxisConfiguration(myKeyEnum, axis, categoryAxisTitleLocation) //delegate to the "main" config
              axis.style.tickOrientation = it
              markAsDirty()
            }
          }
        }

        configurableFont("Title Font", support.getTopTitleLayer(MyKeyEnum.Axis1).configuration.titleFont) {
          onChange {
            support.configuration.topTitleLayerConfiguration = { myKeyEnum, axis ->
              axis.configuration.titleFont = it
            }
            markAsDirty()
          }
        }

        configurableInsetsSeparate("Content VP Margin", contentViewportGestalt::contentViewportMargin)
      }
    }
  }

  enum class MyKeyEnum {
    Axis0,
    Axis1,
  }
}
