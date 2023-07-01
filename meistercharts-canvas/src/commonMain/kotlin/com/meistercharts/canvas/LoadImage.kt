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

import com.meistercharts.Meistercharts
import it.neckar.open.http.Url


/**
 * Loads an image from a URL.
 * The loaded image has its natural size set and will be passed to [callback]
 */
fun loadImage(url: Url, callback: (Image) -> Unit) {
  val cachedImage = Meistercharts.imageCache[url]
  if (cachedImage != null) {
    callback(cachedImage)
  } else {
    loadImageUncached(url) { image ->
      Meistercharts.imageCache.store(url, image)
      callback(image)
    }
  }
}

/**
 * Loads an image from a URL.
 * This function should not cache the loaded image.
 */
expect fun loadImageUncached(url: Url, callback: (Image) -> Unit)
