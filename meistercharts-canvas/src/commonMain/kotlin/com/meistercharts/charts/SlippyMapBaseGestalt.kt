package com.meistercharts.charts

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
          chartSupport.zoomAndTranslationSupport.resetToDefaults()
        }
      }

    }

  }
}
