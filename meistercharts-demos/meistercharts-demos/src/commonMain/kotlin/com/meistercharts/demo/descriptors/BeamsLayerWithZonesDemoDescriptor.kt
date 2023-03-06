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
package com.meistercharts.demo.descriptors

import com.meistercharts.algorithms.KeepOriginOnWindowResize
import com.meistercharts.algorithms.axis.AxisOrientationY
import com.meistercharts.algorithms.impl.ZoomAndTranslationDefaults
import com.meistercharts.algorithms.layers.TransformingChartStateLayer
import com.meistercharts.algorithms.layers.addClearBackground
import com.meistercharts.charts.sick.beams.BeamsLayer
import com.meistercharts.charts.sick.beams.ZonesLayer
import com.meistercharts.charts.sick.beams.ZonesProvider
import com.meistercharts.algorithms.withAdditionalTranslation
import com.meistercharts.algorithms.withWindowSize
import com.meistercharts.annotations.Zoomed
import com.meistercharts.canvas.FixedContentAreaSize
import com.meistercharts.demo.ChartingDemo
import com.meistercharts.demo.ChartingDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.configurableDouble
import com.meistercharts.demo.configurableEnum
import com.meistercharts.demo.configurableInt
import com.meistercharts.demo.section
import com.meistercharts.model.Distance
import com.meistercharts.model.Size

/**
 *
 */
class BeamsLayerWithZonesDemoDescriptor : ChartingDemoDescriptor<Nothing> {
  override val name: String = "Beams Layer + Zones"
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

          val beamLocationProvider = { beamIndex: Int ->
            beamsLayer.getBeamLocation(beamIndex)
          }

          val zonesLayer = ZonesLayer(
            ZonesLayer.Data(
              object : ZonesProvider {
                override val count: Int = 4

                override fun startIndex(zoneIndex: Int): Int {
                  return zoneIndex
                }

                override fun endIndex(zoneIndex: Int): Int {
                  return zoneIndex + 4
                }

                override fun isActive(zoneIndex: Int): Boolean {
                  return zoneIndex == 1
                }
              }, beamLocationProvider
            )
          )

          //TODO how to handle?
          @Zoomed val zonesLayerWidth = zonesLayer.data.zonesProvider.count * zonesLayer.style.zonesGap

          layers.addLayer(TransformingChartStateLayer(beamsLayer) {
            val originalWindowSize = chartSupport.currentChartState.windowSize
            it.withWindowSize(originalWindowSize.withWidth(originalWindowSize.width - zonesLayerWidth))
          })

          layers.addLayer(TransformingChartStateLayer(zonesLayer) {
            it.withAdditionalTranslation(Distance.of(chartSupport.currentChartState.windowWidth - zonesLayerWidth, 0.0))
          })

          section("Beams")

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


          section("Zones")

          configurableDouble("Zones Gap", zonesLayer.style::zonesGap) {
            max = 40.0
          }

          configurableDouble("horizontal line segment", zonesLayer.style::horizontalLineSegmentLength) {
            max = 30.0
          }
        }
      }
    }
  }
}
