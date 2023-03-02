package com.meistercharts.design

import com.meistercharts.algorithms.painter.Color
import com.meistercharts.canvas.FontDescriptorFragment
import com.meistercharts.canvas.FontSize
import com.meistercharts.canvas.FontWeight
import com.meistercharts.canvas.paintable.ButtonColorProvider
import it.neckar.open.provider.MultiProvider

/**
 * Describes a usage
 * @param T the type that will be returned
 */
data class ThemeKey<T>(val id: String, val defaultStyleProvider: (CorporateDesign) -> T) {
  /**
   * Returns the value for the corporate design
   */
  operator fun invoke(design: CorporateDesign = corporateDesign): T {
    return design.resolve(this)
  }
}

/**
 * Contains the usages for the corporate design styles
 */
object Theme {
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

  val darkBackgroundColor: ThemeKey<Color> = ThemeKey("background.color.dark") {
    it.backgroundColorDark
  }

  val lightBackgroundColor: ThemeKey<Color> = ThemeKey("background.color.light") {
    it.backgroundColorLight
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

  /**
   * Returns the state colors in this order:
   * Ok, Warning, Error, Unknown
   */
  val stateColors: ThemeKey<MultiProvider<Any, Color>> = ThemeKey("state.colors") {
    State.all
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

    val all: MultiProvider<Any, Color> = MultiProvider.modulo(ok(), warning(), error(), unknown())
  }
}
