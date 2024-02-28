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
import com.meistercharts.color.RgbaColor
import com.meistercharts.font.FontDescriptorFragment
import com.meistercharts.font.FontFamilyConfiguration
import com.meistercharts.font.FontSize
import com.meistercharts.font.FontStyle
import com.meistercharts.font.FontVariant
import com.meistercharts.font.FontWeight
import com.meistercharts.style.Palette
import it.neckar.open.provider.MultiProvider

/**
 * The default design definition.
 * This definition does not require any pre-installed fonts.
 */
open class DefaultTheme(
  val headlineFontFamily: FontFamilyConfiguration = FontFamilyConfiguration.SansSerif,
  val defaultFontFamily: FontFamilyConfiguration = FontFamilyConfiguration.SansSerif,
) : Theme {
  override val id: String = "Default Design"

  override val primaryColor: RgbaColor = Color.web("#00a1e5").toRgba()

  override val primaryColorDarker: RgbaColor = Color.web("#002e46").toRgba()
  override val primaryColorLighter: RgbaColor = Color.web("#9fd5d8").toRgba()

  override val secondaryColor: RgbaColor = Color.web("#f3c500").toRgba()
  override val defaultLineColor: RgbaColor = Color.web("#737f85").toRgba()

  override val h1: FontDescriptorFragment = FontDescriptorFragment(headlineFontFamily, FontSize(50.0), FontWeight.Normal, style = FontStyle.Normal, variant = FontVariant.Normal)
  override val h1Color: RgbaColor
    get() = primaryColorDarker

  override val h2: FontDescriptorFragment = FontDescriptorFragment(headlineFontFamily, FontSize(40.0), FontWeight.SemiBold, style = FontStyle.Normal, variant = FontVariant.Normal)
  override val h2Color: RgbaColor
    get() = primaryColorDarker

  override val h3: FontDescriptorFragment = FontDescriptorFragment(headlineFontFamily, FontSize(27.0), FontWeight.Normal, style = FontStyle.Normal, variant = FontVariant.Normal)
  override val h3Color: RgbaColor
    get() = primaryColor

  override val h4: FontDescriptorFragment = FontDescriptorFragment(headlineFontFamily, FontSize(20.0), FontWeight.Normal, style = FontStyle.Normal, variant = FontVariant.Normal)
  override val h4Color: RgbaColor
    get() = primaryColorDarker

  override val h5: FontDescriptorFragment = FontDescriptorFragment(headlineFontFamily, FontSize(14.0), FontWeight.SemiBold, style = FontStyle.Normal, variant = FontVariant.Normal)
  override val h5Color: RgbaColor
    get() = primaryColorDarker

  override val textFont: FontDescriptorFragment = FontDescriptorFragment(defaultFontFamily, FontSize(14.0), FontWeight.Normal, style = FontStyle.Normal, variant = FontVariant.Normal)
  override val textColor: RgbaColor
    get() = primaryColorDarker

  override val canvasBackgroundColor: RgbaColor = Color.white()
  override val primaryBackgroundColor: RgbaColor = Color.white()
  override val secondaryBackgroundColor: RgbaColor = Color.web("#002e46").toRgba()
  override val backgroundColorActive: RgbaColor = Color.silver().withAlpha(0.5)

  override val backgroundZebraColors: MultiProvider<Any, RgbaColor> = MultiProvider.Companion.forListModulo(
    listOf(
      Color.web("#DBE1E5").toRgba(),
      Color.web("#F3F5F7").toRgba()
    )
  )

  override val inactiveElementBorder: RgbaColor = Color("#C5CACC").toRgba()

  override val borderColorConverter: (fill: Color?) -> Color = { fill ->
    fill?.toRgba()?.darker(0.15) ?: Color.darkgray()
  }

  override val chartColors: MultiProvider<Any, RgbaColor> = MultiProvider.forListModuloProvider(Palette.chartColors)

  override val enumColors: MultiProvider<Any, RgbaColor> = MultiProvider.forListModulo(
    listOf(
      Color("#8E989D").toRgba(),
      Color("#C5CACC").toRgba(),

      Color("#5C6366").toRgba(),
      Color("#CFDEE6").toRgba(),
      Color("#DAEAF2").toRgba(),
    )
  )


  override val primaryButtonBackgroundColors: ButtonColorProvider = DefaultButtonColorProvider(
    disabledColor = Color.rgba(59, 145, 129, 0.3),
    pressedColor = Color.web("#9fd5d8"),
    hoverColor = Color.rgba(59, 145, 129, 0.7), // TODO shadow
    focusedColor = Color.rgba(59, 145, 129, 0.7), // TODO shadow
    defaultColor = Color.rgb(59, 145, 129)
  )

  override val primaryButtonForegroundColors: ButtonColorProvider = DefaultButtonColorProvider(
    disabledColor = Color.white(),
    pressedColor = Color.web("#002e46"),
    hoverColor = Color.white(),
    focusedColor = Color.white(),
    defaultColor = Color.white(),
  )

  override val secondaryButtonBackgroundColors: ButtonColorProvider = DefaultButtonColorProvider(
    disabledColor = Color.rgba(0, 0, 0, 0.0),
    pressedColor = Color.web("#9fd5d8").toRgba(),
    hoverColor = Color.rgba(0, 0, 0, 0.0), // TODO shadow
    focusedColor = Color.rgba(0, 0, 0, 0.0), // TODO shadow
    defaultColor = Color.rgba(0, 0, 0, 0.0),
  )

  override val secondaryButtonForegroundColors: ButtonColorProvider = DefaultButtonColorProvider(
    disabledColor = Color.rgba(59, 145, 129, 0.3),
    pressedColor = Color.web("#002e46").toRgba(),
    hoverColor = Color.rgb(59, 145, 129),
    focusedColor = Color.rgb(59, 145, 129),
    defaultColor = Color.rgb(59, 145, 129),
  )

  //TODO use colors from Palette???
  override val stateOk: RgbaColor = Color.web("#63b017").toRgba()
  override val stateWarning: RgbaColor = Color.web("#F5C413").toRgba()
  override val stateError: RgbaColor = Color.web("#EA0823").toRgba()
  override val stateUnknown: RgbaColor = Color.web("#737F85").toRgba()

  override val shadowColor: RgbaColor = Color.black().withAlpha(0.6)

  companion object {
    /**
     * The default design.
     * This design does not require any pre-installed fonts.
     */
    val Instance: Theme = DefaultTheme()
  }
}
