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
import com.meistercharts.canvas.paintable.DefaultButtonColorProvider
import com.meistercharts.canvas.paintable.SingleButtonColorProvider
import com.meistercharts.color.Color
import com.meistercharts.color.ColorMapperNullable
import com.meistercharts.color.RgbaColor
import com.meistercharts.font.FontDescriptorFragment
import com.meistercharts.font.FontFamily
import com.meistercharts.font.FontFamilyConfiguration
import com.meistercharts.font.FontSize
import com.meistercharts.font.FontStyle
import com.meistercharts.font.FontVariant
import com.meistercharts.font.FontWeight
import com.meistercharts.font.withSansSerif
import it.neckar.open.provider.MultiProvider

/**
 * Another theme that uses Segoe UI
 */
object SegoeUiTheme : Theme {
  override val id: String = "Segoe UI Theme"

  val headlineFontFamily: FontFamilyConfiguration = FontFamily("Segoe UI").withSansSerif()
  val defaultFontFamily: FontFamilyConfiguration = FontFamily("Segoe UI").withSansSerif()

  override val primaryColor: RgbaColor = Color.web("#0084c2").toRgba()

  override val primaryColorDarker: RgbaColor = Color.web("#004d72").toRgba()
  override val primaryColorLighter: RgbaColor = Color.web("#03adff").toRgba()
  override val secondaryColor: RgbaColor = Color.web("#EE9624").toRgba()

  override val defaultLineColor: RgbaColor = Color.web("#737f85").toRgba()

  override val h1: FontDescriptorFragment = FontDescriptorFragment(headlineFontFamily, FontSize(30.0))
  override val h1Color: RgbaColor = primaryColor

  override val h2: FontDescriptorFragment = FontDescriptorFragment(headlineFontFamily, FontSize(26.0))
  override val h2Color: RgbaColor = primaryColor

  override val h3: FontDescriptorFragment = FontDescriptorFragment(headlineFontFamily, FontSize(22.0), FontWeight.Bold)
  override val h3Color: RgbaColor = Color("#555555").toRgba()

  override val h4: FontDescriptorFragment = FontDescriptorFragment(headlineFontFamily, FontSize(18.0))
  override val h4Color: RgbaColor = Color("#555555").toRgba()

  override val h5: FontDescriptorFragment = FontDescriptorFragment(headlineFontFamily, FontSize(14.0))
  override val h5Color: RgbaColor = Color("#555555").toRgba()

  override val textFont: FontDescriptorFragment = FontDescriptorFragment(defaultFontFamily, FontSize(14.0), FontWeight.Normal, style = FontStyle.Normal, variant = FontVariant.Normal)
  override val textColor: RgbaColor = Color("#555555").toRgba()

  override val canvasBackgroundColor: RgbaColor = Color.white()
  override val primaryBackgroundColor: RgbaColor = Color.white()
  override val secondaryBackgroundColor: RgbaColor = Color.darkgray()

  override val inactiveElementBorder: RgbaColor = Color("#C5CACC").toRgba()
  override val borderColorConverter: ColorMapperNullable = { it?.toRgba()?.darker(0.15) ?: Color.darkgray() }

  override val backgroundColorActive: RgbaColor = Color.silver().withAlpha(0.5)

  override val backgroundZebraColors: MultiProvider<Any, RgbaColor> = MultiProvider.Companion.forListModulo(
    listOf(
      Color.web("#DBE1E5").toRgba(),
      Color.web("#F3F5F7").toRgba()
    )
  )

  override val chartColors: MultiProvider<Any, RgbaColor> = MultiProvider.forListModulo(
    listOf(
      Color("#005B8E").toRgba(),
      Color("#007CC1").toRgba(),
      Color("#009DF4").toRgba(),
      Color("#3B5E43").toRgba(),
      Color("#00B9AF").toRgba(),
      Color("#ABC444").toRgba(),
      Color("#691A12").toRgba(),
      Color("#B96400").toRgba(),
      Color("#C19C31").toRgba(),
      Color("#7000B0").toRgba(),
      Color("#B501EB").toRgba(),
      Color("#FF00ED").toRgba(),
    )
  )

  override val enumColors: MultiProvider<Any, RgbaColor> = MultiProvider.forListModulo(
    listOf(
      //From figma
      Color("#8E989D").toRgba(),
      Color("#C5CACC").toRgba(),

      //own values
      Color("#5C6366").toRgba(),
      Color("#CFDEE6").toRgba(),
      Color("#DAEAF2").toRgba(),
    )
  )

  override val primaryButtonBackgroundColors: ButtonColorProvider = DefaultButtonColorProvider(
    disabledColor = Color.web("#A9A9A9"), // guessed
    pressedColor = Color.web("#D96C25"),
    hoverColor = Color.web("#F6AA40"),
    focusedColor = Color.web("#F6AA40"),
    defaultColor = Color.web("#f39200"),
  )

  override val primaryButtonForegroundColors: ButtonColorProvider = DefaultButtonColorProvider(
    disabledColor = Color.white(),
    pressedColor = Color.web("#002e46").toRgba(),
    hoverColor = Color.white(),
    focusedColor = Color.white(),
    defaultColor = Color.white(),
  )

  override val secondaryButtonBackgroundColors: ButtonColorProvider = DefaultButtonColorProvider(
    disabledColor = Color.web("#A9A9A9").toRgba(), // guessed
    pressedColor = Color.web("#009EEF").toRgba(), // guessed
    hoverColor = Color.web("#009EEF").toRgba(), // guessed
    focusedColor = Color.web("#009EEF").toRgba(), // guessed
    defaultColor = Color.rgb(0, 127, 195),
  )

  override val secondaryButtonForegroundColors: ButtonColorProvider = SingleButtonColorProvider(Color.white) // guessed

  override val stateOk: RgbaColor = Color.web("#63b017").toRgba()
  override val stateWarning: RgbaColor = Color.web("#F5C413").toRgba()
  override val stateError: RgbaColor = Color.web("#EA0823").toRgba()
  override val stateUnknown: RgbaColor = Color.web("#737F85").toRgba()

  override val shadowColor: RgbaColor = Color.black().withAlpha(0.6)
}
