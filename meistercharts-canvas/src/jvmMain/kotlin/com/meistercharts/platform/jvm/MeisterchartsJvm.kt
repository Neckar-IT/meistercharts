package com.meistercharts.platform.jvm

import com.meistercharts.canvas.JvmImageLoader
import com.meistercharts.resources.JvmLocalResourcePaintableFactory
import it.neckar.open.time.JvmTimerSupport
import it.neckar.open.time.jvmTimerSupport

/**
 * Contains the services and constants for Meistercharts on the JVM
 */
object MeisterchartsJvm {
  var jvmImageLoader: JvmImageLoader = JvmImageLoader { _, _ ->
    throw UnsupportedOperationException("please set the jvmImageLoader for the current platform by calling MeisterChartsPlatform.init()")
  }

  /**
   * Reference to the current [JvmTimerSupport]
   */
  var timerSupport: JvmTimerSupport by ::jvmTimerSupport

  /**
   * Holds the current instance of the local resource paintable provider
   */
  var localResourcePaintableFactory: JvmLocalResourcePaintableFactory = JvmLocalResourcePaintableFactory { _, _, _ ->
    throw UnsupportedOperationException("please set the jvmLocalResourcePaintableFactory for the current platform by calling MeisterChartPlatform.init()")
  }

}
