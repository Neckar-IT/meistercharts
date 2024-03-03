import com.meistercharts.design.DarkDesign
import com.meistercharts.design.DebugTheme
import com.meistercharts.design.DefaultTheme
import com.meistercharts.design.NeckarITDesign
import com.meistercharts.design.Theme
import it.neckar.open.charting.api.sanitizing.sanitize

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
/**
 * Supported Look and Feels for meistercharts.
 */
@JsExport
enum class ThemeId {
  /**
   * The default look and feel. Uses the CSS font
   */
  Default,

  /**
   * Dark look and feel. Uses the CSS font.
   */
  Dark,

  /**
   * Look and feel for Neckar IT. Uses own fonts.
   */
  NeckarIT,

  /**
   * Debug look and feel
   */
  Debug,
}

/**
 * Converts the theme id to a theme.
 */
fun ThemeId.toTheme(): Theme {
  return when (this.sanitize()) {
    ThemeId.Default -> DefaultTheme.Instance
    ThemeId.Dark -> DarkDesign
    ThemeId.NeckarIT -> NeckarITDesign
    ThemeId.Debug -> DebugTheme
  }
}