/*
 * Copyright (C) 2013-2026 Neckar IT GmbH, Mössingen, Germany
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Linking this library statically or dynamically with other modules is
 * making a combined work based on this library. Thus, the terms and
 * conditions of the GNU General Public License cover the whole combination.
 *
 * As a special exception, the copyright holders of this library give you
 * permission to link this library with independent modules, regardless of
 * the license terms of these independent modules, and to copy and distribute
 * the resulting combined work under terms of your choice, provided that every
 * copy of the combined work is accompanied by a complete copy of the source
 * code of this library.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package it.neckar.open.http

import java.awt.image.BufferedImage
import java.io.File
import java.net.URL
import javax.imageio.ImageIO

/**
 * Converts to a [java.net.URL]
 */
fun Url.toURL(): URL {
  if (this is Url.DataScheme) {
    throw IllegalArgumentException("Cannot convert data scheme to URL")
  }

  return URL(value)
}

fun Url.Companion.fromURL(url: URL): Url {
  return parse(url.toExternalForm())
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

  return ImageIO.read(dataBytes.inputStream())
}
