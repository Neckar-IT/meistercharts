package com.meistercharts.algorithms.layers

import com.meistercharts.algorithms.environment
import com.meistercharts.algorithms.layers.text.LinesProvider
import com.meistercharts.algorithms.layers.text.addText
import com.meistercharts.model.Direction
import com.meistercharts.model.DirectionBasedBasePointProvider

/**
 * Adds an [Layer] that displays information about the environment
 *
 */
fun Layers.addEnvironment() {
  val textsProvider: LinesProvider = { _, _ ->
    val lines = mutableListOf<String>()
    lines.add("multiTouchSupported: ${environment.multiTouchSupported}")
    lines.add("devicePixelRatio: ${environment.devicePixelRatio}")
    lines
  }

  addText(textsProvider) {
    anchorDirection = Direction.CenterLeft
    anchorPointProvider = DirectionBasedBasePointProvider(anchorDirection)
  }
}
