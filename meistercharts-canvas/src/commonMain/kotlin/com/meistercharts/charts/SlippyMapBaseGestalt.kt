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
package com.meistercharts.charts

import com.meistercharts.algorithms.UpdateReason
import com.meistercharts.algorithms.axis.AxisOrientationY
import com.meistercharts.algorithms.impl.ZoomAndTranslationModifiersBuilder
import com.meistercharts.algorithms.layers.slippymap.SlippyMapCenter
import com.meistercharts.algorithms.layers.slippymap.calculateSlippyMapContentAreaSize
import com.meistercharts.algorithms.layers.slippymap.withSlippyMapZoom
import com.meistercharts.canvas.FixedContentAreaSize
import com.meistercharts.canvas.MeisterChartBuilder
import com.meistercharts.canvas.SnapConfiguration
import com.meistercharts.canvas.devicePixelRatioSupport
import com.meistercharts.canvas.pixelSnapSupport

/**
 * This gestalt can be used to configure MeisterCharts for slippy map
 */
class SlippyMapBaseGestalt : ChartGestalt {
  override fun configure(meisterChartBuilder: MeisterChartBuilder) {
    with(meisterChartBuilder) {
      contentAreaSizingStrategy = FixedContentAreaSize(calculateSlippyMapContentAreaSize())

      //Is usually overwritten
      zoomAndTranslationDefaults(SlippyMapCenter.neckarItCenter)

      zoomAndTranslationModifier = ZoomAndTranslationModifiersBuilder()
        .withSlippyMapZoom()
        .build()

      // This is important! This is the change factor between slippy map zoom levels
      //Slippy map does only support that factor
      zoomChangeFactor = 2.0

      configure {
        //Origin at top for slippy map
        chartSupport.rootChartState.axisOrientationY = AxisOrientationY.OriginAtTop

        //Snapping should be disabled for tiling
        chartSupport.pixelSnapSupport.snapConfiguration = SnapConfiguration.None

        //Update the content area size if the device pixel ratio has been updated
        chartSupport.devicePixelRatioSupport.devicePixelRatioProperty.consume {
          chartSupport.rootChartState.contentAreaSizeProperty.value = calculateSlippyMapContentAreaSize()
          chartSupport.zoomAndTranslationSupport.resetToDefaults(reason = UpdateReason.EnvironmentUpdate)
        }
      }

    }

  }
}
