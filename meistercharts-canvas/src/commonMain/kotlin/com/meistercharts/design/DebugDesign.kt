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
import it.neckar.open.provider.MultiProvider

/**
 * A debug design that can be used to identify all places where a corporate design is used.
 */
object DebugDesign : CorporateDesign {
  override val id: String = "Debug Design"

  val headlineFontFamily: FontFamilyConfiguration = FontFamily("Courier New").withGenericFamily(GenericFamily.Monospace)
  val defaultFontFamily: FontFamilyConfiguration = FontFamily("Impact").withGenericFamily(GenericFamily.Fantasy)

  override val primaryColor: Color = Color.blue

  override val primaryColorDarker: Color = Color.darkblue
  override val primaryColorLighter: Color = Color.lightblue
  override val secondaryColor: Color = Color.red

  override val defaultLineColor: Color = Color.orange

  override val h1: FontDescriptorFragment = FontDescriptorFragment(headlineFontFamily, FontSize(40.0))
  override val h1Color: Color = Color.pink

  override val h2: FontDescriptorFragment = FontDescriptorFragment(headlineFontFamily, FontSize(30.0))
  override val h2Color: Color = Color.deeppink

  override val h3: FontDescriptorFragment = FontDescriptorFragment(headlineFontFamily, FontSize(20.0), FontWeight.Bold)
  override val h3Color: Color = Color.hotpink

  override val h4: FontDescriptorFragment = FontDescriptorFragment(headlineFontFamily, FontSize(16.0))
  override val h4Color: Color = Color.lightpink

  override val h5: FontDescriptorFragment = FontDescriptorFragment(headlineFontFamily, FontSize(12.0))
  override val h5Color: Color = Color.magenta

  override val textFont: FontDescriptorFragment = FontDescriptorFragment(defaultFontFamily, FontSize(12.0), FontWeight.Normal, style = FontStyle.Normal, variant = FontVariant.Normal)
  override val textColor: Color = Color.darkmagenta

  override val backgroundColorLight: Color = Color.lightcoral
  override val backgroundColorDark: Color = Color.darkviolet

  override val inactiveElementBorder: Color = Color.lime
  override val crossWireLineColor: Color = Color.pink
  override val backgroundColorActive: Color = Color.orange.withAlpha(0.5)

  override val chartColors: MultiProvider<Any, Color> = MultiProvider.forListModulo(
    listOf(
      Color.brown,
      Color.black,
      Color.darkolivegreen,
    )
  )

  override val enumColors: MultiProvider<Any, Color> = MultiProvider.forListModulo(
    listOf(
      Color.orange,
      Color.blue,
      Color.azure,
    )
  )

  override val primaryButtonBackgroundColors: ButtonColorProvider = DefaultButtonColorProvider(
    disabledColor = Color.magenta.lighter(0.7),
    pressedColor = Color.magenta.darker(0.2),
    hoverColor = Color.magenta.lighter(0.4),
    focusedColor = Color.magenta.lighter(0.2),
    defaultColor = Color.magenta
  )

  override val primaryButtonForegroundColors: ButtonColorProvider = DefaultButtonColorProvider(
    disabledColor = Color.yellow.lighter(0.7),
    pressedColor = Color.yellow.darker(0.2),
    hoverColor = Color.yellow.lighter(0.4),
    focusedColor = Color.yellow.lighter(0.2),
    defaultColor = Color.yellow
  )

  override val secondaryButtonBackgroundColors: ButtonColorProvider = DefaultButtonColorProvider(
    disabledColor = Color.cyan.lighter(0.7),
    pressedColor = Color.cyan.darker(0.2),
    hoverColor = Color.cyan.lighter(0.4),
    focusedColor = Color.cyan.lighter(0.2),
    defaultColor = Color.cyan
  )

  override val secondaryButtonForegroundColors: ButtonColorProvider = DefaultButtonColorProvider(
    disabledColor = Color.black.lighter(0.7),
    pressedColor = Color.black.darker(0.2),
    hoverColor = Color.black.lighter(0.4),
    focusedColor = Color.black.lighter(0.2),
    defaultColor = Color.black
  )

  override val stateOk: Color = Color.darkgreen
  override val stateWarning: Color = Color.lightgoldenrodyellow
  override val stateError: Color = Color.pink
  override val stateUnknown: Color = Color.aqua

}
