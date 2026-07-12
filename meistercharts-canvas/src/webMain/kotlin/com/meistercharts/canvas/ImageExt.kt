/*
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

import it.neckar.geometry.Coordinates
import it.neckar.geometry.Size
import it.neckar.open.unit.other.px
import org.w3c.dom.HTMLCanvasElement
import org.w3c.dom.HTMLImageElement

/**
 * Creates a new [Image] from a [javafx.scene.image.Image]
 */
fun Image.Companion.create(
  image: HTMLImageElement,

  /**
   * The size - if not set, the natural size of the image is used
   */
  size: @px Size = image.naturalSize,
  /**
   * The alignment point of the image.
   * Is useful for images that have a "natural" base - e.g. the tip of an arrow
   */
  alignmentPoint: Coordinates = Coordinates.origin,
): Image {
  @Suppress("DEPRECATION")
  return Image._fromPlatform(image, size, alignmentPoint)
}

fun Image.Companion.create(
  image: HTMLCanvasElement,

  /**
   * The size - if not set, the natural size of the image is used
   */
  size: @px Size = image.size,
  /**
   * The alignment point of the image.
   * Is useful for images that have a "natural" base - e.g. the tip of an arrow
   */
  alignmentPoint: Coordinates = Coordinates.origin,
): Image {
  @Suppress("DEPRECATION")
  return Image._fromPlatform(image, size, alignmentPoint)
}

val HTMLImageElement.size: Size
  get() {
    return Size(width.toDouble(), height.toDouble())
  }

val HTMLImageElement.naturalSize: Size
  get() {
    return Size(naturalWidth.toDouble(), naturalHeight.toDouble())
  }
