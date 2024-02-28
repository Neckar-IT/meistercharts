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
package com.meistercharts.style

import com.meistercharts.color.Color
import com.meistercharts.color.ColorProvider
import com.meistercharts.color.RgbaColor
import com.meistercharts.color.RgbaColorProvider
import it.neckar.open.kotlin.lang.asProvider
import it.neckar.open.kotlin.lang.getModulo

/**
 * Contains default colors that can be used if no colors are provided
 */
object Palette {
  /**
   * A "nice" gray that is used as default at a lot of places
   */
  val defaultGray: ColorProvider = Color.rgb(115, 127, 133).asProvider()

  val primaryColors: List<RgbaColorProvider> = listOf(
    Color.rgb(0, 161, 229).asProvider(), //first
    Color.rgb(0, 46, 70).asProvider(), //second
    Color.rgb(159, 213, 216).asProvider(), //third
  )

  /**
   * Returns the primary color for the given index (modulo)
   */
  fun getPrimaryColor(index: Int): RgbaColorProvider = primaryColors.getModulo(index)

  val chartColors: List<RgbaColorProvider> = listOf(
    Color.rgb(0, 161, 229).asProvider(),
    Color.rgb(0, 46, 70).asProvider(),
    Color.rgb(159, 213, 216).asProvider(),
    Color.rgb(0, 104, 150).asProvider(),
    Color.rgb(118, 199, 238).asProvider(),
    Color.rgb(59, 145, 129).asProvider(),
  )

  /**
   * Returns a chart color for the given index (modulo)
   */
  fun getChartColor(index: Int): RgbaColorProvider = chartColors.getModulo(index)

  /** 'green'; could also be used for an OK-state */
  val stateSuperior: RgbaColorProvider = Color.rgb(146, 194, 89).asProvider()

  /** 'blue' */
  val stateNormal: RgbaColorProvider = Color.rgb(0, 161, 229).asProvider()

  /** 'yellow' */
  val stateWarning: RgbaColorProvider = Color.rgb(243, 197, 0).asProvider()

  /** 'orange' */
  val stateCritical: RgbaColorProvider = Color.rgb(241, 132, 34).asProvider()

  /** 'red' */
  val stateError: RgbaColorProvider = Color.rgb(230, 44, 86).asProvider()

  /** 'dark blue'; could also be used for an unknown-state */
  val stateOffline: RgbaColorProvider = Color.rgb(0, 46, 70).asProvider()

  /**
   * Contains all state colors
   */
  val stateColors: List<RgbaColorProvider> = listOf(stateSuperior, stateNormal, stateWarning, stateCritical, stateError, stateOffline)

  /**
   * Contains all palettes
   */
  val all: List<List<RgbaColorProvider>> = listOf(primaryColors, chartColors, stateColors)
}
