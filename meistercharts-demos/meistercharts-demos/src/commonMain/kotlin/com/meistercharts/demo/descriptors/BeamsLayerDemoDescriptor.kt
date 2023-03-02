package com.meistercharts.demo.descriptors

import com.meistercharts.algorithms.KeepOriginOnWindowResize
import com.meistercharts.algorithms.axis.AxisOrientationY
import com.meistercharts.algorithms.impl.ZoomAndTranslationDefaults
import com.meistercharts.algorithms.layers.addClearBackground
import com.meistercharts.algorithms.layers.text.addText
import com.meistercharts.canvas.FixedContentAreaSize
import com.meistercharts.charts.sick.beams.BeamProvider
import com.meistercharts.charts.sick.beams.BeamState
import com.meistercharts.charts.sick.beams.BeamsLayer
import com.meistercharts.charts.sick.beams.CrossBeamsConfig
import com.meistercharts.demo.ChartingDemo
import com.meistercharts.demo.ChartingDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.configurableDouble
import com.meistercharts.demo.configurableEnum
import com.meistercharts.demo.configurableInt
import com.meistercharts.model.Size
import it.neckar.open.collections.getModulo
import com.meistercharts.events.ModifierCombination

/**
 *
 */
class BeamsLayerDemoDescriptor : ChartingDemoDescriptor<Nothing> {
  override val name: String = "Beams Layer"
  override val category: DemoCategory = DemoCategory.Layers

  override fun createDemo(configuration: PredefinedConfiguration<Nothing>?): ChartingDemo {
    return ChartingDemo {
      meistercharts {
        contentAreaSizingStrategy = FixedContentAreaSize(Size(100.0, 100.0))

        zoomAndTranslationDefaults {
          ZoomAndTranslationDefaults.noTranslation
        }

        configure {
          chartSupport.windowResizeBehavior = KeepOriginOnWindowResize
          chartSupport.rootChartState.axisOrientationY = AxisOrientationY.OriginAtTop

          layers.addClearBackground()

          val myBeamProvider = MyBeamProvider()
          val beamsLayer = BeamsLayer(BeamsLayer.Data(myBeamProvider))
          layers.addLayer(beamsLayer)


          var lastClickedBeam = -1

          layers.addText({ _, _ ->
            if (lastClickedBeam >= 0) {
              listOf("Last Clicked beam: $lastClickedBeam")
            } else {
              listOf()
            }
          })

          beamsLayer.onBeamClicked { modelBeamIndex: Int, _: ModifierCombination ->
            lastClickedBeam = modelBeamIndex
            markAsDirty()
          }

          configurableInt("Beam count", myBeamProvider::count) {
            max = 30
          }
          configurableEnum("Cross Beams", myBeamProvider::crossBeamsConfig, enumValues()) {
          }

          configurableDouble("beams distance", beamsLayer.style::beamsDistance) {
            max = 50.0
          }

          configurableDouble("deviceBeamGap", beamsLayer.style::deviceBeamGap) {
            max = 30.0
          }
          configurableDouble("device min height", beamsLayer.style::deviceMinHeight) {
            max = 300.0
          }
          configurableDouble("device width", beamsLayer.style::deviceWidth) {
            max = 300.0
          }
          configurableDouble("Beam line width", beamsLayer.style::beamLineWidth) {
            max = 10.0
          }
          configurableDouble("beam label bottom gap", beamsLayer.style::beamLabelBottomGap) {
            max = 10.0
          }
          configurableDouble("Arrow head height", beamsLayer.style::beamArrowHeadHeight) {
            max = 20.0
          }
          configurableDouble("Arrow head width", beamsLayer.style::beamArrowHeadWidth) {
            max = 20.0
          }
          configurableDouble("Beam Arrow start Offset", beamsLayer.style::beamStartAtArrowOffset) {
            max = 20.0
          }
        }
      }
    }
  }
}

class MyBeamProvider : BeamProvider {
  override var count: Int = 17

  override var crossBeamsConfig: CrossBeamsConfig = CrossBeamsConfig.None

  override fun beamState(index: Int): BeamState {
    return BeamState.values().getModulo(index)
  }

  override fun label(index: Int): String? {
    return index.toString()
  }

}

