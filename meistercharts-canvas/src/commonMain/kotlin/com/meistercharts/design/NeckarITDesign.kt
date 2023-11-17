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
import com.meistercharts.color.Color
import com.meistercharts.font.FontDescriptorFragment
import com.meistercharts.font.FontFamily
import com.meistercharts.font.FontFamilyConfiguration
import com.meistercharts.font.FontSize
import com.meistercharts.font.FontStyle
import com.meistercharts.font.FontVariant
import com.meistercharts.font.FontWeight
import com.meistercharts.font.GenericFamily
import com.meistercharts.font.withGenericFamily
import com.meistercharts.style.Palette
import it.neckar.open.provider.MultiProvider

/**
 * The corporate design for the Neckar IT
 */
object NeckarITDesign : CorporateDesign {
  override val id: String = "Neckar IT"

  val headlineFontFamily: FontFamilyConfiguration = FontFamily("Oswald").withGenericFamily(GenericFamily.SansSerif)
  val defaultFontFamily: FontFamilyConfiguration = FontFamily.Verdana.withGenericFamily(GenericFamily.SansSerif)

  override val primaryColor: Color = Color.web("#00a1e5")

  override val primaryColorDarker: Color = Color.web("#002e46")
  override val primaryColorLighter: Color = Color.web("#9fd5d8")

  override val secondaryColor: Color = Color.web("#f3c500")
  override val defaultLineColor: Color = Color.web("#737f85")

  override val h1: FontDescriptorFragment = FontDescriptorFragment(headlineFontFamily, FontSize(50.0), FontWeight.Normal, style = FontStyle.Normal, variant = FontVariant.Normal)
  override val h1Color: Color = primaryColorDarker

  override val h2: FontDescriptorFragment = FontDescriptorFragment(headlineFontFamily, FontSize(40.0), FontWeight.SemiBold, style = FontStyle.Normal, variant = FontVariant.Normal)
  override val h2Color: Color = primaryColorDarker

  override val h3: FontDescriptorFragment = FontDescriptorFragment(headlineFontFamily, FontSize(27.0), FontWeight.Normal, style = FontStyle.Normal, variant = FontVariant.Normal)
  override val h3Color: Color = primaryColor

  override val h4: FontDescriptorFragment = FontDescriptorFragment(headlineFontFamily, FontSize(20.0), FontWeight.Normal, style = FontStyle.Normal, variant = FontVariant.Normal)
  override val h4Color: Color = primaryColorDarker

  override val h5: FontDescriptorFragment = FontDescriptorFragment(headlineFontFamily, FontSize(14.0), FontWeight.SemiBold, style = FontStyle.Normal, variant = FontVariant.Normal)
  override val h5Color: Color = primaryColorDarker

  override val textFont: FontDescriptorFragment = FontDescriptorFragment(defaultFontFamily, FontSize(14.0), FontWeight.Normal, style = FontStyle.Normal, variant = FontVariant.Normal)
  override val textColor: Color = primaryColorDarker

  override val backgroundColorLight: Color = Color.white
  override val backgroundColorDark: Color = Color.web("#002e46")

  override val inactiveElementBorder: Color = Color("#C5CACC")

  override val chartColors: MultiProvider<Any, Color> = MultiProvider.forListModulo(Palette.chartColors)

  override val enumColors: MultiProvider<Any, Color> = MultiProvider.forListModulo(
    listOf(
      Color("#8E989D"),
      Color("#C5CACC"),

      Color("#5C6366"),
      Color("#CFDEE6"),
      Color("#DAEAF2"),
    )
  )

  override val backgroundColorActive: Color = Color.silver.withAlpha(0.5)

  override val primaryButtonBackgroundColors: ButtonColorProvider = DefaultButtonColorProvider(
    disabledColor = Color.rgba(59, 145, 129, 0.3),
    pressedColor = Color.web("#9fd5d8"),
    hoverColor = Color.rgba(59, 145, 129, 0.7), // TODO shadow
    focusedColor = Color.rgba(59, 145, 129, 0.7), // TODO shadow
    defaultColor = Color.rgb(59, 145, 129)
  )

  override val primaryButtonForegroundColors: ButtonColorProvider = DefaultButtonColorProvider(
    disabledColor = Color.white,
    pressedColor = Color.web("#002e46"),
    hoverColor = Color.white,
    focusedColor = Color.white,
    defaultColor = Color.white
  )

  override val secondaryButtonBackgroundColors: ButtonColorProvider = DefaultButtonColorProvider(
    disabledColor = Color.rgba(0, 0, 0, 0.0),
    pressedColor = Color.web("#9fd5d8"),
    hoverColor = Color.rgba(0, 0, 0, 0.0), // TODO shadow
    focusedColor = Color.rgba(0, 0, 0, 0.0), // TODO shadow
    defaultColor = Color.rgba(0, 0, 0, 0.0),
  )

  override val secondaryButtonForegroundColors: ButtonColorProvider = DefaultButtonColorProvider(
    disabledColor = Color.rgba(59, 145, 129, 0.3),
    pressedColor = Color.web("#002e46"),
    hoverColor = Color.rgb(59, 145, 129),
    focusedColor = Color.rgb(59, 145, 129),
    defaultColor = Color.rgb(59, 145, 129),
  )

  //TODO use colors from Palette???
  override val stateOk: Color = Color.web("#63b017")
  override val stateWarning: Color = Color.web("#F5C413")
  override val stateError: Color = Color.web("#EA0823")
  override val stateUnknown: Color = Color.web("#737F85")
}
