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
import it.neckar.open.provider.MultiProvider

/**
 * A dark design definition.
 */
class DarkCorporateDesign : DefaultCorporateDesign() {
  override val id: String = "Dark Design"

  override val primaryColor: RgbaColor = Color.web("#5fd0f5").toRgba()

  override val primaryColorDarker: RgbaColor = Color.web("#4c9bb3").toRgba()
  override val primaryColorLighter: RgbaColor = Color.web("#bfecfb").toRgba()

  override val secondaryColor: RgbaColor = Color.web("#fa8b4d").toRgba()
  override val defaultLineColor: RgbaColor = Color.web("#c5c7d1").toRgba()

  override val primaryBackgroundColor: RgbaColor = Color.web("#1f1e29").toRgba()
  override val secondaryBackgroundColor: RgbaColor = Color.web("#f4f4fe").toRgba()
  override val backgroundColorActive: RgbaColor = primaryBackgroundColor.withAlpha(0.5)

  override val backgroundZebraColors: MultiProvider<Any, RgbaColor> = MultiProvider.Companion.forListModulo(
    listOf(
      primaryBackgroundColor.lighter(0.2),
      primaryBackgroundColor.lighter(0.15),
    )
  )

  override val inactiveElementBorder: RgbaColor = Color("#C5CACC").toRgba()

  override val borderColorConverter: (fill: Color?) -> Color = { fill ->
    fill?.toRgba()?.darker(0.35) ?: Color.darkgray
  }

  override val chartColors: MultiProvider<Any, RgbaColor> = MultiProvider.forListModulo(
    listOf(
      Color("#5fd0f5").toRgba(),
      Color("#ff6763").toRgba(),
      Color("#ffe279").toRgba(),
      Color("#74b863").toRgba(),
      Color("#fa8b4d").toRgba(),
    )
  )

  override val enumColors: MultiProvider<Any, RgbaColor> = MultiProvider.forListModulo(
    listOf(
      Color("#325066").toRgba(),
      Color("#4c9bbe").toRgba(),
      Color("#5fd0f5").toRgba(),
      Color("#7ba3bb").toRgba(),
    )
  )

  override val primaryButtonBackgroundColors: ButtonColorProvider = DefaultButtonColorProvider(
    defaultColor = primaryColor,
    disabledColor = Color.web("#4C9BBE").toRgba(),
    pressedColor = primaryColorLighter,
    hoverColor = primaryColorLighter,
    focusedColor = primaryColorLighter
  )

  override val primaryButtonForegroundColors: ButtonColorProvider = DefaultButtonColorProvider(
    defaultColor = primaryBackgroundColor,
    disabledColor = primaryBackgroundColor,
    pressedColor = primaryBackgroundColor,
    hoverColor = primaryBackgroundColor,
    focusedColor = primaryBackgroundColor
  )

  override val secondaryButtonBackgroundColors: ButtonColorProvider = DefaultButtonColorProvider(
    defaultColor = secondaryColor,
    disabledColor = Color.web("#7f3004").toRgba(),
    pressedColor = Color.web("#fcb994").toRgba(),
    hoverColor = Color.web("#fcb994").toRgba(),
    focusedColor = Color.web("#fcb994").toRgba(),
  )

  override val secondaryButtonForegroundColors: ButtonColorProvider = DefaultButtonColorProvider(
    defaultColor = secondaryBackgroundColor,
    disabledColor = secondaryBackgroundColor,
    pressedColor = secondaryBackgroundColor,
    hoverColor = secondaryBackgroundColor,
    focusedColor = secondaryBackgroundColor,
  )

  override val stateOk: RgbaColor = Color.web("#74b863").toRgba()
  override val stateWarning: RgbaColor = Color.web("#ffe279").toRgba()
  override val stateError: RgbaColor = Color.web("#ff6763").toRgba()
  override val stateUnknown: RgbaColor = Color.web("#60626c").toRgba()

  override val shadowColor: RgbaColor = Color.black
}

/**
 * A dark design
 */
val DarkDesign : CorporateDesign = DarkCorporateDesign()
