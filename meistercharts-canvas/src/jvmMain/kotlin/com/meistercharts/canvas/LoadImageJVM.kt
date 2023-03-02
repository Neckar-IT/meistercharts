package com.meistercharts.canvas


/**
 * Loads an image from an URL (should not use caches)
 */
actual fun loadImageUncached(url: String, callback: (Image) -> Unit) {
  jvmImageLoader.loadImage(url, callback)
}

var jvmImageLoader: JvmImageLoader = JvmImageLoader { _, _ ->
  throw UnsupportedOperationException("please set the jvmImageLoader for the current platform by calling MeisterChartsPlatform.init()")
}

/**
 * Image loader for the JVM
 */
fun interface JvmImageLoader {
  fun loadImage(url: String, callback: (Image) -> Unit)
}
