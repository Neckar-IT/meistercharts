package com.meistercharts

import com.meistercharts.canvas.MeisterChartsPlatformState
import com.meistercharts.loop.RenderLoopSupport

/**
 * Contains all static references to all meistercharts related classes
 */
object Meistercharts {
  /**
   * Offers access to the game loop
   */
  val renderLoop: RenderLoopSupport = RenderLoopSupport()

  /**
   * Contains information about the platform state - especially the active instances
   */
  val platformState: MeisterChartsPlatformState = MeisterChartsPlatformState()
}
