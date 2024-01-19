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
package com.meistercharts.design

import com.meistercharts.canvas.paintable.ButtonColorProvider
import com.meistercharts.color.Color
import com.meistercharts.color.RgbaColor
import com.meistercharts.font.FontDescriptorFragment
import it.neckar.logging.Logger
import it.neckar.logging.LoggerFactory
import it.neckar.logging.debug
import it.neckar.open.provider.MultiProvider

/**
 * Manages the corporate design settings
 */

private val logger: Logger = LoggerFactory.getLogger("com.meistercharts.design.CorporateDesign")


/**
 * The corporate design that has been configured.
 * The design is configured from the MeisterChartsPlatform
 *
 * Is only evaluated directly after startup. Later changes to the corporate design are not supported.
 */
var corporateDesign: CorporateDesign = DefaultDesign
  /**
   * This method should not be called directly.
   * Instead, use MeisterChartsPlatform#init
   */
  private set

/**
 * Applies the corporate design
 */
fun initCorporateDesign(newCorporateDesign: CorporateDesign) {
  logger.debug { "init corporate design: ${newCorporateDesign.id}" }
  corporateDesign = newCorporateDesign
}

/**
 * Represents the settings for a corporate design
 */
interface CorporateDesign {
  /**
   * The id of the corporate design
   */
  val id: String

  /**
   * The primary color, usually combined with [primaryBackgroundColor]
   */
  val primaryColor: RgbaColor

  val primaryColorDarker: RgbaColor
  val primaryColorLighter: RgbaColor

  /**
   * The secondary color, usually combined with [secondaryBackgroundColor]
   */
  val secondaryColor: RgbaColor

  /**
   * The color for the default lines (e.g. borders, axis)
   */
  val defaultLineColor: RgbaColor

  val crossWireLineColor: RgbaColor
    get() {
      return defaultLineColor
    }

  val h1: FontDescriptorFragment
  val h1Color: RgbaColor

  val h2: FontDescriptorFragment
  val h2Color: RgbaColor

  val h3: FontDescriptorFragment
  val h3Color: RgbaColor

  val h4: FontDescriptorFragment
  val h4Color: RgbaColor

  val h5: FontDescriptorFragment
  val h5Color: RgbaColor

  /**
   * The default text font that is applied to the canvas automatically.
   * Uses the defaults for all values that are not provided
   */
  val textFont: FontDescriptorFragment
  val textColor: RgbaColor

  /**
   * The color the canvas is filled with
   */
  val canvasBackgroundColor: RgbaColor

  /**
   * The primary background-color, usually combined with [primaryColor]
   */
  val primaryBackgroundColor: RgbaColor

  /**
   * The secondary background-color, usually combined with [secondaryColor]
   */
  val secondaryBackgroundColor: RgbaColor

  /**
   * Active background (usually some kind of transparent gray)
   */
  val backgroundColorActive: RgbaColor

  val inactiveElementBorder: RgbaColor

  /**
   * Generates a border-color from a given fill color.
   *
   * If there is no fill color, the default border color should be returned.
   * If this converter returns null, the default border color is used.
   */
  val borderColorConverter: (fill: Color?) -> Color?

  /**
   * Colors to be used for charts
   */
  val chartColors: MultiProvider<Any, RgbaColor>

  /**
   * The colors that are used for zebra backgrounds (e.g. in tables, offset areas).
   */
  val backgroundZebraColors: MultiProvider<Any, RgbaColor>

  /**
   * Colors to be used to visualize enums in the chart
   */
  val enumColors: MultiProvider<Any, RgbaColor>

  /**
   * A primary button helps users to complete their journey.
   * Typically, such a button is labelled 'next', 'complete', 'start'.
   *
   * This provides the background color for a primary button with the given state.
   */
  val primaryButtonBackgroundColors: ButtonColorProvider

  /**
   * A primary button helps users to complete their journey.
   * Typically, such a button is labelled 'next', 'complete', 'start'.
   *
   * This provides the foreground color for a primary button with the given state.
   */
  val primaryButtonForegroundColors: ButtonColorProvider

  /**
   * Secondary buttons offer alternatives to the actions offered by a primary button.
   * Typically, a secondary button offers the ‘go back’-action while the primary button offers the ‘next’-action, or the ‘cancel’-action opposed to to the ‘submit’-action.
   *
   * This provides the background color for a secondary button with the given state.
   */
  val secondaryButtonBackgroundColors: ButtonColorProvider

  /**
   * Secondary buttons offer alternatives to the actions offered by a primary button.
   * Typically, a secondary button offers the ‘go back’-action while the primary button offers the ‘next’-action, or the ‘cancel’-action opposed to to the ‘submit’-action.
   *
   * This provides the foreground color for a secondary button with the given state.
   */
  val secondaryButtonForegroundColors: ButtonColorProvider

  val stateOk: RgbaColor
  val stateWarning: RgbaColor
  val stateError: RgbaColor
  val stateUnknown: RgbaColor

  /**
   * The color to be used for shadows
   */
  val shadowColor: RgbaColor

  /**
   * Resolves the given theme key.
   * This method can be overridden by implementations if required.
   */
  fun <T> resolve(key: ThemeKey<T>): T {
    return key.defaultStyleProvider(this)
  }

  /**
   * Returns all colors that are used by the corporate design
   */
  fun colors(): Set<RgbaColor> {
    return setOf(
      primaryColor, primaryColorDarker, primaryColorLighter,
      secondaryColor,
      defaultLineColor,
      inactiveElementBorder,
      shadowColor,
      backgroundColorActive,
      crossWireLineColor,
      h1Color,
      h2Color,
      h3Color,
      h4Color,
      h5Color,
      textColor
    )
  }

}

