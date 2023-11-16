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
import com.meistercharts.font.FontDescriptorFragment
import it.neckar.open.provider.MultiProvider

/**
 * Manages the corporate design settings
 */


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
   * The primary color
   */
  val primaryColor: Color

  val primaryColorDarker: Color
  val primaryColorLighter: Color

  /**
   * The secondary color
   */
  val secondaryColor: Color

  /**
   * The color for the default lines (e.g. borders, axis)
   */
  val defaultLineColor: Color

  val crossWireLineColor: Color
    get() {
      return defaultLineColor
    }

  val h1: FontDescriptorFragment
  val h1Color: Color

  val h2: FontDescriptorFragment
  val h2Color: Color

  val h3: FontDescriptorFragment
  val h3Color: Color

  val h4: FontDescriptorFragment
  val h4Color: Color

  val h5: FontDescriptorFragment
  val h5Color: Color

  /**
   * The default text font that is applied to the canvas automatically.
   * Uses the defaults for all values that are not provided
   */
  val textFont: FontDescriptorFragment
  val textColor: Color

  val backgroundColorLight: Color
  val backgroundColorDark: Color

  /**
   * Active background (usually some kind of transparent gray)
   */
  val backgroundColorActive: Color

  val inactiveElementBorder: Color

  /**
   * Colors to be used for charts
   */
  val chartColors: MultiProvider<Any, Color>

  /**
   * Colors to be used to visualize enums in the chart
   */
  val enumColors: MultiProvider<Any, Color>

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

  val stateOk: Color
  val stateWarning: Color
  val stateError: Color
  val stateUnknown: Color

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
  fun colors(): Set<Color> {
    return setOf(
      primaryColor, primaryColorDarker, primaryColorLighter,
      secondaryColor,
      defaultLineColor,
      h1Color,
      h2Color,
      h3Color,
      h4Color,
      h5Color,
      textColor
    )
  }

}

