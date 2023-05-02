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
package com.meistercharts.algorithms.layers.text

import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.canvas.textService
import it.neckar.open.i18n.I18nConfiguration
import it.neckar.open.i18n.TextService

/**
 * Text provider that returns texts (a list of lines). This can be used for the message layer
 */
typealias LinesProvider = (textService: TextService, i18nConfiguration: I18nConfiguration) -> List<String>

/**
 * Resolves the text for the text provider using the [TextService] and [I18nConfiguration] from the provided painting context
 */
fun LinesProvider.resolve(paintingContext: LayerPaintingContext): List<String> {
  return this(paintingContext.chartSupport.textService, paintingContext.i18nConfiguration)
}
