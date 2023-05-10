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
package com.meistercharts.canvas

import it.neckar.open.observable.ObservableObject
import it.neckar.open.observable.ReadOnlyObservableObject

/**
 * Offers support for tooltips
 */
class TooltipSupport(
  private val _tooltip: ObservableObject<TooltipContent?> = ObservableObject(null)
) {
  /**
   * Returns the property that contains the current tooltip
   */
  val tooltip: ReadOnlyObservableObject<TooltipContent?>
    get() = _tooltip

  /**
   * Contains tooltips.
   *
   * Different layers can set tooltips in different properties.
   * The first property that contains a non null tooltip is used.
   */
  private val toolTips: MutableMap<Any, ObservableObject<TooltipContent?>> = mutableMapOf()

  /**
   * Returns the tooltip property for the given key
   */
  fun tooltipProperty(key: Any): ObservableObject<TooltipContent?> {
    return toolTips.getOrPut(key) {
      val observableObject: ObservableObject<TooltipContent?> = ObservableObject(null)
      observableObject.consumeImmediately {
        updateTooltips()
      }

      observableObject
    }
  }

  private fun updateTooltips() {
    _tooltip.value = calculateTooltip()
  }

  /**
   * Recalculates the tooltip
   */
  private fun calculateTooltip(): TooltipContent? {
    return toolTips
      .values
      .asSequence()
      .map {
        it.value
      }.filterNotNull()
      .firstOrNull()
  }
}

/**
 * The content of a tooltip
 */
data class TooltipContent(
  /**
   * The tooltip text lines
   */
  val lines: List<String>

  //TODO content requires the coordinates or area where it should be displayed
) {
  constructor(text: String) : this(listOf(text))
}
