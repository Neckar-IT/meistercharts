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

import it.neckar.logging.Logger
import it.neckar.logging.LoggerFactory
import it.neckar.open.context.Context


/**
 * Holds the theme for a single chart
 */
class ThemeSupport {
  /**
   * The configured theme.
   * If no theme is configured, the value is null.
   */
  var selectedTheme: Theme? = null

  /**
   * Returns the configured theme or [CurrentTheme] if no theme is configured
   */
  val theme: Theme
    get() {
      return selectedTheme ?: CurrentTheme
    }

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

/**
 * Sets this theme as default
 */
fun Theme.setAsDefault() {
  setDefaultTheme(this)
}
