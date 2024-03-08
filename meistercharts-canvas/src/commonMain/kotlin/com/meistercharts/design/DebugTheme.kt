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
import com.meistercharts.color.ColorMapperNullable
import com.meistercharts.color.RgbaColor
import com.meistercharts.font.FontDescriptorFragment
import com.meistercharts.font.FontFamily
import com.meistercharts.font.FontFamilyConfiguration
import com.meistercharts.font.FontSize
import com.meistercharts.font.FontStyle
import com.meistercharts.font.FontVariant
import com.meistercharts.font.FontWeight
import com.meistercharts.font.GenericFontFamily
import com.meistercharts.font.withGenericFontFamily
import it.neckar.open.provider.MultiProvider

/**
 * A debug design that can be used to identify all places where a theme is used.
 */
object DebugTheme : Theme {
  override val id: String = "Debug Design"

  val headlineFontFamily: FontFamilyConfiguration = FontFamily("Courier New").withGenericFontFamily(GenericFontFamily.Monospace)
  val defaultFontFamily: FontFamilyConfiguration = FontFamily("Impact").withGenericFontFamily(GenericFontFamily.Fantasy)

  override val primaryColor: RgbaColor = Color.blue()

  override val primaryColorDarker: RgbaColor = Color.darkblue()
  override val primaryColorLighter: RgbaColor = Color.lightblue()
  override val secondaryColor: RgbaColor = Color.red()

  override val defaultLineColor: RgbaColor = Color.orange()

  override val h1: FontDescriptorFragment = FontDescriptorFragment(headlineFontFamily, FontSize(40.0))
  override val h1Color: RgbaColor = Color.pink()

  override val h2: FontDescriptorFragment = FontDescriptorFragment(headlineFontFamily, FontSize(30.0))
  override val h2Color: RgbaColor = Color.deeppink()

  override val h3: FontDescriptorFragment = FontDescriptorFragment(headlineFontFamily, FontSize(20.0), FontWeight.Bold)
  override val h3Color: RgbaColor = Color.hotpink()

  override val h4: FontDescriptorFragment = FontDescriptorFragment(headlineFontFamily, FontSize(16.0))
  override val h4Color: RgbaColor = Color.lightpink()

  override val h5: FontDescriptorFragment = FontDescriptorFragment(headlineFontFamily, FontSize(12.0))
  override val h5Color: RgbaColor = Color.magenta()

  override val textFont: FontDescriptorFragment = FontDescriptorFragment(defaultFontFamily, FontSize(12.0), FontWeight.Normal, style = FontStyle.Normal, variant = FontVariant.Normal)
  override val textColor: RgbaColor = Color.darkmagenta()

  override val canvasBackgroundColor: RgbaColor = Color.beige()
  override val primaryBackgroundColor: RgbaColor = Color.lightcoral()
  override val secondaryBackgroundColor: RgbaColor = Color.darkviolet()
  override val backgroundColorActive: RgbaColor = Color.orange().withAlpha(0.5)
  override val backgroundZebraColors: MultiProvider<Any, RgbaColor> = MultiProvider.Companion.forListModulo(
    listOf(
      Color.web("#D441E5").toRgba(),
      Color.web("#55F5F7").toRgba()
    )
  )

  override val inactiveElementBorder: RgbaColor = Color.lime()
  override val borderColorConverter: ColorMapperNullable = { Color.red() }
  override val crossWireLineColor: RgbaColor = Color.pink()

  override val chartColors: MultiProvider<Any, RgbaColor> = MultiProvider.forListModulo(
    listOf(
      Color.brown(),
      Color.black(),
      Color.darkolivegreen(),
    )
  )

  override val enumColors: MultiProvider<Any, RgbaColor> = MultiProvider.forListModulo(
    listOf(
      Color.orange(),
      Color.blue(),
      Color.azure(),
    )
  )

  override val primaryButtonBackgroundColors: ButtonColorProvider = DefaultButtonColorProvider(
    disabledColor = Color.magenta().lighter(0.7),
    pressedColor = Color.magenta().darker(0.2),
    hoverColor = Color.magenta().lighter(0.4),
    focusedColor = Color.magenta().lighter(0.2),
    defaultColor = Color.magenta()
  )

  override val primaryButtonForegroundColors: ButtonColorProvider = DefaultButtonColorProvider(
    disabledColor = Color.yellow().lighter(0.7),
    pressedColor = Color.yellow().darker(0.2),
    hoverColor = Color.yellow().lighter(0.4),
    focusedColor = Color.yellow().lighter(0.2),
    defaultColor = Color.yellow()
  )

  override val secondaryButtonBackgroundColors: ButtonColorProvider = DefaultButtonColorProvider(
    disabledColor = Color.cyan().lighter(0.7),
    pressedColor = Color.cyan().darker(0.2),
    hoverColor = Color.cyan().lighter(0.4),
    focusedColor = Color.cyan().lighter(0.2),
    defaultColor = Color.cyan()
  )

  override val secondaryButtonForegroundColors: ButtonColorProvider = DefaultButtonColorProvider(
    disabledColor = Color.black().lighter(0.7),
    pressedColor = Color.black().darker(0.2),
    hoverColor = Color.black().lighter(0.4),
    focusedColor = Color.black().lighter(0.2),
    defaultColor = Color.black()
  )

  override val stateOk: RgbaColor = Color.darkgreen()
  override val stateWarning: RgbaColor = Color.lightgoldenrodyellow()
  override val stateError: RgbaColor = Color.pink()
  override val stateUnknown: RgbaColor = Color.aqua()
  override val shadowColor: RgbaColor = Color.limegreen()

}
