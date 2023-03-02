package com.meistercharts.canvas

/**
 * Sometimes relative URLs have to be converted - e.g. when using React Router which changes the URLs in a non-predictable way.
 */
object UrlConversion {
  /**
   * Converts the URL
   */
  fun convert(url: String): String {
    return converter.convert(url)
  }

  var converter: UrlConverter = UrlConverter.Noop

  /**
   * Activates the given url converter (replaces the existing converter)
   */
  fun activate(converter: UrlConverter) {
    UrlConversion.converter = converter
  }
}

/**
 * Converts URLs (e.g. to absolute)
 */
fun interface UrlConverter {
  /**
   * Converts a URL
   */
  fun convert(url: String): String

  companion object {
    /**
     * Default implementation - does not change the URL at all
     */
    val Noop: UrlConverter = UrlConverter { it }

    /**
     * Makes each URL absolute
     */
    val MakeAbsolute: UrlConverter = UrlConverter { url ->
      if (isExternalUrl(url)) {
        return@UrlConverter url
      }
      if (isAbsoluteUrl(url)) {
        return@UrlConverter url
      }

      return@UrlConverter "/$url"
    }
  }
}

private fun isAbsoluteUrl(url: String): Boolean {
  return url.startsWith("/")
}

private fun isExternalUrl(url: String): Boolean {
  return url.startsWith("http")
}

