package it.neckar.open.http

import java.awt.image.BufferedImage
import java.io.File
import java.net.URL

/**
 * Converts to a [java.net.URL]
 */
fun Url.toURL(): URL {
  return URL(value)
}

fun Url.Companion.fromURL(url: URL): Url {
  return Url.parse(url.toExternalForm())
}

fun Url.Companion.fromFile(file: File): Url {
  return fromURL(file.toURI().toURL())
}

/**
 * Converts a file to a URL
 */
fun File.toUrl(): Url.Absolute {
  //Java creates URIs with only a single "/"
  val asciiString = toURI().toASCIIString().replace("file:/", "file:///")
  return Url.absolute(asciiString)
}

/**
 * Loads the image from the data scheme.
 *
 * Will throw an exception if the media type is not an imgae
 */
fun Url.DataScheme.loadImage(): BufferedImage {
  require(isImage()) {
    "Is not an image. Media type: $mediaType"
  }

  return javax.imageio.ImageIO.read(dataBytes.inputStream())
}
