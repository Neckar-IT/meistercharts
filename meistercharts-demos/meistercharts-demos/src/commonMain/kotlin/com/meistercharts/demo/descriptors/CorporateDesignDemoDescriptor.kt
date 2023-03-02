package com.meistercharts.demo.descriptors

import com.meistercharts.algorithms.layers.AbstractLayer
import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.algorithms.layers.LayerType
import com.meistercharts.algorithms.layers.addClearBackground
import com.meistercharts.algorithms.layers.text.TextLayer
import com.meistercharts.algorithms.painter.Color
import com.meistercharts.canvas.FontDescriptorFragment
import com.meistercharts.canvas.LayerSupport
import com.meistercharts.demo.ChartingDemo
import com.meistercharts.demo.ChartingDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.design.CorporateDesign
import com.meistercharts.design.DebugDesign
import com.meistercharts.design.NeckarITDesign
import com.meistercharts.design.SegoeUiDesign
import com.meistercharts.design.corporateDesign
import com.meistercharts.model.Direction
import com.meistercharts.model.DirectionBasedBasePointProvider
import com.meistercharts.charts.lizergy.solar.LizergyDesign
import it.neckar.open.kotlin.lang.fastFor
import it.neckar.open.provider.MultiProvider

/**
 *
 */
class CorporateDesignDemoDescriptor : ChartingDemoDescriptor<CorporateDesign?> {
  override val name: String = "Corporate Design"
  override val category: DemoCategory = DemoCategory.ShowCase

  override val predefinedConfigurations: List<PredefinedConfiguration<CorporateDesign?>> = listOf(
    PredefinedConfiguration(null, "null"),
    PredefinedConfiguration(NeckarITDesign, "Neckar IT"),
    PredefinedConfiguration(SegoeUiDesign, "Segoe UI"),
    PredefinedConfiguration(DebugDesign, "Debug"),
    PredefinedConfiguration(LizergyDesign, "Lizergy"),
  )

  override fun createDemo(configuration: PredefinedConfiguration<CorporateDesign?>?): ChartingDemo {
    return ChartingDemo {
      val selectedCorporateDesign = configuration?.payload ?: corporateDesign

      meistercharts {
        configure {
          layers.addClearBackground()
          addLayer(selectedCorporateDesign, selectedCorporateDesign.h1, selectedCorporateDesign.h1Color, 0.0, "H1")
          addLayer(selectedCorporateDesign, selectedCorporateDesign.h2, selectedCorporateDesign.h2Color, 60.0, "H2")
          addLayer(selectedCorporateDesign, selectedCorporateDesign.h3, selectedCorporateDesign.h3Color, 115.0, "H3")
          addLayer(selectedCorporateDesign, selectedCorporateDesign.h4, selectedCorporateDesign.h4Color, 160.0, "H4")
          addLayer(selectedCorporateDesign, selectedCorporateDesign.h5, selectedCorporateDesign.h5Color, 190.0, "H5")
          addLayer(selectedCorporateDesign, selectedCorporateDesign.textFont, selectedCorporateDesign.textColor, 220.0, "Text")
          addLayer(selectedCorporateDesign, selectedCorporateDesign.textFont, selectedCorporateDesign.chartColors, 250.0, "Chart colors")
        }
      }
    }
  }

  private fun LayerSupport.addLayer(selectedCorporateDesign: CorporateDesign, fontDescriptorFragment: FontDescriptorFragment, color: Color, gapTop: Double, description: String) {
    val text = "$description: ${selectedCorporateDesign.id} - $fontDescriptorFragment"

    layers.addLayer(TextLayer({ _, _ ->
                                listOf(
                                  text
                                )
                              }) {
      font = fontDescriptorFragment
      textColor = color
      anchorDirection = Direction.TopCenter
      anchorPointProvider = DirectionBasedBasePointProvider(Direction.TopCenter)
      anchorGapVertical = gapTop
    }
    )
  }

  private fun LayerSupport.addLayer(selectedCorporateDesign: CorporateDesign, fontDescriptorFragment: FontDescriptorFragment, colors: MultiProvider<CorporateDesignDemoDescriptor, Color>, gapTop: Double, description: String) {
    val text = "$description: ${selectedCorporateDesign.id}"
    layers.addLayer(object : AbstractLayer() {
      override val type: LayerType
        get() = LayerType.Content

      override fun paint(paintingContext: LayerPaintingContext) {
        val gc = paintingContext.gc
        val centerX = gc.width * 0.5
        gc.font(fontDescriptorFragment)
        gc.fillText(text, centerX, gapTop, Direction.TopRight)
        10.fastFor {
          gc.fill(colors.valueAt(it))
          gc.fillRect(centerX + it * 24.0 + 10.0, gapTop, 20.0, 20.0)
        }
      }
    })
  }
}
