package com.meistercharts.model

/**
 * Which sides of a rectangle are selected
 */
data class SidesSelection(
  val leftSelected: Boolean = false,
  val rightSelected: Boolean = false,
  val topSelected: Boolean = false,
  val bottomSelected: Boolean = false
) {

  companion object {
    /**
     * All sides are selected
     */
    val all: SidesSelection = SidesSelection(leftSelected = true, rightSelected = true, topSelected = true, bottomSelected = true)

    /**
     * Top and bottom side are selected
     */
    val topAndBottom: SidesSelection = SidesSelection(topSelected = true, bottomSelected = true)

    /**
     * Left and right side are selected
     */
    val leftAndRight: SidesSelection = SidesSelection(leftSelected = true, rightSelected = true)

    /**
     * No side is selected
     */
    val none: SidesSelection = SidesSelection()

    /**
     * Only the left side is selected
     */
    val onlyLeft: SidesSelection = SidesSelection(leftSelected = true)

    /**
     * Only the right side is selected
     */
    val onlyRight: SidesSelection = SidesSelection(rightSelected = true)

    /**
     * Only the top side is selected
     */
    val onlyTop: SidesSelection = SidesSelection(topSelected = true)

    /**
     * Only the bottom side is selected
     */
    val onlyBottom: SidesSelection = SidesSelection(bottomSelected = true)
  }
}
