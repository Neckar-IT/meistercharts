package com.meistercharts.design

import com.meistercharts.algorithms.painter.Color
import com.meistercharts.canvas.FontDescriptor
import com.meistercharts.canvas.FontDescriptorFragment
import com.meistercharts.canvas.FontFamily
import com.meistercharts.canvas.FontSize
import com.meistercharts.canvas.FontStyle
import com.meistercharts.canvas.FontVariant
import com.meistercharts.canvas.FontWeight
import com.meistercharts.canvas.paintable.ButtonColorProvider
import com.meistercharts.canvas.paintable.DefaultButtonColorProvider
import com.meistercharts.canvas.paintable.SingleButtonColorProvider
import it.neckar.open.provider.MultiProvider
import com.meistercharts.style.Palette

/**
 * Manages the corporate design settings
 */


/**
 * The corporate design that has been configured.
 * The design is configured from the MeisterChartsPlatform
 *
 * Is only evaluated directly after startup. Later changes to the corporate design are not supported.
 */
var corporateDesign: CorporateDesign = NeckarITDesign
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
   * The default text font that is applied to the canvas automatically
   */
  val textFont: FontDescriptor
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

/**
 * The corporate design for the Neckar IT
 */
object NeckarITDesign : CorporateDesign {
  override val id: String = "Neckar IT"

  val headlineFontFamily: FontFamily = FontFamily("Oswald")
  val defaultFontFamily: FontFamily = FontFamily("Open Sans")

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

  override val textFont: FontDescriptor = FontDescriptor(defaultFontFamily, FontSize(14.0), FontWeight.Normal, style = FontStyle.Normal, variant = FontVariant.Normal)
  override val textColor: Color = primaryColorDarker

  override val backgroundColorLight: Color = Color.white
  override val backgroundColorDark: Color = Color.web("#002e46")

  override val inactiveElementBorder: Color = Color("#C5CACC")

  override val chartColors: MultiProvider<Any, Color> = MultiProvider.Companion.forListModulo(Palette.chartColors)

  override val enumColors: MultiProvider<Any, Color> = MultiProvider.Companion.forListModulo(
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

/**
 * Another corporate design that uses Segoe UI
 */
object SegoeUiDesign : CorporateDesign {
  override val id: String = "Segoe UI Design"

  val headlineFontFamily: FontFamily = FontFamily("Segoe UI")
  val defaultFontFamily: FontFamily = FontFamily("Segoe UI")

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

  override val textFont: FontDescriptor = FontDescriptor(defaultFontFamily, FontSize(14.0), FontWeight.Normal, style = FontStyle.Normal, variant = FontVariant.Normal)
  override val textColor: Color = Color("#555555")

  override val backgroundColorLight: Color = Color.white
  override val backgroundColorDark: Color = Color.darkgray

  override val inactiveElementBorder: Color = Color("#C5CACC")

  override val backgroundColorActive: Color = Color.silver.withAlpha(0.5)

  override val chartColors: MultiProvider<Any, Color> = MultiProvider.Companion.forListModulo(
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

  override val enumColors: MultiProvider<Any, Color> = MultiProvider.Companion.forListModulo(
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

/**
 * A debug design that can be used to identify all places where a corporate design is used.
 */
object DebugDesign : CorporateDesign {
  override val id: String = "Debug Design"

  val headlineFontFamily: FontFamily = FontFamily("Courier New")
  val defaultFontFamily: FontFamily = FontFamily("Impact")

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

  override val textFont: FontDescriptor = FontDescriptor(defaultFontFamily, FontSize(12.0), FontWeight.Normal, style = FontStyle.Normal, variant = FontVariant.Normal)
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
