package com.meistercharts.design

import it.neckar.logging.Logger
import it.neckar.logging.LoggerFactory
import it.neckar.open.context.Context


/**
 * Holds the theme for a single chart
 */
class ThemeSupport {
  /**
   * The current theme.
   * Is initialized with [CurrentTheme]
   */
  var theme: Theme = CurrentTheme

  companion object {
    internal val logger: Logger = LoggerFactory.getLogger("com.meistercharts.design.ThemeSupport")
  }
}

/**
 * The [Theme] context. Is updated for every chart in its paint method.
 * Is initialized with [DefaultTheme.Instance]
 */
val ThemeContext: Context<Theme> = Context(DefaultTheme.Instance)

/**
 * The theme that has been configured and is currently active.
 *
 * Returns the value stored in [ThemeContext]
 */
val CurrentTheme: Theme
  get() {
    return ThemeContext.current
  }

/**
 * Updates the default theme. Use with care!
 * It is possible to set the theme for a component itself.
 */
fun setDefaultTheme(newTheme: Theme) {
  ThemeContext.defaultValue = newTheme
}
