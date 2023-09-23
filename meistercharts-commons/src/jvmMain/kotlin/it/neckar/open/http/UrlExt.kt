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

/**
 * Converts a file to a URL
 */
fun File.toUrl(): Url {
  //Java creates URIs with only a single "/"
  val asciiString = toURI().toASCIIString().replace("file:/", "file:///")
  return Url(asciiString)
}
