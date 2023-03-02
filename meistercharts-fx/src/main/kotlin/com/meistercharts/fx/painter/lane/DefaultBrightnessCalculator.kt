package com.meistercharts.fx.painter.lane

/**
 * Default implementation that uses the average of the brightnesses provided by the edges
 *
 */
class DefaultBrightnessCalculator : LanesInformationBuilder.BrightnessCalculator {

  override fun calculateBrightness(lowerEdge: LanesInformation.Edge?, upperEdge: LanesInformation.Edge?): Double {
    var sum = 0.0
    var count = 0.0

    if (lowerEdge != null) {
      sum += lowerEdge.brightnessAbove
      count++
    }

    if (upperEdge != null) {
      sum += upperEdge.brightnessBelow
      count++
    }

    return if (count == 0.0) {
      //Default value if there are no active edges around
      0.0
    } else Math.max(0.0, Math.min(1.0, sum / count))
  }
}
