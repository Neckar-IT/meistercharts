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
