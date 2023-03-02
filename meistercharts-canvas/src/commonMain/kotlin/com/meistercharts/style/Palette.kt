package com.meistercharts.style

import com.meistercharts.algorithms.painter.Color
import com.meistercharts.algorithms.painter.RgbaColor
import it.neckar.open.kotlin.lang.getModulo

/**
 * Contains default colors that can be used if no colors are provided
 */
object Palette {
  /**
   * A "nice" gray that is used as default at a lot of places
   */
  val defaultGray: Color = Color.rgb(115, 127, 133)

  val primaryColors: List<RgbaColor> = listOf(
    Color.rgb(0, 161, 229), //first
    Color.rgb(0, 46, 70), //second
    Color.rgb(159, 213, 216) //third
  )

  /**
   * Returns the primary color for the given index (modulo)
   */
  fun getPrimaryColor(index: Int): Color = primaryColors.getModulo(index)

  val chartColors: List<RgbaColor> = listOf(
    Color.rgb(0, 161, 229),
    Color.rgb(0, 46, 70),
    Color.rgb(159, 213, 216),
    Color.rgb(0, 104, 150),
    Color.rgb(118, 199, 238),
    Color.rgb(59, 145, 129)
  )

  /**
   * Returns a chart color for the given index (modulo)
   */
  fun getChartColor(index: Int): Color = chartColors.getModulo(index)

  /** 'green'; could also be used for an OK-state */
  val stateSuperior: RgbaColor = Color.rgb(146, 194, 89)

  /** 'blue' */
  val stateNormal: RgbaColor = Color.rgb(0, 161, 229)

  /** 'yellow' */
  val stateWarning: RgbaColor = Color.rgb(243, 197, 0)

  /** 'orange' */
  val stateCritical: RgbaColor = Color.rgb(241, 132, 34)

  /** 'red' */
  val stateError: RgbaColor = Color.rgb(230, 44, 86)

  /** 'dark blue'; could also be used for an unknown-state */
  val stateOffline: RgbaColor = Color.rgb(0, 46, 70)

  /**
   * Contains all state colors
   */
  val stateColors: List<RgbaColor> = listOf(stateSuperior, stateNormal, stateWarning, stateCritical, stateError, stateOffline)

  /**
   * Contains all palettes
   */
  val all: List<List<RgbaColor>> = listOf(primaryColors, chartColors, stateColors)
}
