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
 * Another corporate design that uses Segoe UI
 */
object SegoeUiDesign : CorporateDesign {
  override val id: String = "Segoe UI Design"

  val headlineFontFamily: FontFamilyConfiguration = FontFamily("Segoe UI").withSansSerif()
  val defaultFontFamily: FontFamilyConfiguration = FontFamily("Segoe UI").withSansSerif()

  override val primaryColor: Color = Color.web("#0084c2")

  override val primaryColorDarker: Color = Color.web("#004d72")
  override val primaryColorLighter: Color = Color.web("#03adff")
  override val secondaryColor: Color = Color.web("#EE9624")

  override val defaultLineColor: Color = Color.web("#737f85")

  override val h1: FontDescriptorFragment = FontDescriptorFragment(headlineFontFamily, FontSize(30.0))
  override val h1Color: Color = primaryColor

  override val h2: FontDescriptorFragment = FontDescriptorFragment(headlineFontFamily, FontSize(26.0))
  override val h2Color: Color = primaryColor

  override val h3: FontDescriptorFragment = FontDescriptorFragment(headlineFontFamily, FontSize(22.0), FontWeight.Bold)
  override val h3Color: Color = Color("#555555")

  override val h4: FontDescriptorFragment = FontDescriptorFragment(headlineFontFamily, FontSize(18.0))
  override val h4Color: Color = Color("#555555")

  override val h5: FontDescriptorFragment = FontDescriptorFragment(headlineFontFamily, FontSize(14.0))
  override val h5Color: Color = Color("#555555")

  override val textFont: FontDescriptorFragment = FontDescriptorFragment(defaultFontFamily, FontSize(14.0), FontWeight.Normal, style = FontStyle.Normal, variant = FontVariant.Normal)
  override val textColor: Color = Color("#555555")

  override val backgroundColorLight: Color = Color.white
  override val backgroundColorDark: Color = Color.darkgray

  override val inactiveElementBorder: Color = Color("#C5CACC")

  override val backgroundColorActive: Color = Color.silver.withAlpha(0.5)

  override val chartColors: MultiProvider<Any, Color> = MultiProvider.forListModulo(
    listOf(
      Color("#005B8E"),
      Color("#007CC1"),
      Color("#009DF4"),
      Color("#3B5E43"),
      Color("#00B9AF"),
      Color("#ABC444"),
      Color("#691A12"),
      Color("#B96400"),
      Color("#C19C31"),
      Color("#7000B0"),
      Color("#B501EB"),
      Color("#FF00ED"),
    )
  )

  override val enumColors: MultiProvider<Any, Color> = MultiProvider.forListModulo(
    listOf(
      //From figma
      Color("#8E989D"),
      Color("#C5CACC"),

      //own values
      Color("#5C6366"),
      Color("#CFDEE6"),
      Color("#DAEAF2"),
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
    disabledColor = Color.white,
    pressedColor = Color.web("#002e46"),
    hoverColor = Color.white,
    focusedColor = Color.white,
    defaultColor = Color.white,
  )

  override val secondaryButtonBackgroundColors: ButtonColorProvider = DefaultButtonColorProvider(
    disabledColor = Color.web("#A9A9A9"), // guessed
    pressedColor = Color.web("#009EEF"), // guessed
    hoverColor = Color.web("#009EEF"), // guessed
    focusedColor = Color.web("#009EEF"), // guessed
    defaultColor = Color.rgb(0, 127, 195),
  )

  override val secondaryButtonForegroundColors: ButtonColorProvider = SingleButtonColorProvider(Color.white) // guessed

  override val stateOk: Color = Color.web("#63b017")
  override val stateWarning: Color = Color.web("#F5C413")
  override val stateError: Color = Color.web("#EA0823")
  override val stateUnknown: Color = Color.web("#737F85")
}
