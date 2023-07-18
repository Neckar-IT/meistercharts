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
package com.meistercharts.algorithms.layers

import com.meistercharts.canvas.textService
import it.neckar.open.kotlin.lang.nullIfBlank
import it.neckar.open.i18n.I18nConfiguration
import it.neckar.open.i18n.TextService

/**
 * Returns the title from the axis style.
 * Returns null if there is no title or the title is blank
 */
fun AxisStyle.resolveTitle(paintingContext: LayerPaintingContext): String? {
  return resolveTitle(paintingContext.chartSupport.textService, paintingContext.i18nConfiguration)
}

/**
 * Returns the title from the axis style.
 * Returns null if there is no title or the title is blank
 */
fun AxisStyle.resolveTitle(textService: TextService, i18nConfiguration: I18nConfiguration): String? {
  if (titleVisible().not()) {
    return null
  }
  val titleProvider = titleProvider ?: return null
  return titleProvider(textService, i18nConfiguration).nullIfBlank()
}
