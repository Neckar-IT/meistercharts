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

import com.meistercharts.font.FontFamily
import com.meistercharts.font.GenericFontFamily
import com.meistercharts.font.withGenericFontFamily

/**
 * The theme for Neckar IT
 */
class NeckarITTheme : DefaultTheme(
  headlineFontFamily = FontFamily("Oswald").withGenericFontFamily(GenericFontFamily.SansSerif),
  defaultFontFamily = FontFamily.Verdana.withGenericFontFamily(GenericFontFamily.SansSerif)
) {
  override val id: String = "Neckar IT"
}

/**
 * The theme for Neckar IT
 */
val NeckarITDesign: Theme = NeckarITTheme()
