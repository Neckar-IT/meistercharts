package com.meistercharts.canvas

import it.neckar.open.collections.Cache
import it.neckar.open.collections.cache


private val imageCache: Cache<String, Image> = cache("CachedImageDownloader", 100)

/**
 * Loads an image from a URL.
 * The loaded image has its natural size set and will be passed to [callback]
 */
fun loadImage(url: String, callback: (Image) -> Unit) {
  val cachedImage = imageCache[url]
  if (cachedImage != null) {
    callback(cachedImage)
  } else {
    loadImageUncached(url) { image ->
      imageCache.store(url, image)
      callback(image)
    }
  }
}

/**
 * Loads an image from an URL.
 * This function should not cache the loaded image passed to [callback]
 */
expect fun loadImageUncached(url: String, callback: (Image) -> Unit)
