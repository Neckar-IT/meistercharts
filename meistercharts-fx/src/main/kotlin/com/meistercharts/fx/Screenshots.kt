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
package com.meistercharts.fx

import it.neckar.open.kotlin.lang.ceil
import it.neckar.open.javafx.screenshot
import it.neckar.open.javafx.toPng
import javafx.scene.SnapshotParameters
import javafx.scene.canvas.Canvas
import javafx.scene.image.WritableImage
import javafx.scene.layout.Region
import java.io.File
import java.io.OutputStream

/**
 * Methods related to creating screenshots
 */

/**
 * Creates a screenshot form a canvas
 */
fun Canvas.screenshot(): WritableImage {
  val writableImage = WritableImage((width).ceil().toInt(), (height).ceil().toInt())

  val snapshotParameters = SnapshotParameters()
  //snapshotParameters.transform = Transform.scale(factor, factor)

  snapshot(snapshotParameters, writableImage)
  return writableImage
}
