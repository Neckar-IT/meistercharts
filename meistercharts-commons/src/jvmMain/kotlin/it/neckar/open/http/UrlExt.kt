package it.neckar.open.http

import java.io.File
import java.net.URL

/**
 * Converts to a [java.net.URL]
 */
fun Url.toURL(): URL {
  return URL(value)
}

fun Url.Companion.fromURL(url: URL): Url {
  return Url(url.toExternalForm())
}

fun Url.Companion.fromFile(file: File): Url {
  return fromURL(file.toURI().toURL())
}
