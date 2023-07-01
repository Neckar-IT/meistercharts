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

import it.neckar.open.http.Url

/**
 * Sometimes relative URLs have to be converted - e.g. when using React Router which changes the URLs in a non-predictable way.
 */
object UrlConversion {
  /**
   * Converts the URL
   */
  fun convert(url: Url): Url {
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
  fun convert(url: Url): Url

  companion object {
    /**
     * Default implementation - does not change the URL at all
     */
    val Noop: UrlConverter = UrlConverter { it }

    /**
     * Makes each URL absolute
     */
    val MakeAbsolute: UrlConverter = UrlConverter { url ->
      if (url.isExternalUrl()) {
        return@UrlConverter url
      }
      if (url.isAbsoluteUrl()) {
        return@UrlConverter url
      }

      return@UrlConverter Url("/$url")
    }
  }
}
