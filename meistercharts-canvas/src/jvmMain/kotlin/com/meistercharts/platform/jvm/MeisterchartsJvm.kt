package com.meistercharts.platform.jvm

import com.meistercharts.canvas.JvmImageLoader

/**
 * Contains the services and constants for Meistercharts on the JVM
 */
object MeisterchartsJvm {
  var jvmImageLoader: JvmImageLoader = JvmImageLoader { _, _ ->
    throw UnsupportedOperationException("please set the jvmImageLoader for the current platform by calling MeisterChartsPlatform.init()")
  }

}
