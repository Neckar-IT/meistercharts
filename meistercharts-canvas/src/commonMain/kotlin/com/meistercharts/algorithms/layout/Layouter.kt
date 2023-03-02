package com.meistercharts.algorithms.layout

/**
 * A layouter that layouts elements (e.g. labels) with equal distance between the center - but without overlapping
 *
 */
class Layouter {

  /**
   * Returns the center points for each element
   * @param elementSizes the sizes for each element
   * @param  minSpaceBetweenElements the minimum space between two elements
   */
  fun calculateCenters(elementSizes: DoubleArray, minSpaceBetweenElements: Double = 0.0): DoubleArray {
    //Contains the center values
    val centers = DoubleArray(elementSizes.size)

    var startOfElement = 0.0

    for ((index, size) in elementSizes.withIndex()) {
      val position = startOfElement + size / 2.0 + minSpaceBetweenElements * index
      centers[index] = position

      startOfElement += size
    }

    return centers
  }

  /**
   * Returns the center points for each element.
   * All centers have the same distance between each other.
   */
  fun calculateEquidistantCenters(elementSizes: DoubleArray, minSpaceBetweenElements: Double = 0.0): DoubleArray {
    //Find the largest distance between two centers (without min space between the elements)
    val netDistance = elementSizes.findLargestDistanceBetweenCenters()

    //The distance between two elements
    val distance = netDistance + minSpaceBetweenElements

    //Contains the center values
    val centers = DoubleArray(elementSizes.size)

    var lastCenter = 0.0

    for ((index, size) in elementSizes.withIndex()) {
      val position = if (index == 0) {
        //The first element is placed only with the required space
        size / 2.0
      } else {
        lastCenter + distance
      }

      centers[index] = position
      lastCenter = position
    }

    return centers
  }
}

/**
 * Returns the largest distance between two center points
 */
fun DoubleArray.findLargestDistanceBetweenCenters(): Double {
  require(isNotEmpty()) { "must not be empty" }

  var largestDistance = -Double.MAX_VALUE

  for (i in 1 until size) {
    val previous = get(i - 1)
    val value = get(i)

    largestDistance = kotlin.math.max(largestDistance, (previous + value) / 2.0)
  }

  return largestDistance
}
