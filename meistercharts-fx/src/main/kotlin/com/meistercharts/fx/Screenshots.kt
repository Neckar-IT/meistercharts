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
