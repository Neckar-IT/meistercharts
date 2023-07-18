package it.neckar.open.http

import java.net.URL

/**
 * Converts to a [java.net.URL]
 */
fun Url.toURL(): URL {
  return URL(value)
}
