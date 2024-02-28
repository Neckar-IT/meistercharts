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
import com.meistercharts.color.ColorMapperNullable
import com.meistercharts.color.RgbaColor
import com.meistercharts.font.FontDescriptorFragment
import com.meistercharts.font.FontSize
import com.meistercharts.font.FontWeight
import it.neckar.open.provider.MultiProvider


/**
 * Represents the settings for a theme
 */
interface Theme {
  /**
   * The id of the theme
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
   * The color for the default lines (e.g., borders, axis)
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

  /**
   * The text color that is used by default
   */
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
  val borderColorConverter: ColorMapperNullable

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
   * Returns all colors that are used by the theme
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

  companion object {
    val axisTitleFont: ThemeKey<FontDescriptorFragment> = ThemeKey("axis.title.font") {
      it.h5
    }

    val axisTitleColor: ThemeKey<Color> = ThemeKey("axis.title.color") {
      it.h5Color
    }

    val axisTickFont: ThemeKey<FontDescriptorFragment> = ThemeKey("axis.tick.font") {
      it.textFont
    }

    val thresholdLabelFont: ThemeKey<FontDescriptorFragment> = ThemeKey("threshold.label.font") {
      it.textFont
    }

    /**
     * Font for the "main" value
     */
    val mainValueLabelFont: ThemeKey<FontDescriptorFragment> = ThemeKey("main.value.label.font") {
      it.h4
    }

    val subValueLabelFont: ThemeKey<FontDescriptorFragment> = ThemeKey("sub.value.label.font") {
      it.h5
    }

    /**
     * Font for the offset tick labels
     */
    val offsetTickFont: ThemeKey<FontDescriptorFragment> = ThemeKey("offset.tick.font") {
      it.textFont.withWeight(FontWeight.Bold)
    }

    val axisTickColor: ThemeKey<Color> = ThemeKey("axis.tick.color") {
      it.textColor
    }

    val axisLineColor: ThemeKey<Color> = ThemeKey("axis.line.color") {
      it.defaultLineColor
    }

    val secondaryBackgroundColor: ThemeKey<Color> = ThemeKey("background.color.secondary") {
      it.secondaryBackgroundColor
    }

    val primaryBackgroundColor: ThemeKey<Color> = ThemeKey("background.color.primary") {
      it.primaryBackgroundColor
    }

    val axisBackgroundColor: ThemeKey<Color> = ThemeKey("background.color.axis") {
      it.primaryBackgroundColor.withAlpha(0.5)
    }

    val canvasBackgroundColor: ThemeKey<Color> = ThemeKey("background.color.canvas") {
      it.canvasBackgroundColor
    }

    val chartColors: ThemeKey<MultiProvider<Any, Color>> = ThemeKey("chart.colors") {
      it.chartColors
    }

    val enumColors: ThemeKey<MultiProvider<Any, Color>> = ThemeKey("chart.enum.colors") {
      it.enumColors
    }

    val inactiveElementBorderColor: ThemeKey<Color> = ThemeKey("inactive.element.border.colors") {
      it.inactiveElementBorder
    }

    /**
     * Generates a border-color from a given color
     */
    val borderColorConverter: ThemeKey<ColorMapperNullable> = ThemeKey("border.color.converter") {
      it.borderColorConverter
    }

    val primaryButtonBackgroundColors: ThemeKey<ButtonColorProvider> = ThemeKey("button.primary.background.colors") {
      it.primaryButtonBackgroundColors
    }

    val primaryButtonForegroundColors: ThemeKey<ButtonColorProvider> = ThemeKey("button.primary.foreground.colors") {
      it.primaryButtonForegroundColors
    }

    val secondaryButtonBackgroundColors: ThemeKey<ButtonColorProvider> = ThemeKey("button.secondary.background.colors") {
      it.secondaryButtonBackgroundColors
    }

    val secondaryButtonForegroundColors: ThemeKey<ButtonColorProvider> = ThemeKey("button.secondary.foreground.colors") {
      it.secondaryButtonForegroundColors
    }

    val buttonFont: ThemeKey<FontDescriptorFragment> = ThemeKey("button.font") {
      it.h5
    }

    /**
     * The font for a slogan
     */
    val sloganFont: ThemeKey<FontDescriptorFragment> = ThemeKey("slogan.font") {
      FontDescriptorFragment(family = it.h1.family, size = FontSize(170.0), weight = FontWeight.ExtraLight)
    }

    /**
     * The line color of the cross wire
     */
    val crossWireLineColor: ThemeKey<Color> = ThemeKey("cross.wire.line.color") {
      it.crossWireLineColor
    }

    val backgroundColorActive: ThemeKey<Color> = ThemeKey("background.active") {
      it.backgroundColorActive
    }

    val backgroundZebra: ThemeKey<MultiProvider<Any, Color>> = ThemeKey("background zebra") {
      it.backgroundZebraColors
    }

    /**
     * The color to be used for shadows
     */
    val shadowColor: ThemeKey<Color> = ThemeKey("shadow.color") {
      it.shadowColor
    }

    /**
     * Returns the state colors in this order:
     * Ok, Warning, Error, Unknown
     */
    val stateColors: ThemeKey<MultiProvider<Any, Color>> = ThemeKey("state.colors") {
      State.all
    }
  }

  object State {
    val ok: ThemeKey<Color> = ThemeKey("state.ok") {
      it.stateOk
    }
    val warning: ThemeKey<Color> = ThemeKey("state.warning") {
      it.stateWarning
    }
    val error: ThemeKey<Color> = ThemeKey("state.error") {
      it.stateError
    }
    val unknown: ThemeKey<Color> = ThemeKey("state.unknown") {
      it.stateUnknown
    }

    val all: MultiProvider<Any, Color> = MultiProvider.moduloProvider(ok.provider(), warning.provider(), error.provider(), unknown.provider())
  }

}

